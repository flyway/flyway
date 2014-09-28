/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport.postgresql;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.migration.MigrationTestCase;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password);
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

    @Test
    public void vacuum() throws Exception {
        flyway.setLocations("migration/dbsupport/postgresql/sql/vacuum");
        flyway.setResolvers(new NoTransactionMigrationResolver(new String[][]{
                {"2.0", "Vacuum without transaction", "vacuum-notrans", "VACUUM t"}
        }));
        flyway.migrate();
    }

    private class NoTransactionMigrationResolver implements MigrationResolver {
        private final String[][] data;

        private NoTransactionMigrationResolver(String[][] data) {
            this.data = data;
        }

        @Override
        public Collection<ResolvedMigration> resolveMigrations() {
            List<ResolvedMigration> resolvedMigrations = new ArrayList<ResolvedMigration>();
            for (String[] migrationData : data) {
                resolvedMigrations.add(new NoTransactionResolvedMigration(migrationData));
            }
            return resolvedMigrations;
        }
    }

    private class NoTransactionResolvedMigration implements ResolvedMigration {
        private final String[] data;

        private NoTransactionResolvedMigration(String[] data) {
            this.data = data;
        }

        @Override
        public MigrationVersion getVersion() {
            return MigrationVersion.fromVersion(data[0]);
        }

        @Override
        public String getDescription() {
            return data[1];
        }

        @Override
        public String getScript() {
            return data[2];
        }

        @Override
        public Integer getChecksum() {
            return data[3].hashCode();
        }

        @Override
        public MigrationType getType() {
            return MigrationType.CUSTOM;
        }

        @Override
        public String getPhysicalLocation() {
            return null;
        }

        @Override
        public MigrationExecutor getExecutor() {
            return new NoTransactionMigrationExecutor(data[3]);
        }
    }

    private class NoTransactionMigrationExecutor implements MigrationExecutor {
        private final String data;

        private NoTransactionMigrationExecutor(String data) {
            this.data = data;
        }

        @Override
        public void execute(Connection connection) throws SQLException {
            jdbcTemplate.executeStatement(data);
        }

        @Override
        public boolean executeInTransaction() {
            return false;
        }
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
        flyway1.setDataSource(new DriverDataSource(Thread.currentThread().getContextClassLoader(),
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

    @Test(expected = FlywayException.class)
    public void warning() {
        flyway.setLocations("migration/dbsupport/postgresql/sql/warning");
        flyway.migrate();
        // Log should contain "This is a warning"
    }
}
