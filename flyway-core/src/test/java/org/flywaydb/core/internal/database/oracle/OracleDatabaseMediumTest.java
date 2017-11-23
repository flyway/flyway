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
package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.flywaydb.core.internal.database.oracle.OracleMigrationMediumTest.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test for the Oracle-specific DB support.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.Oracle.class)
@RunWith(Parameterized.class)
public class OracleDatabaseMediumTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {JDBC_URL_ORACLE_12},
                {JDBC_URL_ORACLE_11},
                {JDBC_URL_ORACLE_10}
        });
    }

    private final String jdbcUrl;

    public OracleDatabaseMediumTest(String jdbcUrl) throws Exception {
        this.jdbcUrl = jdbcUrl;
    }

    @Rule
    public Timeout globalTimeout = new Timeout(180, TimeUnit.SECONDS);

    /**
     * Checks the result of the getCurrentUserName and getCurrentSchemaName calls.
     *
     * @param useProxy     Flag indicating whether to check it using a proxy user or not.
     * @param changeSchema Flag indicating whether to change the current schema or not.
     */
    private void checkCurrentUser(boolean useProxy, boolean changeSchema) throws Exception {
        String user = "flyway";
        String auxUser = "flyway_aux";
        String password = "flyway";

        //further we treat user names as uppercase strings, so make sure they are not quoted
        assertFalse("Provided user name (" + user + ") is expected to be unquoted/case-insensitive", user.contains("\""));
        assertFalse("Provided aux user name (" + auxUser + ") is expected to be unquoted/case-insensitive", auxUser.contains("\""));
        user = user.toUpperCase();
        auxUser = auxUser.toUpperCase();

        String dataSourceUser = useProxy ? "\"flyway_proxy\"[" + user + "]" : user;

        DataSource dataSource = new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, jdbcUrl, dataSourceUser, password, null);

        Connection connection = dataSource.getConnection();
        OracleDatabase database = new OracleDatabase(new Flyway(), connection);

        if (changeSchema) {
            database.getMainConnection().doChangeCurrentSchemaTo(auxUser);
        }

        String currentUser = database.doGetCurrentUser();
        String currentSchema = database.getMainConnection().getCurrentSchemaName();
        connection.close();

        assertEquals(user, currentUser);
        if (changeSchema) {
            assertEquals(auxUser, currentSchema);
        } else {
            assertEquals(user, currentSchema);
        }
    }

    /**
     * Tests that the current user for a connection is correct;
     */
    @Test
    public void currentUser() throws Exception {
        checkCurrentUser(false, false);
    }

    /**
     * Tests that the current user for a proxy connection with conn_user[schema_user] is schema_user and not conn_user;
     */
    @Test
    public void currentUserWithProxy() throws Exception {
        checkCurrentUser(true, false);
    }

    /**
     * Tests that the current schema for a connection after changing the current schema is aux_schema not schema_user;
     */
    @Test
    public void currentSchema() throws Exception {
        checkCurrentUser(false, true);
    }

    /**
     * Tests that the current schema for a proxy connection with conn_user[schema_user] after changing the current
     * schema is aux_schema not schema_user;
     */
    @Test
    public void currentSchemaWithProxy() throws Exception {
        checkCurrentUser(true, true);
    }

    /**
     * Tests for leaking database cursors.
     */
    @Test
    public void tableExistsCursorLeak() throws Exception {
        DataSource dataSource = createDataSource();

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            OracleDatabase database = new OracleDatabase(new Flyway(), connection);
            for (int i = 0; i < 200; i++) {
                database.getMainConnection().getSchema(database.getMainConnection().getCurrentSchemaName()).getTable("schema_version").exists();
            }
        } finally {
            JdbcUtils.closeConnection(connection);
        }
    }

    /**
     * Tests for leaking database cursors.
     */
    @Test
    public void isSchemaEmptyCursorLeak() throws Exception {
        DataSource dataSource = createDataSource();

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            OracleDatabase database = new OracleDatabase(new Flyway(), connection);
            for (int i = 0; i < 200; i++) {
                database.getMainConnection().getSchema(database.getMainConnection().getCurrentSchemaName()).empty();
            }
        } finally {
            JdbcUtils.closeConnection(connection);
        }
    }

    private DataSource createDataSource() throws Exception {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, JDBC_USER, JDBC_PASSWORD, null);
    }
}
