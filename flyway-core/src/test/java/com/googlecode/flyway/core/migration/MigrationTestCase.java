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
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.DbSupportFactory;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlMigration;
import com.googlecode.flyway.core.util.jdbc.JdbcTemplate;
import com.googlecode.flyway.core.validation.ValidationErrorMode;
import com.googlecode.flyway.core.validation.ValidationMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test to demonstrate the migration functionality.
 */
@SuppressWarnings({"JavaDoc"})
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class MigrationTestCase {
    /**
     * The base directory for the regular test migrations.
     */
    protected static final String BASEDIR = "migration/sql";

    /**
     * The datasource to use for single-threaded migration tests.
     */
    @Autowired
    @Qualifier("migrationDataSource")
    private DataSource migrationDataSource;

    private DbSupport dbSupport;

    protected JdbcTemplate jdbcTemplate;

    protected Flyway flyway;

    private Connection connection;

    @Before
    public void setUp() throws Exception {
        connection = migrationDataSource.getConnection();

        dbSupport = DbSupportFactory.createDbSupport(connection);

        jdbcTemplate = dbSupport.getJdbcTemplate();

        flyway = new Flyway();
        flyway.setDataSource(migrationDataSource);
        flyway.setValidationMode(ValidationMode.ALL);
        flyway.clean();
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    /**
     * @return The directory containing the migrations for the quote test.
     */
    protected abstract String getQuoteBaseDir();

    @Test
    public void migrate() throws Exception {
        flyway.setBaseDir(BASEDIR);
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
        flyway.setBaseDir(BASEDIR);

        flyway.setTarget(new SchemaVersion("1.2"));
        flyway.migrate();
        assertEquals("1.2", flyway.status().getVersion().toString());
        assertEquals("Populate table", flyway.status().getDescription());

        flyway.setTarget(new SchemaVersion("1.0"));
        flyway.migrate();
        assertEquals("1.2", flyway.status().getVersion().toString());
        assertEquals("Populate table", flyway.status().getDescription());

        flyway.setTarget(SchemaVersion.LATEST);
        flyway.migrate();
        assertEquals("2.0", flyway.status().getVersion().toString());
    }

    @Test
    public void customTableName() throws Exception {
        flyway.setBaseDir(BASEDIR);
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
        ClassPathResource resource = new ClassPathResource(BASEDIR + "/" + appliedMigration.getScript());
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(new HashMap<String, String>(), "", "");
        Migration sqlMigration = new SqlMigration(resource, placeholderReplacer, "UTF-8", "1", appliedMigration.getScript());
        assertEquals("Wrong checksum for " + appliedMigration.getScript(), sqlMigration.getChecksum(), appliedMigration.getChecksum());
    }

    @Test(expected = FlywayException.class)
    public void validateFails() throws Exception {
        flyway.setBaseDir(BASEDIR);
        flyway.setSqlMigrationSuffix("First.sql");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1", schemaVersion.toString());

        flyway.setSqlMigrationPrefix("CheckValidate");
        flyway.validate();
    }

    @Test(expected = FlywayException.class)
    public void validateMoreAppliedThanAvailable() throws Exception {
        flyway.setBaseDir(BASEDIR);
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("2.0", schemaVersion.toString());

        flyway.setBaseDir("migration/validate");
        flyway.validate();
    }

    @Test
    public void validateClean() throws Exception {
        flyway.setBaseDir("migration/validate");
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
        flyway.setBaseDir("migration/failed");

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
        flyway.setBaseDir("migration/future_failed");

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected
        }

        flyway.setBaseDir(BASEDIR);
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
        flyway.setBaseDir("migration/future_failed");

        try {
            flyway.migrate();
            fail();
        } catch (FlywayException e) {
            //Expected
        }

        flyway.setIgnoreFailedFutureMigration(true);
        flyway.setBaseDir(BASEDIR);
        flyway.migrate();
    }

    @Test
    public void futureFailedMigrationIgnoreAvailableMigrations() throws Exception {
        flyway.setValidationMode(ValidationMode.NONE);
        flyway.setBaseDir("migration/future_failed");

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
        flyway.setBaseDir(BASEDIR);
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
        flyway.setBaseDir(BASEDIR);
        flyway.setTarget(new SchemaVersion("1.1"));
        flyway.migrate();
        assertEquals("1.1", flyway.status().getVersion().toString());

        jdbcTemplate.update("DROP TABLE schema_version");
        flyway.setInitialVersion(new SchemaVersion("1.1"));
        flyway.setInitialDescription("initial version 1.1");
        flyway.init();

        flyway.setTarget(SchemaVersion.LATEST);
        flyway.migrate();
        assertEquals("2.0", flyway.status().getVersion().toString());
        flyway.validate();
    }

    @Test
    public void isSchemaEmpty() throws Exception {
        String schema = dbSupport.getCurrentSchema();

        assertTrue(dbSupport.isSchemaEmpty(schema));

        flyway.setBaseDir(BASEDIR);
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

        flyway.setBaseDir(BASEDIR);
        flyway.migrate();
    }

    @Test
    public void nonEmptySchemaWithInit() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL,\n" +
                "  PRIMARY KEY(name))");

        flyway.setBaseDir(BASEDIR);
        flyway.init();
        flyway.migrate();
    }

    @Test
    public void nonEmptySchemaWithDisableInitCheck() throws Exception {
        jdbcTemplate.execute("CREATE TABLE t1 (\n" +
                "  name VARCHAR(25) NOT NULL,\n" +
                "  PRIMARY KEY(name))");

        flyway.setBaseDir(BASEDIR);
        flyway.setDisableInitCheck(true);
        flyway.migrate();
    }

    @Test
    public void semicolonWithinStringLiteral() throws Exception {
        flyway.setBaseDir("migration/semicolon");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1.1", schemaVersion.toString());
        assertEquals("Populate table", flyway.status().getDescription());

        assertEquals("Mr. Semicolon+Linebreak;\nanother line",
                jdbcTemplate.queryForString("select name from test_user where name like '%line'"));
    }

    @Test
    public void quotesAroundTableName() {
        flyway.setBaseDir(getQuoteBaseDir());
        flyway.migrate();

        // Clean script must also be able to properly deal with these reserved keywords in table names.
        flyway.clean();
    }

    @Test
    public void migrateMultipleSchemas() throws Exception {
        flyway.setSchemas("flyway_1", "flyway_2", "flyway_3");
        flyway.clean();

        assertNull(flyway.status());

        flyway.setBaseDir("migration/multi");
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
}
