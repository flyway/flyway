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
package org.flywaydb.core.internal.database.postgresql;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test to demonstrate the migration functionality using PostgreSQL.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.PostgreSQL.class)
@RunWith(Parameterized.class)
public class PostgreSQLMigrationMediumTest extends MigrationTestCase {
    static final String JDBC_URL_POSTGRESQL_92 = "jdbc:postgresql://localhost:62050/flyway_db";
    static final String JDBC_URL_POSTGRESQL_93 = "jdbc:postgresql://localhost:62051/flyway_db";
    static final String JDBC_URL_POSTGRESQL_94 = "jdbc:postgresql://localhost:62052/flyway_db";
    static final String JDBC_URL_POSTGRESQL_95 = "jdbc:postgresql://localhost:62053/flyway_db";
    static final String JDBC_URL_POSTGRESQL_96 = "jdbc:postgresql://localhost:62054/flyway_db";
    static final String JDBC_URL_POSTGRESQL_100 = "jdbc:postgresql://localhost:62055/flyway_db";
    static final String JDBC_USER = "flyway";
    static final String JDBC_PASSWORD = "flyway";

    private final String jdbcUrl;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {JDBC_URL_POSTGRESQL_100},
                {JDBC_URL_POSTGRESQL_96},
                {JDBC_URL_POSTGRESQL_95},
                {JDBC_URL_POSTGRESQL_94},
                {JDBC_URL_POSTGRESQL_93},
                {JDBC_URL_POSTGRESQL_92}
        });
    }

    public PostgreSQLMigrationMediumTest(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    protected DataSource createDataSource(Properties customProperties) {
        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null,
                jdbcUrl, JDBC_USER, JDBC_PASSWORD);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    /**
     * Tests clean and migrate for PostgreSQL Types.
     */
    @Test
    public void type() throws Exception {
        flyway.setLocations("migration/database/postgresql/sql/type");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();

        // Clean again, to prevent tests with non superuser rights to fail.
        flyway.clean();
    }

    @Test
    public void vacuum() throws Exception {
        flyway.setLocations("migration/database/postgresql/sql/vacuum");
        try {
            flyway.migrate();
        } catch (FlywayException e) {
            assertThat(e.getMessage(), containsString("non-transactional"));
        }
        flyway.setMixed(true);
        flyway.migrate();
    }

    @Test
    public void index() throws Exception {
        flyway.setLocations("migration/database/postgresql/sql/index");
        flyway.setMixed(true);
        flyway.migrate();
    }

    @Test
    public void cleanUnknown() throws Exception {
        flyway.setSchemas("non-existant");
        flyway.clean();
    }

    /**
     * Tests clean and migrate for PostgreSQL Stored Procedures.
     */
    @Test
    public void storedProcedure() throws Exception {
        flyway.setLocations("migration/database/postgresql/sql/procedure");
        flyway.migrate();

        assertEquals("Hello", jdbcTemplate.queryForString("SELECT value FROM test_data"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for PostgreSQL Functions.
     */
    @Test
    public void function() throws Exception {
        flyway.setLocations("migration/database/postgresql/sql/function");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for PostgreSQL Triggers.
     */
    @Test
    public void trigger() throws Exception {
        flyway.setLocations("migration/database/postgresql/sql/trigger");
        flyway.migrate();

        assertEquals(10, jdbcTemplate.queryForInt("SELECT count(*) FROM test4"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();

    }

    /**
     * Tests clean and migrate for PostgreSQL Views.
     */
    @Test
    public void view() throws Exception {
        flyway.setLocations("migration/database/postgresql/sql/view");
        flyway.migrate();

        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM \"\"\"v\"\"\""));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for PostgreSQL Materialized Views.
     */
    @Test
    public void materializedview() throws Exception {
        assumeDatabaseVersionNotLessThan(9, 3);
        flyway.setLocations("migration/database/postgresql/sql/materializedview");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for PostgreSQL child tables.
     */
    @Test
    public void inheritance() throws Exception {
        flyway.setLocations("migration/database/postgresql/sql/inheritance");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for PostgreSQL Domains.
     */
    @Test
    public void domain() throws Exception {
        flyway.setLocations("migration/database/postgresql/sql/domain");
        flyway.migrate();

        assertEquals("foo", jdbcTemplate.queryForString("SELECT x FROM t"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for PostgreSQL Enums.
     */
    @Test
    public void enumeration() throws Exception {
        flyway.setLocations("migration/database/postgresql/sql/enum");
        flyway.migrate();

        assertEquals("positive", jdbcTemplate.queryForString("SELECT x FROM t"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for PostgreSQL Aggregates.
     */
    @Test
    public void aggregate() throws Exception {
        flyway.setLocations("migration/database/postgresql/sql/aggregate");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests parsing support for $$ string literals.
     */
    @Test
    public void dollarQuote() throws Exception {
        flyway.setLocations("migration/database/postgresql/sql/dollar");
        flyway.migrate();
        assertEquals(9, jdbcTemplate.queryForInt("select count(*) from dollar"));
    }

    /**
     * Tests parsing support for multiline string literals.
     */
    @Test
    public void multiLine() throws Exception {
        flyway.setLocations("migration/database/postgresql/sql/multiline");
        flyway.migrate();
        assertEquals(1, jdbcTemplate.queryForInt("select count(*) from address"));
    }

    /**
     * Tests support for COPY FROM STDIN statements generated by pg_dump..
     */
    @Test
    public void copy() throws Exception {
        flyway.setLocations("migration/database/postgresql/sql/copy");
        flyway.migrate();
        assertEquals(6, jdbcTemplate.queryForInt("select count(*) from copy_test"));
    }

    /**
     * Tests that the lock on SCHEMA_VERSION is not blocking SQL commands in migrations. This test won't fail if there's
     * a too restrictive lock - it would just hang endlessly.
     */
    @Test
    public void lock() {
        flyway.setLocations("migration/database/postgresql/sql/lock");
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
        flyway.setLocations("migration/database/postgresql/sql/warning");
        flyway.migrate();
        // Log should contain "This is a warning"
    }
}
