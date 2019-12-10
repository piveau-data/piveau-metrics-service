package de.fhg.fokus.edp.mqa_metric_service.metric.global;

import de.fhg.fokus.edp.mqa_metric_service.model.Metric;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
public interface GlobalMetricProvider {

    static GlobalMetricProvider create(Vertx vertx, Handler<AsyncResult<GlobalMetricProvider>> readyHandler) {
        return new GlobalMetricProviderImpl(vertx, readyHandler);
    }

    static GlobalMetricProvider createProxy(Vertx vertx, String address) {
        return new GlobalMetricProviderVertxEBProxy(vertx, address);
    }

    void tearDown();

    void getRenderDistributions(Handler<AsyncResult<JsonObject>> resultHandler);
    void setRenderDistributions(Boolean render, Handler<AsyncResult<Void>> resultHandler);

    void getRenderViolations(Handler<AsyncResult<JsonObject>> resultHandler);
    void setRenderViolations(Boolean render, Handler<AsyncResult<Void>> resultHandler);

    void getRenderLicences(Handler<AsyncResult<JsonObject>> resultHandler);
    void setRenderLicences(Boolean render, Handler<AsyncResult<Void>> resultHandler);


    void getDistributionAccessibilityAccessUrl(Handler<AsyncResult<JsonObject>> resultHandler);
    void setDistributionAccessibilityAccessUrl(Metric metric, Handler<AsyncResult<Void>> resultHandler);
    void getDistributionAccessibilityDownloadUrl(Handler<AsyncResult<JsonObject>> resultHandler);
    void setDistributionAccessibilityDownloadUrl(Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDistributionStatusCodes(Handler<AsyncResult<JsonObject>> resultHandler);
    void setDistributionStatusCodes(Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDistributionDownloadUrlExists(Handler<AsyncResult<JsonObject>> resultHandler);
    void setDistributionDownloadUrlExists(Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDatasetMachineReadability(Handler<AsyncResult<JsonObject>> resultHandler);
    void setDatasetMachineReadability(Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDistributionFormats(Handler<AsyncResult<JsonObject>> resultHandler);
    void setDistributionFormats(Metric metric, Handler<AsyncResult<Void>> resultHandler);


    void getDatasetViolations(Handler<AsyncResult<JsonObject>> resultHandler);
    void setDatasetViolations(Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDatasetCompliance(Handler<AsyncResult<JsonObject>> resultHandler);
    void setDatasetCompliance(Metric metric, Handler<AsyncResult<Void>> resultHandler);


    void getDatasetLicences(Handler<AsyncResult<JsonObject>> resultHandler);
    void setDatasetLicences(Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDatasetKnownLicences(Handler<AsyncResult<JsonObject>> resultHandler);
    void setDatasetKnownLicences(Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void deleteMetric(String metricName);
}
