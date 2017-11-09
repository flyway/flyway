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
package org.flywaydb.core.internal.dbsupport.redshift;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test to demonstrate the migration functionality using PostgreSQL.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.Redshift.class)
public class RedshiftMigrationMediumTest extends MigrationTestCase {
    static final String JDBC_URL = "jdbc:redshift://52.59.121.141:5439/flywaydb";
    static final String JDBC_USER = "flyway";
    static final String JDBC_PASSWORD = "flywayPWD000";

    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    @Test
    public void vacuum() throws Exception {
        flyway.setLocations("migration/dbsupport/redshift/sql/vacuum");
        try {
            flyway.migrate();
        } catch (FlywayException e) {
            assertThat(e.getMessage(), containsString("non-transactional"));
        }
        flyway.setMixed(true);
        flyway.migrate();
    }

    @Test
    public void cleanUnknown() throws Exception {
        flyway.setSchemas("non-existant");
        flyway.clean();
    }

    @Test
    public void storedProcedure() throws Exception {
        flyway.setLocations("migration/dbsupport/redshift/sql/procedure");
        flyway.migrate();

        assertEquals("ABC123", jdbcTemplate.queryForString("SELECT inc(1)"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    @Test
    public void function() throws Exception {
        flyway.setLocations("migration/dbsupport/redshift/sql/function");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for Redshift Views.
     */
    @Test
    public void view() throws Exception {
        flyway.setLocations("migration/dbsupport/redshift/sql/view");
        flyway.migrate();

        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM \"\"\"v\"\"\""));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests parsing support for $$ string literals.
     */
    @Test
    public void dollarQuote() throws Exception {
        flyway.setLocations("migration/dbsupport/redshift/sql/dollar");
        flyway.migrate();
        assertEquals(8, jdbcTemplate.queryForInt("select count(*) from dollar"));
    }

    /**
     * Tests parsing support for multiline string literals.
     */
    @Test
    public void multiLine() throws Exception {
        flyway.setLocations("migration/dbsupport/redshift/sql/multiline");
        flyway.migrate();
        assertEquals(1, jdbcTemplate.queryForInt("select count(*) from address"));
    }

    /**
     * Tests that the lock on SCHEMA_VERSION is not blocking SQL commands in migrations. This test won't fail if there's
     * a too restrictive lock - it would just hang endlessly.
     */
    @Test
    public void lock() {
        flyway.setLocations("migration/dbsupport/redshift/sql/lock");
        flyway.migrate();
    }

    @Test
    public void emptySearchPath() {
        Flyway flyway1 = new Flyway();
        DriverDataSource driverDataSource = (DriverDataSource) dataSource;
        flyway1.setDataSource(new DriverDataSource(Thread.currentThread().getContextClassLoader(),
                null, driverDataSource.getUrl(), driverDataSource.getUser(), driverDataSource.getPassword(), null) {
            @Override
            public Connection getConnection() throws SQLException {
                Connection connection = super.getConnection();
                Statement statement = null;
                try {
                    statement = connection.createStatement();
                    statement.execute("SELECT set_config('search_path', '', false)");
                } finally {
                    JdbcUtils.closeStatement(statement);
                }
                return connection;
            }
        });
        flyway1.setLocations(getBasedir());
        flyway1.setSchemas("public");
        flyway1.migrate();
    }

    @Test(expected = FlywayException.class)
    public void warning() {
        flyway.setLocations("migration/dbsupport/redshift/sql/warning");
        flyway.migrate();
        // Log should contain "This is a warning"
    }
}
