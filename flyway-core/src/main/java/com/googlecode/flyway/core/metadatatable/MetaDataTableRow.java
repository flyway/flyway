/**
 * Copyright (C) 2010-2012 the original author or authors.
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

import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.migration.MigrationState;
import com.googlecode.flyway.core.migration.SchemaVersion;

import java.util.Date;

/**
 * A row in the schema metadata table containing information about a migration that has already been applied to a db.
 *
 * @deprecated Superseeded by MigrationInfo. Will be removed in Flyway 3.0.
 */
@Deprecated
public class MetaDataTableRow implements Comparable<MetaDataTableRow> {
    /**
     * The migration info this maps to.
     */
    private MigrationInfo migrationInfo;

    /**
     * Initializes a new metadatatable row with this migration info.
     *
     * @param migrationInfo The migration that was or is being applied.
     */
    public MetaDataTableRow(MigrationInfo migrationInfo) {
        this.migrationInfo = migrationInfo;
    }

    /**
     * @return The migration info this maps to.
     */
    public MigrationInfo getMigrationInfo() {
        return migrationInfo;
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
     * @return The checksum of the migration.
     */
    public Integer getChecksum() {
        return migrationInfo.getChecksum();
    }

    /**
     * @return The schema version after the migration is complete.
     */
    public SchemaVersion getVersion() {
        return new SchemaVersion(migrationInfo.getVersion().toString());
    }

    /**
     * @return The description for the migration history.
     */
    public String getDescription() {
        return abbreviateDescription(migrationInfo.getDescription());
    }

    /**
     * @return The state of this migration.
     */
    public MigrationState getState() {
        if (migrationInfo.getState().equals(com.googlecode.flyway.core.api.MigrationState.FAILED)) {
            return MigrationState.FAILED;
        }

        return MigrationState.SUCCESS;
    }

    /**
     * @return The timestamp when this migration was applied to the database. (Automatically set by the database)
     */
    public Date getInstalledOn() {
        return migrationInfo.getInstalledOn();
    }

    /**
     * @return The time (in ms) it took to execute.
     */
    public Integer getExecutionTime() {
        return migrationInfo.getExecutionTime();
    }

    /**
     * @return The script name for the migration history.
     */
    public String getScript() {
        return abbreviateScript(migrationInfo.getScript());
    }

    public int compareTo(MetaDataTableRow o) {
        return getVersion().compareTo(o.getVersion());
    }
}
