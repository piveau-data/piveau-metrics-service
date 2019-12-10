package de.fhg.fokus.edp.mqa_metric_service.metric.catalogue;

import de.fhg.fokus.edp.mqa_metric_service.model.Catalogue;
import de.fhg.fokus.edp.mqa_metric_service.model.Metric;
import de.fhg.fokus.edp.mqa_metric_service.model.accessibility.DatasetAccessibility;
import de.fhg.fokus.edp.mqa_metric_service.model.compliance.DatasetViolation;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;

@ProxyGen
public interface CatalogueMetricProvider {

    static CatalogueMetricProvider create(Vertx vertx, Handler<AsyncResult<CatalogueMetricProvider>> readyHandler) {
        return new CatalogueMetricProviderImpl(vertx, readyHandler);
    }

    static CatalogueMetricProvider createProxy(Vertx vertx, String address) {
        return new CatalogueMetricProviderVertxEBProxy(vertx, address);
    }

    void tearDown();


    void getCatalogueInfo(Handler<AsyncResult<JsonObject>> resultHandler);
    void setCatalogueInfo(List<Catalogue> catalogues, Handler<AsyncResult<Void>> resultHandler);

    void getDistributionAccessibilityInfoSize(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setDistributionAccessibilityInfoSize(String catalogueId, Long count, Handler<AsyncResult<Void>> resultHandler);

    void getDatasetComplianceInfoSize(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setDatasetComplianceInfoSize(String catalogueId, Long count, Handler<AsyncResult<Void>> resultHandler);


    void getRenderDistributions(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setRenderDistributions(String catalogueId, Boolean render, Handler<AsyncResult<Void>> resultHandler);

    void getRenderViolations(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setRenderViolations(String catalogueId, Boolean render, Handler<AsyncResult<Void>> resultHandler);

    void getRenderLicences(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setRenderLicences(String catalogueId, Boolean render, Handler<AsyncResult<Void>> resultHandler);


    void getDistributionAccessibilityAccessUrl(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setDistributionAccessibilityAccessUrl(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDistributionAccessibilityDownloadUrl(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setDistributionAccessibilityDownloadUrl(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDistributionStatusCodes(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setDistributionStatusCodes(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDistributionDownloadUrlExists(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setDistributionDownloadUrlExists(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDatasetMachineReadability(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setDatasetMachineReadability(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDistributionFormats(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setDistributionFormats(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDatasetNotAccessibleCount(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setDatasetNotAccessibleCount(String catalogueId, Long count, Handler<AsyncResult<Void>> resultHandler);


    void getDatasetViolations(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setDatasetViolations(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDatasetCompliance(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setDatasetCompliance(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDatasetNonConformantCount(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setDatasetNonConformantCount(String catalogueId, Long count, Handler<AsyncResult<Void>> resultHandler);


    void getDatasetLicences(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setDatasetLicences(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void getDatasetKnownLicences(String catalogueId, Handler<AsyncResult<JsonObject>> resultHandler);
    void setDatasetKnownLicences(String catalogueId, Metric metric, Handler<AsyncResult<Void>> resultHandler);

    void deleteMetric(String catalogueId, String metricName);
}
