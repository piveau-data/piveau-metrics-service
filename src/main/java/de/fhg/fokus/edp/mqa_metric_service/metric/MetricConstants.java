package de.fhg.fokus.edp.mqa_metric_service.metric;

public final class MetricConstants {

    public static final String COLLECTION_GLOBAL = "_global" ;
    public static final String COLLECTION_INFO = "_info" ;


    // Catalogue info

    public static final String INFO_CATALOGUES = "infoCatalogues";
    public static final String INFO_CATALOGUE_DISTRIBUTIONS_NON_ACCESSIBILE_SIZE = "infoCatalogueDistributionsNonAccessibleSize";
    public static final String INFO_CATALOGUE_DATASETS_NON_COMPLIANT_SIZE = "infoCatalogueDatasetsNonCompliantSize";

    // Render sections

    public static final String METRIC_RENDER_DISTRIBUTIONS = "renderDistributions";
    public static final String METRIC_RENDER_VIOLATIONS = "renderViolations";
    public static final String METRIC_RENDER_LICENCES = "renderLicences";


    // Distribution metrics

    public static final String METRIC_DISTRIBUTION_ACCESSIBILITY_ACCESS_URL = "distributionAccessibilityAccessUrl";
    public static final String METRIC_DISTRIBUTION_ACCESSIBILITY_DOWNLOAD_URL = "distributionAccessibilityDownloadUrl";

    public static final String METRIC_MACHINE_READABILITY = "machineReadability";
    public static final String METRIC_STATUS_CODES = "statusErrorCodes";
    public static final String METRIC_DOWNLOAD_URL_EXIST = "downloadUrlExists";
    public static final String METRIC_DISTRIBUTION_FORMATS = "distributionFormats";

    public static final String METRIC_DATASET_NOT_ACCESSIBLE_COUNT = "datasetNotAccessibleCount";


    // Compliance metrics

    public static final String METRIC_DATASET_VIOLATIONS = "datasetViolations";
    public static final String METRIC_DATASET_COMPLIANCE = "datasetCompliance";
    public static final String METRIC_DATASET_NON_CONFORMANT_COUNT = "datasetNonConformantCount";


    // Licence metrics

    public static final String METRIC_DATASET_LICENCES = "datasetLicences";
    public static final String METRIC_DATASET_KNOWN_LICENCES = "datasetKnownLicences";
}
