package de.fhg.fokus.edp.mqa_metric_service.metric.catalogue;

import de.fhg.fokus.edp.mqa_metric_service.model.Catalogue;
import de.fhg.fokus.edp.mqa_metric_service.model.Metric;
import de.fhg.fokus.edp.mqa_metric_service.metric.MetricProvider;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;

import static de.fhg.fokus.edp.mqa_metric_service.metric.MetricConstants.*;

public class CatalogueMetricProviderImpl extends MetricProvider implements CatalogueMetricProvider {

    CatalogueMetricProviderImpl(Vertx vertx, Handler<AsyncResult<CatalogueMetricProvider>> readyHandler) {
        super(vertx);
        readyHandler.handle(Future.succeededFuture(this));
    }

    @Override
    public void getCatalogueInfo(Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(COLLECTION_INFO, INFO_CATALOGUES, resultHandler);
    }

    @Override
    public void setCatalogueInfo(List<Catalogue> catalogues, Handler<AsyncResult<Void>> resultHandler) {
        saveListDocument(COLLECTION_INFO, INFO_CATALOGUES, catalogues, resultHandler);
    }

    @Override
    public void getDistributionAccessibilityInfoSize(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, INFO_CATALOGUE_DISTRIBUTIONS_NON_ACCESSIBILE_SIZE, resultHandler);
    }

    @Override
    public void setDistributionAccessibilityInfoSize(String catalogueId, Long count, Handler<AsyncResult<Void>> resultHandler) {
        saveCountDocument(catalogueId, INFO_CATALOGUE_DISTRIBUTIONS_NON_ACCESSIBILE_SIZE, count, resultHandler);
    }

    @Override
    public void getDatasetComplianceInfoSize(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, INFO_CATALOGUE_DATASETS_NON_COMPLIANT_SIZE, resultHandler);
    }

    @Override
    public void setDatasetComplianceInfoSize(String catalogueId, Long count, Handler<AsyncResult<Void>> resultHandler) {
        saveCountDocument(catalogueId, INFO_CATALOGUE_DATASETS_NON_COMPLIANT_SIZE, count, resultHandler);
    }

    @Override
    public void getRenderDistributions(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_RENDER_DISTRIBUTIONS, resultHandler);
    }

    @Override
    public void setRenderDistributions(String catalogueId, Boolean render, Handler<AsyncResult<Void>> resultHandler) {
        saveRenderDocument(catalogueId, METRIC_RENDER_DISTRIBUTIONS, render, resultHandler);
    }

    @Override
    public void getRenderViolations(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_RENDER_VIOLATIONS, resultHandler);
    }

    @Override
    public void setRenderViolations(String catalogueId, Boolean render, Handler<AsyncResult<Void>> resultHandler) {
        saveRenderDocument(catalogueId, METRIC_RENDER_VIOLATIONS, render, resultHandler);
    }

    @Override
    public void getRenderLicences(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_RENDER_LICENCES, resultHandler);
    }

    @Override
    public void setRenderLicences(String catalogueId, Boolean render, Handler<AsyncResult<Void>> resultHandler) {
        saveRenderDocument(catalogueId, METRIC_RENDER_LICENCES, render, resultHandler);
    }

    @Override
    public void getDistributionAccessibilityAccessUrl(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_DISTRIBUTION_ACCESSIBILITY_ACCESS_URL, resultHandler);
    }

    @Override
    public void setDistributionAccessibilityAccessUrl(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        saveListDocument(catalogueId, METRIC_DISTRIBUTION_ACCESSIBILITY_ACCESS_URL, metric, resultHandler);
    }

    @Override
    public void getDistributionAccessibilityDownloadUrl(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_DISTRIBUTION_ACCESSIBILITY_DOWNLOAD_URL, resultHandler);
    }

    @Override
    public void setDistributionAccessibilityDownloadUrl(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        saveListDocument(catalogueId, METRIC_DISTRIBUTION_ACCESSIBILITY_DOWNLOAD_URL, metric, resultHandler);
    }

    @Override
    public void getDistributionStatusCodes(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_STATUS_CODES, resultHandler);
    }

    @Override
    public void setDistributionStatusCodes(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        createCatalogueDocument(catalogueId, METRIC_STATUS_CODES, metric, resultHandler);
    }

    @Override
    public void getDistributionDownloadUrlExists(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_DOWNLOAD_URL_EXIST, resultHandler);
    }

    @Override
    public void setDistributionDownloadUrlExists(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        saveListDocument(catalogueId, METRIC_DOWNLOAD_URL_EXIST, metric, resultHandler);
    }

    @Override
    public void getDatasetMachineReadability(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_MACHINE_READABILITY, resultHandler);
    }

    @Override
    public void setDatasetMachineReadability(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        saveListDocument(catalogueId, METRIC_MACHINE_READABILITY, metric, resultHandler);
    }

    @Override
    public void getDistributionFormats(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_DISTRIBUTION_FORMATS, resultHandler);
    }

    @Override
    public void setDistributionFormats(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        createCatalogueDocument(catalogueId, METRIC_DISTRIBUTION_FORMATS, metric, resultHandler);
    }

    @Override
    public void getDatasetNotAccessibleCount(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_DATASET_NOT_ACCESSIBLE_COUNT, resultHandler);
    }

    @Override
    public void setDatasetNotAccessibleCount(String catalogueId, Long count, Handler<AsyncResult<Void>> resultHandler) {
        saveCountDocument(catalogueId, METRIC_DATASET_NOT_ACCESSIBLE_COUNT, count, resultHandler);
    }

    @Override
    public void getDatasetViolations(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_DATASET_VIOLATIONS, resultHandler);
    }

    @Override
    public void setDatasetViolations(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        createCatalogueDocument(catalogueId, METRIC_DATASET_VIOLATIONS, metric, resultHandler);
    }

    @Override
    public void getDatasetCompliance(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_DATASET_COMPLIANCE, resultHandler);
    }

    @Override
    public void setDatasetCompliance(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        saveListDocument(catalogueId, METRIC_DATASET_COMPLIANCE, metric, resultHandler);
    }

    @Override
    public void getDatasetNonConformantCount(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_DATASET_NON_CONFORMANT_COUNT, resultHandler);
    }

    @Override
    public void setDatasetNonConformantCount(String catalogueId, Long count, Handler<AsyncResult<Void>> resultHandler) {
        saveCountDocument(catalogueId, METRIC_DATASET_NON_CONFORMANT_COUNT, count, resultHandler);
    }

    @Override
    public void getDatasetLicences(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_DATASET_LICENCES, resultHandler);
    }

    @Override
    public void setDatasetLicences(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        createCatalogueDocument(catalogueId, METRIC_DATASET_LICENCES, metric, resultHandler);
    }

    @Override
    public void getDatasetKnownLicences(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler) {
        findMetric(catalogueId, METRIC_DATASET_KNOWN_LICENCES, resultHandler);
    }

    @Override
    public void setDatasetKnownLicences(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler) {
        saveListDocument(catalogueId, METRIC_DATASET_KNOWN_LICENCES, metric, resultHandler);
    }

    @Override
    public void deleteMetric(String catalogueId, String metricName) {
        super.deleteMetric(catalogueId, metricName);
    }
}
