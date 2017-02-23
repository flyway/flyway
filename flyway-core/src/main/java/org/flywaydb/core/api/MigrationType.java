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
    SCHEMA,

    /**
     * The type for the metadata baseline migration.
     */
    BASELINE,

    /**
     * The type for sql migrations.
     */
    SQL,

    /**
     * The type for Jdbc java-based migrations.
     */
    JDBC,

    /**
     * The type for Spring Jdbc java-based migrations.
     */
    SPRING_JDBC,

    /**
     * The type for other migrations by custom MigrationResolvers.
     */
    CUSTOM
}
