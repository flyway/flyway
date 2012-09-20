package com.googlecode.flyway.core.info;

import com.googlecode.flyway.core.api.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for DbInfoAggregator.
 */
public class DbInfoAggregatorSmallTest {
    @Test
    public void onlyPending() {
        DbInfoAggregator dbInfoAggregator = new DbInfoAggregator(null, null, MigrationVersion.LATEST);

        List<MigrationInfo> availableMigrations = Arrays.asList(createAvailableMigration(1), createAvailableMigration(2));
        List<MigrationInfo> appliedMigrations = new ArrayList<MigrationInfo>();

        MigrationInfos migrationInfos = dbInfoAggregator.mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations);

        assertNull(migrationInfos.current());
        assertEquals(2, migrationInfos.all().length);
        assertEquals(2, migrationInfos.pending().length);
    }

    @Test
    public void allApplied() {
        DbInfoAggregator dbInfoAggregator = new DbInfoAggregator(null, null, MigrationVersion.LATEST);

        List<MigrationInfo> availableMigrations = Arrays.asList(createAvailableMigration(1), createAvailableMigration(2));
        List<MigrationInfo> appliedMigrations = Arrays.asList(createAppliedMigration(1), createAppliedMigration(2));

        MigrationInfos migrationInfos = dbInfoAggregator.mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations);

        assertEquals("2", migrationInfos.current().getVersion().toString());
        assertEquals(2, migrationInfos.all().length);
        assertEquals(0, migrationInfos.pending().length);
    }

    @Test
    public void onePendingOneApplied() {
        DbInfoAggregator dbInfoAggregator = new DbInfoAggregator(null, null, MigrationVersion.LATEST);

        List<MigrationInfo> availableMigrations = Arrays.asList(createAvailableMigration(1), createAvailableMigration(2));
        List<MigrationInfo> appliedMigrations = Arrays.asList(createAppliedMigration(1));

        MigrationInfos migrationInfos = dbInfoAggregator.mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations);

        assertEquals("1", migrationInfos.current().getVersion().toString());
        assertEquals(2, migrationInfos.all().length);
        assertEquals(1, migrationInfos.pending().length);
    }

    @Test
    public void oneAppliedOneSkipped() {
        DbInfoAggregator dbInfoAggregator = new DbInfoAggregator(null, null, MigrationVersion.LATEST);

        List<MigrationInfo> availableMigrations = Arrays.asList(createAvailableMigration(1), createAvailableMigration(2));
        List<MigrationInfo> appliedMigrations = Arrays.asList(createAppliedMigration(2));

        MigrationInfos migrationInfos = dbInfoAggregator.mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations);

        assertEquals("2", migrationInfos.current().getVersion().toString());
        assertEquals(MigrationState.IGNORED, migrationInfos.all()[0].getState());
        assertEquals(2, migrationInfos.all().length);
        assertEquals(0, migrationInfos.pending().length);
    }

    @Test
    public void twoAppliedOneFuture() {
        DbInfoAggregator dbInfoAggregator = new DbInfoAggregator(null, null, MigrationVersion.LATEST);

        List<MigrationInfo> availableMigrations = Arrays.asList(createAvailableMigration(1));
        List<MigrationInfo> appliedMigrations = Arrays.asList(createAppliedMigration(1), createAppliedMigration(2));

        MigrationInfos migrationInfos = dbInfoAggregator.mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations);

        assertEquals("2", migrationInfos.current().getVersion().toString());
        assertEquals(MigrationState.FUTURE_SUCCESS, migrationInfos.current().getState());
        assertEquals(2, migrationInfos.all().length);
        assertEquals(0, migrationInfos.pending().length);
    }

    @Test
    public void preInit() {
        DbInfoAggregator dbInfoAggregator = new DbInfoAggregator(null, null, MigrationVersion.LATEST);

        List<MigrationInfo> availableMigrations = Arrays.asList(createAvailableMigration(1));
        List<MigrationInfo> appliedMigrations = Arrays.asList(createAppliedInitMigration(2));

        MigrationInfos migrationInfos = dbInfoAggregator.mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations);

        assertEquals("2", migrationInfos.current().getVersion().toString());
        assertEquals(MigrationState.PREINIT, migrationInfos.all()[0].getState());
        assertEquals(2, migrationInfos.all().length);
        assertEquals(0, migrationInfos.pending().length);
    }

    @Test
    public void missing() {
        DbInfoAggregator dbInfoAggregator = new DbInfoAggregator(null, null, MigrationVersion.LATEST);

        List<MigrationInfo> availableMigrations = Arrays.asList(createAvailableMigration(2));
        List<MigrationInfo> appliedMigrations = Arrays.asList(createAppliedMigration(1), createAppliedMigration(2));

        MigrationInfos migrationInfos = dbInfoAggregator.mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations);

        assertEquals("2", migrationInfos.current().getVersion().toString());
        assertEquals(MigrationState.MISSING_SUCCESS, migrationInfos.all()[0].getState());
        assertEquals(2, migrationInfos.all().length);
        assertEquals(0, migrationInfos.pending().length);
    }

    /**
     * Creates a new available migration with this version.
     *
     * @param version The version of the migration.
     * @return The available migration.
     */
    private MigrationInfo createAvailableMigration(int version) {
        return new MigrationInfo(new MigrationVersion(Integer.toString(version)), "abc", "x", null, MigrationType.SQL);
    }

    /**
     * Creates a new applied migration with this version.
     *
     * @param version The version of the migration.
     * @return The applied migration.
     */
    private MigrationInfo createAppliedMigration(int version) {
        MigrationInfo migrationInfo = createAvailableMigration(version);
        migrationInfo.addExecutionDetails(new Date(), 123, MigrationState.SUCCESS);
        return migrationInfo;
    }

    /**
     * Creates a new applied init migration with this version.
     *
     * @param version The version of the migration.
     * @return The applied init migration.
     */
    private MigrationInfo createAppliedInitMigration(int version) {
        MigrationInfo migrationInfo = new MigrationInfo(new MigrationVersion(Integer.toString(version)), "abc", "x", null, MigrationType.INIT);
        migrationInfo.addExecutionDetails(new Date(), 123, MigrationState.SUCCESS);
        return migrationInfo;
    }
}
