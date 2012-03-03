package com.googlecode.flyway.sample.appengine;

import com.google.appengine.api.rdbms.AppEngineDriver;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;

import javax.sql.DataSource;

/**
 * Factory for the datasource of this application.
 */
public class DataSourceFactory {
    /**
     * Creates a new datasource.
     *
     * @return The Google Cloud SQL datasource.
     */
    public static DataSource createDataSource() {
        return new DriverDataSource(
                new AppEngineDriver(),
                "jdbc:google:rdbms://flyway-test-project:flyway-sample/flyway_cloudsql_db",
                null,
                null);
    }
}
