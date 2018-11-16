/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.schemahistory;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.util.AbbreviationUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * The schema history used to track all applied migrations.
 */
public abstract class SchemaHistory {
    /**
     * The schema history table used by flyway.
     * Non-final due to the table name fallback mechanism. Will be made final in Flyway 6.0.
     */
    protected Table table;

    /**
     * Acquires an exclusive read-write lock on the schema history table. This lock will be released automatically upon completion.
     *
     * @return The result of the action.
     */
    public abstract <T> T lock(Callable<T> callable);

    /**
     * @return Whether the schema history table exists.
     */
    public abstract boolean exists();

    /**
     * Creates the schema history. Do nothing if it already exists.
     */
    public abstract void create();

    /**
     * Checks whether the schema history table contains at least one non-synthetic applied migration.
     *
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    public final boolean hasNonSyntheticAppliedMigrations() {
        for (AppliedMigration appliedMigration : allAppliedMigrations()) {
            if (!appliedMigration.getType().isSynthetic()



                    ) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return The list of all migrations applied on the schema in the order they were applied (oldest first).
     * An empty list if no migration has been applied so far.
     */
    public abstract List<AppliedMigration> allAppliedMigrations();

    /**
     * Creates and initializes the Flyway schema history table.
     *
     * @param baselineVersion     The version to tag an existing schema with when executing baseline.
     * @param baselineDescription The description to tag an existing schema with when executing baseline.
     */
    public final void addBaselineMarker(MigrationVersion baselineVersion, String baselineDescription) {
        addAppliedMigration(baselineVersion, baselineDescription, MigrationType.BASELINE,
                baselineDescription, null, 0, true);
    }

    /**
     * Retrieves the baseline marker from the schema history table.
     *
     * @return The baseline marker or {@code null} if none could be found.
     */
    public final AppliedMigration getBaselineMarker() {
        List<AppliedMigration> appliedMigrations = allAppliedMigrations();
        // BASELINE can only be the first or second (in case there is a SCHEMA one) migration.
        for (int i = 0; i < Math.min(appliedMigrations.size(), 2); i++) {
            AppliedMigration appliedMigration = appliedMigrations.get(i);
            if (appliedMigration.getType() == MigrationType.BASELINE) {
                return appliedMigration;
            }
        }
        return null;
    }

    /**
     * <p>
     * Repairs the schema history table after a failed migration.
     * This is only necessary for databases without DDL-transaction support.
     * </p>
     * <p>
     * On databases with DDL transaction support, a migration failure automatically triggers a rollback of all changes,
     * including the ones in the schema history table.
     * </p>
     */
    public abstract void removeFailedMigrations();

    /**
     * Indicates in the schema history table that Flyway created these schemas.
     *
     * @param schemas The schemas that were created by Flyway.
     */
    public final void addSchemasMarker(Schema[] schemas) {
        addAppliedMigration(null, "<< Flyway Schema Creation >>",
                MigrationType.SCHEMA, StringUtils.arrayToCommaDelimitedString(schemas), null, 0, true);
    }

    /**
     * Checks whether the schema history table contains a marker row for schema creation.
     *
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    public final boolean hasSchemasMarker() {
        List<AppliedMigration> appliedMigrations = allAppliedMigrations();
        return !appliedMigrations.isEmpty() && appliedMigrations.get(0).getType() == MigrationType.SCHEMA;
    }


    /**
     * Updates this applied migration to match this resolved migration.
     *
     * @param appliedMigration  The applied migration to update.
     * @param resolvedMigration The resolved migration to source the new values from.
     */
    public abstract void update(AppliedMigration appliedMigration, ResolvedMigration resolvedMigration);

    /**
     * Clears the applied migration cache.
     */
    public void clearCache() {
        // Do nothing by default.
    }

    /**
     * Records a new applied migration.
     *
     * @param version       The target version of this migration.
     * @param description   The description of the migration.
     * @param type          The type of migration (BASELINE, SQL, ...)
     * @param script        The name of the script to execute for this migration, relative to its classpath location.
     * @param checksum      The checksum of the migration. (Optional)
     * @param executionTime The execution time (in millis) of this migration.
     * @param success       Flag indicating whether the migration was successful or not.
     */
    public final void addAppliedMigration(MigrationVersion version, String description, MigrationType type,
                                          String script, Integer checksum, int executionTime, boolean success) {
        int installedRank = type == MigrationType.SCHEMA ? 0 : calculateInstalledRank();
        doAddAppliedMigration(
                installedRank,
                version,
                AbbreviationUtils.abbreviateDescription(description),
                type,
                AbbreviationUtils.abbreviateScript(script),
                checksum,
                executionTime,
                success);
    }

    /**
     * Calculates the installed rank for the new migration to be inserted.
     *
     * @return The installed rank.
     */
    private int calculateInstalledRank() {
        List<AppliedMigration> appliedMigrations = allAppliedMigrations();
        if (appliedMigrations.isEmpty()) {
            return 1;
        }
        return appliedMigrations.get(appliedMigrations.size() - 1).getInstalledRank() + 1;
    }

    protected abstract void doAddAppliedMigration(int installedRank, MigrationVersion version, String description,
                                                  MigrationType type, String script, Integer checksum,
                                                  int executionTime, boolean success);

    @Override
    public String toString() {
        return table.toString();
    }
}