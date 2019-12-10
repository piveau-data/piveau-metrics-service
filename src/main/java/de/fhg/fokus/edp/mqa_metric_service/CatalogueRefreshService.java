package de.fhg.fokus.edp.mqa_metric_service;

import de.fhg.fokus.edp.mqa_metric_service.metric.MetricService;
import de.fhg.fokus.edp.mqa_metric_service.metric.catalogue.CatalogueMetricProvider;
import de.fhg.fokus.edp.mqa_metric_service.model.Catalogue;
import de.fhg.fokus.edp.mqa_metric_service.model.Metric;
import de.fhg.fokus.edp.mqa_metric_service.source.catalogue.CatalogueDataSourceProvider;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static de.fhg.fokus.edp.mqa_metric_service.metric.MetricConstants.METRIC_DISTRIBUTION_ACCESSIBILITY_DOWNLOAD_URL;

class CatalogueRefreshService {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogueRefreshService.class);

    private CatalogueDataSourceProvider catalogueDataSourceProvider;
    private CatalogueMetricProvider catalogueMetricProvider;

    CatalogueRefreshService(CatalogueDataSourceProvider catalogueDataSourceProvider, CatalogueMetricProvider catalogueMetricProvider) {
        this.catalogueDataSourceProvider = catalogueDataSourceProvider;
        this.catalogueMetricProvider = catalogueMetricProvider;
    }

    Future<Void> refreshMetrics() {
        Future<Void> completionFuture = Future.future();
        List<Future> catalogueFutures = new ArrayList<>();

        // refresh metrics for all catalogues
        catalogueDataSourceProvider.getCatalogues().setHandler(handler -> {
            if (handler.succeeded()) {

                Future<Void> catalogueInfoFuture = Future.future();
                catalogueMetricProvider.setCatalogueInfo(handler.result(), infoHandler -> {
                    if (handler.succeeded()) {
                        catalogueInfoFuture.complete();
                    } else {
                        catalogueInfoFuture.fail(infoHandler.cause());
                    }
                });
                catalogueFutures.add(catalogueInfoFuture);

                handler.result().forEach(catalogue ->
                    catalogueFutures.add(refreshCatalogueMetrics(catalogue)));

                CompositeFuture.all(catalogueFutures).setHandler(completionHandler -> {
                    if (completionHandler.succeeded()) {
                        completionFuture.complete();
                    } else {
                        completionFuture.fail(completionHandler.cause());
                    }
                });
            } else {
                completionFuture.fail("Failed to retrieve catalogueIds: " + handler.cause());
            }
        });

        return completionFuture;
    }

    // Recalculates all metrics for a given catalogue and stores them in the database
    private Future<Void> refreshCatalogueMetrics(Catalogue catalogue) {
        Future<Void> completionFuture = Future.future();

        List<Future> refreshFutures = Arrays.asList(new Future[]{
            refreshCatalogueInfo(catalogue.getId()),
            refreshDistributionMetrics(catalogue.getId()),
            refreshViolationMetrics(catalogue),
            refreshLicenceMetrics(catalogue.getId())
        });

        // refresh all metrics separately
        CompositeFuture.all(refreshFutures).setHandler(handler -> {
            if (handler.succeeded()) {
                completionFuture.complete();
            } else {
                completionFuture.fail(handler.cause());
            }
        });

        return completionFuture;
    }

    private Future<Void> refreshCatalogueInfo(String catalogueId) {
        Future<Void> completionSizeFuture = Future.future();
        Future<Void> accessibilitySizeFuture = Future.future();
        Future<Void> complianceFuture = Future.future();

        catalogueDataSourceProvider.countDatasetsWithAccessibilityIssues(catalogueId).setHandler(accessibilityHandler -> {
            if (accessibilityHandler.succeeded()) {
                catalogueMetricProvider.setDistributionAccessibilityInfoSize(catalogueId, accessibilityHandler.result(), handler -> {
                    if (handler.succeeded()) {
                        accessibilitySizeFuture.complete();
                    } else {
                        accessibilitySizeFuture.fail(handler.cause());
                    }
                });
            } else {
                accessibilitySizeFuture.fail(accessibilityHandler.cause());
            }
        });

        catalogueDataSourceProvider.countCatalogueComplianceInfo(catalogueId).setHandler(complianceHandler -> {
            if (complianceHandler.succeeded()) {
                catalogueMetricProvider.setDatasetComplianceInfoSize(catalogueId, complianceHandler.result(), handler -> {
                    if (handler.succeeded()) {
                        complianceFuture.complete();
                    } else {
                        complianceFuture.fail(handler.cause());
                    }
                });
            } else {
                complianceFuture.fail(complianceHandler.cause());
            }
        });

        CompositeFuture.all(accessibilitySizeFuture, complianceFuture).setHandler(handler -> {
            if (handler.succeeded()) {
                completionSizeFuture.complete();
            } else {
                completionSizeFuture.fail(handler.cause());
            }
        });

        return completionSizeFuture;
    }

    private Future<Void> refreshDistributionMetrics(String catalogueId) {
        Future<Void> completionFuture = Future.future();

        catalogueDataSourceProvider.existsDistributions(catalogueId).setHandler(handler -> {
            if (handler.succeeded()) {
                if (handler.result()) {
                    List<Future> databaseFutures = new ArrayList<>();

                    Future<Long> distributionCount = catalogueDataSourceProvider.getDistributionCount(catalogueId);
                    databaseFutures.add(distributionCount);

                    Future<Long> distributionAccessibilityAccessUrlCount = catalogueDataSourceProvider.getDistributionAccessUrlAccessibilityCount(catalogueId);
                    databaseFutures.add(distributionAccessibilityAccessUrlCount);

                    Future<Long> distributionUnknownAccessUrlCount = catalogueDataSourceProvider.getDistributionUnknownAccessUrlAccessibilityCount(catalogueId);
                    databaseFutures.add(distributionUnknownAccessUrlCount);

                    Future<Long> distributionAccessibilityDownloadUrlCount = catalogueDataSourceProvider.getDistributionDownloadUrlAccessibilityCount(catalogueId);
                    databaseFutures.add(distributionAccessibilityDownloadUrlCount);

                    Future<Long> distributionUnknownDownloadUrlCount = catalogueDataSourceProvider.getDistributionUnknownDownloadUrlAccessibilityCount(catalogueId);
                    databaseFutures.add(distributionUnknownDownloadUrlCount);

                    Future<Map<String, Long>> accessStatusCode = catalogueDataSourceProvider.getDistributionAccessStatusCodes(catalogueId);
                    databaseFutures.add(accessStatusCode);

                    Future<Map<String, Long>> downloadStatusCode = catalogueDataSourceProvider.getDistributionDownloadStatusCodes(catalogueId);
                    databaseFutures.add(downloadStatusCode);

                    Future<Long> distributionsWithDownloadUrl = catalogueDataSourceProvider.getDistributionsWithDownloadUrl(catalogueId);
                    databaseFutures.add(distributionsWithDownloadUrl);

                    Future<Long> datasetMachineReadability = catalogueDataSourceProvider.getDatasetMachineReadableCount(catalogueId);
                    databaseFutures.add(datasetMachineReadability);

                    Future<Map<String, Long>> distributionFormatsCount = catalogueDataSourceProvider.getDistributionFormats(catalogueId);
                    databaseFutures.add(distributionFormatsCount);


                    Future<Long> datasetCount = catalogueDataSourceProvider.getDatasetCount(catalogueId);
                    databaseFutures.add(datasetCount);

                    Future<Long> accessibleDatasetCount = catalogueDataSourceProvider.getDatasetAccessibilityCount(catalogueId);
                    databaseFutures.add(accessibleDatasetCount);


                    // wait for all database operations to complete before calculating metrics
                    CompositeFuture.all(databaseFutures).setHandler(sourceHandler -> {
                        if (sourceHandler.succeeded()) {
                            List<Future> metricFutures = new ArrayList<>();

                            long totalDistributionCount = distributionCount.result();

                            Future<Void> renderDistributionsFuture = Future.future();
                            catalogueMetricProvider.setRenderDistributions(catalogueId, true, resultHandler ->
                                handleResult(renderDistributionsFuture, resultHandler));
                            metricFutures.add(renderDistributionsFuture);

                            Future<Void> accessibleDistributionAccessUrlFuture = Future.future();
                            Metric distributionsAccessibleAccessUrl =
                                new Metric(MetricService.getDistributionAccessibility(distributionAccessibilityAccessUrlCount.result(), distributionUnknownAccessUrlCount.result(), totalDistributionCount));
                            catalogueMetricProvider.setDistributionAccessibilityAccessUrl(catalogueId, distributionsAccessibleAccessUrl, resultHandler ->
                                handleResult(accessibleDistributionAccessUrlFuture, resultHandler));
                            metricFutures.add(accessibleDistributionAccessUrlFuture);

                            if (distributionsWithDownloadUrl.result() > 0) {
                                Future<Void> accessibleDistributionDownloadUrlFuture = Future.future();
                                Metric distributionsAccessibleDownloadUrl =
                                    new Metric(MetricService.getDistributionAccessibility(distributionAccessibilityDownloadUrlCount.result(), distributionUnknownDownloadUrlCount.result(), distributionsWithDownloadUrl.result()));
                                catalogueMetricProvider.setDistributionAccessibilityDownloadUrl(catalogueId, distributionsAccessibleDownloadUrl, resultHandler ->
                                    handleResult(accessibleDistributionDownloadUrlFuture, resultHandler));
                                metricFutures.add(accessibleDistributionDownloadUrlFuture);
                            } else {
                                catalogueMetricProvider.deleteMetric(catalogueId, METRIC_DISTRIBUTION_ACCESSIBILITY_DOWNLOAD_URL);
                            }

                            Future<Void> errorStatusCodesFuture = Future.future();
                            Metric errorStatusCodes =
                                new Metric(MetricService.getDistributionStatusCodes(accessStatusCode.result(), downloadStatusCode.result()));
                            catalogueMetricProvider.setDistributionStatusCodes(catalogueId, errorStatusCodes, resultHandler ->
                                handleResult(errorStatusCodesFuture, resultHandler));
                            metricFutures.add(errorStatusCodesFuture);

                            Future<Void> downloadUrlExistsFuture = Future.future();
                            Metric downloadUrlExists =
                                new Metric(MetricService.getDistributionDownloadUrlExists(distributionsWithDownloadUrl.result(), totalDistributionCount));
                            catalogueMetricProvider.setDistributionDownloadUrlExists(catalogueId, downloadUrlExists, resultHandler ->
                                handleResult(downloadUrlExistsFuture, resultHandler));
                            metricFutures.add(downloadUrlExistsFuture);

                            Future<Void> machineReadabilityFuture = Future.future();
                            Metric machineReadability =
                                new Metric(MetricService.getDatasetMachineReadability(datasetMachineReadability.result(), datasetCount.result()));
                            catalogueMetricProvider.setDatasetMachineReadability(catalogueId, machineReadability, resultHandler ->
                                handleResult(machineReadabilityFuture, resultHandler));
                            metricFutures.add(machineReadabilityFuture);

                            Future<Void> distributionFormatsFuture = Future.future();
                            Metric distributionFormats =
                                new Metric(MetricService.getDistributionFormats(distributionFormatsCount.result(), totalDistributionCount));
                            catalogueMetricProvider.setDistributionFormats(catalogueId, distributionFormats, resultHandler ->
                                handleResult(distributionFormatsFuture, resultHandler));
                            metricFutures.add(distributionFormatsFuture);

                            Future<Void> datasetNotAccessibleCountFuture = Future.future();
                            catalogueMetricProvider.setDatasetNotAccessibleCount(catalogueId, datasetCount.result() - accessibleDatasetCount.result(), resultHandler ->
                                handleResult(datasetNotAccessibleCountFuture, resultHandler));
                            metricFutures.add(datasetNotAccessibleCountFuture);

                            CompositeFuture.all(metricFutures).setHandler(metricHandler ->
                                handleResult(completionFuture, metricHandler));

                        } else {
                            LOG.error("Failed to retrieve distribution data for catalogue with ID [{}] from database: {}", catalogueId, sourceHandler.cause());
                            completionFuture.fail(sourceHandler.cause());
                        }
                    });

                } else {
                    catalogueMetricProvider.setRenderDistributions(catalogueId, false, metricHandler ->
                        handleResult(completionFuture, metricHandler));
                }
            } else {
                LOG.error("Failed to retrieve existDistribution data for catalogue with ID [{}]: {}", catalogueId, handler.cause());
                completionFuture.fail(handler.cause());
            }
        });

        return completionFuture;
    }

    private Future<Void> refreshViolationMetrics(Catalogue catalogue) {
        Future<Void> completionFuture = Future.future();

        if (catalogue.getDcat()) {
            List<Future> databaseFutures = new ArrayList<>();

            Future<Long> datasetCount = catalogueDataSourceProvider.getDatasetCount(catalogue.getId());
            databaseFutures.add(datasetCount);


            Future<Map<String, Long>> violations = catalogueDataSourceProvider.getDatasetViolations(catalogue.getId());
            databaseFutures.add(violations);

            Future<Long> compliantDatasets = catalogueDataSourceProvider.getDatasetComplianceCount(catalogue.getId());
            databaseFutures.add(compliantDatasets);


            // wait for all database operations to complete before calculating metrics
            CompositeFuture.all(databaseFutures).setHandler(sourceHandler -> {
                if (sourceHandler.succeeded()) {
                    List<Future> metricFutures = new ArrayList<>();

                    Future<Void> renderViolationsFuture = Future.future();
                    catalogueMetricProvider.setRenderViolations(catalogue.getId(), true, resultHandler ->
                        handleResult(renderViolationsFuture, resultHandler));
                    metricFutures.add(renderViolationsFuture);

                    Future<Void> datasetViolationsFuture = Future.future();
                    Metric datasetViolations =
                        new Metric(MetricService.getDatasetViolations(violations.result()));
                    catalogueMetricProvider.setDatasetViolations(catalogue.getId(), datasetViolations, resultHandler ->
                        handleResult(datasetViolationsFuture, resultHandler));
                    metricFutures.add(datasetViolationsFuture);

                    Future<Void> datasetComplianceFuture = Future.future();
                    Metric datasetCompliance =
                        new Metric(MetricService.getDatasetCompliance(compliantDatasets.result(), datasetCount.result()));
                    catalogueMetricProvider.setDatasetCompliance(catalogue.getId(), datasetCompliance, resultHandler ->
                        handleResult(datasetComplianceFuture, resultHandler));
                    metricFutures.add(datasetComplianceFuture);

                    Future<Void> datasetNonConformantCountFuture = Future.future();
                    catalogueMetricProvider.setDatasetNonConformantCount(catalogue.getId(), datasetCount.result() - compliantDatasets.result(), resultHandler ->
                        handleResult(datasetNonConformantCountFuture, resultHandler));
                    metricFutures.add(datasetNonConformantCountFuture);

                    CompositeFuture.all(metricFutures).setHandler(metricHandler ->
                        handleResult(completionFuture, metricHandler));

                } else {
                    LOG.error("Failed to retrieve violation data for catalogue with ID [{}] from database: {}", catalogue.getId(), sourceHandler.cause());
                    completionFuture.fail(sourceHandler.cause());
                }
            });

        } else {
            catalogueMetricProvider.setRenderViolations(catalogue.getId(), false, metricHandler ->
                handleResult(completionFuture, metricHandler));
        }

        return completionFuture;
    }

    private Future<Void> refreshLicenceMetrics(String catalogueId) {
        Future<Void> completionFuture = Future.future();

        List<Future> databaseFutures = new ArrayList<>();

        Future<Long> datasetCount = catalogueDataSourceProvider.getDatasetCount(catalogueId);
        databaseFutures.add(datasetCount);

        Future<Map<String, Long>> datasetLicencesCount = catalogueDataSourceProvider.getDatasetLicences(catalogueId);
        databaseFutures.add(datasetLicencesCount);

        Future<Long> datasetKnownLicencesCount = catalogueDataSourceProvider.getDatasetKnownLicenceCount(catalogueId);
        databaseFutures.add(datasetKnownLicencesCount);


        // wait for all database operations to complete before calculating metrics
        CompositeFuture.all(databaseFutures).setHandler(sourceHandler -> {
            if (sourceHandler.succeeded()) {
                List<Future> metricFutures = new ArrayList<>();

                Future<Void> renderLicencesFuture = Future.future();
                catalogueMetricProvider.setRenderLicences(catalogueId, true, resultHandler ->
                    handleResult(renderLicencesFuture, resultHandler));
                metricFutures.add(renderLicencesFuture);


                Future<Void> datasetLicencesFuture = Future.future();
                Metric datasetLicences =
                    new Metric(MetricService.getDatasetLicences(datasetLicencesCount.result(), datasetCount.result()));
                catalogueMetricProvider.setDatasetLicences(catalogueId, datasetLicences, resultHandler ->
                    handleResult(datasetLicencesFuture, resultHandler));
                metricFutures.add(datasetLicencesFuture);

                Future<Void> datasetKnownLicencesFuture = Future.future();
                Metric datasetKnownLicences =
                    new Metric(MetricService.getDatasetKnownLicences(datasetKnownLicencesCount.result(), datasetCount.result()));
                catalogueMetricProvider.setDatasetKnownLicences(catalogueId, datasetKnownLicences, resultHandler ->
                    handleResult(datasetKnownLicencesFuture, resultHandler));
                metricFutures.add(datasetKnownLicencesFuture);

                CompositeFuture.all(metricFutures).setHandler(metricHandler ->
                    handleResult(completionFuture, metricHandler));

            } else {
                LOG.error("Failed to retrieve licence data for catalogue with ID [{}] from database: {}", catalogueId, sourceHandler.cause());
                completionFuture.fail(sourceHandler.cause());
            }
        });

        return completionFuture;
    }


    private void handleResult(Future<Void> future, AsyncResult result) {
        if (result.succeeded()) {
            future.complete();
        } else {
            future.fail(result.cause());
        }
    }
}
