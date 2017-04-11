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
package org.flywaydb.core.internal.dbsupport.db2zos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.DbCategory;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.FlywaySqlScriptException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.resolver.FlywayConfigurationForTests;
import org.flywaydb.core.internal.resolver.sql.SqlMigrationResolver;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.scanner.Scanner;
import org.flywaydb.core.migration.MigrationTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(DbCategory.DB2zOS.class)
public class DB2zOSMigrationMediumTest extends MigrationTestCase {

    private String query;

    @Override
    protected void configureFlyway() {
        super.configureFlyway();
        flyway.setTable("SCHEMA_VERSION");
        flyway.setSchemas("AURINT");
        flyway.setBaselineOnMigrate(true);
        try {
            jdbcTemplate.update("SET CURRENT SQLID = 'AURINT';");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void assertChecksum(MigrationInfo migrationInfo) {
        SqlMigrationResolver sqlMigrationResolver = new SqlMigrationResolver(
                dbSupport, new Scanner(Thread.currentThread().getContextClassLoader()),
                new Locations(getBasedir() + "/default"),
                PlaceholderReplacer.NO_PLACEHOLDERS,
                FlywayConfigurationForTests.create());
        List<ResolvedMigration> migrations = sqlMigrationResolver.resolveMigrations();
        for (ResolvedMigration migration : migrations) {
            if (migration.getVersion().toString().equals(migrationInfo.getVersion().toString())) {
                assertEquals("Wrong checksum for " + migrationInfo.getScript(), migration.getChecksum(), migrationInfo.getChecksum());
            }
        }
    }

    @Override
    protected DataSource createDataSource(Properties customProperties) throws Exception {
        String user = customProperties.getProperty("db2.user", "AURINTS");
        String password = customProperties.getProperty("db2.password", "password");
        String url = customProperties.getProperty("db2.url", "jdbc:db2://host:port/schemaname");

        return new DriverDataSource(Thread.currentThread().getContextClassLoader(), null, url, user, password, null);
    }

    @Override
    protected void createFlyway3MetadataTable() throws Exception {
        jdbcTemplate.execute("CREATE TABLESPACE " + "\"SDBVERS\"" +
                "       IN AURINT " +
                "       SEGSIZE 4 " +
                "       BUFFERPOOL BP0 " +
                "       LOCKSIZE PAGE " +
                "       LOCKMAX SYSTEM " +
                "       CLOSE YES " +
                "       COMPRESS YES;");
        jdbcTemplate.execute("CREATE TABLE \"SCHEMA_VERSION\" (\n" +
                "    \"version_rank\" INT NOT NULL,\n" +
                "    \"installed_rank\" INT NOT NULL,\n" +
                "    \"version\" VARCHAR(50) NOT NULL,\n" +
                "    \"description\" VARCHAR(200) NOT NULL,\n" +
                "    \"type\" VARCHAR(20) NOT NULL,\n" +
                "    \"script\" VARCHAR(1000) NOT NULL,\n" +
                "    \"checksum\" INT,\n" +
                "    \"installed_by\" VARCHAR(100) NOT NULL,\n" +
                "    \"installed_on\" TIMESTAMP NOT NULL WITH DEFAULT,\n" +
                "    \"execution_time\" INT NOT NULL,\n" +
                "    \"success\" SMALLINT NOT NULL,\n" +
                "    CONSTRAINT \"schema_version_s\" CHECK (\"success\" in(0,1))\n" +
                ")\n" +
                "IN AURINT.\"SDBVERS\";");
    }

    @Override
    protected void createTestTable() throws SQLException {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL,\n" +
                "  PRIMARY KEY(name)) IN AURINT.SPERS;");
    }

    protected String getAliasLocation() {
        return "migration/dbsupport/db2zos/sql/alias";
    }

    @Override
    protected String getBasedir() {
        return "migration/dbsupport/db2zos/sql/";
    }

    @Override
    protected String getCommentLocation() {
        return "migration/dbsupport/db2zos/sql/comment";
    }

    @Override
    protected String getFutureFailedLocation() {
        return "migration/dbsupport/db2zos/sql/future_failed";
    }

    @Override
    protected String getMigrationDir() {
        return "migration/dbsupport/db2zos/sql/crud/";
    }

    @Override
    protected String getQuoteLocation() {
        return "migration/dbsupport/db2zos/sql/quote/";
    }

    protected String getRoutineLocation() {
        return "migration/dbsupport/db2zos/sql/routines";
    }

    @Override
    protected String getSemiColonLocation() {
        return "migration/dbsupport/db2zos/sql/semicolon";
    }

    protected String getSequenceLocation() {
        return "migration/dbsupport/db2zos/sql/sequence";
    }

    protected String getTriggerLocation() {
        return "migration/dbsupport/db2zos/sql/trigger";
    }

    protected String getTypeLocation() {
        return "migration/dbsupport/db2zos/sql/type";
    }

    @Override
    protected String getValidateLocation() {
        return "migration/dbsupport/db2zos/sql/validate/";
    }

    protected String getViewLocation() {
        return "migration/dbsupport/db2zos/sql/view";
    }

    protected void insertIntoFlyway3MetadataTable(JdbcTemplate jdbcTemplate, int versionRank, int installedRank, String version, String description, String type, String script, Integer checksum, String installedBy,
            int executionTime, boolean success) throws SQLException {
        jdbcTemplate.execute("INSERT INTO AURINT" + "." + dbSupport.quote("SCHEMA_VERSION")
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

    /**
     * Override setUp to create DB2 table space.
     * 
     * @throws Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        jdbcTemplate.execute("CREATE TABLESPACE SPERS " +
                "       IN AURINT " +
                "       SEGSIZE 4 " +
                "       BUFFERPOOL BP0 " +
                "       LOCKSIZE PAGE " +
                "       LOCKMAX SYSTEM " +
                "       CLOSE YES " +
                "       COMPRESS YES;");
    }

    /**
     * Override tearDown to delete DB2 table space.
     * 
     * @throws Exception
     */
    @Override
    @After
    public void tearDown() throws Exception {
        try {
            jdbcTemplate.execute("DROP TABLESPACE AURINT.SPERS\n");
        } catch (SQLException e) {
            if (!e.getMessage().contains("-204")) {
                fail();
            }
        }
        super.tearDown();
    }

    @Test
    public void alias() throws Exception {
        flyway.setLocations(getAliasLocation());
        flyway.baseline();
        flyway.migrate();
    }

    /**
     * Override checkValidationWithInitRow. Setting schema and table space in SQL.
     *
     * @throws Exception
     */
    @Override
    @Test
    public void checkValidationWithInitRow() throws Exception {
        flyway.setLocations(getMigrationDir());
        flyway.setTarget(MigrationVersion.fromVersion("1.1"));
        flyway.baseline();
        flyway.migrate();
        assertEquals("1.1", flyway.info().current().getVersion().toString());

        jdbcTemplate.update("DROP TABLESPACE AURINT.SFLYWAY\n");
        flyway.setBaselineVersionAsString("1.1");
        flyway.setBaselineDescription("initial version 1.1");
        flyway.baseline();

        flyway.setTarget(MigrationVersion.fromVersion("1.3"));
        flyway.migrate();
        assertEquals("1.3", flyway.info().current().getVersion().toString());
        flyway.validate();
    }

    /**
     * Override comment. Setting schema and table space in SQL.
     *
     * @throws Exception
     */
    @Override
    @Test
    public void comment() {
        flyway.setLocations(getCommentLocation());
        flyway.baseline();
        assertEquals(1, flyway.migrate());
    }

    /**
     * Override customTableName. Setting schema and table space in SQL.
     *
     * @throws Exception
     */
    @Override
    @Test
    public void customTableName() throws Exception {
        flyway.setLocations(getMigrationDir());
        flyway.setTable("my_custom_table");
        flyway.baseline();
        flyway.migrate();
        int count = jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + dbSupport.quote("my_custom_table"));

        // Same as 'migrate()', count is 5 when we have a schema creation marker
        if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
            assertEquals(5, count);
        } else {
            assertEquals(4, count);
        }
    }

    /**
     * Override failedMigration. Setting schema and table space in SQL.
     *
     * @throws Exception
     */
    @Override
    @Test
    public void failedMigration() throws Exception {
        String tableName = "before_the_error";

        flyway.setLocations(getBasedir() + "/failed");
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("tableName", dbSupport.quote(tableName));
        flyway.setPlaceholders(placeholders);
        flyway.baseline();

        try {
            flyway.migrate();
            fail();
        } catch (FlywaySqlScriptException e) {
            System.out.println(e.getMessage());
            // root cause of exception must be defined, and it should be FlywaySqlScriptException
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof SQLException);
            // and make sure the failed statement was properly recorded
            assertEquals(23, e.getLineNumber());
            assertEquals("THIS IS NOT VALID SQL", e.getStatement());
        }

        MigrationInfo migration = flyway.info().current();
        assertEquals(
                dbSupport.supportsDdlTransactions(),
                !dbSupport.getSchema(dbSupport.getCurrentSchemaName()).getTable(tableName).exists());
        if (dbSupport.supportsDdlTransactions()) {
            assertTrue(migration.getType().toString() == "BASELINE");
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

    /**
     * Override futureFailedMigration. Setting schema and table space in SQL.
     *
     * @throws Exception
     */
    @Override
    @Test
    public void futureFailedMigration() throws Exception {
        flyway.setValidateOnMigrate(false);
        flyway.setLocations(getFutureFailedLocation());

        try {
            flyway.baseline();
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            // Expected
        }

        flyway.setIgnoreFutureMigrations(false);
        flyway.setLocations(getMigrationDir());
        if (dbSupport.supportsDdlTransactions()) {
            flyway.migrate();
        } else {
            try {
                flyway.migrate();
                fail();
            } catch (FlywayException e) {
                // Expected
            }
        }
    }

    /**
     * Override futureFailedMigrationIgnore. Setting schema and table space in SQL.
     *
     * @throws Exception
     */
    @Override
    @Test
    public void futureFailedMigrationIgnore() throws Exception {
        flyway.setValidateOnMigrate(false);
        flyway.setLocations(getFutureFailedLocation());

        try {
            flyway.baseline();
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            // Expected
        }

        flyway.setIgnoreFailedFutureMigration(true);
        flyway.setLocations(getMigrationDir());
        flyway.migrate();
    }

    /**
     * Override futureFailedMigrationIgnoreAvailableMigrations. Setting schema and table space in SQL.
     *
     * @throws Exception
     */
    @Override
    @Test
    public void futureFailedMigrationIgnoreAvailableMigrations() throws Exception {
        flyway.setValidateOnMigrate(false);
        flyway.setLocations(getFutureFailedLocation());
        flyway.baseline();

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            // Expected
        }

        flyway.setIgnoreFailedFutureMigration(true);
        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            if (dbSupport.supportsDdlTransactions()) {
                assertTrue(e.getMessage().contains("THIS IS NOT VALID SQL"));
            } else {
                assertTrue(e.getMessage().contains("contains a failed migration"));
            }
        }
    }

