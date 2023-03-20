/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.core.extensibility;

import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.info.AppliedMigrationAttributes;
import org.flywaydb.core.internal.info.MigrationInfoContext;
import org.flywaydb.core.internal.util.Pair;

import java.util.Date;
import java.util.List;

/**
 * A migration applied to the database (maps to a row in the schema history table).
 */
public interface AppliedMigration extends Plugin {
    /**
     * @return The order in which this migration was applied amongst all others.
     */
    int getInstalledRank();

    /**
     * @return The version of this migration.
     */
    MigrationVersion getVersion();

    /**
     * @return The description of this migration.
     */
    String getDescription();

    /**
     * @return The type of this migration.
     */
    MigrationType getType();

    /**
     * @return The name of the script to execute for this migration, relative to its classpath location.
     */
    String getScript();

    /**
     * @return The checksum of this migration. (Optional)
     */
    Integer getChecksum();

    /**
     * @return The timestamp when this migration was installed.
     */
    Date getInstalledOn();

    /**
     * @return The user that installed this migration.
     */
    String getInstalledBy();

    /**
     * @return The execution time (in milliseconds) of this migration.
     */
    int getExecutionTime();

    /**
     * @return whether this migration was successful or not.
     */
    boolean isSuccess();

    /**
     * @return whether this migration handles the given migration type.
     */
    boolean handlesType(String type);

    default boolean isVersioned() {
        return getVersion() != null;
    }

    /**
     * @return a new instance of this type of applied migration from the given arguments.
     */
    AppliedMigration create(int installedRank,
                            MigrationVersion version,
                            String description,
                            String type,
                            String script,
                            Integer checksum,
                            Date installedOn,
                            String installedBy,
                            int executionTime,
                            boolean success);

    MigrationState getState(MigrationInfoContext context, boolean outOfOrder, ResolvedMigration resolvedMigration);

    /**
     * Updates the attributes in the given list of migrations based on the information in this migration
     */
    default void updateAttributes(List<Pair<AppliedMigration, AppliedMigrationAttributes>> appliedMigrations) { }

    default int compareTo(AppliedMigration o) {
        return getInstalledRank() - o.getInstalledRank();
    }
}