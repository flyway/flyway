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
import com.googlecode.flyway.core.ValidationType;
import com.googlecode.flyway.core.util.ResourceUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test to demonstrate the migration functionality using H2.
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
        flyway.setValidationType(ValidationType.ALL);
        flyway.clean();
    }

    /**
     * @return The directory containing the migrations for the tests.
     */
    protected abstract String getBaseDir();

    @Test
    public void migrate() throws Exception {
        flyway.setBaseDir(getBaseDir());
        flyway.migrate();
        SchemaVersion schemaVersion = flyway.status().getVersion();
        assertEquals("2.0", schemaVersion.getVersion());
        assertEquals("Add foreign key", schemaVersion.getDescription());
        assertEquals(0, flyway.migrate());
        assertEquals(3, flyway.history().size());

        assertChecksum(0, "V1__First.sql");
        assertChecksum(1, "V1_1__Populate_table.sql");
        assertChecksum(2, "V2_0__Add_foreign_key.sql");
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

        flyway.setValidationType(ValidationType.ALL_CLEAN);
        flyway.setSqlMigrationPrefix("CheckValidate");
        assertEquals(1, flyway.migrate());
    }


    private void assertChecksum(int index, String sqlFile) {
        final List<Migration> migrationList = flyway.history();
        Migration migration1 = migrationList.get(index);
        final String sql1 = ResourceUtils.loadResourceAsString(getBaseDir() + "/" + sqlFile);
        ResourceUtils.calculateChecksum(sql1);
        Assert.assertEquals("wrong checksum for " + migration1.getScriptName(), ResourceUtils.calculateChecksum(sql1), migration1.getChecksum());
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

        Migration migration = flyway.status();
        SchemaVersion schemaVersion = migration.getVersion();
        assertEquals("1", schemaVersion.getVersion());
        assertEquals("Should Fail", schemaVersion.getDescription());
        assertEquals(MigrationState.FAILED, migration.getState());
        assertEquals(1, flyway.history().size());
    }
}
