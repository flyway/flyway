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

import com.googlecode.flyway.core.api.MigrationState;
import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.migration.MigrationInfoImpl;
import com.googlecode.flyway.core.migration.MigrationInfosImpl;
import com.googlecode.flyway.core.migration.MigrationResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    public MigrationInfosImpl aggregateMigrationInfo() {
        List<MigrationInfoImpl> availableMigrations = migrationResolver.resolveMigrations();
        List<MigrationInfoImpl> appliedMigrations = metaDataTable.allAppliedMigrations();

        List<MigrationInfoImpl> allMigrations = mergeAvailableAndAppliedMigrations(availableMigrations, appliedMigrations);

        return new MigrationInfosImpl(allMigrations);
    }

    /**
     * Merges the available and the applied migrations to produce one fully aggregated and consolidated list.
     *
     * @param availableMigrations The available migrations.
     * @param appliedMigrations   The applied migrations.
     * @return The complete list of migrations.
     */
    /* private -> testing */
    List<MigrationInfoImpl> mergeAvailableAndAppliedMigrations(List<MigrationInfoImpl> availableMigrations, List<MigrationInfoImpl> appliedMigrations) {
        Map<MigrationVersion, MigrationInfoImpl> allMigrationsMap = new TreeMap<MigrationVersion, MigrationInfoImpl>();

        for (MigrationInfoImpl availableMigration : availableMigrations) {
            MigrationVersion version = availableMigration.getVersion();
            if (version.compareTo(target) > 0) {
                availableMigration.setState(MigrationState.ABOVE_TARGET);
            }
            allMigrationsMap.put(version, availableMigration);
        }

        MigrationVersion lastAvailableVersion = MigrationVersion.EMPTY;
        if (!availableMigrations.isEmpty()) {
            lastAvailableVersion = availableMigrations.get(availableMigrations.size() - 1).getVersion();
        }

        for (MigrationInfoImpl appliedMigration : appliedMigrations) {
            if (!allMigrationsMap.containsKey(appliedMigration.getVersion())) {
                if (appliedMigration.getVersion().compareTo(lastAvailableVersion) < 0) {
                    // Missing migrations
                    if (MigrationState.SUCCESS.equals(appliedMigration.getState())) {
                        appliedMigration.setState(MigrationState.MISSING_SUCCESS);
                    } else {
                        appliedMigration.setState(MigrationState.MISSING_FAILED);
                    }
                } else if (appliedMigration.getVersion().compareTo(lastAvailableVersion) > 0) {
                    // Future migrations
                    if (MigrationState.SUCCESS.equals(appliedMigration.getState())) {
                        appliedMigration.setState(MigrationState.FUTURE_SUCCESS);
                    } else {
                        appliedMigration.setState(MigrationState.FUTURE_FAILED);
                    }
                }
            }
            allMigrationsMap.put(appliedMigration.getVersion(), appliedMigration);
        }

        if (!appliedMigrations.isEmpty() && MigrationType.INIT.equals(appliedMigrations.get(0).getType())) {
            MigrationVersion initVersion = appliedMigrations.get(0).getVersion();

            for (MigrationInfoImpl migrationInfo : allMigrationsMap.values()) {
                if (migrationInfo.getVersion().compareTo(initVersion) < 0) {
                    migrationInfo.setState(MigrationState.PREINIT);
                } else {
                    break;
                }
            }
        }

        if (!appliedMigrations.isEmpty()) {
            MigrationVersion lastAppliedVersion = appliedMigrations.get(appliedMigrations.size() - 1).getVersion();

            for (MigrationInfoImpl migrationInfo : allMigrationsMap.values()) {
                if ((migrationInfo.getVersion().compareTo(lastAppliedVersion) < 0)
                        && MigrationState.PENDING.equals(migrationInfo.getState())) {
                    migrationInfo.setState(MigrationState.IGNORED);
                }
            }
        }

        return new ArrayList<MigrationInfoImpl>(allMigrationsMap.values());
    }
}
