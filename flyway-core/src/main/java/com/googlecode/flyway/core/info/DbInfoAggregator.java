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
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.migration.ExecutableMigration;
import com.googlecode.flyway.core.migration.MigrationResolver;

import java.util.ArrayList;
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
     * Creates a new info aggregator.
     *
     * @param migrationResolver The migration resolver for available migrations.
     * @param metaDataTable The metadata table for applied migrations.
     */
    public DbInfoAggregator(MigrationResolver migrationResolver, MetaDataTable metaDataTable) {
        this.migrationResolver = migrationResolver;
        this.metaDataTable = metaDataTable;
    }

    /**
     * Aggregates info about all known migrations from both the classpath and the DB.
     *
     * @return The info about the migrations.
     */
    public MigrationInfos aggregateMigrationInfo() {
        List<ExecutableMigration> executableMigrations = migrationResolver.resolveMigrations();
        List<MigrationInfo> migrationInfos = extractMigrationInfos(executableMigrations);

        metaDataTable.allAppliedMigrations();

        return new MigrationInfos();
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
