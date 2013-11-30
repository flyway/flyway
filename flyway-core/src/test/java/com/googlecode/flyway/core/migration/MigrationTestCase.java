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
package com.googlecode.flyway.core.migration;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.api.*;
import com.googlecode.flyway.core.api.MigrationState;
import com.googlecode.flyway.core.dbsupport.*;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.metadatatable.MetaDataTableTo202FormatUpgrader;
import com.googlecode.flyway.core.metadatatable.MetaDataTableTo20FormatUpgrader;
import com.googlecode.flyway.core.metadatatable.MetaDataTableTo21FormatUpgrader;
import com.googlecode.flyway.core.resolver.CompositeMigrationResolver;
import com.googlecode.flyway.core.resolver.ResolvedMigration;
import com.googlecode.flyway.core.resolver.sql.SqlMigrationResolver;
import com.googlecode.flyway.core.util.*;
import com.googlecode.flyway.core.validation.ValidationErrorMode;
import com.googlecode.flyway.core.validation.ValidationMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Test to demonstrate the migration functionality.
 */
@SuppressWarnings({"JavaDoc"})
public abstract class MigrationTestCase {

    /**
     * The base directory for the regular test migrations.
     */
    protected static final String BASEDIR = "migration/sql";

    protected DataSource dataSource;
    private Connection connection;
    protected DbSupport dbSupport;

    protected JdbcTemplate jdbcTemplate;
    protected Flyway flyway;

    @Before
    public void setUp() throws Exception {
        File customPropertiesFile = new File(System.getProperty("user.home") + "/flyway-mediumtests.properties");
        Properties customProperties = new Properties();
        if (customPropertiesFile.canRead()) {
            customProperties.load(new FileInputStream(customPropertiesFile));
        }
        dataSource = createDataSource(customProperties);

        connection = dataSource.getConnection();
        dbSupport = DbSupportFactory.createDbSupport(connection);
        jdbcTemplate = dbSupport.getJdbcTemplate();

        flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setValidationMode(ValidationMode.ALL);
        flyway.clean();
    }

    /**
     * Creates the datasource for this testcase based on these optional custom properties from the user home.
     *
     * @param customProperties The optional custom properties.
     * @return The new datasource.
     */
    protected abstract DataSource createDataSource(Properties customProperties) throws Exception;

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void repair() throws Exception {
        flyway.setLocations("migration/future_failed");
        assertEquals(4, flyway.info().all().length);

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected
        }

        if (dbSupport.supportsDdlTransactions()) {
            assertEquals("2.0", flyway.info().current().getVersion().toString());
            assertEquals(com.googlecode.flyway.core.api.MigrationState.SUCCESS, flyway.info().current().getState());
        } else {
            assertEquals("3", flyway.info().current().getVersion().toString());
            assertEquals(com.googlecode.flyway.core.api.MigrationState.FAILED, flyway.info().current().getState());
        }

