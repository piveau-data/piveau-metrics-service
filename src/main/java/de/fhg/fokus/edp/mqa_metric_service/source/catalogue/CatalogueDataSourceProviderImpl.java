package de.fhg.fokus.edp.mqa_metric_service.source.catalogue;

import de.fhg.fokus.edp.mqa_metric_service.model.Catalogue;
import de.fhg.fokus.edp.mqa_metric_service.model.accessibility.DatasetAccessibility;
import de.fhg.fokus.edp.mqa_metric_service.model.accessibility.Distribution;
import de.fhg.fokus.edp.mqa_metric_service.model.compliance.DatasetViolation;
import de.fhg.fokus.edp.mqa_metric_service.model.compliance.Violation;
import de.fhg.fokus.edp.mqa_metric_service.source.DataSourceProvider;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.fhg.fokus.edp.mqa_metric_service.source.catalogue.CatalogueQueries.*;

public class CatalogueDataSourceProviderImpl extends DataSourceProvider implements CatalogueDataSourceProvider {

    public CatalogueDataSourceProviderImpl(Vertx vertx) throws SQLException {
        super(vertx);
    }

    @Override
    public void tearDown() {
        dbClient.close();
    }

    @Override
    public Future<List<Catalogue>> getCatalogues() {
        Future<List<Catalogue>> future = Future.future();

        dbClient.query(CATALOGUE_INFO, handler -> {
            if (handler.succeeded()) {
                List<Catalogue> results = new ArrayList<>();

                handler.result().getRows().forEach(row -> {
                    Catalogue catalogue = new Catalogue();
                    catalogue.setId(String.valueOf(row.getInteger("id")));
                    catalogue.setName(row.getString("name"));
                    catalogue.setTitle(row.getString("title"));
                    catalogue.setDescription(row.getString("description"));
                    catalogue.setSpatial(row.getString("spatial"));
                    catalogue.setDcat(row.getBoolean("dcat"));
                    catalogue.setRating(Math.random());
                    results.add(catalogue);
                });

                future.complete(results);
            } else {
                LOG.error("Failed to retrieve catalogueIds: {}", handler.cause().getMessage());
                future.fail(handler.cause());
            }
        });

        return future;
    }

    @Override
    public Future<Long> countDatasetsWithAccessibilityIssues(String catalogueId) {
        return getLongValue(COUNT_DATASETS_WITH_ACCESSIBILITY_ISSUES, new JsonArray().add(Integer.valueOf(catalogueId)), "countDatasetsWithAccessibilityIssues");
    }

    @Override
    public Future<List<DatasetAccessibility>> getDatasetsWithAccessibilityIssues(String catalogueId, Integer limit, Integer offset) {
        Future<List<DatasetAccessibility>> future = Future.future();

        JsonArray queryParams = new JsonArray()
            .add(Integer.valueOf(catalogueId))
            .add(limit)
            .add(offset);

        dbClient.queryWithParams(GET_DATASETS_WITH_ACCESSIBILITY_ISSUES, queryParams, datasetHandler -> {
            if (datasetHandler.succeeded()) {
                List<Future> datasetFutures = new ArrayList<>();

                datasetHandler.result().getRows().forEach(row -> {
                    Future<DatasetAccessibility> datasetFuture = Future.future();
                    datasetFutures.add(datasetFuture);

                    DatasetAccessibility datasetAccessibility = new DatasetAccessibility();
                    datasetAccessibility.setId(String.valueOf(row.getInteger("id")));
                    datasetAccessibility.setName(row.getString("title"));

                    dbClient.queryWithParams(NON_ACCESSIBLE_DISTRIBUTIONS_BY_ID, new JsonArray().add(Integer.valueOf(datasetAccessibility.getId())), distributionHandler -> {
                        if (distributionHandler.succeeded()) {
                            List<Distribution> distributions = distributionHandler.result().getRows().stream().map(distRow -> {
                                Distribution distribution = new Distribution();
                                distribution.setId(String.valueOf(distRow.getInteger("id")));
                                distribution.setFormat(distRow.getString("format"));
                                distribution.setMetaDataMediaType(distRow.getString("media_type"));
                                distribution.setCheckedMediaType(distRow.getString("media_type_checked"));
                                distribution.setAccessUrl(distRow.getString("access_url"));
                                distribution.setStatusAccessUrl(distRow.getInteger("status_access_url"));
                                distribution.setDownloadUrl(distRow.getString("download_url"));
                                distribution.setStatusDownloadUrl(distRow.getInteger("status_download_url"));
                                return distribution;
                            }).collect(Collectors.toList());

                            datasetAccessibility.setDistributions(distributions);
                            datasetFuture.complete(datasetAccessibility);
                        } else {
                            datasetFuture.fail(distributionHandler.cause());
                        }
                    });
                });

                CompositeFuture.all(datasetFutures).setHandler(completionHandler -> {
                    if (completionHandler.succeeded()) {
                        future.complete(completionHandler.result().list());
                    } else {
                        future.fail(completionHandler.cause());
                    }
                });
            } else {
                LOG.error("Fail: " + datasetHandler.cause());
                future.fail(datasetHandler.cause());
            }
        });

        return future;
    }

