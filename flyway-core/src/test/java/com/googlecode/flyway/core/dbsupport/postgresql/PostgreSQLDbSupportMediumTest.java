package com.googlecode.flyway.core.dbsupport.postgresql;

import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import com.googlecode.flyway.core.util.jdbc.JdbcUtils;
import org.junit.Test;
import org.postgresql.Driver;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test for PostgreSQLDbSupport.
 */
public class PostgreSQLDbSupportMediumTest {
    /**
     * Checks that the search_path is extended and not overwritten so that objects in PUBLIC can still be found.
     */
    @Test
    public void setCurrentSchema() throws Exception {
        Connection connection = createDataSource().getConnection();
        PostgreSQLDbSupport dbSupport = new PostgreSQLDbSupport(connection);
        dbSupport.setCurrentSchema("flyway_3");
        String searchPath = dbSupport.getJdbcTemplate().queryForString("SHOW search_path");
        assertEquals("flyway_3, \"$user\", public", searchPath);
        JdbcUtils.closeConnection(connection);
    }


    /**
     * Creates a datasource for use in tests.
     * @return The new datasource.
     */
    private DataSource createDataSource() throws Exception {
        File customPropertiesFile = new File(System.getProperty("user.home") + "/flyway-mediumtests.properties");
        Properties customProperties = new Properties();
        if (customPropertiesFile.canRead()) {
            customProperties.load(new FileInputStream(customPropertiesFile));
        }
        String user = customProperties.getProperty("postgresql.user", "flyway");
        String password = customProperties.getProperty("postgresql.password", "flyway");
        String url = customProperties.getProperty("postgresql.url", "jdbc:postgresql://localhost/flyway_db");

        return new DriverDataSource(new Driver(), url, user, password);
    }


}
