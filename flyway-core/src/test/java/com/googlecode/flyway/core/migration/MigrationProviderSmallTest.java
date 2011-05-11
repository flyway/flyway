/**
 * Copyright (C) 2010-2011 the original author or authors.
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

import com.googlecode.flyway.core.dbsupport.DbSupport;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Test for MigrationProvider.
 */
public class MigrationProviderSmallTest {
    /**
     * Checks that migrations are properly collected, eliminating all exact duplicates.
     */
    @Test
    public void collectMigrations() {
        MigrationResolver migrationResolver = new MigrationResolver() {
            @Override
            public Collection<Migration> resolveMigrations() {
                Collection<Migration> migrations = new ArrayList<Migration>();

                migrations.add(createTestMigration(MigrationType.JAVA, "1", "Description", "Migration1", 123));
                migrations.add(createTestMigration(MigrationType.JAVA, "1", "Description", "Migration1", 123));
                migrations.add(createTestMigration(MigrationType.SQL, "2", "Description2", "Migration2", 1234));
                return migrations;
            }
        };
        Collection<MigrationResolver> migrationResolvers = new ArrayList<MigrationResolver>();
        migrationResolvers.add(migrationResolver);

        Collection<Migration> migrations = MigrationProvider.collectMigrations(migrationResolvers);
        assertEquals(2, migrations.size());
    }

    /**
     * Creates a migration for our tests.
     *
     * @param aMigrationType The migration type.
     * @param aVersion       The version.
     * @param aDescription   The description.
     * @param aScript        The script.
     * @param aChecksum      The checksum.
     * @return The new test migration.
     */
    private Migration createTestMigration(final MigrationType aMigrationType, final String aVersion, final String aDescription, final String aScript, final Integer aChecksum) {
        return new Migration() {
            {
                schemaVersion = new SchemaVersion(aVersion);
                description = aDescription;
                script = aScript;
                checksum = aChecksum;
            }

            @Override
            public MigrationType getMigrationType() {
                return aMigrationType;
            }

            @Override
            public void migrate(JdbcTemplate jdbcTemplate, DbSupport dbSupport) throws DataAccessException {
            }
        };
    }
}
