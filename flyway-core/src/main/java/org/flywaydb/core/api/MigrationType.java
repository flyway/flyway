/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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

public enum MigrationType {
    /**
     * Schema creation migration.
     */
    SCHEMA(true, false, false),
    /**
     * Baseline migration.
     */
    BASELINE(true, false, false),
    /**
     * Deleted migration
     */
    DELETE(true, false, false),
    /**
     * SQL migrations.
     */
    SQL(false, false, false),
    /**
     * SQL state scripts.
     */
    SQL_STATE_SCRIPT(false, false, true),
    /**
     * Undo SQL migrations.
     */
    UNDO_SQL(false, true, false),
    /**
     * JDBC Java-based migrations.
     */
    JDBC(false, false, false),
    /**
     * JDBC Java-based state scripts.
     */
    JDBC_STATE_SCRIPT(false, false, true),
    /**
     * Undo JDBC java-based migrations.
     */
    UNDO_JDBC(false, true, false),










    /**
     * Migrations using custom MigrationResolvers.
     */
    CUSTOM(false, false, false),
    /**
     * Undo migrations using custom MigrationResolvers.
     */
    UNDO_CUSTOM(false, true, false);

    private final boolean synthetic;
    private final boolean undo;
    private final boolean stateScript;

    MigrationType(boolean synthetic, boolean undo, boolean stateScript) {
        this.synthetic = synthetic;
        this.undo = undo;
        this.stateScript = stateScript;
    }

    /**
     * @return Whether this is a synthetic migration type, which is only ever present in the schema history table,
     * but never discovered by migration resolvers.
     */
    public boolean isSynthetic() {
        return synthetic;
    }

    /**
     * @return Whether this is an undo migration, which has undone an earlier migration present in the schema history table.
     */
    public boolean isUndo() {
        return undo;
    }

    /**
     * @return Whether this is a state script, which represents all migrations with
     * version <= current state script version.
     */
    public boolean isStateScript() {
        return stateScript;
    }
}