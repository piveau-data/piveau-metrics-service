package de.fhg.fokus.edp.mqa_metric_service;

import de.fhg.fokus.edp.mqa_metric_service.metric.catalogue.CatalogueMetricProvider;
import de.fhg.fokus.edp.mqa_metric_service.metric.global.GlobalMetricProvider;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static de.fhg.fokus.edp.mqa_metric_service.ApplicationConfig.*;


public class MainVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

    private JsonObject config;
    private ApiKeyHandler apiKeyHandler;

    private GlobalMetricProvider globalMetricProvider;
    private CatalogueMetricProvider catalogueMetricProvider;

    @Override
    public void start() {
        LOG.info("Launching MQA-Metric-Service...");

        // startup is only successful if no step failed
        Future<Void> steps = loadConfig()
            .compose(handler -> initApiKey())
            .compose(handler -> bootstrapVerticles())
            .compose(handler -> startServer());

        steps.setHandler(handler -> {
            if (handler.succeeded()) {
                globalMetricProvider = GlobalMetricProvider.createProxy(vertx, METRIC_SERVICE_GLOBAL_READ_ADDRESS);
                catalogueMetricProvider = CatalogueMetricProvider.createProxy(vertx, METRIC_SERVICE_CATALOGUE_READ_ADDRESS);

                LOG.info("MQA-Metric-Service successfully launched");
            } else {
                handler.cause().printStackTrace();
                LOG.error("Failed to launch MQA-Metric-Service: " + handler.cause());
                vertx.close();
            }
        });
    }

    private Future<Void> loadConfig() {
        Future<Void> future = Future.future();

        ConfigRetriever configRetriever = ConfigRetriever.create(vertx);

        configRetriever.getConfig(handler -> {
            if (handler.succeeded()) {
                config = handler.result();
                LOG.info(config.encodePrettily());
                future.complete();
            } else {
                future.fail("Failed to load config: " + handler.cause());
            }
        });

        configRetriever.listen(change ->
            config = change.getNewConfiguration());

        return future;
    }

    private Future<Void> initApiKey() {
        Future<Void> future = Future.future();

        String apiKey = config.getString(ENV_API_KEY);

        if (apiKey != null && !apiKey.isEmpty()) {
            apiKeyHandler = new ApiKeyHandler(apiKey);
            future.complete();
        } else {
            future.fail("No API key specified");
        }

        return future;
    }

    private CompositeFuture bootstrapVerticles() {
        DeploymentOptions options = new DeploymentOptions()
            .setConfig(config);
//            .setWorker(true);

        List<Future> deploymentFutures = new ArrayList<>();
        deploymentFutures.add(startVerticle(options, GlobalMetricVerticle.class.getName()));
        deploymentFutures.add(startVerticle(options, CatalogueMetricVerticle.class.getName()));

        return CompositeFuture.join(deploymentFutures);
    }

    private Future<Void> startServer() {
        Future<Void> startFuture = Future.future();
        Integer port = config.getInteger(ENV_APPLICATION_PORT, DEFAULT_APPLICATION_PORT);

        OpenAPI3RouterFactory.create(vertx, "webroot/openapi.yaml", handler -> {
            if (handler.succeeded()) {
                OpenAPI3RouterFactory routerFactory = handler.result();
                RouterFactoryOptions options = new RouterFactoryOptions().setMountNotImplementedHandler(true).setMountValidationFailureHandler(true);
                routerFactory.setOptions(options);

                routerFactory.addSecurityHandler("ApiKeyAuth", apiKeyHandler::checkApiKey);

                addCatalogueInfoEndpoints(routerFactory);
                addGlobalMetricEndpoints(routerFactory);
                addCatalogueMetricEndpoints(routerFactory);

                routerFactory.addHandlerByOperationId("refreshMetrics", this::refreshMetrics);

                Router router = routerFactory.getRouter();
                router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET).allowedHeader("Access-Control-Allow-Origin: *"));
                router.route("/*").handler(StaticHandler.create());

                HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(port));
                server.requestHandler(router).listen();

                LOG.info("Server successfully launched on port [{}]", port);
                startFuture.complete();
            } else {
                // Something went wrong during router factory initialization
                LOG.error("Failed to start server at [{}]: {}", port, handler.cause());
                startFuture.fail(handler.cause());
            }
        });

        return startFuture;
    }

    private void addCatalogueInfoEndpoints(OpenAPI3RouterFactory routerFactory) {
        routerFactory.addHandlerByOperationId("infoCatalogues", context ->
            handleVerticleRequest(context, METRIC_SERVICE_CATALOGUE_INFO_ADDRESS));

        routerFactory.addHandlerByOperationId("infoCatalogueAccessibility", context -> {
            handleVerticleRequest(context, METRIC_SERVICE_CATALOGUE_NON_ACCESSIBLE_ADDRESS);
        });

        routerFactory.addHandlerByOperationId("infoCatalogueAccessibilitySize", context ->
            catalogueMetricProvider.getDistributionAccessibilityInfoSize(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));


        routerFactory.addHandlerByOperationId("infoCatalogueCompliance", context -> {
            handleVerticleRequest(context, METRIC_SERVICE_CATALOGUE_NON_COMPLIANT_ADDRESS);
        });

        routerFactory.addHandlerByOperationId("infoCatalogueComplianceSize", context ->
            catalogueMetricProvider.getDatasetComplianceInfoSize(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));
    }

    private void addGlobalMetricEndpoints(OpenAPI3RouterFactory routerFactory) {
        routerFactory.addHandlerByOperationId("globalRenderDistributions", context ->
            globalMetricProvider.getRenderDistributions(result -> handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("globalRenderViolations", context ->
            globalMetricProvider.getRenderViolations(result -> handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("globalRenderLicences", context ->
            globalMetricProvider.getRenderLicences(result -> handleMetricRequest(context, result)));


        routerFactory.addHandlerByOperationId("globalDistributionAccessibilityAccessUrl", context ->
            globalMetricProvider.getDistributionAccessibilityAccessUrl(result -> handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("globalDistributionAccessibilityDownloadUrl", context ->
            globalMetricProvider.getDistributionAccessibilityDownloadUrl(result -> handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("globalDistributionStatusCodes", context ->
            globalMetricProvider.getDistributionStatusCodes(result -> handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("globalDistributionDownloadUrlExists", context ->
            globalMetricProvider.getDistributionDownloadUrlExists(result -> handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("globalDatasetMachineReadability", context ->
            globalMetricProvider.getDatasetMachineReadability(result -> handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("globalDistributionFormats", context ->
            globalMetricProvider.getDistributionFormats(result -> handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("globalDatasetViolation", context ->
            globalMetricProvider.getDatasetViolations(result -> handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("globalDatasetCompliance", context ->
            globalMetricProvider.getDatasetCompliance(result -> handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("globalDatasetLicences", context ->
            globalMetricProvider.getDatasetLicences(result -> handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("globalDatasetKnownLicences", context ->
            globalMetricProvider.getDatasetKnownLicences(result -> handleMetricRequest(context, result)));
    }

    private void addCatalogueMetricEndpoints(OpenAPI3RouterFactory routerFactory) {
        routerFactory.addHandlerByOperationId("catalogueRenderDistributions", context ->
            catalogueMetricProvider.getRenderDistributions(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("catalogueRenderViolations", context ->
            catalogueMetricProvider.getRenderViolations(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("catalogueRenderLicences", context ->
            catalogueMetricProvider.getRenderLicences(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));


        routerFactory.addHandlerByOperationId("catalogueDistributionAccessibilityAccessUrl", context ->
            catalogueMetricProvider.getDistributionAccessibilityAccessUrl(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("catalogueDistributionAccessibilityDownloadUrl", context ->
            catalogueMetricProvider.getDistributionAccessibilityDownloadUrl(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("catalogueDistributionStatusCodes", context ->
            catalogueMetricProvider.getDistributionStatusCodes(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("catalogueDistributionDownloadUrlExists", context ->
            catalogueMetricProvider.getDistributionDownloadUrlExists(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("catalogueDistributionMachineReadability", context ->
            catalogueMetricProvider.getDatasetMachineReadability(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("catalogueDistributionFormats", context ->
            catalogueMetricProvider.getDistributionFormats(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("catalogueDatasetNotAccessibleCount", context ->
            catalogueMetricProvider.getDatasetNotAccessibleCount(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("catalogueDatasetViolation", context ->
            catalogueMetricProvider.getDatasetViolations(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("catalogueDatasetCompliance", context ->
            catalogueMetricProvider.getDatasetCompliance(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("catalogueDatasetNonConformantCount", context ->
            catalogueMetricProvider.getDatasetNonConformantCount(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("catalogueDatasetLicences", context ->
            catalogueMetricProvider.getDatasetLicences(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));

        routerFactory.addHandlerByOperationId("catalogueDatasetKnownLicences", context ->
            catalogueMetricProvider.getDatasetKnownLicences(context.pathParam("catalogueId"), result ->
                handleMetricRequest(context, result)));
    }

    private void refreshMetrics(RoutingContext context) {
        vertx.eventBus().send(METRIC_SERVICE_GLOBAL_REFRESH_ADDRESS, "msg.refresh");
        vertx.eventBus().send(METRIC_SERVICE_CATALOGUE_REFRESH_ADDRESS, "msg.refresh");
        context.response().setStatusCode(202).end();
    }

    private void handleMetricRequest(RoutingContext context, AsyncResult<JsonObject> result) {
        if (result.succeeded()) {
            context.response().putHeader("Content-Type", "application/json");
            context.response().setStatusCode(200).end(result.result().encode());
        } else {
            context.response().setStatusCode(500).end();
        }
    }

    private void handleVerticleRequest(RoutingContext context, String eventBusAddress) {
        context.response().putHeader("Content-Type", "application/json");

        JsonObject message = new JsonObject();

        if (eventBusAddress.equals(METRIC_SERVICE_CATALOGUE_NON_ACCESSIBLE_ADDRESS) || eventBusAddress.equals(METRIC_SERVICE_CATALOGUE_NON_COMPLIANT_ADDRESS)) {

            Integer limit = context.queryParam("limit").size() > 0
                ? Integer.valueOf(context.queryParam("limit").get(0))
                : config.getInteger(ENV_PGSQL_DEFAULT_LIMIT, DEFAULT_PGSQL_DEFAULT_LIMIT);

            Integer offset = context.queryParam("offset").size() > 0
                ? Integer.valueOf(context.queryParam("offset").get(0))
                : 0;

            message
                .put("catalogueId", context.pathParam("catalogueId"))
                .put("limit", limit)
                .put("offset", offset);
        }

        vertx.eventBus().send(eventBusAddress, Json.encode(message), new DeliveryOptions().setSendTimeout(150000), sendHandler -> {
            if (sendHandler.succeeded()) {
                context.response()
                    .setStatusCode(200)
                    .end(Json.encode(sendHandler.result().body()));
            } else {
                LOG.error("Failed to request for address [{}]: [{}]", eventBusAddress, sendHandler.cause());

                JsonObject response = new JsonObject();
                response.put("message", "Failed to handle request: " + sendHandler.cause());
                context.response()
                    .setStatusCode(500)
                    .end(response.encode());
            }
        });
    }

    private Future<Void> startVerticle(DeploymentOptions options, String className) {
        Future<Void> future = Future.future();

        vertx.deployVerticle(className, options, handler -> {
            if (handler.succeeded()) {
                future.complete();
            } else {
                LOG.error("Failed to deploy verticle [{}] : {}", className, handler.cause());
                future.fail("Failed to deploy [" + className + "] : " + handler.cause());
            }
        });

        return future;
    }

    @Override
    public void stop(Future<Void> future) {
        globalMetricProvider.tearDown();
        catalogueMetricProvider.tearDown();
        future.complete();
    }
}
