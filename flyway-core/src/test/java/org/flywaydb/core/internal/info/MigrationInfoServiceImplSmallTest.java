/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.info;

import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for MigrationInfoServiceImpl.
 */
public class MigrationInfoServiceImplSmallTest {
    @Test
    public void onlyPending() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createAvailableMigration(1), createAvailableMigration(2)),
                        createMetaDataTable(), MigrationVersion.LATEST, false, true);
        migrationInfoService.refresh();

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
                        MigrationVersion.LATEST, false, true);
        migrationInfoService.refresh();

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void appliedOverridesAvailable() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createAvailableMigration(1)),
                        createMetaDataTable(createAppliedMigration(1, "xyz")),
                        MigrationVersion.LATEST, false, true);
        migrationInfoService.refresh();

        assertEquals("1", migrationInfoService.current().getVersion().toString());
        assertEquals("xyz", migrationInfoService.current().getDescription());
        assertEquals(1, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void onePendingOneApplied() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createAvailableMigration(1), createAvailableMigration(2)),
                        createMetaDataTable(createAppliedMigration(1)),
                        MigrationVersion.LATEST, false, true);
        migrationInfoService.refresh();

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
                        MigrationVersion.LATEST, false, true);
        migrationInfoService.refresh();

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
                        MigrationVersion.LATEST, false, true);
        migrationInfoService.refresh();

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.FUTURE_SUCCESS, migrationInfoService.current().getState());
        assertEquals(MigrationState.FUTURE_SUCCESS, migrationInfoService.future()[0].getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void preInit() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createAvailableMigration(1)),
                        createMetaDataTable(createAppliedInitMigration(2)),
                        MigrationVersion.LATEST, false, true);
        migrationInfoService.refresh();

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
                        MigrationVersion.LATEST, false, true);
        migrationInfoService.refresh();

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.MISSING_SUCCESS, migrationInfoService.all()[0].getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void schemaCreation() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createAvailableMigration(1)),
                        createMetaDataTable(createAppliedSchemaMigration(), createAppliedMigration(1)),
                        MigrationVersion.LATEST, false, true);
        migrationInfoService.refresh();

        assertEquals("1", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, migrationInfoService.all()[0].getState());
        assertEquals(MigrationState.SUCCESS, migrationInfoService.all()[1].getState());
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
        ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
        migration.setVersion(MigrationVersion.fromVersion(Integer.toString(version)));
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
        return createAppliedMigration(version, "x");
    }

    /**
     * Creates a new applied migration with this version.
     *
     * @param version     The version of the migration.
     * @param description The description of the migration.
     * @return The applied migration.
     */
    private AppliedMigration createAppliedMigration(int version, String description) {
        return new AppliedMigration(version, version, MigrationVersion.fromVersion(Integer.toString(version)), description,
                MigrationType.SQL, "x", null, new Date(), "sa", 123, true);
    }

    /**
     * Creates a new applied init migration with this version.
     *
     * @param version The version of the migration.
     * @return The applied init migration.
     */
    private AppliedMigration createAppliedInitMigration(int version) {
        return new AppliedMigration(version, version, MigrationVersion.fromVersion(Integer.toString(version)), "abc",
                MigrationType.INIT, "x", null, new Date(), "sa", 0, true);
    }

    /**
     * Creates a new applied schema migration with this version.
     *
     * @return The applied schema migration.
     */
    private AppliedMigration createAppliedSchemaMigration() {
        return new AppliedMigration(0, 0, MigrationVersion.fromVersion(Integer.toString(0)), "<< Schema Creation >>",
                MigrationType.SCHEMA, "x", null, new Date(), "sa", 0, true);
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
        MetaDataTable metaDataTable = mock(MetaDataTable.class);
        when(metaDataTable.allAppliedMigrations()).thenReturn(Arrays.asList(appliedMigrations));
        return metaDataTable;
    }
}