    @Test
    public void init() throws Exception {
        query = "SELECT COUNT(*) FROM sysibm.systables WHERE dbname = 'AURINT'";

        flyway.baseline();
        int countTablesAfterInit = jdbcTemplate.queryForInt(query);
        assertEquals(1, countTablesAfterInit);
    }

    /**
     * Override isSchemaEmpty. Setting schema and table space in SQL.
     *
     * @throws Exception
     */
    @Override
    @Test
    public void isSchemaEmpty() throws Exception {
        Schema schema = dbSupport.getSchema("AURINT");

        assertTrue(schema.empty());

        flyway.setLocations(getMigrationDir());
        flyway.baseline();
        flyway.migrate();

        assertFalse(schema.empty());

        flyway.clean();

        assertTrue(schema.empty());
    }

    /**
     * Override migrate. Setting schema and table space in SQL.
     *
     * @throws Exception
     */
    @Override
    @Test
    public void migrate() throws Exception {
        flyway.setLocations(getBasedir() + "/default");
        flyway.baseline();
        flyway.migrate();
        MigrationVersion version = flyway.info().current().getVersion();
        assertEquals("1.3", version.toString());
        assertEquals(0, flyway.migrate());

        // We should have 5 rows if we have a schema creation marker as the first entry, 4 otherwise
        if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
            assertEquals(5, flyway.info().applied().length);
        } else {
            assertEquals(4, flyway.info().applied().length);
        }

