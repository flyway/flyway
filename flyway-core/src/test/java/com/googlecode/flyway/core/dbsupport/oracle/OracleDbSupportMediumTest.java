/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.oracle;

import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import oracle.jdbc.OracleDriver;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test for the Oracle-specific DB support.
 */
@SuppressWarnings({"JavaDoc"})
public class OracleDbSupportMediumTest {
    /**
     * Checks the result of the getCurrentSchema call.
     *
     * @param useProxy Flag indicating whether to check it using a proxy user or not.
     */
    private void checkCurrentSchema(boolean useProxy) throws Exception {
        File customPropertiesFile = new File(System.getProperty("user.home") + "/flyway-mediumtests.properties");
        Properties customProperties = new Properties();
        if (customPropertiesFile.canRead()) {
            customProperties.load(new FileInputStream(customPropertiesFile));
        }
        String user = customProperties.getProperty("oracle.user", "flyway");
        String password = customProperties.getProperty("orcale.password", "flyway");
        String url = customProperties.getProperty("oracle.url", "jdbc:oracle:thin:@localhost:1521:XE");

        String dataSourceUser = useProxy ? "flyway_proxy[" + user + "]" : user;

        DataSource dataSource = new DriverDataSource(new OracleDriver(), url, dataSourceUser, password);

        Connection connection = dataSource.getConnection();
        String currentSchema = new OracleDbSupport(connection).getCurrentSchema();
        connection.close();

        assertEquals(user.toUpperCase(), currentSchema);
    }

    /**
     * Tests that the current schema for a connection is correct;
     */
    @Test
    public void currentSchema() throws Exception {
        checkCurrentSchema(false);
    }

    /**
     * Tests that the current schema for a proxy connection with conn_user[schema_user] is schema_user and not conn_user;
     */
    @Test
    public void currentSchemaWithProxy() throws Exception {
        checkCurrentSchema(true);
    }
}
