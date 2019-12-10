package de.fhg.fokus.edp.mqa_metric_service;

public final class ApplicationConfig {

    static final String METRIC_SERVICE_GLOBAL_READ_ADDRESS = "service.metric.global.read";
    static final String METRIC_SERVICE_GLOBAL_REFRESH_ADDRESS = "service.metric.global.refresh";
    static final String METRIC_SERVICE_CATALOGUE_READ_ADDRESS = "service.metric.catalogue.read";
    static final String METRIC_SERVICE_CATALOGUE_REFRESH_ADDRESS = "service.metric.catalogue.refresh";
    static final String METRIC_SERVICE_CATALOGUE_INFO_ADDRESS = "service.info.catalogues";
    static final String METRIC_SERVICE_CATALOGUE_NON_ACCESSIBLE_ADDRESS = "service.info.catalogues.non-accessible";
    static final String METRIC_SERVICE_CATALOGUE_NON_COMPLIANT_ADDRESS = "service.info.catalogues.non-compliant";

    static final String ENV_APPLICATION_PORT = "PORT";
    static final Integer DEFAULT_APPLICATION_PORT = 8083;

    static final String ENV_API_KEY = "API_KEY";

    public static final String ENV_MONGODB_SERVER_HOST = "MONGODB_SERVER_HOST";
    public static final String DEFAULT_MONGODB_SERVER_HOST = "localhost";

    public static final String ENV_MONGODB_SERVER_PORT = "MONGODB_SERVER_PORT";
    public static final Integer DEFAULT_MONGODB_SERVER_PORT = 27017;

    public static final String ENV_MONGODB_USERNAME = "MONGODB_USERNAME";
    public static final String DEFAULT_MONGODB_USERNAME = null;

    public static final String ENV_MONGODB_PASSWORD = "MONGODB_PASSWORD";
    public static final String DEFAULT_MONGODB_PASSWORD = "";

    public static final String ENV_MONGODB_DB_NAME = "MONGODB_DB_NAME";
    public static final String DEFAULT_MONGODB_DB_NAME = "mqa-metrics";

    public static final String ENV_PGSQL_SERVER_HOST = "PGSQL_SERVER_HOST";
    public static final String DEFAULT_PGSQL_SERVER_HOST = "jdbc:postgresql://localhost:5432/mqa_hub";

    public static final String ENV_PGSQL_USERNAME = "PGSQL_USERNAME";
    public static final String DEFAULT_PGSQL_USERNAME = "postgres";

    public static final String ENV_PGSQL_PASSWORD = "PGSQL_PASSWORD";
    public static final String DEFAULT_PGSQL_PASSWORD = "postgres";

    public static final String ENV_PGSQL_DEFAULT_LIMIT = "PGSQL_DEFAULT_LIMIT";
    public static final Integer DEFAULT_PGSQL_DEFAULT_LIMIT = 20;
}
