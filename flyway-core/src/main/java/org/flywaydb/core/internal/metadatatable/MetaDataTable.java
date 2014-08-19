/**
 * Copyright 2010-2014 Axel Fontaine
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

/**
 * The metadata table used to track all applied migrations.
 */
public interface MetaDataTable {
    /**
     * Acquires an exclusive read-write lock on the metadata table. This lock will be released automatically on commit.
     */
    void lock();

    /**
     * Adds this migration as executed to the metadata table.
     *
     * @param appliedMigration The migration that was executed.
     */
    void addAppliedMigration(AppliedMigration appliedMigration);

    /**
     * Checks whether the metadata table contains at least one applied migration.
     *
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    boolean hasAppliedMigrations();

    /**
     * @return The list of all migrations applied on the schema (oldest first). An empty list if no migration has been
     * applied so far.
     */
    List<AppliedMigration> allAppliedMigrations();

    /**
     * Creates and initializes the Flyway metadata table.
     *
     * @param initVersion     The version to tag an existing schema with when executing init.
     * @param initDescription The description to tag an existing schema with when executing init.
     */
    void addInitMarker(MigrationVersion initVersion, String initDescription);

    /**
     * Checks whether the metadata table contains a marker row for schema init.
     *
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    boolean hasInitMarker();

    /**
     * Retrieves the init marker from the metadata table.
     *
     * @return The init marker or {@code null} if none could be found.
     */
    AppliedMigration getInitMarker();

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
     * Update the checksum for this version to this new value.
     *
     * @param version  The version to update.
     * @param checksum The new checksum.
     */
    void updateChecksum(MigrationVersion version, Integer checksum);
}
