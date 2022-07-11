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
package org.flywaydb.core.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.extensibility.MigrationType;

@RequiredArgsConstructor
@Getter
public enum CoreMigrationType implements MigrationType {
    /**
     * Schema creation migration.
     */
    SCHEMA(true, false, false),
    /**
     * Baseline migration.
     */
    BASELINE(true, false, true),
    /**
     * Deleted migration
     */
    DELETE(true, false, false),
    /**
     * SQL migrations.
     */
    SQL(false, false, false),
    /**
     * Undo SQL migrations.
     */
    UNDO_SQL(false, true, false),
    /**
     * JDBC Java-based migrations.
     */
    JDBC(false, false, false),
    /**
     * Undo JDBC java-based migrations.
     */
    UNDO_JDBC(false, true, false),
    /**
     * Script migrations.
     */
    SCRIPT(false, false, false),
    /**
     * @deprecated Will be removed and replaced with just SCRIPT
     *
     * Script baseline migrations.
     */
    @Deprecated
    SCRIPT_BASELINE(false, false, true),
    /**
     * Undo Script migrations.
     */
    UNDO_SCRIPT(false, true, false),
    /**
     * Migrations using custom MigrationResolvers.
     */
    CUSTOM(false, false, false),
    /**
     * Undo migrations using custom MigrationResolvers.
     */
    UNDO_CUSTOM(false, true, false);

    /**
     * @return Whether this is a synthetic migration type, which is only ever present in the schema history table,
     * but never discovered by migration resolvers.
     */
    private final boolean synthetic;
    /**
     * @return Whether this is an undo migration, which has undone an earlier migration present in the schema history table.
     */
    private final boolean undo;
    /**
     * @return Whether this is a baseline type
     */
    private final boolean baseline;

    public static MigrationType fromString(String migrationType) {
        // Convert legacy types to maintain compatibility
        if ("SPRING_JDBC".equals(migrationType)) {
            return JDBC;
        }
        if ("UNDO_SPRING_JDBC".equals(migrationType)) {
            return UNDO_JDBC;
        }
        return valueOf(migrationType);
    }
}