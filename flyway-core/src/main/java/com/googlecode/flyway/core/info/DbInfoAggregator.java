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

import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.api.MigrationInfos;
import com.googlecode.flyway.core.api.MigrationState;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.migration.ExecutableMigration;
import com.googlecode.flyway.core.migration.MigrationResolver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Aggregates info about all known migrations from both the classpath and the DB.
 */
public class DbInfoAggregator {
    /**
     * The migration resolver for available migrations.
     */
    private final MigrationResolver migrationResolver;

    /**
     * The metadata table for applied migrations.
     */
    private final MetaDataTable metaDataTable;

    /**
     * The target version up to which to retrieve the info.
     */
    private final MigrationVersion target;

    /**
     * Creates a new info aggregator.
     *
     * @param migrationResolver The migration resolver for available migrations.
     * @param metaDataTable     The metadata table for applied migrations.
     * @param target            The target version up to which to retrieve the info.
     */
    public DbInfoAggregator(MigrationResolver migrationResolver, MetaDataTable metaDataTable, MigrationVersion target) {
        this.migrationResolver = migrationResolver;
        this.metaDataTable = metaDataTable;
        this.target = target;
    }

    /**
     * Aggregates info about all known migrations from both the classpath and the DB.
     *
     * @return The info about the migrations.
     */
    public MigrationInfos aggregateMigrationInfo() {
        Iterator<MigrationInfo> availableMigrationsIterator = extractMigrationInfos(migrationResolver.resolveMigrations()).iterator();

        List<MigrationInfo> appliedMigrationsList = metaDataTable.allAppliedMigrations();
        Iterator<MigrationInfo> appliedMigrationsIterator = appliedMigrationsList.iterator();

        List<MigrationInfo> allMigrations = new ArrayList<MigrationInfo>();
        if (appliedMigrationsIterator.hasNext()
                && MigrationType.INIT.equals(appliedMigrationsList.get(0).getType())) {
            MigrationVersion initVersion = appliedMigrationsList.get(0).getVersion();

            while (availableMigrationsIterator.hasNext()) {
                MigrationInfo availableMigration = availableMigrationsIterator.next();
                if (availableMigration.getVersion().compareTo(initVersion) >= 0) {
                    break;
                }
                addPreInitMigration(allMigrations, availableMigration);
            }

            allMigrations.add(appliedMigrationsIterator.next());
        }

        while (availableMigrationsIterator.hasNext() && appliedMigrationsIterator.hasNext()) {
            MigrationInfo availableMigration = availableMigrationsIterator.next();
            MigrationInfo appliedMigration = appliedMigrationsIterator.next();

            while (availableMigration.getVersion().compareTo(appliedMigration.getVersion()) < 0) {
                addIgnoredMigration(allMigrations, availableMigration);

                if (availableMigrationsIterator.hasNext()) {
                    availableMigration = availableMigrationsIterator.next();
                }
            }

            while (appliedMigration.getVersion().compareTo(availableMigration.getVersion()) < 0) {
                addMissingMigration(allMigrations, appliedMigration);

                if (appliedMigrationsIterator.hasNext()) {
                    appliedMigration = appliedMigrationsIterator.next();
                }
            }

            if (availableMigration.getVersion().equals(appliedMigration.getVersion())) {
                allMigrations.add(appliedMigration);
            }
        }

        addPendingMigrations(allMigrations, availableMigrationsIterator);
        addFutureMigrations(allMigrations, appliedMigrationsIterator);

        filterOutMigrationsAboveTarget(allMigrations);

        return new MigrationInfos(allMigrations);
    }

    /**
     * Adds this pre-init migration to the list of all migrations.
     *
     * @param allMigrations The migration list.
     * @param migration The migration to add.
     */
    private void addPreInitMigration(List<MigrationInfo> allMigrations, MigrationInfo migration) {
        migration.addExecutionDetails(null, null, MigrationState.PREINIT);
        allMigrations.add(migration);
    }

    /**
     * Adds this ignored migration to the list of all migrations.
     *
     * @param allMigrations The migration list.
     * @param migration The migration to add.
     */
    private void addIgnoredMigration(List<MigrationInfo> allMigrations, MigrationInfo migration) {
        migration.addExecutionDetails(null, null, MigrationState.IGNORED);
        allMigrations.add(migration);
    }

    /**
     * Adds this missing migration to the list of all migrations.
     *
     * @param allMigrations The migration list.
     * @param migration The migration to add.
     */
    private void addMissingMigration(List<MigrationInfo> allMigrations, MigrationInfo migration) {
        if (migration.getState() == MigrationState.SUCCESS) {
            migration.addExecutionDetails(
                    migration.getInstalledOn(), migration.getExecutionTime(), MigrationState.MISSING_SUCCESS);
        } else {
            migration.addExecutionDetails(
                    migration.getInstalledOn(), migration.getExecutionTime(), MigrationState.MISSING_FAILED);
        }
        allMigrations.add(migration);
    }

    /**
     * Adds all pending migrations to the list of all migrations.
     *
     * @param allMigrations The migration list.
     * @param iterator The migration iterator.
     */
    private void addPendingMigrations(List<MigrationInfo> allMigrations, Iterator<MigrationInfo> iterator) {
        MigrationInfo availableMigration;
        while (iterator.hasNext()) {
            availableMigration = iterator.next();
            allMigrations.add(availableMigration);
        }
    }

    /**
     * Adds all future migrations to the list of all migrations.
     *
     * @param allMigrations The migration list.
     * @param iterator The migration iterator.
     */
    private void addFutureMigrations(List<MigrationInfo> allMigrations, Iterator<MigrationInfo> iterator) {
        MigrationInfo appliedMigration;
        while (iterator.hasNext()) {
            appliedMigration = iterator.next();
            if (MigrationState.SUCCESS == appliedMigration.getState()) {
                appliedMigration.addExecutionDetails(
                        appliedMigration.getInstalledOn(), appliedMigration.getExecutionTime(),
                        MigrationState.FUTURE_SUCCESS);
            } else {
                appliedMigration.addExecutionDetails(
                        appliedMigration.getInstalledOn(), appliedMigration.getExecutionTime(),
                        MigrationState.FUTURE_FAILED);
            }
            allMigrations.add(appliedMigration);
        }
    }

    /**
     * Filters out the migrations from this list that have a version newer than target.
     *
     * @param migrations The list to filter.
     */
    private void filterOutMigrationsAboveTarget(List<MigrationInfo> migrations) {
        Iterator<MigrationInfo> iterator = migrations.iterator();
        while (iterator.hasNext()) {
            MigrationInfo info = iterator.next();
            if (info.getVersion().compareTo(target) > 0) {
                iterator.remove();
            }
        }
    }

    /**
     * Extract the migration infos from these executable migrations.
     *
     * @param executableMigrations The executable migrations to get the infos from.
     * @return The migration infos.
     */
    private List<MigrationInfo> extractMigrationInfos(List<ExecutableMigration> executableMigrations) {
        List<MigrationInfo> migrationInfos = new ArrayList<MigrationInfo>(executableMigrations.size());
        for (ExecutableMigration executableMigration : executableMigrations) {
            migrationInfos.add(executableMigration.getInfo());
        }
        return migrationInfos;
    }
}
