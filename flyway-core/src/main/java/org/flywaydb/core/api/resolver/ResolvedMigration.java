/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.core.api.resolver;

import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.internal.info.MigrationInfoContext;

/**
 * Migration resolved through a MigrationResolver. Can be applied against a database.
 */
public interface ResolvedMigration extends ChecksumMatcher {
    /**
     * @return The version of the database after applying this migration. {@code null} for repeatable migrations.
     */
    MigrationVersion getVersion();

    default boolean isVersioned()  {
        return getVersion() != null;
    }

    /**
     * @return The description of the migration.
     */
    String getDescription();

    /**
     * @return The name of the script to execute for this migration, relative to its base (classpath/filesystem) location.
     */
    String getScript();

    /**
     * @return The checksum of the migration. Optional. Can be {@code null} if not unique checksum is computable.
     */
    Integer getChecksum();

    /**
     * @return The type of migration (INIT, SQL, ...)
     */
    MigrationType getType();

    /**
     * @return The physical location of the migration on disk. Used for more precise error reporting in case of conflict.
     */
    String getPhysicalLocation();

    /**
     * @return The executor to run this migration.
     */
    MigrationExecutor getExecutor();

    default MigrationState getState(MigrationInfoContext context) {
        if (getVersion() != null) {
            if (getVersion().compareTo(context.lastApplied == MigrationVersion.EMPTY ? context.pendingBaseline : context.appliedBaseline) < 0) {
                return MigrationState.BELOW_BASELINE;
            }
            if (getVersion().compareTo(context.lastApplied == MigrationVersion.EMPTY ? context.pendingBaseline : context.appliedBaseline) == 0) {
                return MigrationState.BASELINE_IGNORED;
            }
            if (context.target != null && context.target != MigrationVersion.NEXT && getVersion().compareTo(context.target) > 0) {
                return MigrationState.ABOVE_TARGET;
            }
            if ((getVersion().compareTo(context.lastApplied) < 0) && !context.outOfOrder) {
                return MigrationState.IGNORED;
            }
        }
        return MigrationState.PENDING;
    }

    default boolean canCompareWith(ResolvedMigration o) {
        return true;
    }
}