        flyway.repair();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(com.googlecode.flyway.core.api.MigrationState.SUCCESS, flyway.info().current().getState());
    }

    /**
     * @return The location containing the migrations for the quote test.
     */
    protected abstract String getQuoteLocation();

    @Test
    public void migrate() throws Exception {
        flyway.setLocations(BASEDIR);
        flyway.migrate();
        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("2.0", schemaVersion.toString());
        assertEquals("Add foreign key and super mega humongous padding to exceed the maximum column length in the metad...", flyway.status().getDescription());
        assertEquals(0, flyway.migrate());
        assertEquals(4, flyway.history().size());

        for (MetaDataTableRow metaDataTableRow : flyway.history()) {
            assertChecksum(metaDataTableRow);
        }

        assertEquals(2, jdbcTemplate.queryForInt("select count(*) from all_misters"));
    }

    @Test
    public void target() throws Exception {
        flyway.setLocations(BASEDIR);

        flyway.setTarget(new SchemaVersion("1.2"));
        flyway.migrate();
        assertEquals("1.2", flyway.status().getVersion().toString());
        assertEquals("Populate table", flyway.status().getDescription());

        flyway.setTarget(new MigrationVersion("1.0"));
        flyway.migrate();
        assertEquals("1.2", flyway.status().getVersion().toString());
        assertEquals("Populate table", flyway.status().getDescription());

        flyway.setTarget(MigrationVersion.LATEST);
        flyway.migrate();
        assertEquals("2.0", flyway.status().getVersion().toString());
    }

    @Test
    public void customTableName() throws Exception {
        flyway.setLocations(BASEDIR);
        flyway.setTable("my_custom_table");
        flyway.migrate();
        int count = jdbcTemplate.queryForInt("select count(*) from " + dbSupport.quote("my_custom_table"));
        assertEquals(4, count);
    }

    /**
     * Compares the DB checksum to the classpath checksum of this migration.
     *
     * @param appliedMigration The migration to check.
     */
    private void assertChecksum(MetaDataTableRow appliedMigration) {
        SqlMigrationResolver sqlMigrationResolver = new SqlMigrationResolver(
                dbSupport,
                new Location(BASEDIR),
                PlaceholderReplacer.NO_PLACEHOLDERS,
                "UTF-8",
                "V",
                ".sql");
        List<ResolvedMigration> migrations = sqlMigrationResolver.resolveMigrations();
        for (ResolvedMigration migration : migrations) {
            if (migration.getVersion().toString().equals(appliedMigration.getVersion().toString())) {
                assertEquals("Wrong checksum for " + appliedMigration.getScript(), migration.getChecksum(), appliedMigration.getChecksum());
            }
        }
    }

    @Test(expected = FlywayException.class)
    public void validateFails() throws Exception {
        flyway.setLocations(BASEDIR);
        flyway.setSqlMigrationSuffix("First.sql");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1", schemaVersion.toString());

        flyway.setSqlMigrationPrefix("CheckValidate");
        flyway.validate();
    }

    @Test(expected = FlywayException.class)
    public void validateMoreAppliedThanAvailable() throws Exception {
        flyway.setLocations(BASEDIR);
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("2.0", schemaVersion.toString());

        flyway.setLocations("migration/validate");
        flyway.validate();
    }

    @Test
    public void validateClean() throws Exception {
        flyway.setLocations("migration/validate");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1", schemaVersion.toString());

        flyway.setValidationMode(ValidationMode.ALL);
        flyway.setValidationErrorMode(ValidationErrorMode.CLEAN);
        flyway.setSqlMigrationPrefix("CheckValidate");
        assertEquals(1, flyway.migrate());
    }

    @Test
    public void failedMigration() throws Exception {
        String tableName = "before_the_error";

        flyway.setLocations("migration/failed");
        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("tableName", dbSupport.quote(tableName));
        flyway.setPlaceholders(placeholders);

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            // root cause of exception must be defined, and it should be FlywaySqlScriptException
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof FlywaySqlScriptException);
            // and make sure the failed statement was properly recorded
            FlywaySqlScriptException cause = (FlywaySqlScriptException) e.getCause();
            assertEquals(21, cause.getLineNumber());
            assertEquals("THIS IS NOT VALID SQL", cause.getStatement());
        }

        MigrationInfo migration = flyway.info().current();
        assertEquals(
                dbSupport.supportsDdlTransactions(),
                !dbSupport.getCurrentSchema().getTable(tableName).exists());
        if (dbSupport.supportsDdlTransactions()) {
            assertNull(migration);
        } else {
            MigrationVersion version = migration.getVersion();
            assertEquals("1", version.toString());
            assertEquals("Should Fail", migration.getDescription());
            assertEquals(MigrationState.FAILED, migration.getState());
            assertEquals(1, flyway.info().applied().length);
        }
    }

    @Test
    public void futureFailedMigration() throws Exception {
        flyway.setValidationMode(ValidationMode.NONE);
        flyway.setLocations("migration/future_failed");

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected
        }

        flyway.setLocations(BASEDIR);
        if (dbSupport.supportsDdlTransactions()) {
            flyway.migrate();
        } else {
            try {
                flyway.migrate();
                fail();
            } catch (FlywayException e) {
                //Expected
            }
        }
    }

    @Test
    public void futureFailedMigrationIgnore() throws Exception {
        flyway.setValidationMode(ValidationMode.NONE);
        flyway.setLocations("migration/future_failed");

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected
        }

        flyway.setIgnoreFailedFutureMigration(true);
        flyway.setLocations(BASEDIR);
        flyway.migrate();
    }

    @Test
    public void futureFailedMigrationIgnoreAvailableMigrations() throws Exception {
        flyway.setValidationMode(ValidationMode.NONE);
        flyway.setLocations("migration/future_failed");

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected
        }

        flyway.setIgnoreFailedFutureMigration(true);
        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            if (dbSupport.supportsDdlTransactions()) {
                assertTrue(e.getMessage().contains("rolled back"));
            } else {
                assertTrue(e.getMessage().contains("roll back"));
            }
        }
    }

    @Test
    public void tableExists() throws Exception {
        flyway.init();
        assertTrue(dbSupport.getCurrentSchema().getTable("schema_version").exists());
        assertTrue(dbSupport.getSchema(flyway.getSchemas()[0]).getTable(flyway.getTable()).exists());
    }

    @Test
    public void columnExists() throws Exception {
        flyway.init();
        assertTrue(dbSupport.getSchema(flyway.getSchemas()[0]).getTable(flyway.getTable()).hasColumn("version_rank"));
        assertFalse(dbSupport.getSchema(flyway.getSchemas()[0]).getTable(flyway.getTable()).hasColumn("dummy"));
    }

    @Test
    public void quote() throws Exception {
        flyway.setLocations(getQuoteLocation());
        flyway.migrate();
        assertEquals("0",
                jdbcTemplate.queryForString("SELECT COUNT(name) FROM " + dbSupport.quote(flyway.getSchemas()[0], "table")));
    }

    /**
     * Check validation with INIT row.
     */
    @Test
    public void checkValidationWithInitRow() throws Exception {
        flyway.setLocations(BASEDIR);
        flyway.setTarget(new MigrationVersion("1.1"));
        flyway.migrate();
        assertEquals("1.1", flyway.status().getVersion().toString());

        jdbcTemplate.update("DROP TABLE " + dbSupport.quote(flyway.getTable()));
        flyway.setInitialVersion(new SchemaVersion("1.1"));
        flyway.setInitialDescription("initial version 1.1");
        flyway.init();

        flyway.setTarget(MigrationVersion.LATEST);
        flyway.migrate();
        assertEquals("2.0", flyway.status().getVersion().toString());
        flyway.validate();
    }

    @Test
    public void isSchemaEmpty() throws Exception {
        Schema schema = dbSupport.getCurrentSchema();

        assertTrue(schema.empty());

        flyway.setLocations(BASEDIR);
        flyway.migrate();

        assertFalse(schema.empty());

        flyway.clean();

        assertTrue(schema.empty());
    }

    @Test(expected = FlywayException.class)
    public void nonEmptySchema() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL,\n" +
                "  PRIMARY KEY(name))");

        flyway.setLocations(BASEDIR);
        flyway.migrate();
    }

    @Test
    public void nonEmptySchemaWithInit() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL,\n" +
                "  PRIMARY KEY(name))");

        flyway.setLocations(BASEDIR);
        flyway.setInitVersion("0");
        flyway.init();
        flyway.migrate();
    }

    @Test
    public void nonEmptySchemaWithDisableInitCheck() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL,\n" +
                "  PRIMARY KEY(name))");

        flyway.setLocations(BASEDIR);
        flyway.setDisableInitCheck(true);
        flyway.migrate();
    }

    @Test
    public void nonEmptySchemaWithInitOnMigrate() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL,\n" +
                "  PRIMARY KEY(name))");

        flyway.setLocations(BASEDIR);
        flyway.setInitVersion("0");
        flyway.setInitOnMigrate(true);
        flyway.migrate();
        MigrationInfo[] migrationInfos = flyway.info().all();

        assertEquals(5, migrationInfos.length);

        assertEquals(com.googlecode.flyway.core.api.MigrationType.INIT, migrationInfos[0].getType());
        assertEquals("0", migrationInfos[0].getVersion().toString());

        assertEquals("2.0", flyway.info().current().getVersion().toString());
    }

    @Test
    public void nonEmptySchemaWithInitOnMigrateHighVersion() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL,\n" +
                "  PRIMARY KEY(name))");

        flyway.setLocations(BASEDIR);
        flyway.setInitOnMigrate(true);
        flyway.setInitialVersion(new MigrationVersion("99"));
        flyway.migrate();
        MigrationInfo[] migrationInfos = flyway.info().all();

        assertEquals(5, migrationInfos.length);

        assertEquals(MigrationType.SQL, migrationInfos[0].getType());
        assertEquals("1", migrationInfos[0].getVersion().toString());
        assertEquals(com.googlecode.flyway.core.api.MigrationState.PREINIT, migrationInfos[0].getState());

        MigrationInfo migrationInfo = flyway.info().current();
        assertEquals(MigrationType.INIT, migrationInfo.getType());
        assertEquals("99", migrationInfo.getVersion().toString());
    }

    @Test
    public void semicolonWithinStringLiteral() throws Exception {
        flyway.setLocations("migration/semicolon");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1.1", schemaVersion.toString());
        assertEquals("Populate table", flyway.status().getDescription());

        assertEquals("Mr. Semicolon+Linebreak;\nanother line",
                jdbcTemplate.queryForString("select name from test_user where name like '%line'"));
    }

    @Test
    public void quotesAroundTableName() {
        flyway.setLocations(getQuoteLocation());
        flyway.migrate();

        // Clean script must also be able to properly deal with these reserved keywords in table names.
        flyway.clean();
    }

    @Test
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
        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("2.0", schemaVersion.toString());
        assertEquals("Add foreign key", flyway.status().getDescription());
        assertEquals(0, flyway.migrate());

        assertEquals(4, flyway.info().applied().length);
        assertEquals(2, jdbcTemplate.queryForInt("select count(*) from " + dbSupport.quote("flyway_1") + ".test_user1"));
        assertEquals(2, jdbcTemplate.queryForInt("select count(*) from " + dbSupport.quote("flyway_2") + ".test_user2"));
        assertEquals(2, jdbcTemplate.queryForInt("select count(*) from " + dbSupport.quote("flyway_3") + ".test_user3"));

        flyway.clean();
    }

    @Test
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

    @Test
    public void subDir() {
        flyway.setLocations("migration/subdir");
        assertEquals(3, flyway.migrate());
    }

    @Test
    public void comment() {
        flyway.setLocations("migration/comment");
        assertEquals(1, flyway.migrate());
    }

    @Test
    public void outOfOrderMultipleRankIncrease() {
        flyway.setLocations("migration/sql");
        flyway.migrate();

        flyway.setLocations("migration/sql", "migration/outoforder");
        flyway.setOutOfOrder(true);
        flyway.migrate();

        assertEquals(com.googlecode.flyway.core.api.MigrationState.OUT_OF_ORDER, flyway.info().all()[2].getState());
    }

    @Test
    public void format20upgrade() throws Exception {
        createMetaDataTableIn17Format();
        upgradeMetaDataTableTo20Format();
        assertTrue(dbSupport.getSchema(flyway.getSchemas()[0]).getTable(flyway.getTable()).hasPrimaryKey());
    }

    @Test
    public void format202upgrade() throws Exception {
        createMetaDataTableIn17Format();
        upgradeMetaDataTableTo20Format();
        upgradeMetaDataTableTo202Format();
    }

    @Test
    public void format21upgrade() throws Exception {
        createMetaDataTableIn17Format();
        upgradeMetaDataTableTo20Format();
        upgradeMetaDataTableTo202Format();
        upgradeMetaDataTableTo21Format();
    }

    @Test
    public void format20upgradeMixedCase() throws Exception {
        flyway.setTable("MiXeD_CaSe");
        createMetaDataTableIn17Format();
        upgradeMetaDataTableTo20Format();
    }

    @Test
    public void format20upgradeUpperCase() throws Exception {
        flyway.setTable("UPPER_CASE");
        createMetaDataTableIn17Format();
        upgradeMetaDataTableTo20Format();
    }

    @Test(expected = FlywayException.class)
    public void format20upgradeEmptyDescription() throws Exception {
        createMetaDataTableIn17Format();
        insert17Row("1", null, "SQL", "V1__First.sql", 1234, 666, "SUCCESS");
        upgradeMetaDataTableTo20Format();
    }

    @Test
    public void format20upgradeCheckRank() throws Exception {
        createMetaDataTableIn17Format();
        insert17Row("1.1", "View", "SQL", "V1_1__View.sql", Integer.MAX_VALUE, 666, "SUCCESS");
        insert17Row("1", "First", "SQL", "V1__First.sql", Integer.MIN_VALUE, 666, "SUCCESS");
        insert17Row("3", "Spring", "JAVA", "V3__Spring", null, 55, "SUCCESS");
        insert17Row("4", "Jdbc", "JDBC", "V4__Jdbc", null, 55, "SUCCESS");
        upgradeMetaDataTableTo20Format();
        assertIntColumnValue("1", "version_rank", 1);
        assertIntColumnValue("1", "installed_rank", 1);
        assertIntColumnValue("1.1", "version_rank", 2);
        assertIntColumnValue("1.1", "installed_rank", 2);
        assertIntColumnValue("3", "version_rank", 3);
        assertIntColumnValue("3", "installed_rank", 3);
        assertIntColumnValue("4", "version_rank", 4);
        assertIntColumnValue("4", "installed_rank", 4);
        assertStringColumnValue("3", "type", "SPRING_JDBC");
    }

    @Test
    public void formatUpgradeOnMigrate() throws Exception {
        createMetaDataTableIn17Format();
        insert17Row("1", "First", "SQL", "V1__First.sql", Integer.MIN_VALUE, 666, "SUCCESS");
        jdbcTemplate.execute("CREATE TABLE test_user (id INT NOT NULL,name VARCHAR(25) NOT NULL,PRIMARY KEY(name))");
        flyway.setLocations(BASEDIR);
        flyway.migrate();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
    }

    /**
     * Creates a metadata table in Flyway 1.7 format.
     */
    private void createMetaDataTableIn17Format() throws SQLException {
        Resource resource = new ClassPathResource(dbSupport.getScriptLocation() + "createMetaDataTable17.sql");
        String source = resource.loadAsString("UTF-8");

        Map<String, String> placeholders = new HashMap<String, String>();
        placeholders.put("schema", dbSupport.getCurrentSchema().getName());
        placeholders.put("table", flyway.getTable());
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, "${", "}");

        SqlScript sqlScript = new SqlScript(placeholderReplacer.replacePlaceholders(source), dbSupport);
        sqlScript.execute(jdbcTemplate);
    }

    /**
     * Inserts a row in the old metadata table in 1.7 format.
     */
    private void insert17Row(String version, String description, String migrationType, String script, Integer checksum, int executionTime, String state) throws SQLException {
        jdbcTemplate.update("INSERT INTO " + dbSupport.getCurrentSchema() + "." + flyway.getTable()
                + " (version, description, type, script, checksum, installed_by, execution_time, state, current_version)"
                + " VALUES (?, ?, ?, ?, ?, " + dbSupport.getCurrentUserFunction() + ", ?, ?, "
                + dbSupport.getBooleanTrue() + ")",
                version, description, migrationType, script, checksum, executionTime, state);
    }

    /**
     * Upgrade a Flyway 1.7 format metadata table to the Flyway 2.0 format.
     */
    private void upgradeMetaDataTableTo20Format() throws Exception {
        CompositeMigrationResolver migrationResolver = new CompositeMigrationResolver(dbSupport, new Locations(BASEDIR), "UTF-8", "V", ".sql", new HashMap<String, String>(), "${", "}");

        MetaDataTableTo20FormatUpgrader upgrader = new MetaDataTableTo20FormatUpgrader(dbSupport, dbSupport.getCurrentSchema().getTable(flyway.getTable()), migrationResolver);
        upgrader.upgrade();
    }

    /**
     * Upgrade a Flyway 2.0 format metadata table to the Flyway 2.0.2 format.
     */
    private void upgradeMetaDataTableTo202Format() throws Exception {
        new MetaDataTableTo202FormatUpgrader(dbSupport, dbSupport.getCurrentSchema().getTable(flyway.getTable())).upgrade();
    }

    /**
     * Upgrade a Flyway 2.0 format metadata table to the Flyway 2.1 format.
     */
    private void upgradeMetaDataTableTo21Format() throws Exception {
        new MetaDataTableTo21FormatUpgrader(dbSupport, dbSupport.getCurrentSchema().getTable(flyway.getTable())).upgrade();
    }

    /**
     * Checks the value contained in this int column in the metadata table against this expected value.
     *
     * @param version  The migration version to check.
     * @param column   The column to check.
     * @param expected The expected value.
     */
    private void assertIntColumnValue(String version, String column, int expected) throws Exception {
        int actual = jdbcTemplate.queryForInt("SELECT " + dbSupport.quote(column)
                + " FROM " + dbSupport.getCurrentSchema() + "." + dbSupport.quote(flyway.getTable())
                + " WHERE " + dbSupport.quote("version") + " = ?", version);
        assertEquals("Wrong value for column: " + column, expected, actual);
    }

    /**
     * Checks the value contained in this string column in the metadata table against this expected value.
     *
     * @param version  The migration version to check.
     * @param column   The column to check.
     * @param expected The expected value.
     */
    private void assertStringColumnValue(String version, String column, String expected) throws Exception {
        String actual = jdbcTemplate.queryForString("SELECT " + dbSupport.quote(column)
                + " FROM " + dbSupport.getCurrentSchema() + "." + dbSupport.quote(flyway.getTable())
                + " WHERE " + dbSupport.quote("version") + " = ?", version);
        assertEquals("Wrong value for column: " + column, expected, actual);
    }

    @Test
    public void schemaExists() throws SQLException {
        assertTrue(dbSupport.getCurrentSchema().exists());
        assertFalse(dbSupport.getSchema("InVaLidScHeMa").exists());
    }
}
