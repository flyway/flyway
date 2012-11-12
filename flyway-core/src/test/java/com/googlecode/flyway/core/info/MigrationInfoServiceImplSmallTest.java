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
package com.googlecode.flyway.core.info;

import com.googlecode.flyway.core.api.MigrationInfoService;
import com.googlecode.flyway.core.api.MigrationState;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.metadatatable.AppliedMigration;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.resolver.MigrationResolver;
import com.googlecode.flyway.core.resolver.ResolvedMigration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for DbInfoAggregator.
 */
public class MigrationInfoServiceImplSmallTest {
    @Test
    public void onlyPending() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createAvailableMigration(1), createAvailableMigration(2)),
                        createMetaDataTable(), MigrationVersion.LATEST, false);

        assertNull(migrationInfoService.current());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(2, migrationInfoService.pending().length);
    }

    @Test
    public void allApplied() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createAvailableMigration(1), createAvailableMigration(2)),
                        createMetaDataTable(createAppliedMigration(1), createAppliedMigration(2)),
                        MigrationVersion.LATEST, false);

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void onePendingOneApplied() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createAvailableMigration(1), createAvailableMigration(2)),
                        createMetaDataTable(createAppliedMigration(1)),
                        MigrationVersion.LATEST, false);

        assertEquals("1", migrationInfoService.current().getVersion().toString());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(1, migrationInfoService.pending().length);
    }

    @Test
    public void oneAppliedOneSkipped() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createAvailableMigration(1), createAvailableMigration(2)),
                        createMetaDataTable(createAppliedMigration(2)),
                        MigrationVersion.LATEST, false);

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.IGNORED, migrationInfoService.all()[0].getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void twoAppliedOneFuture() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createAvailableMigration(1)),
                        createMetaDataTable(createAppliedMigration(1), createAppliedMigration(2)),
                        MigrationVersion.LATEST, false);

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.FUTURE_SUCCESS, migrationInfoService.current().getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void preInit() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createAvailableMigration(1)),
                        createMetaDataTable(createAppliedInitMigration(2)),
                        MigrationVersion.LATEST, false);

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.PREINIT, migrationInfoService.all()[0].getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void missing() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createAvailableMigration(2)),
                        createMetaDataTable(createAppliedMigration(1), createAppliedMigration(2)),
                        MigrationVersion.LATEST, false);

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.MISSING_SUCCESS, migrationInfoService.all()[0].getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    /**
     * Creates a new available migration with this version.
     *
     * @param version The version of the migration.
     * @return The available migration.
     */
    private ResolvedMigration createAvailableMigration(int version) {
        ResolvedMigration migration = new ResolvedMigration();
        migration.setVersion(new MigrationVersion(Integer.toString(version)));
        migration.setDescription("abc");
        migration.setScript("x");
        migration.setType(MigrationType.SQL);
        return migration;
    }

    /**
     * Creates a new applied migration with this version.
     *
     * @param version The version of the migration.
     * @return The applied migration.
     */
    private AppliedMigration createAppliedMigration(int version) {
        return new AppliedMigration(version, version, new MigrationVersion(Integer.toString(version)), "abc",
                MigrationType.SQL, "x", null, new Date(), "sa", 123, true);
    }

    /**
     * Creates a new applied init migration with this version.
     *
     * @param version The version of the migration.
     * @return The applied init migration.
     */
    private AppliedMigration createAppliedInitMigration(int version) {
        return new AppliedMigration(version, version, new MigrationVersion(Integer.toString(version)), "abc",
                MigrationType.INIT, "x", null, new Date(), "sa", 0, true);
    }

    /**
     * Creates a migrationResolver for testing.
     *
     * @param resolvedMigrations The resolved migrations.
     * @return The migration resolver.
     */
    private MigrationResolver createMigrationResolver(final ResolvedMigration... resolvedMigrations) {
        return new MigrationResolver() {
            public List<ResolvedMigration> resolveMigrations() {
                return Arrays.asList(resolvedMigrations);
            }
        };
    }

    /**
     * Creates a metadata table for testing.
     *
     * @param appliedMigrations The applied migrations.
     * @return The metadata table.
     */
    private MetaDataTable createMetaDataTable(final AppliedMigration... appliedMigrations) {
        return new MetaDataTable() {
            public void createIfNotExists() {
            }

            public void lock() {
            }

            public void insert(AppliedMigration appliedMigration) {
            }

            public List<AppliedMigration> allAppliedMigrations() {
                return Arrays.asList(appliedMigrations);
            }

            public boolean hasFailedMigration() {
                return false;
            }

            public MigrationVersion getCurrentSchemaVersion() {
                return null;
            }

            public void repair() {
            }
        };
    }
}
