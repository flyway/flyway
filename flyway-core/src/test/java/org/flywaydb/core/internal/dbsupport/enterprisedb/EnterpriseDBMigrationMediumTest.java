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
package org.flywaydb.core.internal.dbsupport.enterprisedb;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test to demonstrate the migration functionality using EnterpriseDB.
 */
@SuppressWarnings({"JavaDoc"})
@Category(DbCategory.EnterpriseDB.class)
public class EnterpriseDBMigrationMediumTest extends MigrationTestCase {

    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        String user = customProperties.getProperty("enterprisedb.user", "flyway");
        String password = customProperties.getProperty("enterprisedb.password", "flyway");
        String url = customProperties.getProperty("enterprisedb.url", "jdbc:edb://localhost/flyway_db");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, new Properties());
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/quote";
    }

    /**
     * Tests migrations containing placeholders.
     */
    @Test
    public void migrationsWithPlaceholders() throws Exception {
        int countUserObjects1 = jdbcTemplate.queryForInt("SELECT count(*) FROM user_objects");

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("tableName", "test_user");
        flyway.setPlaceholders(placeholders);
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/placeholders");

        flyway.migrate();
        MigrationVersion version = flyway.info().current().getVersion();
        assertEquals("1.1", version.toString());
        assertEquals("Populate table", flyway.info().current().getDescription());

        assertEquals("Mr. T triggered", jdbcTemplate.queryForString("select name from test_user"));

        flyway.clean();

        int countUserObjects2 = jdbcTemplate.queryForInt("SELECT count(*) FROM user_objects");
        assertEquals(countUserObjects1, countUserObjects2);

        MigrationInfo[] migrationInfos = flyway.info().applied();
        for (MigrationInfo migrationInfo : migrationInfos) {
            assertNotNull(migrationInfo.getScript() + " has no checksum", migrationInfo.getChecksum());
        }
    }


    @Test
    public void vacuum() throws Exception {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/vacuum");
        flyway.setResolvers(new EnterpriseDBMigrationMediumTest.NoTransactionMigrationResolver(new String[][]{
                {"2.0", "Vacuum without transaction", "vacuum-notrans", "VACUUM t"}
        }));
        flyway.migrate();
    }

    @Test
    public void cleanUnknown() throws Exception {
        flyway.setSchemas("non-existant");
        flyway.clean();
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
                resolvedMigrations.add(new EnterpriseDBMigrationMediumTest.NoTransactionResolvedMigration(migrationData));
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
            return new EnterpriseDBMigrationMediumTest.NoTransactionMigrationExecutor(data[3]);
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
     * Tests clean and migrate for enterprisedb Stored Procedures.
     * TODO: Identify why the Oracle tests include a V2__Invalid.sql file
     */
    @Test
    public void storedProcedure() throws Exception {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/procedure");
        flyway.migrate();

        assertEquals("Hello", jdbcTemplate.queryForString("SELECT value FROM test_data"));

        flyway.clean();

        flyway.migrate();
    }

    /**
     * Tests parsing of CREATE PACKAGE.
     * TODO: Follow-up on EDB bug to see if the fix allows the complex comments in package body to work
     */
    @Test
    public void createPackage() throws FlywayException {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/package");
        flyway.migrate();
    }

    @Test
    public void count() throws FlywayException {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/count");
        flyway.migrate();
    }

    /**
     * Tests parsing of object names that contain keywords such as MY_TABLE.
     */
    @Test
    public void objectNames() throws FlywayException {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/objectnames");
        flyway.migrate();
    }

    /**
     * Tests cleaning up after CREATE MATERIALIZED VIEW.
     */
    @Test
    public void createMaterializedView() throws FlywayException {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/materialized");
        flyway.migrate();
        flyway.clean();
    }

    /**
     * Tests clean and migrate for Views.
     */
    @Test
    public void view() throws Exception {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/view");
        flyway.migrate();

        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM \"\"\"v\"\"\""));

        flyway.clean();

        flyway.migrate();
    }

    /**
     * Tests clean and migrate for child tables.
     */
    @Test
    public void inheritance() throws Exception {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/inheritance");
        flyway.migrate();

        flyway.clean();

        flyway.migrate();
    }

    /**
     * Tests clean and migrate for Domains.
     */
    @Test
    public void domain() throws Exception {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/domain");
        flyway.migrate();

        assertEquals("foo", jdbcTemplate.queryForString("SELECT x FROM t"));

        flyway.clean();

        flyway.migrate();
    }

    /**
     * Tests clean and migrate for Enums.
     */
    @Test
    public void enumeration() throws Exception {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/enum");
        flyway.migrate();

        assertEquals("positive", jdbcTemplate.queryForString("SELECT x FROM t"));

        flyway.clean();

        flyway.migrate();
    }

    /**
     * Tests clean and migrate for Aggregates.
     */
    @Test
    public void aggregate() throws Exception {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/aggregate");
        flyway.migrate();

        flyway.clean();

        flyway.migrate();
    }

    /**
     * Tests parsing support for $$ string literals.
     */
    @Test
    public void dollarQuote() throws Exception {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/dollar");
        flyway.migrate();
        assertEquals(9, jdbcTemplate.queryForInt("select count(*) from dollar"));
    }

    /**
     * Tests parsing support for multiline string literals.
     */
    @Test
    public void multiLine() throws Exception {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/multiline");
        flyway.migrate();
        assertEquals(1, jdbcTemplate.queryForInt("select count(*) from address"));
    }

    /**
     * Tests support for COPY FROM STDIN statements generated by pg_dump..
     */
    @Test
    @Ignore("The EDB JDBC driver doesn't currently support copy operations")
    public void copy() throws Exception {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/copy");
        flyway.migrate();
        assertEquals(6, jdbcTemplate.queryForInt("select count(*) from copy_test"));
    }

    /**
     * Tests support for user defined types.
     * TODO: Uncomment the TYPE BODY stanza in the SQL file once the EDB bug is fixed.
     */
    @Test
    public void type() throws FlywayException {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/type");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
    }

    /**
     * Tests support for create function.
     */
    @Test
    public void function() throws FlywayException {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/function");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
    }

    /**
     * Tests support for create trigger. Ensures that a Statement is used instead of a PreparedStatement.
     * Also ensures that schema-level triggers are properly cleaned.
     */
    @Test
    public void trigger() throws FlywayException {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/trigger");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
    }

    /**
     * Tests support for clean together with EnterpriseDB Text indexes.
     */
    @Test
    @Ignore("Beyond the scope of the current iteration")
    public void text() throws FlywayException {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/text");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
    }

    /**
     * Tests support for clean together with Index Organized Tables.
     */
    @Test
    public void indexOrganizedTable() throws FlywayException {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/iot");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void commentEnterpriseDB() throws Exception {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/comment");
        assertEquals(3, flyway.migrate());

        String statusWithComment = jdbcTemplate.queryForString("select ob.STATUS from user_objects ob where ob.OBJECT_NAME = 'PERSON_WITH_COMMENT' ");
        String statusWithoutComment = jdbcTemplate.queryForString("select ob.STATUS from user_objects ob where ob.OBJECT_NAME = 'PERSON_WITHOUT_COMMENT' ");
        assertEquals("VALID", statusWithoutComment);
        assertEquals("VALID", statusWithComment);
    }

    /**
     * Tests support for clean together with XML Type.
     */
    @Test
    @Ignore("Beyond the scope of the current iteration")
    public void xml() throws FlywayException {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/xml");
        flyway.migrate();
        flyway.clean();
        flyway.migrate();
    }

    /**
     * Tests that the lock on SCHEMA_VERSION is not blocking SQL commands in migrations. This test won't fail if there's
     * a too restrictive lock - it would just hang endlessly.
     * TODO: Figure out whether the default LOCK MODE is valid
     */
    @Test
    public void lock() {
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/lock");
        flyway.migrate();
    }

    @Test
    public void emptySearchPath() {
        Flyway flyway1 = new Flyway();
        DriverDataSource driverDataSource = (DriverDataSource) dataSource;
        flyway1.setDataSource(new DriverDataSource(Thread.currentThread().getContextClassLoader(),
                null, driverDataSource.getUrl(), driverDataSource.getUser(), driverDataSource.getPassword(), new Properties()) {
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
        flyway.setLocations("migration/dbsupport/enterprisedb/sql/warning");
        flyway.migrate();
        // Log should contain "This is a warning"
    }

    @Ignore("not needed")
    @Override
    public void upgradeMetadataTableTo40Format() throws Exception {
    }
}
