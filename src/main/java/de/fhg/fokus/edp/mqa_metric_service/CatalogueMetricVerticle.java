package de.fhg.fokus.edp.mqa_metric_service;

import de.fhg.fokus.edp.mqa_metric_service.model.Catalogue;
import de.fhg.fokus.edp.mqa_metric_service.metric.catalogue.CatalogueMetricProvider;
import de.fhg.fokus.edp.mqa_metric_service.source.catalogue.CatalogueDataSourceProvider;
import de.fhg.fokus.edp.mqa_metric_service.source.catalogue.CatalogueDataSourceProviderImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static de.fhg.fokus.edp.mqa_metric_service.ApplicationConfig.*;

public class CatalogueMetricVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogueMetricVerticle.class);

    private CatalogueDataSourceProvider catalogueDataSourceProvider;
    private CatalogueMetricProvider catalogueMetricProvider;

    private CatalogueRefreshService catalogueRefreshService;

    @Override
    public void start(Future<Void> startFuture) {
        try {
            catalogueDataSourceProvider = new CatalogueDataSourceProviderImpl(vertx);

            Future<Void> metricProviderFuture = Future.future();

            catalogueMetricProvider = CatalogueMetricProvider.create(vertx, ready -> {
                if (ready.succeeded()) {
                    new ServiceBinder(vertx)
                        .setAddress(METRIC_SERVICE_CATALOGUE_READ_ADDRESS)
                        .register(CatalogueMetricProvider.class, ready.result());

                    vertx.eventBus().consumer(METRIC_SERVICE_CATALOGUE_REFRESH_ADDRESS, request ->
                        catalogueRefreshService.refreshMetrics().setHandler(handler -> {
                            if (handler.succeeded()) {
                                LOG.info("Catalogue metrics refreshed successfully");
                            } else {
                                LOG.error("Failed to refresh catalogue metrics: {}", handler.cause());
                            }
                        }));

                    metricProviderFuture.complete();
                } else {
                    metricProviderFuture.fail(ready.cause());
                }
            });

            metricProviderFuture.setHandler(ready -> {
                if (ready.succeeded()) {
                    catalogueRefreshService = new CatalogueRefreshService(catalogueDataSourceProvider, catalogueMetricProvider);

                    vertx.eventBus().consumer(METRIC_SERVICE_CATALOGUE_INFO_ADDRESS, this::handleCatalogueInfoRequest);
                    vertx.eventBus().consumer(METRIC_SERVICE_CATALOGUE_NON_ACCESSIBLE_ADDRESS, this::handleCatalogueNonAccessibleDistributionsRequest);
                    vertx.eventBus().consumer(METRIC_SERVICE_CATALOGUE_NON_COMPLIANT_ADDRESS, this::handleCatalogueNonCompliantDatasetsRequest);

                    startFuture.complete();
                } else {
                    startFuture.fail(ready.cause());
                }
            });

        } catch (SQLException e) {
            startFuture.fail(e);
        }
    }

    @Override
    public void stop(Future<Void> future) {
        catalogueDataSourceProvider.tearDown();
        future.complete();
    }

    private void handleCatalogueInfoRequest(Message<String> message) {
        catalogueMetricProvider.getCatalogueInfo(catalogueHandler -> {
            boolean success = catalogueHandler.succeeded()
                && catalogueHandler.result() != null
                && catalogueHandler.result().getBoolean("success");

            if (success) {

                JsonArray result = new JsonArray();
                List<Future> catalogueFutures = new ArrayList<>();

                catalogueHandler.result().getJsonArray("result").forEach(c -> {

                    Catalogue catalogue = Json.decodeValue(((JsonObject) c).encode(), Catalogue.class);

                    Future<Void> catalogueFuture = Future.future();
                    catalogueFutures.add(catalogueFuture);

                    List<Future> metricFutures = new ArrayList<>();

                    Future<JsonObject> distributionFuture = getCatalogueDistributionInfo(catalogue.getId());
                    metricFutures.add(distributionFuture);

                    Future<JsonObject> violationFuture = getCatalogueViolationInfo(catalogue.getId());
                    metricFutures.add(violationFuture);

                    Future<JsonObject> licenceFuture = getCatalogueLicenceInfo(catalogue.getId());
                    metricFutures.add(licenceFuture);

                    CompositeFuture.all(metricFutures).setHandler(handler -> {
                        if (handler.succeeded()) {

                            JsonObject jsonCatalogue = JsonObject.mapFrom(catalogue);
                            jsonCatalogue.put("distributions", distributionFuture.result());
                            jsonCatalogue.put("violations", violationFuture.result());
                            jsonCatalogue.put("licences", licenceFuture.result());

                            result.add(jsonCatalogue);
                            LOG.debug("Finished retrieving metrics for catalogue [{}]", catalogue.getId());
                        } else {
                            LOG.error("Failed to retrieve some catalogue metrics: {}", handler.cause().getMessage());
                        }

                        catalogueFuture.complete();
                    });
                });

                CompositeFuture.all(catalogueFutures).setHandler(handler -> {
                    if (handler.succeeded()) {
                        LOG.debug("Finished retrieving catalogue metrics");

                        message.reply(new JsonObject()
                            .put("success", true)
                            .put("result", result));
                    } else {
                        message.reply(new JsonObject()
                            .put("success", false)
                            .put("message", catalogueHandler.cause()));
                    }
                });
            } else {
                message.reply(new JsonObject()
                    .put("success", false)
                    .put("message", catalogueHandler.cause()));
            }
        });
    }

    private Future<JsonObject> getCatalogueDistributionInfo(String catalogueId) {

        Future<JsonObject> distributionFuture = Future.future();

        catalogueMetricProvider.getRenderDistributions(catalogueId, renderHandler -> {

            boolean renderDistributions = renderHandler.succeeded()
                && (renderHandler.result() != null
                && renderHandler.result().getBoolean("result"));

            JsonObject result = new JsonObject()
                .put("render", renderDistributions);

            if (renderDistributions) {

                List<Future> metricFutures = new ArrayList<>();

                Future<JsonObject> accessibilityAccessUrlFuture = Future.future();
                metricFutures.add(accessibilityAccessUrlFuture);
                catalogueMetricProvider.getDistributionAccessibilityAccessUrl(catalogueId, handler ->
                    accessibilityAccessUrlFuture.complete(handler.succeeded() ? handler.result() : null));

                Future<JsonObject> accessibilityDownloadUrlFuture = Future.future();
                metricFutures.add(accessibilityDownloadUrlFuture);
                catalogueMetricProvider.getDistributionAccessibilityDownloadUrl(catalogueId, handler ->
                    accessibilityDownloadUrlFuture.complete(handler.succeeded() ? handler.result() : null));

                Future<JsonObject> machineReadabilityFuture = Future.future();
                metricFutures.add(machineReadabilityFuture);
                catalogueMetricProvider.getDatasetMachineReadability(catalogueId, handler ->
                    machineReadabilityFuture.complete(handler.succeeded() ? handler.result() : null));

                CompositeFuture.all(metricFutures).setHandler(metricHandler -> {
                    result.put("accessibility_access_url", accessibilityAccessUrlFuture.result());
                    result.put("accessibility_download_url", accessibilityDownloadUrlFuture.result());
                    result.put("machine_readable", machineReadabilityFuture.result());
                    distributionFuture.complete(result);
                });
            } else {
                distributionFuture.complete(result);
            }
        });

        return distributionFuture;
    }

    private Future<JsonObject> getCatalogueViolationInfo(String catalogueId) {

        Future<JsonObject> violationFuture = Future.future();

        catalogueMetricProvider.getRenderViolations(catalogueId, renderHandler -> {
            boolean renderViolations = renderHandler.succeeded()
                && renderHandler.result() != null
                && renderHandler.result().getBoolean("result");

            JsonObject result = new JsonObject()
                .put("render", renderViolations);

            if (renderViolations) {
                catalogueMetricProvider.getDatasetCompliance(catalogueId, handler -> {
                    result.put("compliance", handler.succeeded()
                        ? handler.result()
                        : null);

                    violationFuture.complete(result);
                });
            } else {
                violationFuture.complete(result);
            }
        });

        return violationFuture;
    }

    private Future<JsonObject> getCatalogueLicenceInfo(String catalogueId) {

        Future<JsonObject> licenceFuture = Future.future();

        catalogueMetricProvider.getRenderLicences(catalogueId, renderHandler -> {
            boolean renderLicences = renderHandler.succeeded()
                && renderHandler.result() != null
                && renderHandler.result().getBoolean("result");

            JsonObject result = new JsonObject()
                .put("render", renderLicences);

            if (renderLicences) {
                catalogueMetricProvider.getDatasetKnownLicences(catalogueId, handler -> {
                    result.put("known_licences", handler.succeeded()
                        ? handler.result()
                        : null);

                    licenceFuture.complete(result);
                });
            } else {
                licenceFuture.complete(result);
            }
        });

        return licenceFuture;
    }

    private void handleCatalogueNonAccessibleDistributionsRequest(Message<String> message) {
        try {
            JsonObject parameters = new JsonObject(message.body());
            String catalogueId = parameters.getString("catalogueId");
            Integer limit = parameters.getInteger("limit");
            Integer offset = parameters.getInteger("offset");

            catalogueDataSourceProvider.getDatasetsWithAccessibilityIssues(catalogueId, limit, offset).setHandler(handler -> {
                JsonObject response = new JsonObject();

                if (handler.succeeded()) {
                    response.put("success", true).put("result", new JsonArray(Json.encode(handler.result())));
                } else {
                    response.put("success", false);
                }

                message.reply(response);
            });
        } catch (DecodeException e) {
            message.reply(new JsonObject().put("success", false));
        }
    }

    private void handleCatalogueNonCompliantDatasetsRequest(Message<String> message) {
        try {
            JsonObject parameters = new JsonObject(message.body());
            String catalogueId = parameters.getString("catalogueId");
            Integer limit = parameters.getInteger("limit");
            Integer offset = parameters.getInteger("offset");

            catalogueDataSourceProvider.getCatalogueComplianceInfo(catalogueId, limit, offset).setHandler(handler -> {
                JsonObject response = new JsonObject();

                if (handler.succeeded()) {
                    response.put("success", true).put("result", new JsonArray(Json.encode(handler.result())));
                } else {
                    response.put("success", false);
                }

                message.reply(response);
            });
        } catch (DecodeException e) {
            message.reply(new JsonObject().put("success", false));
        }
    }
}
