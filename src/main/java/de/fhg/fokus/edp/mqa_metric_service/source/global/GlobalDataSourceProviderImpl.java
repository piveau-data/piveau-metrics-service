package de.fhg.fokus.edp.mqa_metric_service.source.global;

import de.fhg.fokus.edp.mqa_metric_service.source.DataSourceProvider;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.sql.SQLException;
import java.util.Map;

import static de.fhg.fokus.edp.mqa_metric_service.source.global.GlobalQueries.*;

public class GlobalDataSourceProviderImpl extends DataSourceProvider implements GlobalDataSourceProvider {

    public GlobalDataSourceProviderImpl(Vertx vertx) throws SQLException {
        super(vertx);
    }

    @Override
    public void tearDown() {
        dbClient.close();
    }

    @Override
    public Future<Long> getDatasetCount() {
        return getLongValue(DATASET_COUNT, "datasetCount");
    }

    @Override
    public Future<Long> getMachineReadableDatasetCount() {
        return getLongValue(DATASETS_MACHINE_READABLE_COUNT, "datasetMachineReadableCount");
    }

    @Override
    public Future<Long> getCompliantDatasetCount() {
        return getLongValue(DATASET_COMPLIANCE_COUNT, "datasetComplianceCount");
    }

    @Override
    public Future<Long> getDatasetKnownLicenceCount() {
        return getLongValue(DATASET_KNOWN_LICENCES_COUNT, "datasetKnownLicencesCount");
    }

    @Override
    public Future<Long> getDistributionCount() {
        return getLongValue(DISTRIBUTIONS_COUNT, "distributionCount");
    }

    @Override
    public Future<Long> getDistributionAccessUrlAccessibilityCount() {
        return getLongValue(DISTRIBUTIONS_ACCESS_URL_ACCESSIBILITY, "accessibleDistributionCount");
    }

    @Override
    public Future<Long> getDistributionAccessUrlUnknownAccessibilityCount() {
        return getLongValue(DISTRIBUTIONS_ACCESS_URL_UNKNOWN_ACCESSIBILITY, "unknownDistributionAccessUrlCount");
    }

    @Override
    public Future<Long> getDistributionDownloadUrlAccessibilityCount() {
        return getLongValue(DISTRIBUTIONS_DOWNLOAD_URL_ACCESSIBILITY, "accessibleDistributionDownloadUrlCount");
    }

    @Override
    public Future<Long> getDistributionDownloadUrlUnknownAccessibilityCount() {
        return getLongValue(DISTRIBUTIONS_DOWNLOAD_URL_UNKNOWN_ACCESSIBILITY, "unknownDistributionDownloadUrlCount");
    }

    @Override
    public Future<Map<String, Long>> getDistributionAccessStatusCodes() {
        return getCountValues(DISTRIBUTIONS_ACCESS_STATUS_CODES, "status_access_url", "accessStatusCodes");
    }

    @Override
    public Future<Map<String, Long>> getDistributionDownloadStatusCodes() {
        return getCountValues(DISTRIBUTIONS_DOWNLOAD_STATUS_CODES, "status_download_url", "downloadStatusCodes");
    }

    @Override
    public Future<Long> getDistributionsWithDownloadUrl() {
        return getLongValue(DISTRIBUTIONS_DOWNLOAD_URL_EXISTS, "distributionDownloadUrlExists");
    }

    @Override
    public Future<Map<String, Long>> getDistributionFormats() {
        return getCountValues(DISTRIBUTIONS_FORMATS, "format", "distributionFormats");
    }

    @Override
    public Future<Map<String, Long>> getDatasetViolations() {
        return getCountValues(DATASET_VIOLATION_COUNT, "violation_name", "datasetViolations");
    }

    @Override
    public Future<Map<String, Long>> getDatasetLicences() {
        return getCountValues(DATASET_LICENCES, "licence_id", "datasetLicences");
    }

    @Override
    public Future<Boolean> existsDistributions() {
        return getBooleanValue(EXIST_DISTRIBUTIONS, "renderDistributions");
    }

    @Override
    public Future<Boolean> existsDcatCatalogue() {
        return getBooleanValue(EXIST_DCAT_CATALOGUE, "renderViolations");
    }
}
