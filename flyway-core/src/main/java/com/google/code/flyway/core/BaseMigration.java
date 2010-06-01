/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.google.code.flyway.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Base migration for migrations that use the standard Flyway version + description embedding in their name.
 * These migrations have names like V1_2__Description .
 */
public abstract class BaseMigration implements Migration {
    /**
     * Logger.
     */
    private static final Log log = LogFactory.getLog(BaseMigration.class);

    /**
     * The target schema version of this migration.
     */
    private SchemaVersion schemaVersion;

    /**
     * The state of this migration.
     */
    private MigrationState migrationState = MigrationState.UNKNOWN;

    /**
     * The time (in ms) it took to execute.
     */
    private long executionTime = -1;

    @Override
    public SchemaVersion getVersion() {
        if (schemaVersion == null) {
            throw new IllegalStateException("SchemaVersion not initialized properly");
        }
        return schemaVersion;
    }

    /**
     * Initializes the version of this Migration based on this standard Flyway name.
     *
     * @param migrationName The migration name in standard Flyway format. Ex.: V1_2__Description .
     */
    protected final void initVersion(String migrationName) {
        this.schemaVersion = extractSchemaVersion(migrationName);
    }

    /**
     * Extracts the schema version from a migration name formatted as V1_2__Description.
     *
     * @param migrationName The string to parse.
     * @return The extracted schema version.
     */
    /* private -> for testing */
    static SchemaVersion extractSchemaVersion(String migrationName) {
        // Drop the leading V
        String version = migrationName.substring(1);

        // Handle the description
        String description = null;
        int descriptionPos = version.indexOf("__");
        if (descriptionPos != -1) {
            description = version.substring(descriptionPos + 2).replaceAll("_", " ");
            version = version.substring(0, descriptionPos);
        }

        return new SchemaVersion(version.replace("_", "."), description);
    }

    @Override
    public MigrationState getState() {
        return migrationState;
    }

    @Override
    public long getExecutionTime() {
        return executionTime;
    }

    @Override
    public final void migrate(SimpleJdbcTemplate jdbcTemplate) {
        final long start = System.currentTimeMillis();
        try {
            doMigrate(jdbcTemplate);
            migrationState = MigrationState.SUCCESS;
        } catch (Exception e) {
            log.fatal("Migration failed: " + schemaVersion + " - " + getScriptName(), e);
            migrationState = MigrationState.FAILED;
        }
        long finish = System.currentTimeMillis();
        executionTime = finish - start;
    }

    /**
     * Performs the migration.
     *
     * @param jdbcTemplate To execute the migration statements.
     *
     * @throws Exception Thrown when the migration failed.
     */
    protected abstract void doMigrate(SimpleJdbcTemplate jdbcTemplate) throws Exception;
}
