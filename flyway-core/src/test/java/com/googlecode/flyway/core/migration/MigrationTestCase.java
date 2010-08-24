/**
 * Copyright (C) 2009-2010 the original author or authors.
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
import com.googlecode.flyway.core.validation.ValidationErrorMode;
import com.googlecode.flyway.core.validation.ValidationMode;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlMigration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test to demonstrate the migration functionality.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class MigrationTestCase {
    /**
     * The datasource to use for single-threaded migration tests.
     */
    @Resource
    protected DataSource migrationDataSource;

    protected Flyway flyway;

    @Before
    public void setUp() {
        flyway = new Flyway();
        flyway.setDataSource(migrationDataSource);
        flyway.setValidationMode(ValidationMode.ALL);
        flyway.clean();
    }

    /**
     * @return The directory containing the migrations for the tests.
     */
    protected abstract String getBaseDir();

    /**
     * @return The DbSupport class to test.
     */
    protected abstract DbSupport getDbSupport();

    @Test
    public void migrate() throws Exception {
        flyway.setBaseDir(getBaseDir());
        flyway.migrate();
        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("2.0", schemaVersion.getVersion());
        assertEquals("Add foreign key", schemaVersion.getDescription());
        assertEquals(0, flyway.migrate());
        assertEquals(3, flyway.history().size());

        for (MetaDataTableRow metaDataTableRow : flyway.history()) {
            assertChecksum(metaDataTableRow);
        }
    }

    /**
     * Compares the DB checksum to the classpath checksum of this migration.
     *
     * @param appliedMigration The migration to check.
     */
    private void assertChecksum(MetaDataTableRow appliedMigration) {
        ClassPathResource resource = new ClassPathResource(getBaseDir() + "/" + appliedMigration.getScript());
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(new HashMap<String, String>(), "", "");
        Migration sqlMigration = new SqlMigration(resource, placeholderReplacer, "UTF-8", "1");
        assertEquals("Wrong checksum for " + appliedMigration.getScript(), sqlMigration.getChecksum(), appliedMigration.getChecksum());
    }

    @Test(expected = IllegalStateException.class)
    public void validateFails() throws Exception {
        flyway.setBaseDir(getBaseDir());
        flyway.setSqlMigrationSuffix("First.sql");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1", schemaVersion.getVersion());

        flyway.setSqlMigrationPrefix("CheckValidate");
        flyway.validate();
    }

    @Test
    public void validateClean() throws Exception {
        flyway.setBaseDir(getBaseDir());
        flyway.setSqlMigrationSuffix("First.sql");
        flyway.migrate();

        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("1", schemaVersion.getVersion());

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
        } catch (IllegalStateException e) {
            //Expected
        }

        MetaDataTableRow migration = flyway.status();
        SchemaVersion schemaVersion = migration.getVersion();
        assertEquals("1", schemaVersion.getVersion());
        assertEquals("Should Fail", schemaVersion.getDescription());
        assertEquals(MigrationState.FAILED, migration.getState());
        assertEquals(1, flyway.history().size());
    }

    @Test
    public void tableExists() throws Exception {
        flyway.init(null);
        assertTrue(getDbSupport().tableExists(new JdbcTemplate(migrationDataSource), "SCHEMA_VERSION"));
    }

    @Test
    public void columnExists() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(migrationDataSource);

        flyway.init(null);
        assertTrue(getDbSupport().columnExists(jdbcTemplate, "SCHEMA_VERSION", "DESCRIPTION"));
        assertFalse(getDbSupport().columnExists(jdbcTemplate, "SCHEMA_VERSION", "INVALID"));
    }
}
