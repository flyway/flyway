/**
 * Copyright 2010-2015 Axel Fontaine
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
package org.flywaydb.core.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.*;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.dbsupport.*;
import org.flywaydb.core.internal.resolver.sql.SqlMigrationResolver;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
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
    protected static final String MIGRATIONDIR = "migration";
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
        dbSupport = DbSupportFactory.createDbSupport(connection, false);
        jdbcTemplate = dbSupport.getJdbcTemplate();

		configureFlyway();
		flyway.clean();
	}

	protected void configureFlyway() {
        flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setValidateOnMigrate(true);
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
        flyway.setLocations(getMigrationDir() + "/future_failed");
        assertEquals(4, flyway.info().all().length);

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected
        }

        if (dbSupport.supportsDdlTransactions()) {
            assertEquals("2.0", flyway.info().current().getVersion().toString());
            assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
        } else {
            assertEquals("3", flyway.info().current().getVersion().toString());
            assertEquals(MigrationState.FAILED, flyway.info().current().getState());
        }

        flyway.repair();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());
    }

    @Test
    public void repairChecksum() {
        flyway.setLocations(getMigrationDir() + "/comment");
        Integer commentChecksum = flyway.info().pending()[0].getChecksum();

        flyway.setLocations(getQuoteLocation());
        Integer quoteChecksum = flyway.info().pending()[0].getChecksum();

        assertNotEquals(commentChecksum, quoteChecksum);

        flyway.migrate();

        if(flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
            assertEquals(quoteChecksum, flyway.info().applied()[1].getChecksum());
        } else {
            assertEquals(quoteChecksum, flyway.info().applied()[0].getChecksum());
        }

        flyway.setLocations(getMigrationDir() + "/comment");
        flyway.repair();

        if(flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
            assertEquals(commentChecksum, flyway.info().applied()[1].getChecksum());
        } else {
            assertEquals(commentChecksum, flyway.info().applied()[0].getChecksum());
        }
    }

    /**
     * @return The location containing the migrations for the quote test.
     */
    protected abstract String getQuoteLocation();

    protected String getMigrationDir() { return MIGRATIONDIR; }
    protected String getBaseDir() { return BASEDIR; }
    

    @Test
    public void migrate() throws Exception {
        flyway.setLocations(getBaseDir());
        flyway.migrate();
        MigrationVersion version = flyway.info().current().getVersion();
        assertEquals("2.0", version.toString());
        assertEquals(0, flyway.migrate());

        // We should have 5 rows if we have a schema creation marker as the first entry, 4 otherwise
        if(flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
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
    public void target() throws Exception {
        flyway.setLocations(getBaseDir());

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
    public void customTableName() throws Exception {
        flyway.setLocations(getBaseDir());
        flyway.setTable("my_custom_table");
        flyway.migrate();
        int count = jdbcTemplate.queryForInt("select count(*) from " + dbSupport.quote("my_custom_table"));

        // Same as 'migrate()', count is 5 when we have a schema creation marker
        if(flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
            assertEquals(5, count);
        } else {
            assertEquals(4, count);
        }
    }

    /**
     * Compares the DB checksum to the classpath checksum of this migration.
     *
     * @param migrationInfo The migration to check.
     */
    private void assertChecksum(MigrationInfo migrationInfo) {
        SqlMigrationResolver sqlMigrationResolver = new SqlMigrationResolver(
                dbSupport, Thread.currentThread().getContextClassLoader(),
                new Location(getBaseDir()),
                PlaceholderReplacer.NO_PLACEHOLDERS,
                "UTF-8",
                "V", "__", ".sql");
        List<ResolvedMigration> migrations = sqlMigrationResolver.resolveMigrations();
        for (ResolvedMigration migration : migrations) {
            if (migration.getVersion().toString().equals(migrationInfo.getVersion().toString())) {
                assertEquals("Wrong checksum for " + migrationInfo.getScript(), migration.getChecksum(), migrationInfo.getChecksum());
            }
        }
    }

    @Test(expected = FlywayException.class)
    public void validateFails() throws Exception {
        flyway.setLocations(getBaseDir());
        flyway.setSqlMigrationSuffix("First.sql");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());

        flyway.setSqlMigrationPrefix("CheckValidate");
        flyway.validate();
    }

    @Test(expected = FlywayException.class)
    public void validateMoreAppliedThanAvailable() throws Exception {
        flyway.setLocations(getBaseDir());
        flyway.migrate();

        assertEquals("2.0", flyway.info().current().getVersion().toString());

        flyway.setLocations(getMigrationDir() + "/validate");
        flyway.validate();
    }

    @Test
    public void validateClean() throws Exception {
        flyway.setLocations(getMigrationDir() + "/validate");
        flyway.migrate();

        assertEquals("1", flyway.info().current().getVersion().toString());

        flyway.setValidateOnMigrate(true);
        flyway.setCleanOnValidationError(true);
        flyway.setSqlMigrationPrefix("CheckValidate");
        assertEquals(1, flyway.migrate());
    }

    @Test
    public void failedMigration() throws Exception {
        String tableName = "before_the_error";

        flyway.setLocations(getMigrationDir() + "/failed");
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

            // With schema markers, we'll have 2 applied
            if(flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
                assertEquals(2, flyway.info().applied().length);
            } else {
                assertEquals(1, flyway.info().applied().length);
            }

        }
    }

    @Test
    public void futureFailedMigration() throws Exception {
        flyway.setValidateOnMigrate(false);
        flyway.setLocations(getMigrationDir() + "/future_failed");

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected
        }

        flyway.setLocations(getBaseDir());
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
        flyway.setValidateOnMigrate(false);
        flyway.setLocations(getMigrationDir() + "/future_failed");

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected
        }

        flyway.setIgnoreFailedFutureMigration(true);
        flyway.setLocations(getBaseDir());
        flyway.migrate();
    }

    @Test
    public void futureFailedMigrationIgnoreAvailableMigrations() throws Exception {
        flyway.setValidateOnMigrate(false);
        flyway.setLocations(getMigrationDir() + "/future_failed");

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
                assertTrue(e.getMessage().contains("THIS IS NOT VALID SQL"));
            } else {
                assertTrue(e.getMessage().contains("contains a failed migration"));
            }
        }
    }

    @Test
    public void tableExists() throws Exception {
        flyway.init();
		assertTrue(dbSupport.getCurrentSchema().getTable(flyway.getTable()).exists());
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
        flyway.setLocations(getBaseDir());
        flyway.setTarget(MigrationVersion.fromVersion("1.1"));
        flyway.migrate();
        assertEquals("1.1", flyway.info().current().getVersion().toString());

        jdbcTemplate.update("DROP TABLE " + dbSupport.quote(flyway.getTable()));
        flyway.setInitVersion(MigrationVersion.fromVersion("1.1"));
        flyway.setInitDescription("initial version 1.1");
        flyway.init();

        flyway.setTarget(MigrationVersion.LATEST);
        flyway.migrate();
        assertEquals("2.0", flyway.info().current().getVersion().toString());
        flyway.validate();
    }

    @Test
    public void isSchemaEmpty() throws Exception {
        Schema schema = dbSupport.getCurrentSchema();

        assertTrue(schema.empty());

        flyway.setLocations(getBaseDir());
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

        flyway.setLocations(getBaseDir());
        flyway.migrate();
    }

    @Test
    public void nonEmptySchemaWithInit() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL,\n" +
                "  PRIMARY KEY(name))");

        flyway.setLocations(getBaseDir());
        flyway.setInitVersion("0");
        flyway.init();
        flyway.migrate();
    }

    @Test
    public void nonEmptySchemaWithInitOnMigrate() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL,\n" +
                "  PRIMARY KEY(name))");

        flyway.setLocations(getBaseDir());
        flyway.setInitVersion("0");
        flyway.setInitOnMigrate(true);
        flyway.migrate();
        MigrationInfo[] migrationInfos = flyway.info().all();

        assertEquals(5, migrationInfos.length);

        assertEquals(MigrationType.BASELINE, migrationInfos[0].getType());
        assertEquals("0", migrationInfos[0].getVersion().toString());

        assertEquals("2.0", flyway.info().current().getVersion().toString());
    }

    @Test
    public void nonEmptySchemaWithInitOnMigrateHighVersion() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL,\n" +
                "  PRIMARY KEY(name))");

        flyway.setLocations(getBaseDir());
        flyway.setInitOnMigrate(true);
        flyway.setInitVersion(MigrationVersion.fromVersion("99"));
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

    @Test
    public void semicolonWithinStringLiteral() throws Exception {
        flyway.setLocations(getMigrationDir() + "/semicolon");
        flyway.migrate();

        assertEquals("1.1", flyway.info().current().getVersion().toString());
        assertEquals("Populate table", flyway.info().current().getDescription());

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

        flyway.setLocations(getMigrationDir() + "/multi");
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

            flyway.setLocations(getMigrationDir() + "/current_schema");
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
        flyway.setLocations(getMigrationDir() + "/subdir");
        assertEquals(3, flyway.migrate());
    }

    @Test
    public void comment() {
        flyway.setLocations(getMigrationDir() + "/comment");
        assertEquals(1, flyway.migrate());
    }

    @Test
    public void outOfOrderMultipleRankIncrease() {
        flyway.setLocations(getBaseDir());
        flyway.migrate();

        flyway.setLocations(getBaseDir(), getMigrationDir() + "/outoforder");
        flyway.setOutOfOrder(true);
        flyway.migrate();

        // With schema markers, we'll have 4 migrations instead of 3
        if(flyway.info().applied()[0].getType() == MigrationType.SCHEMA) {
            assertEquals(org.flywaydb.core.api.MigrationState.OUT_OF_ORDER, flyway.info().all()[3].getState());
        }
        else {
            assertEquals(org.flywaydb.core.api.MigrationState.OUT_OF_ORDER, flyway.info().all()[2].getState());
        }
    }

    @Test
    public void schemaExists() throws SQLException {
        assertTrue(dbSupport.getCurrentSchema().exists());
        assertFalse(dbSupport.getSchema("InVaLidScHeMa").exists());
    }
}
