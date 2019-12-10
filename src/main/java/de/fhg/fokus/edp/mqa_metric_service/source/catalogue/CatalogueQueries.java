package de.fhg.fokus.edp.mqa_metric_service.source.catalogue;

final class CatalogueQueries {

    static final String EXIST_DISTRIBUTIONS = "SELECT EXISTS (SELECT 1 " +
        "FROM dataset d JOIN distribution dist ON d.id = dist.distribution_id " +
        "WHERE d.dataset_id = ?)";

    static final String CATALOGUE_INFO = "SELECT * FROM catalog";

    static final String COUNT_DATASETS_WITH_ACCESSIBILITY_ISSUES = "SELECT COUNT(*) FROM (SELECT DISTINCT dset " +
        "FROM dataset dset JOIN distribution d ON dset.id = d.distribution_id " +
        "WHERE dset.dataset_id = ? AND (d.status_access_url < 200 OR d.status_access_url >= 400 OR d.status_download_url < 200 OR d.status_download_url >= 400)) " +
        "AS temp";
    static final String GET_DATASETS_WITH_ACCESSIBILITY_ISSUES = "SELECT DISTINCT dset.id, dset.title " +
        "FROM dataset dset JOIN distribution d ON dset.id = d.distribution_id " +
        "WHERE dset.dataset_id = ? AND (d.status_access_url < 200 OR d.status_access_url >= 400 OR d.status_download_url < 200 OR d.status_download_url >= 400) " +
        "LIMIT ? OFFSET ?";
    static final String NON_ACCESSIBLE_DISTRIBUTIONS_BY_ID = "SELECT * " +
        "FROM distribution d " +
        "WHERE d.distribution_id = ? AND (d.status_access_url < 200 OR d.status_access_url >= 400 OR d.status_download_url < 200 OR d.status_download_url >= 400)";

    static final String COUNT_DATASETS_WITH_COMPLIANCE_ISSUES = "SELECT COUNT(d) " +
        "FROM dataset d " +
        "WHERE d.dataset_id = ? AND EXISTS (" +
        "SELECT 1 FROM violation v WHERE v.violation_id =  d.id)";
    static final String GET_DATASETS_WITH_COMPLIANCE_ISSUES = "SELECT DISTINCT d.id, d.title " +
        "FROM dataset d " +
        "WHERE d.dataset_id = ? AND EXISTS (" +
        "SELECT 1 FROM violation v WHERE v.violation_id =  d.id) " +
        "LIMIT ? OFFSET ?";
    static final String VIOLATIONS_BY_DATASET_ID = "SELECT * " +
        "FROM dataset d JOIN violation v ON d.id = v.violation_id " +
        "WHERE d.id = ?";


    static final String DATASET_COUNT = "SELECT COUNT(d) " +
        "FROM dataset d " +
        "WHERE d.dataset_id = ?";
    static final String DATASET_ACCESSIBILITY = "SELECT COUNT(*) FROM (SELECT DISTINCT dset " +
        "FROM dataset dset JOIN distribution d ON dset.id = d.distribution_id " +
        "WHERE dset.dataset_id = ? AND (d.status_access_url >= 200 AND d.status_access_url < 400) AND (d.download_url IS NULL OR (d.status_download_url >= 200 AND d.status_download_url < 400))) " +
        "AS temp"; // faster than COUNT(DISTINCT *)
    static final String DATASET_MACHINE_READABLE_COUNT = "SELECT COUNT(d) " +
        "FROM dataset d " +
        "WHERE d.machine_readable = true AND d.dataset_id = ?";
    static final String DATASET_COMPLIANCE_COUNT = "SELECT COUNT(d) " +
        "FROM dataset d " +
        "WHERE d.dataset_id = ? AND NOT EXISTS (" +
        "SELECT 1 FROM violation v WHERE v.violation_id = d.id)";
    static final String DATASET_VIOLATION_COUNT = "SELECT DISTINCT(v.violation_name), COUNT(v.violation_name) " +
        "FROM dataset d JOIN violation v ON d.id = v.violation_id " +
        "WHERE d.dataset_id = ? " +
        "GROUP BY v.violation_name";
    static final String DATASET_LICENCE_COUNT = "SELECT d.licence_id, COUNT(d) " +
        "FROM dataset d " +
        "WHERE d.dataset_id = ? " +
        "GROUP BY d.licence_id";
    static final String DATASET_KNOWN_LICENCES_COUNT = "SELECT COUNT(d) " +
        "FROM dataset d " +
        "WHERE d.dataset_id = ? AND EXISTS(" +
        "SELECT 1 FROM licence l WHERE d.licence_id = l.licence_id)";

