/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.metadatatable;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.dbsupport.Schema;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * The metadata table used to track all applied migrations.
 */
public interface MetaDataTable {
    /**
     * Acquires an exclusive read-write lock on the metadata table. This lock will be released automatically upon completion.
     *
     * @return The result of the action.
     */
    <T> T lock(Callable<T> callable);

    /**
     * Adds this migration as executed to the metadata table.
     *
     * @param appliedMigration The migration that was executed.
     */
    void addAppliedMigration(AppliedMigration appliedMigration);

    /**
     * @return Whether the metadata table exists.
     */
    boolean exists();

    /**
     * Checks whether the metadata table contains at least one applied migration.
     *
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    boolean hasAppliedMigrations();

    /**
     * @return The list of all migrations applied on the schema in the order they were applied (oldest first).
     * An empty list if no migration has been applied so far.
     */
    List<AppliedMigration> allAppliedMigrations();

    /**
     * Creates and initializes the Flyway metadata table.
     *
     * @param initVersion     The version to tag an existing schema with when executing baseline.
     * @param initDescription The description to tag an existing schema with when executing baseline.
     */
    void addBaselineMarker(MigrationVersion initVersion, String initDescription);

    /**
     * Checks whether the metadata table contains a marker row for schema baseline.
     *
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    boolean hasBaselineMarker();

    /**
     * Retrieves the baseline marker from the metadata table.
     *
     * @return The baseline marker or {@code null} if none could be found.
     */
    AppliedMigration getBaselineMarker();

    /**
     * <p>
     * Repairs the metadata table after a failed migration.
     * This is only necessary for databases without DDL-transaction support.
     * </p>
     * <p>
     * On databases with DDL transaction support, a migration failure automatically triggers a rollback of all changes,
     * including the ones in the metadata table.
     * </p>
     */
    void removeFailedMigrations();

    /**
     * Indicates in the metadata table that Flyway created these schemas.
     *
     * @param schemas The schemas that were created by Flyway.
     */
    void addSchemasMarker(Schema[] schemas);

    /**
     * Checks whether the metadata table contains a marker row for schema creation.
     *
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    boolean hasSchemasMarker();

    /**
     * Update the description and checksum for this version to these new values.
     *
     * @param version     The version to update.
     * @param description The new description.
     * @param checksum    The new checksum.
     */
    void update(MigrationVersion version, String description, Integer checksum);

    /**
     * Upgrades the Metadata table to Flyway 4.0 format if necessary.
     *
     * @return {@code true} if it was upgraded.
     */
    boolean upgradeIfNecessary();

    /**
     * Clears the applied migration cache.
     */
    void clearCache();
}
