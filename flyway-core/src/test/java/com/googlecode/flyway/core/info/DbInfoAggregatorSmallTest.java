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
import com.googlecode.flyway.core.migration.MigrationInfoImpl;
import com.googlecode.flyway.core.migration.MigrationInfoServiceImpl;
import com.googlecode.flyway.core.resolver.ResolvedMigration;
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
        DbInfoAggregator dbInfoAggregator = new DbInfoAggregator(null, null, MigrationVersion.LATEST, false);

        List<ResolvedMigration> availableMigrations = Arrays.asList(createAvailableMigration(1), createAvailableMigration(2));
        List<MigrationInfoImpl> appliedMigrations = new ArrayList<MigrationInfoImpl>();

        MigrationInfoService migrationInfoService =
                new MigrationInfoServiceImpl(dbInfoAggregator.mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations));

        assertNull(migrationInfoService.current());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(2, migrationInfoService.pending().length);
    }

    @Test
    public void allApplied() {
        DbInfoAggregator dbInfoAggregator = new DbInfoAggregator(null, null, MigrationVersion.LATEST, false);

        List<ResolvedMigration> availableMigrations = Arrays.asList(createAvailableMigration(1), createAvailableMigration(2));
        List<MigrationInfoImpl> appliedMigrations = Arrays.asList(createAppliedMigration(1), createAppliedMigration(2));

        MigrationInfoService migrationInfoService = new MigrationInfoServiceImpl(dbInfoAggregator.mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations));

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void onePendingOneApplied() {
        DbInfoAggregator dbInfoAggregator = new DbInfoAggregator(null, null, MigrationVersion.LATEST, false);

        List<ResolvedMigration> availableMigrations = Arrays.asList(createAvailableMigration(1), createAvailableMigration(2));
        List<MigrationInfoImpl> appliedMigrations = Arrays.asList(createAppliedMigration(1));

        MigrationInfoService migrationInfoService = new MigrationInfoServiceImpl(dbInfoAggregator.mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations));

        assertEquals("1", migrationInfoService.current().getVersion().toString());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(1, migrationInfoService.pending().length);
    }

    @Test
    public void oneAppliedOneSkipped() {
        DbInfoAggregator dbInfoAggregator = new DbInfoAggregator(null, null, MigrationVersion.LATEST, false);

        List<ResolvedMigration> availableMigrations = Arrays.asList(createAvailableMigration(1), createAvailableMigration(2));
        List<MigrationInfoImpl> appliedMigrations = Arrays.asList(createAppliedMigration(2));

        MigrationInfoService migrationInfoService = new MigrationInfoServiceImpl(dbInfoAggregator.mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations));

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.IGNORED, migrationInfoService.all()[0].getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void twoAppliedOneFuture() {
        DbInfoAggregator dbInfoAggregator = new DbInfoAggregator(null, null, MigrationVersion.LATEST, false);

        List<ResolvedMigration> availableMigrations = Arrays.asList(createAvailableMigration(1));
        List<MigrationInfoImpl> appliedMigrations = Arrays.asList(createAppliedMigration(1), createAppliedMigration(2));

        MigrationInfoService migrationInfoService = new MigrationInfoServiceImpl(dbInfoAggregator.mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations));

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.FUTURE_SUCCESS, migrationInfoService.current().getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void preInit() {
        DbInfoAggregator dbInfoAggregator = new DbInfoAggregator(null, null, MigrationVersion.LATEST, false);

        List<ResolvedMigration> availableMigrations = Arrays.asList(createAvailableMigration(1));
        List<MigrationInfoImpl> appliedMigrations = Arrays.asList(createAppliedInitMigration(2));

        MigrationInfoService migrationInfoService = new MigrationInfoServiceImpl(dbInfoAggregator.mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations));

        assertEquals("2", migrationInfoService.current().getVersion().toString());
        assertEquals(MigrationState.PREINIT, migrationInfoService.all()[0].getState());
        assertEquals(2, migrationInfoService.all().length);
        assertEquals(0, migrationInfoService.pending().length);
    }

    @Test
    public void missing() {
        DbInfoAggregator dbInfoAggregator = new DbInfoAggregator(null, null, MigrationVersion.LATEST, false);

        List<ResolvedMigration> availableMigrations = Arrays.asList(createAvailableMigration(2));
        List<MigrationInfoImpl> appliedMigrations = Arrays.asList(createAppliedMigration(1), createAppliedMigration(2));

        MigrationInfoService migrationInfoService = new MigrationInfoServiceImpl(dbInfoAggregator.mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations));

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
    private MigrationInfoImpl createAppliedMigration(int version) {
        MigrationInfoImpl migrationInfo =
                new MigrationInfoImpl(new MigrationVersion(Integer.toString(version)), "abc", "x", null, MigrationType.SQL);
        migrationInfo.setInstalledOn(new Date());
        migrationInfo.setExecutionTime(123);
        migrationInfo.setState(MigrationState.SUCCESS);
        return migrationInfo;
    }

    /**
     * Creates a new applied init migration with this version.
     *
     * @param version The version of the migration.
     * @return The applied init migration.
     */
    private MigrationInfoImpl createAppliedInitMigration(int version) {
        MigrationInfoImpl migrationInfo = new MigrationInfoImpl(new MigrationVersion(Integer.toString(version)), "abc", "x", null, MigrationType.INIT);
        migrationInfo.setInstalledOn(new Date());
        migrationInfo.setExecutionTime(123);
        migrationInfo.setState(MigrationState.SUCCESS);
        return migrationInfo;
    }
}