    static final String DISTRIBUTIONS_COUNT = "SELECT COUNT(dist) " +
        "FROM dataset d JOIN distribution dist ON d.id = dist.distribution_id " +
        "WHERE d.dataset_id = ?";
    static final String DISTRIBUTION_ACCESSIBILITY_ACCESS_URL = "SELECT COUNT(d) " +
        "FROM dataset dset JOIN distribution d ON dset.id = d.distribution_id " +
        "WHERE dset.dataset_id = ? AND (d.status_access_url >= 200 AND d.status_access_url < 400)";
    static final String DISTRIBUTION_UNKNOWN_ACCESSIBILITY_ACCESS_URL = "SELECT COUNT(d) " +
        "FROM dataset dset JOIN distribution d ON dset.id = d.distribution_id " +
        "WHERE dset.dataset_id = ? AND d.status_access_url = 0";
    static final String DISTRIBUTION_ACCESSIBILITY_DOWNLOAD_URL = "SELECT COUNT(d) " +
        "FROM dataset dset JOIN distribution d ON dset.id = d.distribution_id " +
        "WHERE dset.dataset_id = ? AND d.download_url IS NOT NULL AND (d.status_download_url >= 200 AND d.status_download_url < 400)";
    static final String DISTRIBUTION_UNKNOWN_ACCESSIBILITY_DOWNLOAD_URL = "SELECT COUNT(d) " +
        "FROM dataset dset JOIN distribution d ON dset.id = d.distribution_id " +
        "WHERE dset.dataset_id = ? AND (d.download_url IS NOT NULL AND d.status_download_url = 0)";
    static final String DISTRIBUTIONS_ACCESS_STATUS_CODES = "SELECT dist.status_access_url::text, COUNT(dist) " +
        "FROM dataset d JOIN distribution dist ON d.id = dist.distribution_id " +
        "WHERE d.dataset_id = ? AND dist.status_access_url >= 400 AND dist.status_access_url < 1000 " +
        "GROUP BY dist.status_access_url";
    static final String DISTRIBUTIONS_DOWNLOAD_STATUS_CODES = "SELECT dist.status_download_url::text, COUNT(dist) " +
        "FROM dataset d JOIN distribution dist ON d.id = dist.distribution_id " +
        "WHERE d.dataset_id = ? AND dist.status_download_url >= 400 AND dist.status_download_url < 1000 " +
        "GROUP BY dist.status_download_url";
    static final String DISTRIBUTIONS_DOWNLOAD_URL_EXISTS = "SELECT COUNT(dist) " +
        "FROM dataset d JOIN distribution dist ON d.id = dist.distribution_id " +
        "WHERE d.dataset_id = ? AND dist.download_url IS NOT NULL AND dist.status_download_url != 1000"; // 1000 is the mqa code for BAD URL ERROR
    static final String DISTRIBUTIONS_FORMATS = "SELECT DISTINCT(dist.format), COUNT(dist) " +
        "FROM dataset d JOIN distribution dist ON d.id = dist.distribution_id " +
        "WHERE d.dataset_id = ? " +
        "GROUP BY dist.format";
}
