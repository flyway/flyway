/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.core.dbsupport.postgresql;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.migration.MigrationTestCase;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import com.googlecode.flyway.core.util.jdbc.JdbcUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import com.googlecode.flyway.core.DbCategory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Test to demonstrate the migration functionality using PostgreSQL.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.PostgreSQL.class)
public class PostgreSQLMigrationMediumTest extends MigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) {
        String user = customProperties.getProperty("postgresql.user", "flyway");
        String password = customProperties.getProperty("postgresql.password", "flyway");
        String url = customProperties.getProperty("postgresql.url", "jdbc:postgresql://localhost/flyway_db");

        return new DriverDataSource(null, url, user, password);
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
        flyway.setLocations("migration/dbsupport/postgresql/sql/type");
        flyway.migrate();

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();

        // Clean again, to prevent tests with non superuser rights to fail.
        flyway.clean();
    }

    /**
     * Tests clean and migrate for PostgreSQL Stored Procedures.
     */
    @Test
    public void storedProcedure() throws Exception {
        flyway.setLocations("migration/dbsupport/postgresql/sql/procedure");
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
        flyway.setLocations("migration/dbsupport/postgresql/sql/function");
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
        flyway.setLocations("migration/dbsupport/postgresql/sql/trigger");
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
        flyway.setLocations("migration/dbsupport/postgresql/sql/view");
        flyway.migrate();

        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM \"\"\"v\"\"\""));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for PostgreSQL child tables.
     */
    @Test
    public void inheritance() throws Exception {
        flyway.setLocations("migration/dbsupport/postgresql/sql/inheritance");
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
        flyway.setLocations("migration/dbsupport/postgresql/sql/domain");
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
        flyway.setLocations("migration/dbsupport/postgresql/sql/enum");
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
        flyway.setLocations("migration/dbsupport/postgresql/sql/aggregate");
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
        flyway.setLocations("migration/dbsupport/postgresql/sql/dollar");
        flyway.migrate();
        assertEquals(9, jdbcTemplate.queryForInt("select count(*) from dollar"));
    }

    /**
     * Tests parsing support for multiline string literals.
     */
    @Test
    public void multiLine() throws Exception {
        flyway.setLocations("migration/dbsupport/postgresql/sql/multiline");
        flyway.migrate();
        assertEquals(1, jdbcTemplate.queryForInt("select count(*) from address"));
    }

    /**
     * Tests that the lock on SCHEMA_VERSION is not blocking SQL commands in migrations. This test won't fail if there's
     * a too restrictive lock - it would just hang endlessly.
     */
    @Test
    public void lock() {
        flyway.setLocations("migration/dbsupport/postgresql/sql/lock");
        flyway.migrate();
    }

    @Test
    public void emptySearchPath() {
        Flyway flyway1 = new Flyway();
        DriverDataSource driverDataSource = (DriverDataSource) dataSource;
        flyway1.setDataSource(new DriverDataSource(
                null, driverDataSource.getUrl(), driverDataSource.getUser(), driverDataSource.getPassword()) {
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
        flyway1.setLocations(BASEDIR);
        flyway1.setSchemas("public");
        flyway1.migrate();
    }
}
