package de.fhg.fokus.edp.mqa_metric_service.source.catalogue;

import de.fhg.fokus.edp.mqa_metric_service.model.Catalogue;
import de.fhg.fokus.edp.mqa_metric_service.model.accessibility.DatasetAccessibility;
import de.fhg.fokus.edp.mqa_metric_service.model.compliance.DatasetViolation;
import io.vertx.core.Future;

import java.util.List;
import java.util.Map;

public interface CatalogueDataSourceProvider {

    void tearDown();

    Future<List<Catalogue>> getCatalogues();
    Future<Long> countDatasetsWithAccessibilityIssues(String catalogueId);
    Future<List<DatasetAccessibility>> getDatasetsWithAccessibilityIssues(String catalogueId, Integer limit, Integer offset);
    Future<Long> countCatalogueComplianceInfo(String catalogueId);
    Future<List<DatasetViolation>> getCatalogueComplianceInfo(String catalogueId, Integer limit, Integer offset);

    Future<Boolean> existsDistributions(String catalogueId);

    Future<Long> getDistributionCount(String catalogueId);
    Future<Long> getDistributionAccessUrlAccessibilityCount(String catalogueId);
    Future<Long> getDistributionUnknownAccessUrlAccessibilityCount(String catalogueId);
    Future<Long> getDistributionDownloadUrlAccessibilityCount(String catalogueId);
    Future<Long> getDistributionUnknownDownloadUrlAccessibilityCount(String catalogueId);
    Future<Long> getDatasetMachineReadableCount(String catalogueId);
    Future<Map<String, Long>> getDistributionFormats(String catalogueId);
    Future<Long> getDistributionsWithDownloadUrl(String catalogueId);
    Future<Map<String, Long>> getDistributionAccessStatusCodes(String catalogueId);
    Future<Map<String, Long>> getDistributionDownloadStatusCodes(String catalogueId);

    Future<Long> getDatasetCount(String catalogueId);
    Future<Long> getDatasetComplianceCount(String catalogueId);
    Future<Long> getDatasetAccessibilityCount(String catalogueId);
    Future<Long> getDatasetKnownLicenceCount(String catalogueId);
    Future<Map<String, Long>> getDatasetViolations(String catalogueId);
    Future<Map<String, Long>> getDatasetLicences(String catalogueId);

}
