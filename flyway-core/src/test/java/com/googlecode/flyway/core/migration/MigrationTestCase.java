/**
 * Copyright (C) 2010-2012 the original author or authors.
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
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.DbSupportFactory;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlMigrationResolver;
import com.googlecode.flyway.core.util.jdbc.JdbcTemplate;
import com.googlecode.flyway.core.validation.ValidationErrorMode;
import com.googlecode.flyway.core.validation.ValidationMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.List;
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

    private Connection connection;
    private DbSupport dbSupport;

    protected JdbcTemplate jdbcTemplate;
    protected Flyway flyway;

    @Before
    public void setUp() throws Exception {
        File customPropertiesFile = new File(System.getProperty("user.home") + "/flyway-mediumtests.properties");
        Properties customProperties = new Properties();
        if (customPropertiesFile.canRead()) {
            customProperties.load(new FileInputStream(customPropertiesFile));
        }
        DataSource migrationDataSource = createDataSource(customProperties);

        connection = migrationDataSource.getConnection();
        dbSupport = DbSupportFactory.createDbSupport(connection);
        jdbcTemplate = dbSupport.getJdbcTemplate();

        flyway = new Flyway();
        flyway.setDataSource(migrationDataSource);
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
        assertNull(flyway.info().current());
        assertEquals(0, flyway.info().all().length);

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
        int count = jdbcTemplate.queryForInt("select count(*) from my_custom_table");
        assertEquals(4, count);
    }

    /**
     * Compares the DB checksum to the classpath checksum of this migration.
     *
     * @param appliedMigration The migration to check.
     */
    private void assertChecksum(MetaDataTableRow appliedMigration) {
        SqlMigrationResolver sqlMigrationResolver = new SqlMigrationResolver(
                BASEDIR,
                PlaceholderReplacer.NO_PLACEHOLDERS,
                "UTF-8",
                "V",
                ".sql");
        List<ExecutableMigration> migrations = sqlMigrationResolver.resolveMigrations();
        for (ExecutableMigration migration : migrations) {
            if (migration.getInfo().getVersion().toString().equals(appliedMigration.getVersion().toString())) {
                assertEquals("Wrong checksum for " + appliedMigration.getScript(), migration.getInfo().getChecksum(), appliedMigration.getChecksum());
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
        flyway.setSqlMigrationSuffix("First.sql");
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
        flyway.setLocations("migration/failed");

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected
        }

        MetaDataTableRow migration = flyway.status();
        if (dbSupport.supportsDdlTransactions()) {
            assertNull(migration);
        } else {
            SchemaVersion version = migration.getVersion();
            assertEquals("1", version.toString());
            assertEquals("Should Fail", migration.getDescription());
            assertEquals(MigrationState.FAILED, migration.getState());
            assertEquals(1, flyway.history().size());
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
        } catch (MigrationException e) {
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
        assertTrue(dbSupport.tableExists(dbSupport.getCurrentSchema(), "SCHEMA_VERSION"));
    }

    /**
     * Check if meta table has no current migration (manually edited).
     */
    @Test(expected = FlywayException.class)
    public void checkForInvalidMetatable() throws Exception {
        flyway.setLocations(BASEDIR);
        flyway.migrate();

        jdbcTemplate.update("UPDATE schema_version SET current_version = " + dbSupport.getBooleanFalse()
                + " where current_version = " + dbSupport.getBooleanTrue());
        flyway.migrate();
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

        jdbcTemplate.update("DROP TABLE schema_version");
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
        String schema = dbSupport.getCurrentSchema();

        assertTrue(dbSupport.isSchemaEmpty(schema));

        flyway.setLocations(BASEDIR);
        flyway.migrate();

        assertFalse(dbSupport.isSchemaEmpty(schema));

        flyway.clean();

        assertTrue(dbSupport.isSchemaEmpty(schema));
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

        assertNull(flyway.status());

        flyway.setLocations("migration/multi");
        flyway.migrate();
        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("2.0", schemaVersion.toString());
        assertEquals("Add foreign key", flyway.status().getDescription());
        assertEquals(0, flyway.migrate());

        assertEquals(3, flyway.history().size());
        assertEquals(3, jdbcTemplate.queryForInt("select count(*) from flyway_1.schema_version"));

        assertEquals(2, jdbcTemplate.queryForInt("select count(*) from flyway_1.test_user1"));
        assertEquals(2, jdbcTemplate.queryForInt("select count(*) from flyway_2.test_user2"));
        assertEquals(2, jdbcTemplate.queryForInt("select count(*) from flyway_3.test_user3"));

        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void subDir() {
        flyway.setLocations("migration/subdir");
        assertEquals(3, flyway.migrate());
    }
}
