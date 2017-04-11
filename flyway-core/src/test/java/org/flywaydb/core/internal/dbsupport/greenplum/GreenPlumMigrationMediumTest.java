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
package org.flywaydb.core.internal.dbsupport.greenplum;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test to demonstrate the migration functionality using GreenPlum.
 *
 * Note: most of these tests re-use the *.sql test resources that were created for postgresql.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.GreenPlum.class)
public class GreenPlumMigrationMediumTest extends MigrationTestCase {
    @Override
    protected DataSource createDataSource(Properties customProperties) {
    	String user = customProperties.getProperty("greenplum.user", "flyway");
        // Password must be at least 8 characters, with upper and lower case:
        String password = customProperties.getProperty("greenplum.password", "Flyway123");
        // Create an ssh tunnel on port 5439 to your Redshift instance before running this test!
        String url = customProperties.getProperty("greenplum.url", "jdbc:pivotal:greenplum://localhost:5432;DatabaseName=flyway;");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password);
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
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
     * Tests parsing support for $$ string literals.
     */
    @Test
    public void dollarQuote() throws Exception {
        flyway.setLocations("migration/dbsupport/redshift/sql/dollar");
        flyway.migrate();
        assertEquals(5, jdbcTemplate.queryForInt("select count(*) from dollar"));
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
    //@Test
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
        flyway1.setLocations(getBasedir());
        flyway1.setSchemas("public");
        flyway1.migrate();
    }

    @Override
    protected void createFlyway3MetadataTable() throws Exception {
        jdbcTemplate.execute("CREATE TABLE \"schema_version\" (\n" +
                "    \"version_rank\" INT NOT NULL,\n" +
                "    \"installed_rank\" INT NOT NULL,\n" +
                "    \"version\" VARCHAR(50) NOT NULL,\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INTEGER,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP NOT NULL DEFAULT current_date,\n" +
                "    \"execution_time\" INTEGER NOT NULL,\n" +
                "    \"success\" BOOLEAN NOT NULL\n" +
                ")");
        jdbcTemplate.execute("ALTER TABLE \"schema_version\" ADD CONSTRAINT \"schema_version_pk\" PRIMARY KEY (\"version\")");
    }
}
