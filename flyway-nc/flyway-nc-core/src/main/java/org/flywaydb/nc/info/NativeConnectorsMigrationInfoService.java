/*-
 * ========================LICENSE_START=================================
 * flyway-nc-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.nc.info;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.MigrationFilter;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.InfoResult;
import org.flywaydb.core.internal.nc.NativeConnectorsMigrationInfoFilter;

public class NativeConnectorsMigrationInfoService implements MigrationInfoService {

    private final MigrationInfo[] allMigrations;
    private final Configuration configuration;
    private final MigrationInfo[] applicableMigrations;
    private final String databaseName;
    private final Boolean allSchemasEmpty;

    public NativeConnectorsMigrationInfoService(final MigrationInfo[] migrations,
        final Configuration configuration,
        final String databaseName,
        final Boolean allSchemasEmpty) {
        this.allMigrations = migrations;
        this.configuration = configuration;
        this.allSchemasEmpty = allSchemasEmpty;
        applicableMigrations = getApplicableMigrations(migrations, configuration);
        this.databaseName = databaseName;
    }

    private MigrationInfo[] getApplicableMigrations(final MigrationInfo[] migrations,
        final Configuration configuration) {
        final MigrationInfo[] applicableMigrations;
        final List<NativeConnectorsMigrationInfoFilter> filters = configuration.getPluginRegister()
            .getLicensedPlugins(NativeConnectorsMigrationInfoFilter.class, configuration);
        MigrationInfo[] tempMigrations = Arrays.copyOf(migrations, migrations.length);
        for (final NativeConnectorsMigrationInfoFilter filter : filters) {
            final Predicate<MigrationInfo> predicate = filter.getFilter(configuration);
            tempMigrations = Arrays.stream(tempMigrations).filter(predicate).toArray(MigrationInfo[]::new);
        }
        applicableMigrations = tempMigrations;
        return applicableMigrations;
    }

    @Override
    public MigrationInfo[] all() {
        return allMigrations.clone();
    }

    @Override
    public MigrationInfo[] all(final MigrationFilter filter) {
        return Arrays.stream(allMigrations).filter(filter::matches).toArray(MigrationInfo[]::new);
    }

    @Override
    public MigrationInfo current() {
        MigrationInfo current = null;
        for (final MigrationInfo migrationInfo : allMigrations) {
            if (migrationInfo.getState().isApplied()
                && !MigrationState.DELETED.equals(migrationInfo.getState())
                && !migrationInfo.getType().equals(CoreMigrationType.DELETE)
                && !MigrationState.UNDONE.equals(migrationInfo.getState())
                && !migrationInfo.getType().isUndo()
                && migrationInfo.isVersioned()
                && (current == null || migrationInfo.getVersion().compareTo(current.getVersion()) > 0)) {
                current = migrationInfo;
            }
        }
        if (current != null) {
            return current;
        }

        // If no versioned migration has been applied so far, fall back to the latest repeatable one
        for (int i = allMigrations.length - 1; i >= 0; i--) {
            final MigrationInfo migrationInfo = allMigrations[i];
            if (migrationInfo.getState().isApplied()
                && !MigrationState.DELETED.equals(migrationInfo.getState())
                && !migrationInfo.getType().equals(CoreMigrationType.DELETE)
                && !MigrationState.UNDONE.equals(migrationInfo.getState())
                && migrationInfo.getVersion() == null) {
                return migrationInfo;
            }
        }

        return null;
    }

    @Override
    public MigrationInfo[] pending() {
        return Arrays.stream(applicableMigrations)
            .filter(migrationInfo -> MigrationState.PENDING == migrationInfo.getState())
            .toArray(MigrationInfo[]::new);
    }

    @Override
    public MigrationInfo[] applied() {
        return Arrays.stream(applicableMigrations).filter(x -> x.getState().isApplied()).toArray(MigrationInfo[]::new);
    }

    @Override
    public InfoResult getInfoResult() {
        return getInfoResult(all());
    }

    @Override
    public InfoResult getInfoResult(final MigrationFilter filter) {
        return getInfoResult(all(filter));
    }

    private InfoResult getInfoResult(MigrationInfo[] infos) {
        return CommandResultFactory.createInfoResult(configuration, databaseName, infos, current(), allSchemasEmpty);
    }
}
