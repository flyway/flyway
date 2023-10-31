/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.core.api.migration.baseline;

import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.internal.info.MigrationInfoContext;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;

public class BaselineResolvedMigration extends ResolvedMigrationImpl {
    public BaselineResolvedMigration(MigrationVersion version,
                                     String description,
                                     String script,
                                     Integer checksum,
                                     Integer equivalentChecksum,
                                     String physicalLocation,
                                     MigrationExecutor executor,
                                     Configuration config) {
        this(version, description, script, checksum, equivalentChecksum, BaselineMigrationType.SQL_BASELINE, physicalLocation, executor, config);
    }

    public BaselineResolvedMigration(MigrationVersion version,
                                     String description,
                                     String script,
                                     Integer checksum,
                                     Integer equivalentChecksum,
                                     MigrationType migrationType,
                                     String physicalLocation,
                                     MigrationExecutor executor,
                                     Configuration config) {
        super(version, description, script, checksum, equivalentChecksum, migrationType, physicalLocation, executor);
    }

    @Override
    public boolean canCompareWith(ResolvedMigration o) {
        return o instanceof BaselineResolvedMigration;
    }

    @Override
    public MigrationState getState(MigrationInfoContext context) {
        MigrationState migrationState = super.getState(context);
        if (migrationState == MigrationState.PENDING && migrationsAppliedOrBaselineExists(context)) {
            return MigrationState.IGNORED;
        }
        if (migrationState == MigrationState.BASELINE_IGNORED && getVersion().equals(context.pendingBaseline)) {
            if (migrationsAppliedOrBaselineExists(context)) {
                return MigrationState.BASELINE_IGNORED;
            }
            return MigrationState.PENDING;
        }
        return migrationState;
    }

    private boolean migrationsAppliedOrBaselineExists(MigrationInfoContext context) {
        return context.appliedBaseline != null || context.lastApplied != MigrationVersion.EMPTY;
    }
}