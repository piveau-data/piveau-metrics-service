package de.fhg.fokus.edp.mqa_metric_service.source;

import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.fhg.fokus.edp.mqa_metric_service.ApplicationConfig.*;

public abstract class DataSourceProvider {

    protected static final Logger LOG = LoggerFactory.getLogger(DataSourceProvider.class);

    protected JDBCClient dbClient;

    protected DataSourceProvider(Vertx vertx) throws SQLException {
        JsonObject env = vertx.getOrCreateContext().config();

        JsonObject config = new JsonObject()
            .put("url", env.getString(ENV_PGSQL_SERVER_HOST, DEFAULT_PGSQL_SERVER_HOST))
            .put("driver_class", "org.postgresql.Driver")
            .put("user", env.getString(ENV_PGSQL_USERNAME, DEFAULT_PGSQL_USERNAME))
            .put("password", env.getString(ENV_PGSQL_PASSWORD, DEFAULT_PGSQL_PASSWORD));

        LOG.debug("Postgres config: {}", config);

        dbClient = JDBCClient.createShared(vertx, config);

        // FIXME this needs to be done in a blocking manner
//        // test if DB connection can be established
//        Future<Boolean> dbConnectionFuture = Future.future();
//        dbClient.getConnection(res ->
//            dbConnectionFuture.complete(res.succeeded()));
//
//        if (dbConnectionFuture.result())
//            throw new SQLException("Could not acquire database connection");
    }

    protected Future<Boolean> getBooleanValue(String query, String queryName) {
        Future<Boolean> future = Future.future();

        dbClient.query(query, handler ->
            handleBooleanResult(queryName, future, handler));

        return future;
    }

    protected Future<Boolean> getBooleanValue(String query, JsonArray params, String queryName) {
        Future<Boolean> future = Future.future();

        dbClient.queryWithParams(query, params, handler ->
            handleBooleanResult(queryName, future, handler));

        return future;
    }

    private void handleBooleanResult(String queryName, Future<Boolean> future, AsyncResult<ResultSet> handler) {
        if (handler.succeeded()) {
            List<JsonArray> results = handler.result().getResults();

            if (results == null || results.isEmpty()) {
                future.fail("No results found for " + queryName);
            } else if (results.size() > 1) {
                future.fail("Too many results found for " + queryName);
            } else {
                future.complete(results.get(0).getBoolean(0));
            }

        } else {
            LOG.error("Failed to retrieve {}: {}", queryName, handler.cause());
            future.fail(handler.cause());
        }
    }

    protected Future<Long> getLongValue(String query, String queryName) {
        Future<Long> future = Future.future();

        dbClient.query(query, handler ->
            handleLongResult(queryName, future, handler));

        return future;
    }

    protected Future<Long> getLongValue(String query, JsonArray params, String queryName) {
        Future<Long> future = Future.future();

        dbClient.queryWithParams(query, params, handler ->
            handleLongResult(queryName, future, handler));

        return future;
    }

    private void handleLongResult(String queryName, Future<Long> future, AsyncResult<ResultSet> handler) {
        if (handler.succeeded()) {
            List<JsonArray> results = handler.result().getResults();

            if (results == null || results.isEmpty()) {
                future.fail("No results found for " + queryName);
            } else if (results.size() > 1) {
                future.fail("Too many results found for " + queryName);
            } else {
                future.complete(results.get(0).getLong(0));
            }

        } else {
            LOG.error("Failed to retrieve data: {}", queryName, handler.cause());
            future.fail(handler.cause());
        }
    }

    protected Future<Map<String, Long>> getCountValues(String query, String keyName, String queryName) {
        Future<Map<String, Long>> future = Future.future();

        dbClient.query(query, handler ->
            handleCountResult(queryName, keyName, future, handler));

        return future;
    }

    protected Future<Map<String, Long>> getCountValues(String query, JsonArray params, String keyName, String queryName) {
        Future<Map<String, Long>> future = Future.future();

        dbClient.queryWithParams(query, params, handler ->
            handleCountResult(queryName, keyName, future, handler));

        return future;
    }

    private void handleCountResult(String queryName, String keyName, Future<Map<String, Long>> future, AsyncResult<ResultSet> handler) {
        if (handler.succeeded()) {
            Map<String, Long> count = new HashMap<>();

            handler.result().getRows().forEach(row ->
                putSafe(count, row.getString(keyName), row.getLong("count")));

            future.complete(count);
        } else {
            LOG.error("Failed to retrieve {}: {}", queryName, handler.cause());
            future.fail(handler.cause());
        }
    }

    // sanitizes keys by adding empty and null values and replacing the key with '?'
    private void putSafe(Map<String, Long> map, String key, Long value) {
        String sanitizedKey = key != null && !key.isEmpty() ? key : "?";

        if (!map.containsKey(sanitizedKey)) {
            map.put(sanitizedKey, value);
        } else {
            map.put(sanitizedKey, map.get(sanitizedKey) + value);
        }
    }
}