    @Override
    public Future<Long> countCatalogueComplianceInfo(String catalogueId) {
        return getLongValue(COUNT_DATASETS_WITH_COMPLIANCE_ISSUES, new JsonArray().add(Integer.valueOf(catalogueId)), "countDatasetsWithComplianceIssues");
    }

    @Override
    public Future<List<DatasetViolation>> getCatalogueComplianceInfo(String catalogueId, Integer limit, Integer offset) {
        Future<List<DatasetViolation>> future = Future.future();

        JsonArray queryParams = new JsonArray()
            .add(Integer.valueOf(catalogueId))
            .add(limit)
            .add(offset);

        dbClient.queryWithParams(GET_DATASETS_WITH_COMPLIANCE_ISSUES, queryParams, datasetHandler -> {
            if (datasetHandler.succeeded()) {
                List<Future> datasetFutures = new ArrayList<>();

                datasetHandler.result().getRows().forEach(row -> {
                    Future<DatasetViolation> datasetFuture = Future.future();
                    datasetFutures.add(datasetFuture);

                    DatasetViolation datasetViolation = new DatasetViolation();
                    datasetViolation.setId(String.valueOf(row.getInteger("id")));
                    datasetViolation.setName(row.getString("title"));

                    dbClient.queryWithParams(VIOLATIONS_BY_DATASET_ID, new JsonArray().add(Integer.valueOf(datasetViolation.getId())), violationHandler -> {
                        if (violationHandler.succeeded()) {
                            List<Violation> violations = violationHandler.result().getRows().stream().map(violationRow -> {
                                Violation violation = new Violation();
                                violation.setName(violationRow.getString("violation_name"));
                                violation.setMessage(violationRow.getString("violation_instance"));
                                return violation;
                            }).collect(Collectors.toList());

                            datasetViolation.setViolations(violations);
                            datasetFuture.complete(datasetViolation);
                        } else {
                            datasetFuture.fail(violationHandler.cause());
                        }
                    });
                });

                CompositeFuture.all(datasetFutures).setHandler(completionHandler -> {
                    if (completionHandler.succeeded()) {
                        future.complete(completionHandler.result().list());
                    } else {
                        future.fail(completionHandler.cause());
                    }
                });
            } else {
                future.fail(datasetHandler.cause());
            }
        });

        return future;
    }

    @Override
    public Future<Boolean> existsDistributions(String catalogueId) {
        return getBooleanValue(EXIST_DISTRIBUTIONS, new JsonArray().add(Integer.valueOf(catalogueId)), "renderDistributions");
    }

    @Override
    public Future<Long> getDatasetCount(String catalogueId) {
        return getLongValue(DATASET_COUNT, new JsonArray().add(Integer.valueOf(catalogueId)), "datasetCount");
    }

    @Override
    public Future<Long> getDatasetAccessibilityCount(String catalogueId) {
        return getLongValue(DATASET_ACCESSIBILITY, new JsonArray().add(Integer.valueOf(catalogueId)), "accessibleDatasetCount");
    }

