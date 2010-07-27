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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

	/**
	 * @return The directory containing the migrations for the tests.
	 */
	protected abstract String getBaseDir();

	@Test
	public void migrate() throws Exception {
		Flyway flyway = new Flyway();
		flyway.setDataSource(migrationDataSource);
		flyway.setBaseDir(getBaseDir());
        flyway.clean();
		flyway.migrate();
		SchemaVersion schemaVersion = flyway.getMetaDataTable().latestAppliedMigration().getVersion();
		assertEquals("2.0", schemaVersion.getVersion());
		assertEquals("Add foreign key", schemaVersion.getDescription());
		assertEquals(0, flyway.migrate());
        assertEquals(4, flyway.getMetaDataTable().allAppliedMigrations().size());
        final List<Migration> migrationList = flyway.getMetaDataTable().allAppliedMigrations();
        for (Migration migration : migrationList) {
            if (!migration.getVersion().equals(new SchemaVersion("0", null))) {
                Assert.assertNotNull(migration.getScriptName() + " has no checksum", migration.getChecksum());
            }
        }
    }
}
