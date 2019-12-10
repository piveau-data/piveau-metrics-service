package de.fhg.fokus.edp.mqa_metric_service.source.global;

final class GlobalQueries {

    static final String EXIST_DISTRIBUTIONS = "SELECT EXISTS (SELECT 1 FROM distribution)";
    static final String EXIST_DCAT_CATALOGUE = "SELECT EXISTS (SELECT 1 FROM catalog c WHERE c.dcat = 't')";

    static final String DATASET_COUNT = "SELECT COUNT(d) " +
        "FROM dataset d";

    static final String DATASET_COMPLIANCE_COUNT = "SELECT COUNT(d) " +
        "FROM dataset d WHERE NOT EXISTS(SELECT 1 FROM violation v " +
        "WHERE v.violation_id = d.id)";

    static final String DATASET_VIOLATION_COUNT = "SELECT DISTINCT(v.violation_name), COUNT(v.violation_name) " +
        "FROM violation v " +
        "GROUP BY v.violation_name";

    static final String DATASET_LICENCES = "SELECT d.licence_id, COUNT(d) " +
        "FROM dataset d " +
        "GROUP BY d.licence_id";

    static final String DATASETS_MACHINE_READABLE_COUNT = "SELECT COUNT(d) " +
        "FROM dataset d " +
        "WHERE d.machine_readable = true";

    static final String DATASET_KNOWN_LICENCES_COUNT = "SELECT COUNT(d) " +
        "FROM dataset d " +
        "WHERE EXISTS(" +
        "SELECT 1 FROM licence l WHERE d.licence_id = l.licence_id)";


    static final String DISTRIBUTIONS_COUNT = "SELECT COUNT(d) " +
        "FROM distribution d";

    static final String DISTRIBUTIONS_ACCESS_URL_ACCESSIBILITY = "SELECT COUNT(d) " +
        "FROM distribution d " +
        "WHERE (d.status_access_url >= 200 AND d.status_access_url < 400)";

    static final String DISTRIBUTIONS_ACCESS_URL_UNKNOWN_ACCESSIBILITY = "SELECT COUNT(d) " +
        "FROM distribution d " +
        "WHERE d.status_access_url = 0";

    static final String DISTRIBUTIONS_DOWNLOAD_URL_ACCESSIBILITY = "SELECT COUNT(d) " +
        "FROM distribution d " +
        "WHERE d.download_url IS NOT NULL AND (d.status_download_url >= 200 AND d.status_download_url < 400)";

    static final String DISTRIBUTIONS_DOWNLOAD_URL_UNKNOWN_ACCESSIBILITY = "SELECT COUNT(d) " +
        "FROM distribution d " +
        "WHERE d.download_url IS NOT NULL AND d.status_download_url = 0";

    static final String DISTRIBUTIONS_ACCESS_STATUS_CODES = "SELECT d.status_access_url::text, COUNT(d) " +
        "FROM distribution d " +
        "WHERE d.status_access_url >= 400 AND d.status_access_url < 1000 " +
        "GROUP BY d.status_access_url";

    static final String DISTRIBUTIONS_DOWNLOAD_STATUS_CODES = "SELECT d.status_download_url::text, COUNT(d) " +
        "FROM distribution d " +
        "WHERE d.status_download_url >= 400 AND d.status_download_url < 1000 " +
        "GROUP BY d.status_download_url";

    static final String DISTRIBUTIONS_DOWNLOAD_URL_EXISTS = "SELECT COUNT(d) " +
        "FROM distribution d " +
        "WHERE d.download_url IS NOT NULL AND d.status_download_url != 1000"; // 1000 is the mqa code for BAD URL ERROR

    static final String DISTRIBUTIONS_FORMATS = "SELECT DISTINCT(d.format), COUNT(d) " +
        "FROM distribution d " +
        "GROUP BY d.format";
}
