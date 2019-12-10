package de.fhg.fokus.edp.mqa_metric_service.metric;

import de.fhg.fokus.edp.mqa_metric_service.model.ListDocument;
import de.fhg.fokus.edp.mqa_metric_service.model.Metric;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static de.fhg.fokus.edp.mqa_metric_service.ApplicationConfig.*;

public abstract class MetricProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MetricProvider.class);

    private MongoClient dbClient;

    protected MetricProvider(Vertx vertx) {
        JsonObject env = vertx.getOrCreateContext().config();

        JsonObject config = new JsonObject()
            .put("serverSelectionTimeoutMS", 1000)
            .put("host", env.getString(ENV_MONGODB_SERVER_HOST, DEFAULT_MONGODB_SERVER_HOST))
            .put("port", env.getInteger(ENV_MONGODB_SERVER_PORT, DEFAULT_MONGODB_SERVER_PORT))
            .put("username", env.getString(ENV_MONGODB_USERNAME, DEFAULT_MONGODB_USERNAME))
            .put("password", env.getString(ENV_MONGODB_PASSWORD, DEFAULT_MONGODB_PASSWORD))
            .put("db_name", env.getString(ENV_MONGODB_DB_NAME, DEFAULT_MONGODB_DB_NAME));

        LOG.debug("MongoDB config: {}", config);

        dbClient = MongoClient.createShared(vertx, config);
    }

    public void tearDown() {
        dbClient.close();
    }

    protected void findMetric(String collection, String metric, Handler<AsyncResult<JsonObject>> resultHandler) {
        JsonObject metricId = new JsonObject().put("_id", metric);
        LOG.debug("Attempting to retrieve metric [{}] from collection [{}]", metric, collection);

        dbClient.find(collection, metricId, handler -> {
            if (handler.succeeded()) {

                JsonObject response = new JsonObject();
                List<JsonObject> resultList = handler.result();

                if (resultList.size() == 1) {
                    JsonObject result = resultList.get(0);
                    result.remove("_id");
                    response.put("success", true).put("result", result.getValue("value"));
                } else if (resultList.isEmpty()) {
                    response.put("success", false).put("message", "No results found");
                } else {
                    response.put("success", false).put("message", "Multiple results found");
                }

                resultHandler.handle(Future.succeededFuture(response));
            } else {
                LOG.error("Failed to retrieve metric [{}] from collection [{}]. Cause: {}", metric, collection, handler.cause());
                resultHandler.handle(Future.failedFuture(handler.cause()));
            }
        });
    }

    protected void deleteMetric(String collection, String metricName) {
        JsonObject metricId = new JsonObject().put("_id", metricName);

        dbClient.removeDocument(collection, metricId, handler -> {
            if (handler.succeeded()) {
                LOG.debug("Successfully removed metric with ID [{}] from collection [{}]", metricId, collection);
            } else {
                LOG.error("Failed to remove metric with ID [{}] from collection [{}]. Cause: {}", metricId, collection, handler.cause());
            }
        });
    }

    protected void saveRenderDocument(String collection, String metricId, boolean render, Handler<AsyncResult<Void>> resultHandler) {
        JsonObject document = new JsonObject()
            .put("_id", metricId)
            .put("value", render);

        saveMetric(collection, metricId, document, resultHandler);
    }

    protected void saveCountDocument(String collection, String metricId, long count, Handler<AsyncResult<Void>> resultHandler) {
        JsonObject document = new JsonObject()
            .put("_id", metricId)
            .put("value", count);

        saveMetric(collection, metricId, document, resultHandler);
    }

    // creates a direct mapping from key to value
    protected void saveListDocument(String collection, String metricId, Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        JsonObject values = new JsonObject();
        metric.getValues().forEach(values::put);

        JsonObject document = new JsonObject()
            .put("_id", metricId)
            .put("value", values);

        saveMetric(collection, metricId, document, resultHandler);
    }

    protected void saveListDocument(String collection, String metricId, List<? extends ListDocument> entities, Handler<AsyncResult<Void>> resultHandler) {
        JsonArray jsonList = new JsonArray();
        entities.forEach(entity ->
            jsonList.add(entity.toJson()));

        JsonObject result = new JsonObject()
            .put("_id", metricId)
            .put("value", jsonList);

        saveMetric(collection, metricId, result, resultHandler);
    }


    // uses name as key, useful when dynamic keys are expected (eg catalogue names)
    protected void createCatalogueDocument(String collection, String metricId, Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        JsonArray values = new JsonArray();
        metric.getValues().forEach((key, value) -> {
            JsonObject entry = new JsonObject()
                .put("name", key)
                .put("percentage", value);

            values.add(entry);
        });

        JsonObject document = new JsonObject()
            .put("_id", metricId)
            .put("value", values);

        saveMetric(collection, metricId, document, resultHandler);
    }

    private void saveMetric(String collection, String metricId, JsonObject document, Handler<AsyncResult<Void>> resultHandler) {
        dbClient.save(collection, document, handler -> {
            if (handler.succeeded()) {
                LOG.debug("Successfully saved metric with ID [{}] to collection [{}]: {}", metricId, collection, document.encodePrettily());
                resultHandler.handle(Future.succeededFuture());
            } else {
                LOG.error("Failed to upsert metric with ID [{}] to collection [{}]. Cause: {}", metricId, collection, handler.cause());
                resultHandler.handle(Future.failedFuture(handler.cause()));
            }
        });
    }
}
