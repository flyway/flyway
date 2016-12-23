/**
 * Copyright 2010-2016 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.dbsupport.informix;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test for the Informix-specific DB support.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.Informix.class)
public class InformixDbSupportMediumTest {
    /**
     * Checks the result of the getCurrentSchemaName call.
     *
     * @param useProxy Flag indicating whether to check it using a proxy user or not.
     */
    private void checkCurrentSchema(boolean useProxy) throws Exception {
        Properties customProperties = getConnectionProperties();
        String user = customProperties.getProperty("informix.user");
        String password = customProperties.getProperty("informix.password");
        String url = customProperties.getProperty("informix.url");

        String dataSourceUser = user; //useProxy ? "\"flyway_proxy\"[" + user + "]" : user;

        DataSource dataSource = new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, dataSourceUser, password);

        Connection connection = dataSource.getConnection();
        InformixDbSupport dbSupport = new InformixDbSupport(connection);
        String currentSchema = dbSupport.getCurrentSchemaName();
        connection.close();

        assertEquals(user.toUpperCase(), currentSchema.toUpperCase());
    }

    private Properties getConnectionProperties() throws IOException {
        File customPropertiesFile = new File(System.getProperty("user.home") + "/flyway-mediumtests.properties");
        Properties connectionProperties = new Properties();
        if (customPropertiesFile.canRead()) {
            connectionProperties.load(new FileInputStream(customPropertiesFile));
        }
        if (!connectionProperties.containsKey("informix.user")) {
            connectionProperties.setProperty("informix.user", "flyway");
        }
        if (!connectionProperties.containsKey("informix.password")) {
            connectionProperties.setProperty("informix.password", "flyway");
        }
        if (!connectionProperties.containsKey("informix.url")) {
            connectionProperties.setProperty("informix.url", "jdbc:informix-sqli://localhost:7102/stores_demo:informixserver=devb_test");
        }
        return connectionProperties;
    }

    /**
     * Tests that the current schema for a connection is correct;
     * @throws java.lang.Exception
     */
    @Test
    public void currentSchema() throws Exception {
        checkCurrentSchema(false);
    }

    /**
     * Tests that the current schema for a proxy connection with conn_user[schema_user] is schema_user and not conn_user;
     * @throws java.lang.Exception
     */
    @Test
    public void currentSchemaWithProxy() throws Exception {
        checkCurrentSchema(true);
    }

    /**
     * Tests for leaking database cursors.
     * @throws java.lang.Exception
     */
    @Test
    public void tableExistsCursorLeak() throws Exception {
        DataSource dataSource = createDataSource();

        Connection connection = dataSource.getConnection();
        InformixDbSupport dbSupport = new InformixDbSupport(connection);
        for (int i = 0; i < 200; i++) {
            dbSupport.getSchema(dbSupport.getCurrentSchemaName()).getTable("schema_version").exists();
        }
        connection.close();
    }

    /**
     * Tests for leaking database cursors.
     * @throws java.lang.Exception
     */
    @Test
    public void isSchemaEmptyCursorLeak() throws Exception {
        DataSource dataSource = createDataSource();

        Connection connection = dataSource.getConnection();
        InformixDbSupport dbSupport = new InformixDbSupport(connection);
        for (int i = 0; i < 200; i++) {
            dbSupport.getSchema(dbSupport.getCurrentSchemaName()).empty();
        }
        connection.close();
    }

    /**
     * Creates a datasource for use in tests.
     *
     * @return The new datasource.
     */
    private DataSource createDataSource() throws Exception {
        Properties customProperties = getConnectionProperties();
        String user = customProperties.getProperty("informix.user");
        String password = customProperties.getProperty("informix.password");
        String url = customProperties.getProperty("informix.url");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password);
    }
}
