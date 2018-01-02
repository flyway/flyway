/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.info;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for MigrationInfoServiceImpl.
 */
public class MigrationInfoServiceImplSmallTest {
    private int installedRank;

    @Test
    public void onlyPending() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createResolvedMigration(1), createResolvedMigration(2)),
                        createSchemaHistoryTable(), MigrationVersion.LATEST, false, true, true, true);
        migrationInfoService.refresh();

        assertNull(migrationInfoService.current());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(2, migrationInfoService.pending().length);
    }

    @Test
    public void allApplied() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createResolvedMigration(1), createResolvedMigration(2)),
                        createSchemaHistoryTable(createAppliedMigration(1), createAppliedMigration(2)),
                        MigrationVersion.LATEST, false, true, true, true);
        migrationInfoService.refresh();

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void appliedOverridesAvailable() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createResolvedMigration(1)),
                        createSchemaHistoryTable(createAppliedMigration(1, "xyz")),
                        MigrationVersion.LATEST, false, true, true, true);
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
                        createMigrationResolver(createResolvedMigration(1), createResolvedMigration(2)),
                        createSchemaHistoryTable(createAppliedMigration(1)),
                        MigrationVersion.LATEST, false, true, true, true);
        migrationInfoService.refresh();

        assertEquals("1", migrationInfoService.current().getVersion().toString());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(1, migrationInfoService.pending().length);
    }

    @Test
    public void oneAppliedOneSkipped() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createResolvedMigration(1), createResolvedMigration(2)),
                        createSchemaHistoryTable(createAppliedMigration(2)),
                        MigrationVersion.LATEST, false, true, true, true);
        migrationInfoService.refresh();

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.IGNORED, migrationInfoService.all()[0].getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
        // even with pending = true we should get a validation error for IGNORED migrations
        assertNotNull(migrationInfoService.validate());
    }

    @Test
    public void oneAppliedOneSkippedCurrent() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createResolvedMigration(1), createResolvedMigration(2)),
                        createSchemaHistoryTable(createAppliedMigration(2)),
                        MigrationVersion.CURRENT, false, true, true, true);
        migrationInfoService.refresh();

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.IGNORED, migrationInfoService.all()[0].getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
        // even with pending = true we should get a validation error for IGNORED migrations
        assertNotNull(migrationInfoService.validate());
    }

    @Test
    public void twoAppliedOnePending() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createResolvedMigration(1), createResolvedMigration(2), createResolvedMigration(3)),
                        createSchemaHistoryTable(createAppliedMigration(1), createAppliedMigration(3)),
                        MigrationVersion.LATEST, true, true, true, true);
        migrationInfoService.refresh();

        assertEquals("3", migrationInfoService.current().getVersion().toString());
        final MigrationInfo[] all = migrationInfoService.all();
        assertEquals(3, all.length);
        assertEquals(MigrationState.SUCCESS, all[0].getState());
        assertEquals(MigrationState.SUCCESS, all[1].getState());
        assertEquals(MigrationState.PENDING, all[2].getState());
        assertEquals(1, migrationInfoService.pending().length);
    }

    @Test
    public void twoAppliedOneFuture() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createResolvedMigration(1)),
                        createSchemaHistoryTable(createAppliedMigration(1), createAppliedMigration(2)),
                        MigrationVersion.LATEST, false, true, true, true);
        migrationInfoService.refresh();

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.FUTURE_SUCCESS, migrationInfoService.current().getState());
        assertEquals(MigrationState.FUTURE_SUCCESS, migrationInfoService.future()[0].getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void belowBaseline() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createResolvedMigration(1)),
                        createSchemaHistoryTable(createAppliedBaselineMigration(2)),
                        MigrationVersion.LATEST, false, true, true, true);
        migrationInfoService.refresh();

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.BELOW_BASELINE, migrationInfoService.all()[0].getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void missing() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createResolvedMigration(2)),
                        createSchemaHistoryTable(createAppliedMigration(1), createAppliedMigration(2)),
                        MigrationVersion.LATEST, false, true, true, true);
        migrationInfoService.refresh();

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.MISSING_SUCCESS, migrationInfoService.all()[0].getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void rAfterV() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(
                                createResolvedMigration(1),
                                createResolvedRepeatableMigration("xxx"),
                                createResolvedMigration(2)
                        ),
                        createSchemaHistoryTable(
                                createAppliedMigration(1),
                                createAppliedRepeatableMigration("xxx", 123)
                        ),
                        MigrationVersion.LATEST, false, true, true, true);
        migrationInfoService.refresh();

        assertEquals("1", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.OUTDATED, migrationInfoService.all()[1].getState());
        assertEquals(4, migrationInfoService.all().length);
        assertEquals(2, migrationInfoService.pending().length);
        assertEquals("2", migrationInfoService.pending()[0].getVersion().toString());
        assertNull(migrationInfoService.pending()[1].getVersion());
    }

    @Test
    public void schemaCreation() {
        MigrationInfoServiceImpl migrationInfoService =
                new MigrationInfoServiceImpl(
                        createMigrationResolver(createResolvedMigration(1)),
                        createSchemaHistoryTable(createAppliedSchemaMigration(), createAppliedMigration(1)),
                        MigrationVersion.LATEST, false, true, true, true);
        migrationInfoService.refresh();

        assertEquals("1", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.SUCCESS, migrationInfoService.all()[0].getState());
        assertEquals(MigrationState.SUCCESS, migrationInfoService.all()[1].getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    /**
     * Creates a new resolved migration with this version.
     *
     * @param version The version of the migration.
     * @return The resolved migration.
     */
    private ResolvedMigration createResolvedMigration(int version) {
        ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
        migration.setVersion(MigrationVersion.fromVersion(Integer.toString(version)));
        migration.setDescription("abc");
        migration.setScript("x");
        migration.setType(MigrationType.SQL);
        return migration;
    }

    /**
     * Creates a new resolved repeatable migration with this description.
     *
     * @param description The description of the migration.
     * @return The resolved migration.
     */
    private ResolvedMigration createResolvedRepeatableMigration(String description) {
        ResolvedMigrationImpl migration = new ResolvedMigrationImpl();
        migration.setVersion(null);
        migration.setDescription(description);
        migration.setChecksum(description.hashCode());
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
        return createAppliedMigration(version, "abc");
    }

    /**
     * Creates a new applied migration with this version.
     *
     * @param version     The version of the migration.
     * @param description The description of the migration.
     * @return The applied migration.
     */
    private AppliedMigration createAppliedMigration(int version, String description) {
        return new AppliedMigration(installedRank++, MigrationVersion.fromVersion(Integer.toString(version)), description,
                MigrationType.SQL, "x", null, new Date(), "sa", 123, true);
    }

    /**
     * Creates a new applied repeatable migration with this description and this checksum.
     *
     * @param description The description of the migration.
     * @param checksum    The checksum of the migration.
     * @return The applied migration.
     */
    private AppliedMigration createAppliedRepeatableMigration(String description, int checksum) {
        return new AppliedMigration(installedRank++, null, description,
                MigrationType.SQL, "x", checksum, new Date(), "sa", 123, true);
    }

    /**
     * Creates a new applied baseline migration with this version.
     *
     * @param version The version of the migration.
     * @return The applied baseline migration.
     */
    private AppliedMigration createAppliedBaselineMigration(int version) {
        return new AppliedMigration(installedRank++, MigrationVersion.fromVersion(Integer.toString(version)), "abc",
                MigrationType.BASELINE, "x", null, new Date(), "sa", 0, true);
    }

    /**
     * Creates a new applied schema migration with this version.
     *
     * @return The applied schema migration.
     */
    private AppliedMigration createAppliedSchemaMigration() {
        return new AppliedMigration(installedRank++, MigrationVersion.fromVersion(Integer.toString(0)), "<< Schema Creation >>",
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
     * Creates a schema history table for testing.
     *
     * @param appliedMigrations The applied migrations.
     * @return The schema history table.
     */
    private SchemaHistory createSchemaHistoryTable(final AppliedMigration... appliedMigrations) {
        SchemaHistory schemaHistory = mock(SchemaHistory.class);
        when(schemaHistory.allAppliedMigrations()).thenReturn(Arrays.asList(appliedMigrations));
        return schemaHistory;
    }
}