    @Override
    public Future<Long> getDatasetMachineReadableCount(String catalogueId) {
        return getLongValue(DATASET_MACHINE_READABLE_COUNT, new JsonArray().add(Integer.valueOf(catalogueId)), "datasetMachineReadableCount");
    }

    @Override
    public Future<Long> getDatasetComplianceCount(String catalogueId) {
        return getLongValue(DATASET_COMPLIANCE_COUNT, new JsonArray().add(Integer.valueOf(catalogueId)), "datasetComplianceCount");
    }

    @Override
    public Future<Long> getDatasetKnownLicenceCount(String catalogueId) {
        return getLongValue(DATASET_KNOWN_LICENCES_COUNT, new JsonArray().add(Integer.valueOf(catalogueId)), "datasetKnownLicenceCount");
    }

    @Override
    public Future<Map<String, Long>> getDatasetViolations(String catalogueId) {
        return getCountValues(DATASET_VIOLATION_COUNT, new JsonArray().add(Integer.valueOf(catalogueId)), "violation_name", "datasetViolations");
    }

    @Override
    public Future<Map<String, Long>> getDatasetLicences(String catalogueId) {
        return getCountValues(DATASET_LICENCE_COUNT, new JsonArray().add(Integer.valueOf(catalogueId)), "licence_id", "datasetLicences");
    }

    @Override
    public Future<Long> getDistributionCount(String catalogueId) {
        return getLongValue(DISTRIBUTIONS_COUNT, new JsonArray().add(Integer.valueOf(catalogueId)), "distributionCount");
    }

    @Override
    public Future<Long> getDistributionAccessUrlAccessibilityCount(String catalogueId) {
        return getLongValue(DISTRIBUTION_ACCESSIBILITY_ACCESS_URL, new JsonArray().add(Integer.valueOf(catalogueId)), "accessibleDistributionAccessUrlCount");
    }

    @Override
    public Future<Long> getDistributionUnknownAccessUrlAccessibilityCount(String catalogueId) {
        return getLongValue(DISTRIBUTION_UNKNOWN_ACCESSIBILITY_ACCESS_URL, new JsonArray().add(Integer.valueOf(catalogueId)), "unknownDistributionAccessUrlCount");
    }

    @Override
    public Future<Long> getDistributionDownloadUrlAccessibilityCount(String catalogueId) {
        return getLongValue(DISTRIBUTION_ACCESSIBILITY_DOWNLOAD_URL, new JsonArray().add(Integer.valueOf(catalogueId)), "accessibleDistributionDownloadUrlCount");
    }

    @Override
    public Future<Long> getDistributionUnknownDownloadUrlAccessibilityCount(String catalogueId) {
        return getLongValue(DISTRIBUTION_UNKNOWN_ACCESSIBILITY_DOWNLOAD_URL, new JsonArray().add(Integer.valueOf(catalogueId)), "unknownDistributionDownloadUrlCount");
    }

    @Override
    public Future<Map<String, Long>> getDistributionFormats(String catalogueId) {
        return getCountValues(DISTRIBUTIONS_FORMATS, new JsonArray().add(Integer.valueOf(catalogueId)), "format", "distributionFormats");
    }

    @Override
    public Future<Map<String, Long>> getDistributionAccessStatusCodes(String catalogueId) {
        return getCountValues(DISTRIBUTIONS_ACCESS_STATUS_CODES, new JsonArray().add(Integer.valueOf(catalogueId)), "status_access_url", "distributionAccessCodes");
    }

    @Override
    public Future<Map<String, Long>> getDistributionDownloadStatusCodes(String catalogueId) {
        return getCountValues(DISTRIBUTIONS_DOWNLOAD_STATUS_CODES, new JsonArray().add(Integer.valueOf(catalogueId)), "status_download_url", "distributionDownloadCodes");
    }

    @Override
    public Future<Long> getDistributionsWithDownloadUrl(String catalogueId) {
        return getLongValue(DISTRIBUTIONS_DOWNLOAD_URL_EXISTS, new JsonArray().add(Integer.valueOf(catalogueId)), "distributionDownloadUrlExists");
    }
}
