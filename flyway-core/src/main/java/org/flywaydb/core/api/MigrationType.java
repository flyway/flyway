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
package org.flywaydb.core.api;

/**
 * Type of migration.
 */
public enum MigrationType {
    /**
     * The type for the schema creation migration.
     */
    SCHEMA(true),

    /**
     * The type for the metadata baseline migration.
     */
    BASELINE(true),

    /**
     * The type for sql migrations.
     */
    SQL(false),

    /**
     * The type for Jdbc java-based migrations.
     */
    JDBC(false),

    /**
     * The type for Spring Jdbc java-based migrations.
     */
    SPRING_JDBC(false),

    /**
     * The type for other migrations by custom MigrationResolvers.
     */
    CUSTOM(false);

    private final boolean synthetic;

    MigrationType(boolean synthetic) {
        this.synthetic = synthetic;
    }

    /**
     * @return Whether this is a synthetic migration type, which is only ever present in the metadata table,
     * but never discovered by migration resolvers.
     */
    public boolean isSynthetic() {
        return synthetic;
    }
}
