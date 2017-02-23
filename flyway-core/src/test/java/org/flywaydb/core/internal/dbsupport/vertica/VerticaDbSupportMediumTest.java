/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.vertica;

import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.flywaydb.core.DbCategory;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test for VerticaDbSupport.
 */
@Category(DbCategory.Vertica.class)
public class VerticaDbSupportMediumTest {
    /**
     * Checks that the search_path is extended and not overwritten so that objects in PUBLIC can still be found.
     */
    @Test
    public void setCurrentSchema() throws Exception {
        Connection connection = createDataSource().getConnection();
        VerticaDbSupport dbSupport = new VerticaDbSupport(connection);
        Schema schema = dbSupport.getSchema("search_path_test");
        schema.create();
        dbSupport.changeCurrentSchemaTo(dbSupport.getSchema("search_path_test"));
        String searchPath = dbSupport.doGetCurrentSchemaName();
        assertEquals("search_path_test, \"$user\", public, v_catalog, v_monitor, v_internal", searchPath);
        schema.drop();
        JdbcUtils.closeConnection(connection);
    }


    /**
     * Creates a datasource for use in tests.
     *
     * @return The new datasource.
     */
    private DataSource createDataSource() throws Exception {
        File customPropertiesFile = new File(System.getProperty("user.home") + "/flyway-mediumtests.properties");
        Properties customProperties = new Properties();
        if (customPropertiesFile.canRead()) {
            customProperties.load(new FileInputStream(customPropertiesFile));
        }
        String user = customProperties.getProperty("vertica.user", "dbadmin");
        String password = customProperties.getProperty("vertica.password", "flyway");
        String url = customProperties.getProperty("vertica.url", "jdbc:vertica://localhost/flyway");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null);
    }


}
