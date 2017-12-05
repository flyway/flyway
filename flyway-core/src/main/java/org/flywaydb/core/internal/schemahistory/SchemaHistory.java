/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.schemahistory;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * The schema history used to track all applied migrations.
 */
public abstract class SchemaHistory {
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
     * Checks whether the schema history table contains at least one applied migration.
     *
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    public abstract boolean hasAppliedMigrations();

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
     * Checks whether the schema history table contains a marker row for schema baseline.
     *
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    public abstract boolean hasBaselineMarker();

    /**
     * Retrieves the baseline marker from the schema history table.
     *
     * @return The baseline marker or {@code null} if none could be found.
     */
    public abstract AppliedMigration getBaselineMarker();

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
    public abstract void addSchemasMarker(Schema[] schemas);

    protected final void doAddSchemasMarker(Schema[] schemas) {
        addAppliedMigration(null, "<< Flyway Schema Creation >>",
                MigrationType.SCHEMA, StringUtils.arrayToCommaDelimitedString(schemas), null, 0, true);
    }

    /**
     * Checks whether the schema history table contains a marker row for schema creation.
     *
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    public abstract boolean hasSchemasMarker();

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
        doAddAppliedMigration(version, abbreviateDescription(description), type, abbreviateScript(script), checksum,
                executionTime, success);
    }

    protected abstract void doAddAppliedMigration(MigrationVersion version, String description, MigrationType type,
                                                  String script, Integer checksum, int executionTime, boolean success);

    /**
     * Abbreviates this description to a length that will fit in the database.
     *
     * @param description The description to process.
     * @return The abbreviated version.
     */
    private String abbreviateDescription(String description) {
        if (description == null) {
            return null;
        }

        if (description.length() <= 200) {
            return description;
        }

        return description.substring(0, 197) + "...";
    }

    /**
     * Abbreviates this script to a length that will fit in the database.
     *
     * @param script The script to process.
     * @return The abbreviated version.
     */
    private String abbreviateScript(String script) {
        if (script == null) {
            return null;
        }

        if (script.length() <= 1000) {
            return script;
        }

        return "..." + script.substring(3, 1000);
    }
}
