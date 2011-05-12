/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.core.metadatatable;

import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationState;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.migration.SchemaVersion;

import java.util.Date;

/**
 * A row in the schema metadata table containing information about a migration that has already been applied to a db.
 */
public class MetaDataTableRow implements Comparable<MetaDataTableRow> {
    /**
     * The version of this migration.
     */
    private SchemaVersion schemaVersion;

    /**
     * The description for the migration history.
     */
    private String description;

    /**
     * The type of the migration (INIT, SQL or JAVA).
     */
    private MigrationType migrationType;

    /**
     * The script name for the migration history.
     */
    private String script;

    /**
     * The checksum of the migration.
     */
    private Integer checksum;

    /**
     * The timestamp when this migration was applied to the database. (Automatically set by the database)
     */
    private Date installedOn;

    /**
     * The time (in ms) it took to execute.
     */
    private Integer executionTime;

    /**
     * The state of this migration.
     */
    private MigrationState state;

    /**
     * Creates a new MetaDataTableRow. This constructor is here to support the rowmapper.
     *
     * @param schemaVersion The version of this migration.
     * @param description   The description for the migration history.
     * @param migrationType The type of the migration (INIT, SQL or JAVA).
     * @param script        The script name for the migration history.
     * @param checksum      The checksum of the migration.
     * @param installedOn   The timestamp when this migration was applied to the database. (Automatically set by the
     *                      database)
     * @param executionTime The time (in ms) it took to execute.
     * @param state         The state of this migration.
     */
    public MetaDataTableRow(SchemaVersion schemaVersion, String description, MigrationType migrationType, String script,
                            Integer checksum, Date installedOn, Integer executionTime, MigrationState state) {
        this.schemaVersion = schemaVersion;
        this.description = abbreviateDescription(description);
        this.migrationType = migrationType;
        this.script = abbreviateScript(script);
        this.checksum = checksum;
        this.installedOn = installedOn;
        this.executionTime = executionTime;
        this.state = state;
    }

    /**
     * Initializes a new metadatatable row with this migration.
     *
     * @param migration The migration that was or is being applied.
     */
    public MetaDataTableRow(Migration migration) {
        schemaVersion = migration.getVersion();
        description = abbreviateDescription(migration.getDescription());
        migrationType = migration.getMigrationType();
        script = abbreviateScript(migration.getScript());
        checksum = migration.getChecksum();
    }

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

        if (description.length() <= 100) {
            return description;
        }

        return description.substring(0, 97) + "...";
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

        if (script.length() <= 200) {
            return script;
        }

        return "..." + script.substring(3, 200);
    }

    /**
     * Updates this MetaDataTableRow with this execution time and this migration state.
     *
     * @param executionTime The time (in ms) it took to execute.
     * @param state         The state of this migration.
     */
    public void update(Integer executionTime, MigrationState state) {
        this.executionTime = executionTime;
        this.state = state;
    }

    /**
     * @return The type of the migration (INIT, SQL or JAVA).
     */
    public MigrationType getMigrationType() {
        return migrationType;
    }

    /**
     * @return The checksum of the migration.
     */
    public Integer getChecksum() {
        return checksum;
    }

    /**
     * @return The schema version after the migration is complete.
     */
    public SchemaVersion getVersion() {
        return schemaVersion;
    }

    /**
     * @return The description for the migration history.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The state of this migration.
     */
    public MigrationState getState() {
        return state;
    }

    /**
     * @return The timestamp when this migration was applied to the database. (Automatically set by the database)
     */
    public Date getInstalledOn() {
        return installedOn;
    }

    /**
     * @return The time (in ms) it took to execute.
     */
    public Integer getExecutionTime() {
        return executionTime;
    }

    /**
     * @return The script name for the migration history.
     */
    public String getScript() {
        return script;
    }

    public int compareTo(MetaDataTableRow o) {
        return getVersion().compareTo(o.getVersion());
    }
}
