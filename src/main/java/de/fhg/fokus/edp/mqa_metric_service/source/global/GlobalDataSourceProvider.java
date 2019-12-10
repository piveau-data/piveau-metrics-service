package de.fhg.fokus.edp.mqa_metric_service.source.global;

import io.vertx.core.Future;

import java.util.Map;

public interface GlobalDataSourceProvider {

    void tearDown();

    Future<Boolean> existsDistributions();
    Future<Boolean> existsDcatCatalogue();

    Future<Long> getDatasetCount();

    Future<Long> getMachineReadableDatasetCount();
    Future<Long> getCompliantDatasetCount();
    Future<Long> getDatasetKnownLicenceCount();


    Future<Long> getDistributionCount();

    Future<Long> getDistributionAccessUrlAccessibilityCount();
    Future<Long> getDistributionAccessUrlUnknownAccessibilityCount();

    Future<Long> getDistributionDownloadUrlAccessibilityCount();
    Future<Long> getDistributionDownloadUrlUnknownAccessibilityCount();

    Future<Map<String, Long>> getDistributionAccessStatusCodes();
    Future<Map<String, Long>> getDistributionDownloadStatusCodes();
    Future<Long> getDistributionsWithDownloadUrl();

    Future<Map<String, Long>> getDistributionFormats();
    Future<Map<String, Long>> getDatasetViolations();
    Future<Map<String, Long>> getDatasetLicences();
}
