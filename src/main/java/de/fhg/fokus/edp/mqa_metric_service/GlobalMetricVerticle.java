package de.fhg.fokus.edp.mqa_metric_service;

import de.fhg.fokus.edp.mqa_metric_service.metric.global.GlobalMetricProvider;
import de.fhg.fokus.edp.mqa_metric_service.metric.MetricService;
import de.fhg.fokus.edp.mqa_metric_service.model.Metric;
import de.fhg.fokus.edp.mqa_metric_service.source.global.GlobalDataSourceProvider;
import de.fhg.fokus.edp.mqa_metric_service.source.global.GlobalDataSourceProviderImpl;
import io.vertx.core.*;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.fhg.fokus.edp.mqa_metric_service.ApplicationConfig.*;
import static de.fhg.fokus.edp.mqa_metric_service.metric.MetricConstants.METRIC_DISTRIBUTION_ACCESSIBILITY_DOWNLOAD_URL;

public class GlobalMetricVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalMetricVerticle.class);

    private GlobalDataSourceProvider globalDataSourceProvider;
    private GlobalMetricProvider globalMetricProvider;

    @Override
    public void start(Future<Void> startFuture) {
        try {
            globalDataSourceProvider = new GlobalDataSourceProviderImpl(vertx);

            globalMetricProvider = GlobalMetricProvider.create(vertx, ready -> {
                if (ready.succeeded()) {
                    new ServiceBinder(vertx)
                        .setAddress(METRIC_SERVICE_GLOBAL_READ_ADDRESS)
                        .register(GlobalMetricProvider.class, ready.result());

                    vertx.eventBus().consumer(METRIC_SERVICE_GLOBAL_REFRESH_ADDRESS, request -> refreshMetrics());

                    startFuture.complete();
                } else {
                    startFuture.fail(ready.cause());
                }
            });
        } catch (SQLException e) {
            startFuture.fail(e);
        }
    }

    @Override
    public void stop(Future<Void> future) {
        globalDataSourceProvider.tearDown();
        future.complete();
    }

    // Recalculates all metrics and stores them in the database
    private void refreshMetrics() {
        // refresh all metrics separately
        CompositeFuture.all(refreshDistributionMetrics(), refreshViolationMetrics(), refreshLicenceMetrics()).setHandler(handler -> {
            if (handler.succeeded()) {
                LOG.info("Global metrics refreshed successfully");
            } else {
                LOG.error("Failed to refresh global metrics: {}", handler.cause().getMessage());
            }
        });
    }

    private Future<Void> refreshDistributionMetrics() {
        Future<Void> completionFuture = Future.future();

        globalDataSourceProvider.existsDistributions().setHandler(handler -> {
            if (handler.succeeded()) {
                if (handler.result()) {
                    List<Future> databaseFutures = new ArrayList<>();

                    Future<Long> datasetCount = globalDataSourceProvider.getDatasetCount();
                    databaseFutures.add(datasetCount);

                    Future<Long> distributionCount = globalDataSourceProvider.getDistributionCount();
                    databaseFutures.add(distributionCount);

                    Future<Map<String, Long>> accessStatusCode = globalDataSourceProvider.getDistributionAccessStatusCodes();
                    databaseFutures.add(accessStatusCode);

                    Future<Map<String, Long>> downloadStatusCode = globalDataSourceProvider.getDistributionDownloadStatusCodes();
                    databaseFutures.add(downloadStatusCode);

                    Future<Long> accessibleDistributionsAccessUrl = globalDataSourceProvider.getDistributionAccessUrlAccessibilityCount();
                    databaseFutures.add(accessibleDistributionsAccessUrl);

                    Future<Long> unknownDistributionsAccessUrl = globalDataSourceProvider.getDistributionAccessUrlUnknownAccessibilityCount();
                    databaseFutures.add(unknownDistributionsAccessUrl);

                    Future<Long> accessibleDistributionsDownloadUrl = globalDataSourceProvider.getDistributionDownloadUrlAccessibilityCount();
                    databaseFutures.add(accessibleDistributionsDownloadUrl);

                    Future<Long> unknownDistributionsDownloadUrl = globalDataSourceProvider.getDistributionDownloadUrlUnknownAccessibilityCount();
                    databaseFutures.add(unknownDistributionsDownloadUrl);

                    Future<Long> distributionsWithDownloadUrl = globalDataSourceProvider.getDistributionsWithDownloadUrl();
                    databaseFutures.add(distributionsWithDownloadUrl);

                    Future<Long> datasetMachineReadability = globalDataSourceProvider.getMachineReadableDatasetCount();
                    databaseFutures.add(datasetMachineReadability);

                    Future<Map<String, Long>> distributionFormatsCount = globalDataSourceProvider.getDistributionFormats();
                    databaseFutures.add(distributionFormatsCount);


                    // wait for all database operations to complete before calculating metrics
                    CompositeFuture.all(databaseFutures).setHandler(sourceHandler -> {
                        if (sourceHandler.succeeded()) {
                            List<Future> metricFutures = new ArrayList<>();

                            Future<Void> renderDistributionsFuture = Future.future();
                            globalMetricProvider.setRenderDistributions(true, resultHandler ->
                                handleResult(renderDistributionsFuture, resultHandler));
                            metricFutures.add(renderDistributionsFuture);

                            Future<Void> accessibleDistributionsAccessUrlPercentagesFuture = Future.future();
                            Metric accessibleDistributionsAccessUrlPercentages =
                                new Metric(MetricService.getDistributionAccessibility(accessibleDistributionsAccessUrl.result(), unknownDistributionsAccessUrl.result(), distributionCount.result()));
                            globalMetricProvider.setDistributionAccessibilityAccessUrl(accessibleDistributionsAccessUrlPercentages, resultHandler ->
                                handleResult(accessibleDistributionsAccessUrlPercentagesFuture, resultHandler));
                            metricFutures.add(accessibleDistributionsAccessUrlPercentagesFuture);

                            if (distributionsWithDownloadUrl.result() > 0) {
                                Future<Void> accessibleDistributionsDownloadUrlPercentagesFuture = Future.future();
                                Metric accessibleDistributionsDownloadUrlPercentages =
                                    new Metric(MetricService.getDistributionAccessibility(accessibleDistributionsDownloadUrl.result(), unknownDistributionsDownloadUrl.result(), distributionsWithDownloadUrl.result()));
                                globalMetricProvider.setDistributionAccessibilityDownloadUrl(accessibleDistributionsDownloadUrlPercentages, resultHandler ->
                                    handleResult(accessibleDistributionsDownloadUrlPercentagesFuture, resultHandler));
                                metricFutures.add(accessibleDistributionsDownloadUrlPercentagesFuture);
                            } else {
                                globalMetricProvider.deleteMetric(METRIC_DISTRIBUTION_ACCESSIBILITY_DOWNLOAD_URL);
                            }

                            Future<Void> errorStatusCodesFuture = Future.future();
                            Metric errorStatusCodes =
                                new Metric(MetricService.getDistributionStatusCodes(accessStatusCode.result(), downloadStatusCode.result()));
                            globalMetricProvider.setDistributionStatusCodes(errorStatusCodes, resultHandler ->
                                handleResult(errorStatusCodesFuture, resultHandler));
                            metricFutures.add(errorStatusCodesFuture);

                            Future<Void> downloadUrlExistsFuture = Future.future();
                            Metric downloadUrlExists =
                                new Metric(MetricService.getDistributionDownloadUrlExists(distributionsWithDownloadUrl.result(), distributionCount.result()));
                            globalMetricProvider.setDistributionDownloadUrlExists(downloadUrlExists, resultHandler ->
                                handleResult(downloadUrlExistsFuture, resultHandler));
                            metricFutures.add(downloadUrlExistsFuture);

                            Future<Void> machineReadabilityFuture = Future.future();
                            Metric machineReadability =
                                new Metric(MetricService.getDatasetMachineReadability(datasetMachineReadability.result(), datasetCount.result()));
                            globalMetricProvider.setDatasetMachineReadability(machineReadability, resultHandler ->
                                handleResult(machineReadabilityFuture, resultHandler));
                            metricFutures.add(machineReadabilityFuture);

                            Future<Void> distributionFormatsFuture = Future.future();
                            Metric distributionFormats =
                                new Metric(MetricService.getDistributionFormats(distributionFormatsCount.result(), distributionCount.result()));
                            globalMetricProvider.setDistributionFormats(distributionFormats, resultHandler ->
                                handleResult(distributionFormatsFuture, resultHandler));
                            metricFutures.add(distributionFormatsFuture);


                            CompositeFuture.all(metricFutures).setHandler(metricHandler ->
                                handleResult(completionFuture, metricHandler));

                        } else {
                            LOG.error("Failed to retrieve global distribution data from database: {}", sourceHandler.cause().getMessage());
                            completionFuture.fail(sourceHandler.cause());
                        }
                    });

                } else {
                    globalMetricProvider.setRenderDistributions(false, metricHandler ->
                        handleResult(completionFuture, metricHandler));
                }
            } else {
                LOG.error("Failed to retrieve globalExistDistribution data: {}", handler.cause().getMessage());
                completionFuture.fail(handler.cause());
            }
        });

        return completionFuture;
    }

    private Future<Void> refreshViolationMetrics() {
        Future<Void> completionFuture = Future.future();

        globalDataSourceProvider.existsDcatCatalogue().setHandler(handler -> {
            if (handler.succeeded()) {
                if (handler.result()) {
                    List<Future> databaseFutures = new ArrayList<>();

                    Future<Long> datasetCount = globalDataSourceProvider.getDatasetCount();
                    databaseFutures.add(datasetCount);

                    Future<Map<String, Long>> violations = globalDataSourceProvider.getDatasetViolations();
                    databaseFutures.add(violations);

                    Future<Long> compliantDatasets = globalDataSourceProvider.getCompliantDatasetCount();
                    databaseFutures.add(compliantDatasets);


                    // wait for all database operations to complete before calculating metrics
                    CompositeFuture.all(databaseFutures).setHandler(sourceHandler -> {
                        if (sourceHandler.succeeded()) {
                            List<Future> metricFutures = new ArrayList<>();

                            Future<Void> renderViolationsFuture = Future.future();
                            globalMetricProvider.setRenderViolations(true, resultHandler ->
                                handleResult(renderViolationsFuture, resultHandler));
                            metricFutures.add(renderViolationsFuture);

                            Future<Void> datasetViolationsFuture = Future.future();
                            Metric datasetViolations =
                                new Metric(MetricService.getDatasetViolations(violations.result()));
                            globalMetricProvider.setDatasetViolations(datasetViolations, resultHandler ->
                                handleResult(datasetViolationsFuture, resultHandler));
                            metricFutures.add(datasetViolationsFuture);

                            Future<Void> datasetComplianceFuture = Future.future();
                            Metric datasetCompliance =
                                new Metric(MetricService.getDatasetCompliance(compliantDatasets.result(), datasetCount.result()));
                            globalMetricProvider.setDatasetCompliance(datasetCompliance, resultHandler ->
                                handleResult(datasetComplianceFuture, resultHandler));
                            metricFutures.add(datasetComplianceFuture);


                            CompositeFuture.all(metricFutures).setHandler(metricHandler ->
                                handleResult(completionFuture, metricHandler));

                        } else {
                            LOG.error("Failed to retrieve global violation data from database: {}", sourceHandler.cause().getMessage());
                            completionFuture.fail(sourceHandler.cause());
                        }
                    });

                } else {
                    globalMetricProvider.setRenderViolations(false, metricHandler ->
                        handleResult(completionFuture, metricHandler));
                }
            } else {
                LOG.error("Failed to retrieve globalExistViolations data: {}", handler.cause().getMessage());
                completionFuture.fail(handler.cause());
            }
        });

        return completionFuture;
    }

    private Future<Void> refreshLicenceMetrics() {
        Future<Void> completionFuture = Future.future();

        List<Future> databaseFutures = new ArrayList<>();

        Future<Long> datasetCount = globalDataSourceProvider.getDatasetCount();
        databaseFutures.add(datasetCount);

        Future<Map<String, Long>> datasetLicencesCount = globalDataSourceProvider.getDatasetLicences();
        databaseFutures.add(datasetLicencesCount);

        Future<Long> datasetKnownLicencesCount = globalDataSourceProvider.getDatasetKnownLicenceCount();
        databaseFutures.add(datasetKnownLicencesCount);


        // wait for all database operations to complete before calculating metrics
        CompositeFuture.all(databaseFutures).setHandler(sourceHandler -> {
            if (sourceHandler.succeeded()) {
                List<Future> metricFutures = new ArrayList<>();

                Future<Void> renderLicencesFuture = Future.future();
                globalMetricProvider.setRenderLicences(true, resultHandler ->
                    handleResult(renderLicencesFuture, resultHandler));
                metricFutures.add(renderLicencesFuture);

                Future<Void> datasetLicencesFuture = Future.future();
                Metric datasetLicences =
                    new Metric(MetricService.getDatasetLicences(datasetLicencesCount.result(), datasetCount.result()));
                globalMetricProvider.setDatasetLicences(datasetLicences, resultHandler ->
                    handleResult(datasetLicencesFuture, resultHandler));
                metricFutures.add(datasetLicencesFuture);

                Future<Void> datasetKnownLicencesFuture = Future.future();
                Metric datasetKnownLicences =
                    new Metric(MetricService.getDatasetKnownLicences(datasetKnownLicencesCount.result(), datasetCount.result()));
                globalMetricProvider.setDatasetKnownLicences(datasetKnownLicences, resultHandler ->
                    handleResult(datasetKnownLicencesFuture, resultHandler));
                metricFutures.add(datasetKnownLicencesFuture);


                CompositeFuture.all(metricFutures).setHandler(metricHandler ->
                    handleResult(completionFuture, metricHandler));

            } else {
                LOG.error("Failed to retrieve global licence data from database: {}", sourceHandler.cause().getMessage());
                completionFuture.fail(sourceHandler.cause());
            }
        });

        return completionFuture;
    }

    private void handleResult(Future<Void> future, AsyncResult result) {
        if (result.succeeded()) {
            future.complete();
        } else {
            future.fail(result.cause());
        }
    }
}
