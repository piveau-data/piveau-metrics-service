package de.fhg.fokus.edp.mqa_metric_service.metric;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MetricService {

    public static Map<String, Double> getDistributionAccessibility(long availableDistributionCount, long unknownDistributionCount, long totalDistributionCount) {
        double availablePercentage = availableDistributionCount * 100f / totalDistributionCount;
        double unknownPercentage = unknownDistributionCount * 100f / totalDistributionCount;

        Map<String, Double> result = new HashMap<>();
        result.put("yes", availablePercentage);
        result.put("unknown", unknownPercentage);
        result.put("no", 100 - availablePercentage - unknownPercentage);

        return result;
    }

    public static Map<String, Double> getDistributionStatusCodes(Map<String, Long> accessStatusCodes, Map<String, Long> downloadStatusCodes) {
        Map<String, Double> errorStatusCodePercentages = new HashMap<>();

        Map<String, Long> distributionErrorCodesCount = Stream.of(accessStatusCodes, downloadStatusCodes)
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                Long::sum
            ));

        // sanitize
        distributionErrorCodesCount.remove(null);

        long totalErrorCodeCount = distributionErrorCodesCount
            .values()
            .stream()
            .mapToLong(Long::longValue)
            .sum();

        distributionErrorCodesCount.forEach((code, count) -> {
            if (code != null)
                errorStatusCodePercentages.put(code, count * 100d / totalErrorCodeCount);
        });

        return errorStatusCodePercentages;
    }

    public static Map<String, Double> getDistributionDownloadUrlExists(long distributionWithDownloadUrlCount, long totalDistributionCount) {
        return getPercentagesForTwo(distributionWithDownloadUrlCount, totalDistributionCount);
    }

    public static Map<String, Double> getDatasetMachineReadability(long machineReadableDistributionCount, long totalDistributionCount) {
        return getPercentagesForTwo(machineReadableDistributionCount, totalDistributionCount);
    }

    public static Map<String, Double> getDistributionFormats(Map<String, Long> distributionFormatsCount, long totalDistributionCount) {
        Map<String, Double> distributionFormatPercentages = new HashMap<>();

        distributionFormatsCount.forEach((format, count) ->
            distributionFormatPercentages.put(format, count * 100d / totalDistributionCount));

        return distributionFormatPercentages;
    }

    public static Map<String, Double> getDatasetViolations(Map<String, Long> datasetViolationsCount) {
        Map<String, Double> datasetViolationPercentages = new HashMap<>();

        long totalDatasetCount = datasetViolationsCount.values().stream().mapToLong(Long::longValue).sum();

        datasetViolationsCount.forEach((violation, count) ->
            datasetViolationPercentages.put(violation, count * 100d / totalDatasetCount));

        return datasetViolationPercentages;
    }

    public static Map<String, Double> getDatasetCompliance(long compliantDatasetCount, long totalDatasetCount) {
        return getPercentagesForTwo(compliantDatasetCount, totalDatasetCount);
    }

    public static Map<String, Double> getDatasetLicences(Map<String, Long> datasetLicenceCount, long totalDatasetCount) {
        Map<String, Double> datasetLicencePercentages = new HashMap<>();

        datasetLicenceCount.forEach((format, count) ->
            datasetLicencePercentages.put(format, count * 100d / totalDatasetCount));

        return datasetLicencePercentages;
    }

    public static Map<String, Double> getDatasetKnownLicences(long datasetKnownLicenceCount, long totalDatasetCount) {
        return getPercentagesForTwo(datasetKnownLicenceCount, totalDatasetCount);
    }


    private static Map<String, Double> getPercentagesForTwo(long current, long total) {
        double percentage = current * 100f / total;

        Map<String, Double> result = new HashMap<>();
        result.put("yes", percentage);
        result.put("no", 100 - percentage);

        return result;
    }
}
