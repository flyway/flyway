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
package org.flywaydb.core.internal.dbsupport.memsql;

import org.flywaydb.core.api.*;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.FlywaySqlScriptException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.resolver.sql.SqlMigrationResolver;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Scanner;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Test to demonstrate the migration functionality using MemSQL and its derivatives.
 */
@SuppressWarnings({"JavaDoc"})
public abstract class MemSQLMigrationTestCase extends MigrationTestCase {
    @Override
    protected String getQuoteLocation() {
        return "migration/dbsupport/memsql/sql/quote";
    }

    @Test
    public void storedProcedure() throws Exception {
        //Not supported by MemSQL
    }

    @Test
    public void delimiter() throws Exception {
        flyway.setLocations("migration/dbsupport/memsql/sql/delimiter");
        flyway.migrate();
    }

    @Test
    public void hashComment() throws Exception {
        flyway.setLocations("migration/dbsupport/memsql/sql/hashcomment");
        flyway.migrate();
    }

    @Test
    public void trigger() throws Exception {
        //Not supported by MemSQL
    }

    @Test
    public void migrateMultipleSchemas() throws Exception {
        //Not supported by MemSQL
    }

    /**
     * Tests clean and migrate for MemSQL Views.
     */
    @Test
    public void view() throws Exception {
        flyway.setLocations("migration/dbsupport/memsql/sql/view");
        flyway.migrate();

        assertEquals(150, jdbcTemplate.queryForInt("SELECT value FROM v"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }

    /**
     * Tests clean and migrate for MemSQL Events.
     */
    @Test
    public void event() throws Exception {
        //Not supported in MemSQL
    }

    /**
     * Tests clean and migrate for MemSQL dumps.
     */
    @Test
    public void dump() throws Exception {
        flyway.setLocations("migration/dbsupport/memsql/sql/dump");
        flyway.migrate();

        assertEquals(0, jdbcTemplate.queryForInt("SELECT count(id) FROM user_account"));

        flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        flyway.migrate();
    }
    /**
     * Tests clean and migrate for MemSQL tables with upper case names.
     * <p/>
     * Only effective on Windows when the server is configured using:
     * <p/>
     * [mysqld]
     * lower-case-table-names=0
     * <p/>
     * This should be added to a file called my.cnf
     * <p/>
     * The server can then be started with this command:
     * <p/>
     * mysqld --defaults-file="path/to/my.cnf"
     */
    @Test
    public void upperCase() throws Exception {
        flyway.setLocations("migration/dbsupport/memsql/sql/uppercase");
        flyway.migrate();

        assertEquals(0, jdbcTemplate.queryForInt("SELECT count(*) FROM A1"));

        //flyway.clean();

        // Running migrate again on an unclean database, triggers duplicate object exceptions.
        //flyway.migrate();
    }

    /**
     * Tests parsing support for " string literals.
     */
    @Test
    public void doubleQuote() throws FlywayException {
        flyway.setLocations("migration/dbsupport/memsql/sql/doublequote");
        flyway.migrate();
    }

    /**
     * Tests parsing support for \' in string literals.
     */
    @Test
    public void escapeSingleQuote() throws FlywayException {
        flyway.setLocations("migration/dbsupport/memsql/sql/escape");
        flyway.migrate();
    }

    /**
     * Tests whether locking problems occur when Flyway's DB connection gets reused.
     */
    @Test
    public void lockOnConnectionReUse() throws SQLException {
        DataSource twoConnectionsDataSource = new TwoConnectionsDataSource(flyway.getDataSource());
        flyway.setDataSource(twoConnectionsDataSource);
        flyway.setLocations("migration/dbsupport/memsql/sql/others");
        flyway.migrate();

        Connection connection1 = twoConnectionsDataSource.getConnection();
        Connection connection2 = twoConnectionsDataSource.getConnection();
        assertEquals(2, new JdbcTemplate(connection1, 0).queryForInt("SELECT COUNT(*) FROM test_user"));
        assertEquals(2, new JdbcTemplate(connection2, 0).queryForInt("SELECT COUNT(*) FROM test_user"));
    }

    private static class TwoConnectionsDataSource extends AbstractDataSource {
        private final DataSource[] dataSources;
        private int count;

        public TwoConnectionsDataSource(DataSource dataSource) throws SQLException {
            dataSources = new DataSource[] {
                    new SingleConnectionDataSource(dataSource.getConnection(), true),
                    new SingleConnectionDataSource(dataSource.getConnection(), true)
            };
        }

        public Connection getConnection() throws SQLException {
            return dataSources[count++ % dataSources.length].getConnection();
        }

        public Connection getConnection(String username, String password) throws SQLException {
            return null;
        }

        public Logger getParentLogger() {
            throw new UnsupportedOperationException("getParentLogger");
        }
    }

    @Override
    protected void createFlyway3MetadataTable() throws Exception {
        jdbcTemplate.execute("CREATE REFERENCE TABLE `schema_version` (\n" +
                "    `version_rank` INT NOT NULL,\n" +
                "    `installed_rank` INT NOT NULL,\n" +
                "    `version` VARCHAR(50) NOT NULL PRIMARY KEY,\n" +
                "    `description` VARCHAR(200) NOT NULL,\n" +
                "    `type` VARCHAR(20) NOT NULL,\n" +
                "    `script` VARCHAR(1000) NOT NULL,\n" +
                "    `checksum` INT,\n" +
                "    `installed_by` VARCHAR(100) NOT NULL,\n" +
                "    `installed_on` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                "    `execution_time` INT NOT NULL,\n" +
                "    `success` TINYINT(1) NOT NULL\n" +
                ")");
        jdbcTemplate.execute("CREATE INDEX `schema_version_vr_idx` ON `schema_version` (`version_rank`)");
        jdbcTemplate.execute("CREATE INDEX `schema_version_ir_idx` ON `schema_version` (`installed_rank`)");
        jdbcTemplate.execute("CREATE INDEX `schema_version_s_idx` ON `schema_version` (`success`)");
    }

    private void insertIntoFlyway3MetadataTable(JdbcTemplate jdbcTemplate, int versionRank, int installedRank, String version, String description, String type, String script, Integer checksum, String installedBy,
                                                int executionTime, boolean success) throws SQLException {
        jdbcTemplate.execute("INSERT INTO " + dbSupport.quote("schema_version")
                        + " (" + dbSupport.quote("version_rank")
                        + "," + dbSupport.quote("installed_rank")
                        + "," + dbSupport.quote("version")
                        + "," + dbSupport.quote("description")
                        + "," + dbSupport.quote("type")
                        + "," + dbSupport.quote("script")
                        + "," + dbSupport.quote("checksum")
                        + "," + dbSupport.quote("installed_by")
                        + "," + dbSupport.quote("execution_time")
                        + "," + dbSupport.quote("success")
                        + ") VALUES (?,?,?,?,?,?,?,?,?,?)",
                versionRank, installedRank, version, description, type, script, checksum, installedBy, executionTime, success);
    }

    @Test
    public void upgradeMetadataTableTo40Format() throws Exception {
        createFlyway3MetadataTable();
        jdbcTemplate.execute("CREATE TABLE test_user (\n" +
                "  id INT NOT NULL,\n" +
                "  name VARCHAR(25) NOT NULL,\n" +
                "  PRIMARY KEY(name)\n" +
                ")");
        insertIntoFlyway3MetadataTable(jdbcTemplate, 1, 1, "0.1", "<< INIT >>", "INIT", "<< INIT >>", null, "flyway3", 0, true);
        insertIntoFlyway3MetadataTable(jdbcTemplate, 2, 2, "1", "First", "SQL", "V1__First.sql", 1234, "flyway3", 15, true);
        flyway.setLocations("migration/dbsupport/memsql/sql/others");
        assertEquals(3, flyway.migrate());
        flyway.validate();
        assertEquals(5, flyway.info().applied().length);
        assertEquals(814278929, flyway.info().applied()[1].getChecksum().intValue());
    }

    @Test
    public void outOfOrderMultipleRankIncrease() {
        flyway.setLocations("migration/dbsupport/memsql/sql/others");
        flyway.migrate();

        flyway.setLocations("migration/dbsupport/memsql/sql/others", getMigrationDir() + "/outoforder");
        flyway.setOutOfOrder(true);
        flyway.migrate();

        MigrationInfo[] all = flyway.info().all();
        assertEquals(org.flywaydb.core.api.MigrationState.OUT_OF_ORDER, all[all.length - 1].getState());
    }

    @Test
    public void isSchemaEmpty() throws Exception {
        Schema schema = dbSupport.getOriginalSchema();

        assertTrue(schema.empty());

        flyway.setLocations("migration/dbsupport/memsql/sql/others");
        flyway.migrate();

        assertFalse(schema.empty());

        flyway.clean();

        assertTrue(schema.empty());
    }

    @Test
    public void nonEmptySchemaWithInitOnMigrate() throws Exception {
        createTestTable();
        flyway.setLocations("migration/dbsupport/memsql/sql/others");
        flyway.setBaselineVersionAsString("0");
        flyway.setBaselineOnMigrate(true);
        flyway.migrate();
        MigrationInfo[] migrationInfos = flyway.info().all();

        assertEquals(5, migrationInfos.length);

        assertEquals(MigrationType.BASELINE, migrationInfos[0].getType());
        assertEquals("0", migrationInfos[0].getVersion().toString());

        assertEquals("2.0", flyway.info().current().getVersion().toString());
    }

    /**
     * Compares the DB checksum to the classpath checksum of this migration.
     *
     * @param migrationInfo
     *            The migration to check.
     */
    protected void assertChecksum(MigrationInfo migrationInfo) {
        SqlMigrationResolver sqlMigrationResolver = new SqlMigrationResolver(
                dbSupport, new Scanner(Thread.currentThread().getContextClassLoader()),
                new Location("migration/dbsupport/memsql/sql/others"),
                PlaceholderReplacer.NO_PLACEHOLDERS,
                "UTF-8",
                "V", "R", "__", ".sql");
        List<ResolvedMigration> migrations = sqlMigrationResolver.resolveMigrations();
        for (ResolvedMigration migration : migrations) {
            if (migration.getVersion().toString().equals(migrationInfo.getVersion().toString())) {
                assertEquals("Wrong checksum for " + migrationInfo.getScript(), migration.getChecksum(), migrationInfo.getChecksum());
            }
        }
    }

    @Test
    public void migrate() throws Exception {
        flyway.setLocations("migration/dbsupport/memsql/sql/others");
        flyway.migrate();
        MigrationVersion version = flyway.info().current().getVersion();
        assertEquals("2.0", version.toString());
        assertEquals(0, flyway.migrate());

        // We should have 5 rows if we have a schema creation marker as the first entry, 4 otherwise
        if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
            assertEquals(5, flyway.info().applied().length);
        } else {
            assertEquals(4, flyway.info().applied().length);
        }

        for (MigrationInfo migrationInfo : flyway.info().applied()) {
            assertChecksum(migrationInfo);
        }

        assertEquals(2, jdbcTemplate.queryForInt("select count(*) from all_misters"));
    }

    @Test
    public void customTableName() throws Exception {
        flyway.setLocations("migration/dbsupport/memsql/sql/others");
        flyway.setTable("my_custom_table");
        flyway.migrate();
        int count = jdbcTemplate.queryForInt("select count(*) from " + dbSupport.quote("my_custom_table"));

        // Same as 'migrate()', count is 5 when we have a schema creation marker
        if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
            assertEquals(5, count);
        } else {
            assertEquals(4, count);
        }
    }