        for (MigrationInfo migrationInfo : flyway.info().applied()) {
            if (migrationInfo.getType().toString() != "BASELINE") {
                assertChecksum(migrationInfo);
            }
        }

        assertEquals(4, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM person"));
    }

    @Test
    public void migrateCRUD() throws Exception {

        query = "SELECT COUNT(*) FROM sysibm.systables WHERE dbname = 'AURINT'";

        flyway.setLocations(getMigrationDir());

        int countTablesBeforeMigration = jdbcTemplate.queryForInt(query);
        assertEquals(0, countTablesBeforeMigration);
        flyway.baseline();
        flyway.migrate();
        int countTablesAfterMigration = jdbcTemplate.queryForInt(query);
        assertEquals(2, countTablesAfterMigration);

        MigrationVersion version = flyway.info().current().getVersion();
        assertEquals("1.3", version.toString());
        assertEquals("UpdateTable", flyway.info().current().getDescription());
        assertEquals("Nils", jdbcTemplate.queryForString("SELECT firstname FROM AURINT.PERSON WHERE lastname = 'Nilsen'"));

    }

    /**
     * Override migrateMultipleSchemas. Setting schema and table space in SQL.
     *
     * @throws Exception
     */
    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void migrateMultipleSchemas() throws Exception {
        flyway.setSchemas("flyway_1", "flyway_2", "flyway_3");
        flyway.clean();

        flyway.setLocations("migration/multi");
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("schema1", dbSupport.quote("flyway_1"));
        placeholders.put("schema2", dbSupport.quote("flyway_2"));
        placeholders.put("schema3", dbSupport.quote("flyway_3"));
        flyway.setPlaceholders(placeholders);
        flyway.migrate();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals("Add foreign key", flyway.info().current().getDescription());
        assertEquals(0, flyway.migrate());

        assertEquals(4, flyway.info().applied().length);
        assertEquals(2, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + dbSupport.quote("flyway_1") + ".test_user1"));
        assertEquals(2, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + dbSupport.quote("flyway_2") + ".test_user2"));
        assertEquals(2, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + dbSupport.quote("flyway_3") + ".test_user3"));

        flyway.clean();
    }

    /**
     * Override nonEmptySchemaWithDisableInitCheck. createTestTable change.
     *
     * @throws Exception
     */
    @Test
    public void nonEmptySchemaWithDisableInitCheck() throws Exception {
        createTestTable();

        flyway.setLocations(getMigrationDir());
        flyway.setBaselineVersionAsString("0_1");
        flyway.setBaselineOnMigrate(false);
        flyway.baseline();
        flyway.migrate();
    }

    /**
     * Override nonEmptySchemaWithInit. createTestTable change.
     *
     * @throws Exception
     */
    @Override
    @Test
    public void nonEmptySchemaWithInit() throws Exception {
        createTestTable();

        flyway.setLocations(getMigrationDir());
        flyway.setBaselineVersionAsString("0");
        flyway.baseline();
        flyway.migrate();
    }

    /**
     * Override nonEmptySchemaWithInitOnMigrate. createTestTable change.
     *
     * @throws Exception
     */
    @Override
    @Test
    public void nonEmptySchemaWithInitOnMigrate() throws Exception {
        createTestTable();

        flyway.setLocations(getMigrationDir());
        flyway.setBaselineVersionAsString("0");
        flyway.setBaselineOnMigrate(true);
        flyway.migrate();
        MigrationInfo[] migrationInfos = flyway.info().all();

        assertEquals(5, migrationInfos.length);

        assertEquals(MigrationType.BASELINE, migrationInfos[0].getType());
        assertEquals("0", migrationInfos[0].getVersion().toString());

        assertEquals("1.3", flyway.info().current().getVersion().toString());
    }

    /**
     * Override nonEmptySchemaWithInitOnMigrateHighVersion. createTestTable change.
     *
     * @throws Exception
     */
    @Override
    @Test
    public void nonEmptySchemaWithInitOnMigrateHighVersion() throws Exception {
        createTestTable();

        flyway.setLocations(getMigrationDir());
        flyway.setBaselineOnMigrate(true);
        flyway.setBaselineVersion(MigrationVersion.fromVersion("99"));
        flyway.migrate();
        MigrationInfo[] migrationInfos = flyway.info().all();

        assertEquals(5, migrationInfos.length);

        assertEquals(MigrationType.SQL, migrationInfos[0].getType());
        assertEquals("1", migrationInfos[0].getVersion().toString());
        assertEquals(MigrationState.BELOW_BASELINE, migrationInfos[0].getState());

        MigrationInfo migrationInfo = flyway.info().current();
        assertEquals(MigrationType.BASELINE, migrationInfo.getType());
        assertEquals("99", migrationInfo.getVersion().toString());
    }

    /**
     * Override outOfOrderMultipleRankIncrease. Setting schema and table space in SQL.
     * 
     */
    @Override
    @Test
    public void outOfOrderMultipleRankIncrease() {
        flyway.setLocations(getMigrationDir());
        flyway.baseline();
        flyway.migrate();

        flyway.setLocations(getMigrationDir(), getBasedir() + "/outoforder");
        flyway.setOutOfOrder(true);
        flyway.migrate();

        MigrationInfo[] all = flyway.info().all();
        assertEquals(org.flywaydb.core.api.MigrationState.OUT_OF_ORDER, all[all.length - 1].getState());
    }

    /**
     * Override quote. Setting schema and table space in SQL.
     * 
     * @throws Exception
     */
    @Override
    @Test
    public void quote() throws Exception {
        flyway.setLocations(getQuoteLocation());
        flyway.baseline();
        flyway.migrate();
        assertEquals("0",
                jdbcTemplate.queryForString("SELECT COUNT(name) FROM " + dbSupport.quote(flyway.getSchemas()[0], "table")));
    }

    /**
     * Override quotesAroundTableName. Setting schema and table space in SQL.
     * 
     * @throws Exception
     */
    @Override
    @Test
    public void quotesAroundTableName() {
        flyway.setLocations(getQuoteLocation());
        flyway.baseline();
        flyway.migrate();
    }

    /**
     * Override repair. Setting schema and table space in SQL.
     * 
     * @throws Exception
     */
    @Override
    @Test
    public void repair() throws Exception {
        flyway.setLocations(getFutureFailedLocation());
        assertEquals(4, flyway.info().all().length);

        try {
            flyway.baseline();
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            // Expected
        }

        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());

        flyway.repair();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
    }

    /**
     * Override repairChecksum. Setting schema and table space in SQL.
     * 
     * @throws Exception
     */
    @Override
    @Test
    public void repairChecksum() {
        flyway.setLocations(getMigrationDir());
        flyway.baseline();
        Integer firstChecksum = flyway.info().pending()[0].getChecksum();
        Integer secondChecksum = flyway.info().pending()[1].getChecksum();

        assertNotEquals(firstChecksum, secondChecksum);

        flyway.migrate();

        if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
            assertEquals(firstChecksum, flyway.info().applied()[2].getChecksum());
        } else {
            assertEquals(firstChecksum, flyway.info().applied()[1].getChecksum());
        }

        flyway.setLocations(getCommentLocation());
        flyway.repair();

        if (flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
            assertEquals(secondChecksum, flyway.info().applied()[3].getChecksum());
        } else {
            assertEquals(secondChecksum, flyway.info().applied()[2].getChecksum());
        }
    }

    @Test
    public void routines() throws Exception {
        flyway.setLocations(getRoutineLocation());
        flyway.baseline();
        flyway.migrate();
    }

    @Override
    @Test
    public void schemaExists() throws SQLException {
        assertTrue(dbSupport.getSchema("AURINT").exists());
        assertFalse(dbSupport.getSchema("InVaLidScHeMa").exists());
    }

    /**
     * Override semicolonWithinStringLiteral. Setting schema and table space in SQL.
     * 
     * @throws Exception
     */
    @Override
    @Test
    public void semicolonWithinStringLiteral() throws Exception {
        flyway.setLocations(getSemiColonLocation());
        flyway.baseline();
        flyway.migrate();

        assertEquals("1.1", flyway.info().current().getVersion().toString());
        assertEquals("Populate table", flyway.info().current().getDescription());

        assertEquals("Semicolon+Linebreak;\nanother line",
                jdbcTemplate.queryForString("SELECT lastname FROM person WHERE lastname like '%line'"));
    }

    @Test
    public void sequence() throws Exception {
        flyway.setLocations(getSequenceLocation());
        flyway.baseline();
        flyway.migrate();

        MigrationVersion migrationVersion = flyway.info().current().getVersion();
        assertEquals("1.1", migrationVersion.toString());
        assertEquals("Sequence", flyway.info().current().getDescription());
        assertEquals(666, jdbcTemplate.queryForInt("SELECT NEXTVAL FOR AURINT.beast_seq FROM sysibm.sysdummy1"));
    }

    /**
     * Override schema test. DB2 on zOS does not support "Create schema"
     *
     * @throws Exception
     */
    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void setCurrentSchema() throws Exception {
        Schema schema = dbSupport.getSchema("current_schema_test");
        try {
            schema.create();

            flyway.setSchemas("current_schema_test");
            flyway.clean();

            flyway.setLocations("migration/current_schema");
            Map<String, String> placeholders = new HashMap<String, String>();
            placeholders.put("schema1", dbSupport.quote("current_schema_test"));
            flyway.setPlaceholders(placeholders);
            flyway.migrate();
        } finally {
            schema.drop();
        }
    }

    /**
     * Override subDir. Setting schema and table space in SQL.
     * 
     * @throws Exception
     */
    @Override
    @Test
    public void subDir() {
        flyway.setLocations(getBasedir() + "/subdir");
        flyway.baseline();
        assertEquals(3, flyway.migrate());
    }

    /**
     * Override tableExists. Schema not same as user in DB2.
     * 
     * @throws Exception
     */
    @Override
    @Test
    public void tableExists() throws Exception {
        flyway.baseline();
        assertTrue(dbSupport.getSchema("AURINT").getTable(flyway.getTable()).exists());
        assertTrue(dbSupport.getSchema(flyway.getSchemas()[0]).getTable(flyway.getTable()).exists());
    }

    /**
     * Override target. Setting schema and table space in SQL.
     * 
     * @throws Exception
     */
    @Override
    @Test
    public void target() throws Exception {
        flyway.setLocations(getMigrationDir());

        flyway.setTarget(MigrationVersion.fromVersion("1.2"));
        flyway.baseline();
        flyway.migrate();
        assertEquals("1.2", flyway.info().current().getVersion().toString());
        assertEquals("UpdateTable", flyway.info().current().getDescription());

        flyway.setTarget(MigrationVersion.fromVersion("1.0"));
        flyway.migrate();
        assertEquals("1.2", flyway.info().current().getVersion().toString());
        assertEquals("UpdateTable", flyway.info().current().getDescription());

        flyway.setTarget(MigrationVersion.LATEST);
        flyway.migrate();
        assertEquals("1.3", flyway.info().current().getVersion().toString());
        assertEquals("UpdateTable", flyway.info().current().getDescription());
    }

    @Test
    public void trigger() throws Exception {
        flyway.setLocations(getTriggerLocation());
        flyway.baseline();
        flyway.migrate();
    }

    @Test
    public void type() throws Exception {
        flyway.setLocations(getTypeLocation());
        flyway.baseline();
        flyway.migrate();
    }

    /**
     * Override upgradeMetadataTableTo40Format. Setting schema and table space in SQL.
     * 
     * @throws Exception
     */
    @Override
    @Test
    public void upgradeMetadataTableTo40Format() throws Exception {
        createFlyway3MetadataTable();
        jdbcTemplate.execute("CREATE TABLE test_user (\n" +
                "id INT NOT NULL,\n" +
                "name VARCHAR(25) NOT NULL,  -- this is a valid ' comment\n" +
                "PRIMARY KEY(name)  /* and so is this ! */\n" +
                ") IN AURINT.SPERS;");
        insertIntoFlyway3MetadataTable(jdbcTemplate, 1, 1, "0.1", "<< INIT >>", "INIT", "<< INIT >>", null, "flyway3", 0, true);
        insertIntoFlyway3MetadataTable(jdbcTemplate, 2, 2, "1", "First", "SQL", "V1__First.sql", 1234, "flyway3", 15, true);
        flyway.setLocations(getMigrationDir());
        assertEquals(3, flyway.migrate());
        flyway.validate();
        assertEquals(5, flyway.info().applied().length);
        assertEquals(801496293, flyway.info().applied()[1].getChecksum().intValue());
    }

    /**
     * Override validateClean. Setting schema and table space in SQL.
     * 
     * @throws Exception
     */
    @Override
    @Test
    public void validateClean() throws Exception {
        flyway.setLocations(getValidateLocation());
        flyway.baseline();
        flyway.migrate();

        assertEquals("1.1", flyway.info().current().getVersion().toString());

        flyway.setValidateOnMigrate(true);
        flyway.setCleanOnValidationError(true);
        flyway.setSqlMigrationPrefix("CheckValidate");

        flyway.validate();
        assertEquals(0, jdbcTemplate.queryForInt("SELECT COUNT(*) FROM sysibm.systables WHERE owner='AURINT'"));
    }

    @Test
    public void view() throws Exception {
        flyway.setLocations(getViewLocation());
        flyway.baseline();
        flyway.migrate();
    }
}
