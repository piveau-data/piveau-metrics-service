package de.fhg.fokus.edp.mqa_metric_service.metric.global;

import de.fhg.fokus.edp.mqa_metric_service.model.Metric;
import de.fhg.fokus.edp.mqa_metric_service.metric.MetricProvider;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import static de.fhg.fokus.edp.mqa_metric_service.metric.MetricConstants.*;

public class GlobalMetricProviderImpl extends MetricProvider implements GlobalMetricProvider {

    GlobalMetricProviderImpl(Vertx vertx, Handler<AsyncResult<GlobalMetricProvider>> readyHandler) {
        super(vertx);
        readyHandler.handle(Future.succeededFuture(this));
    }

    @Override
    public void getRenderDistributions(Handler<AsyncResult<JsonObject>> resultHandler) {
        findGlobalMetric(METRIC_RENDER_DISTRIBUTIONS, resultHandler);
    }

    @Override
    public void setRenderDistributions(Boolean render, Handler<AsyncResult<Void>> resultHandler) {
        saveRenderDocument(COLLECTION_GLOBAL, METRIC_RENDER_DISTRIBUTIONS, render, resultHandler);
    }

    @Override
    public void getRenderViolations(Handler<AsyncResult<JsonObject>> resultHandler) {
        findGlobalMetric(METRIC_RENDER_VIOLATIONS, resultHandler);
    }

    @Override
    public void setRenderViolations(Boolean render, Handler<AsyncResult<Void>> resultHandler) {
        saveRenderDocument(COLLECTION_GLOBAL, METRIC_RENDER_VIOLATIONS, render, resultHandler);
    }

    @Override
    public void getRenderLicences(Handler<AsyncResult<JsonObject>> resultHandler) {
        findGlobalMetric(METRIC_RENDER_LICENCES, resultHandler);
    }

    @Override
    public void setRenderLicences(Boolean render, Handler<AsyncResult<Void>> resultHandler) {
        saveRenderDocument(COLLECTION_GLOBAL, METRIC_RENDER_LICENCES, render, resultHandler);
    }

    @Override
    public void getDistributionAccessibilityAccessUrl(Handler<AsyncResult<JsonObject>> resultHandler) {
        findGlobalMetric(METRIC_DISTRIBUTION_ACCESSIBILITY_ACCESS_URL, resultHandler);
    }

    @Override
    public void setDistributionAccessibilityAccessUrl(Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        saveListDocument(COLLECTION_GLOBAL, METRIC_DISTRIBUTION_ACCESSIBILITY_ACCESS_URL, metric, resultHandler);
    }

    @Override
    public void getDistributionAccessibilityDownloadUrl(Handler<AsyncResult<JsonObject>> resultHandler) {
        findGlobalMetric(METRIC_DISTRIBUTION_ACCESSIBILITY_DOWNLOAD_URL, resultHandler);
    }

    @Override
    public void setDistributionAccessibilityDownloadUrl(Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        saveListDocument(COLLECTION_GLOBAL, METRIC_DISTRIBUTION_ACCESSIBILITY_DOWNLOAD_URL, metric, resultHandler);
    }

    @Override
    public void getDistributionStatusCodes(Handler<AsyncResult<JsonObject>> resultHandler) {
        findGlobalMetric(METRIC_STATUS_CODES, resultHandler);
    }

    @Override
    public void setDistributionStatusCodes(Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        createCatalogueDocument(COLLECTION_GLOBAL, METRIC_STATUS_CODES, metric, resultHandler);
    }

    @Override
    public void getDistributionDownloadUrlExists(Handler<AsyncResult<JsonObject>> resultHandler) {
        findGlobalMetric(METRIC_DOWNLOAD_URL_EXIST, resultHandler);
    }

    @Override
    public void setDistributionDownloadUrlExists(Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        saveListDocument(COLLECTION_GLOBAL, METRIC_DOWNLOAD_URL_EXIST, metric, resultHandler);
    }

    @Override
    public void getDatasetMachineReadability(Handler<AsyncResult<JsonObject>> resultHandler) {
        findGlobalMetric(METRIC_MACHINE_READABILITY, resultHandler);
    }

    @Override
    public void setDatasetMachineReadability(Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        saveListDocument(COLLECTION_GLOBAL, METRIC_MACHINE_READABILITY, metric, resultHandler);
    }

    @Override
    public void getDistributionFormats(Handler<AsyncResult<JsonObject>> resultHandler) {
        findGlobalMetric(METRIC_DISTRIBUTION_FORMATS, resultHandler);
    }

    @Override
    public void setDistributionFormats(Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        createCatalogueDocument(COLLECTION_GLOBAL, METRIC_DISTRIBUTION_FORMATS, metric, resultHandler);
    }

    @Override
    public void getDatasetViolations(Handler<AsyncResult<JsonObject>> resultHandler) {
        findGlobalMetric(METRIC_DATASET_VIOLATIONS, resultHandler);
    }

    @Override
    public void setDatasetViolations(Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        createCatalogueDocument(COLLECTION_GLOBAL, METRIC_DATASET_VIOLATIONS, metric, resultHandler);
    }

    @Override
    public void getDatasetCompliance(Handler<AsyncResult<JsonObject>> resultHandler) {
        findGlobalMetric(METRIC_DATASET_COMPLIANCE, resultHandler);
    }

    @Override
    public void setDatasetCompliance(Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        saveListDocument(COLLECTION_GLOBAL, METRIC_DATASET_COMPLIANCE, metric, resultHandler);
    }

    @Override
    public void getDatasetLicences(Handler<AsyncResult<JsonObject>> resultHandler) {
        findGlobalMetric(METRIC_DATASET_LICENCES, resultHandler);
    }

    @Override
    public void setDatasetLicences(Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        createCatalogueDocument(COLLECTION_GLOBAL, METRIC_DATASET_LICENCES, metric, resultHandler);
    }

    @Override
    public void getDatasetKnownLicences(Handler<AsyncResult<JsonObject>> resultHandler) {
        findGlobalMetric(METRIC_DATASET_KNOWN_LICENCES, resultHandler);
    }

    @Override
    public void setDatasetKnownLicences(Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        saveListDocument(COLLECTION_GLOBAL, METRIC_DATASET_KNOWN_LICENCES, metric, resultHandler);
    }

    @Override
    public void deleteMetric(String metricName) {
        deleteMetric(COLLECTION_GLOBAL, metricName);
    }

    private void findGlobalMetric(String metricId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(COLLECTION_GLOBAL, metricId, resultHandler);
    }
}