    @Test
    public void nonEmptySchemaWithInit() throws Exception {
        createTestTable();
        flyway.setLocations("migration/dbsupport/memsql/sql/others");
        flyway.setBaselineVersionAsString("0");
        flyway.baseline();
        flyway.migrate();
    }

    protected String getFutureFailedLocation() {
        return "migration/dbsupport/memsql/sql/future_failed";
    }

    @Test
    public void futureFailedMigrationIgnore() throws Exception {
        flyway.setValidateOnMigrate(false);
        flyway.setLocations(getFutureFailedLocation());

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            // Expected
        }

        flyway.setIgnoreFailedFutureMigration(true);
        flyway.setLocations("migration/dbsupport/memsql/sql/others");
        flyway.migrate();
    }

    @Test
    public void subDir() {
        flyway.setLocations("migration/dbsupport/memsql/sql" + "/subdir");
        assertEquals(3, flyway.migrate());
    }

    @Test
    public void target() throws Exception {
        flyway.setLocations("migration/dbsupport/memsql/sql/others");

        flyway.setTarget(MigrationVersion.fromVersion("1.2"));
        flyway.migrate();
        assertEquals("1.2", flyway.info().current().getVersion().toString());
        assertEquals("Populate table", flyway.info().current().getDescription());

        flyway.setTarget(MigrationVersion.fromVersion("1.0"));
        flyway.migrate();
        assertEquals("1.2", flyway.info().current().getVersion().toString());
        assertEquals("Populate table", flyway.info().current().getDescription());

        flyway.setTarget(MigrationVersion.LATEST);
        flyway.migrate();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
    }

    @Test
    public void checkValidationWithInitRow() throws Exception {
        flyway.setLocations("migration/dbsupport/memsql/sql/others");
        flyway.setTarget(MigrationVersion.fromVersion("1.1"));
        flyway.migrate();
        assertEquals("1.1", flyway.info().current().getVersion().toString());

        jdbcTemplate.update("DROP TABLE " + dbSupport.quote(flyway.getTable()));
        flyway.setBaselineVersion(MigrationVersion.fromVersion("1.1"));
        flyway.setBaselineDescription("initial version 1.1");
        flyway.baseline();

        flyway.setTarget(MigrationVersion.LATEST);
        flyway.migrate();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        flyway.validate();
    }

    @Test
    public void failedMigration() throws Exception {
        String tableName = "before_the_error";

        flyway.setLocations("migration/dbsupport/memsql/sql" + "/failed");
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("tableName", dbSupport.quote(tableName));
        flyway.setPlaceholders(placeholders);

        try {
            flyway.migrate();
            fail();
        } catch (FlywaySqlScriptException e) {
            System.out.println(e.getMessage());
            // root cause of exception must be defined, and it should be FlywaySqlScriptException
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof SQLException);
            // and make sure the failed statement was properly recorded
            assertEquals(21, e.getLineNumber());
            assertEquals("THIS IS NOT VALID SQL", e.getStatement());
        }

        MigrationInfo migration = flyway.info().current();
        String schema = dbSupport.getSchema(dbSupport.getCurrentSchemaName()).getTable(tableName).getName();
        boolean check = dbSupport.getSchema(dbSupport.getCurrentSchemaName()).getTable(tableName).exists();
    //    assertEquals(
    //            dbSupport.supportsDdlTransactions(),
    //            !dbSupport.getSchema(dbSupport.getCurrentSchemaName()).getTable(tableName).exists());
        if (dbSupport.supportsDdlTransactions()) {
            assertNull(migration);
        } else {
            MigrationVersion version = migration.getVersion();
            assertEquals("1", version.toString());
            assertEquals("Should Fail", migration.getDescription());
            assertEquals(MigrationState.FAILED, migration.getState());

            // With schema markers, we'll have 2 applied
            if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
                assertEquals(2, flyway.info().applied().length);
            } else {
                assertEquals(1, flyway.info().applied().length);
            }

        }
    }
}
