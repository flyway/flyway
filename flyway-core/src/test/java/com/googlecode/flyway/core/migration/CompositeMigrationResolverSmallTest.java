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

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for CompositeMigrationResolver.
 */
public class CompositeMigrationResolverSmallTest {
    @Test
    public void mergeLocations() {
        CompositeMigrationResolver migrationResolver = new CompositeMigrationResolver(new String[]{"db/locations"}, "db/files", "db/classes", "UTF-8", "V", ".sql", new HashMap<String, String>(), "${", "}");
        Set<String> locations = migrationResolver.mergeLocations();
        assertEquals(3, locations.size());
        Iterator<String> iterator = locations.iterator();
        assertEquals("db/classes", iterator.next());
        assertEquals("db/files", iterator.next());
        assertEquals("db/locations", iterator.next());
    }

    @Test
    public void mergeLocationsDuplicate() {
        CompositeMigrationResolver migrationResolver = new CompositeMigrationResolver(new String[]{"db/locations"}, "db/migration", "db/migration", "UTF-8", "V", ".sql", new HashMap<String, String>(), "${", "}");
        Set<String> locations = migrationResolver.mergeLocations();
        assertEquals(2, locations.size());
        Iterator<String> iterator = locations.iterator();
        assertEquals("db/locations", iterator.next());
        assertEquals("db/migration", iterator.next());
    }

    @Test
    public void mergeLocationsOverlap() {
        CompositeMigrationResolver migrationResolver = new CompositeMigrationResolver(new String[]{"db/migration/oracle"}, "db/migration", "db/migration", "UTF-8", "V", ".sql", new HashMap<String, String>(), "${", "}");
        Set<String> locations = migrationResolver.mergeLocations();
        assertEquals(1, locations.size());
        assertEquals("db/migration", locations.iterator().next());
    }

    @Test
    public void resolveMigrationsMultipleLocations() {
        MigrationResolver migrationResolver = new CompositeMigrationResolver(new String[]{"migration/subdir/dir2"}, "db.migration", "migration/subdir/dir1", "UTF-8", "V", ".sql", new HashMap<String, String>(), "${", "}");

        List<ResolvedMigration> migrations = migrationResolver.resolveMigrations();

        assertEquals(2, migrations.size());
        assertEquals("First", migrations.get(0).getDescription());
        assertEquals("Add foreign key", migrations.get(1).getDescription());
    }

    /**
     * Checks that migrations are properly collected, eliminating all exact duplicates.
     */
    @Test
    public void collectMigrations() {
        MigrationResolver migrationResolver = new MigrationResolver() {
            public List<ResolvedMigration> resolveMigrations() {
                List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();

                migrations.add(createTestMigration(MigrationType.SPRING_JDBC, "1", "Description", "Migration1", 123));
                migrations.add(createTestMigration(MigrationType.SPRING_JDBC, "1", "Description", "Migration1", 123));
                migrations.add(createTestMigration(MigrationType.SQL, "2", "Description2", "Migration2", 1234));
                return migrations;
            }
        };
        Collection<MigrationResolver> migrationResolvers = new ArrayList<MigrationResolver>();
        migrationResolvers.add(migrationResolver);

        Collection<ResolvedMigration> migrations = CompositeMigrationResolver.collectMigrations(migrationResolvers);
        assertEquals(2, migrations.size());
    }

    @Test
    public void checkForIncompatibilitiesMessage() {
        ResolvedMigration migration1 = createTestMigration(MigrationType.SQL, "1", "First", "V1__First.sql", 123);
        migration1.setPhysicalLocation("target/test-classes/migration/validate/V1__First.sql");

        ResolvedMigration migration2 = createTestMigration(MigrationType.SPRING_JDBC, "1", "Description", "Migration1", 123);
        migration2.setPhysicalLocation("Migration1");

        List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();
        migrations.add(migration1);
        migrations.add(migration2);

        try {
            CompositeMigrationResolver.checkForIncompatibilities(migrations);
        } catch (FlywayException e) {
            assertTrue(e.getMessage().contains("target/test-classes/migration/validate/V1__First.sql"));
            assertTrue(e.getMessage().contains("Migration1"));
        }
    }

    /**
     * Makes sure no validation exception is thrown.
     */
    @Test
    public void checkForIncompatibilitiesNoConflict() {
        List<ResolvedMigration> migrations = new ArrayList<ResolvedMigration>();
        migrations.add(createTestMigration(MigrationType.SPRING_JDBC, "1", "Description", "Migration1", 123));
        migrations.add(createTestMigration(MigrationType.SQL, "2", "Description2", "Migration2", 1234));

        CompositeMigrationResolver.checkForIncompatibilities(migrations);
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
    private ResolvedMigration createTestMigration(final MigrationType aMigrationType, final String aVersion, final String aDescription, final String aScript, final Integer aChecksum) {
        ResolvedMigration migration = new ResolvedMigration();
        migration.setVersion(new MigrationVersion(aVersion));
        migration.setDescription(aDescription);
        migration.setScript(aScript);
        migration.setChecksum(aChecksum);
        migration.setType(aMigrationType);
        return migration;
    }
}
