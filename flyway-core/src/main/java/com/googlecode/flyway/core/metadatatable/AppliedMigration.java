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

import com.googlecode.flyway.core.api.MigrationType;
import com.googlecode.flyway.core.api.MigrationVersion;

import java.util.Date;

/**
 * A migration applied to the database (maps to a row in the metadata table).
 */
public class AppliedMigration {
    /**
     * The position of this version amongst all others. (For easy order by sorting)
     */
    private int versionRank;

    /**
     * The order in which this migration was applied amongst all others. (For out of order detection)
     */
    private int installedRank;

    /**
     * The target version of this migration.
     */
    private MigrationVersion version;

    /**
     * The description of the migration.
     */
    private String description;

    /**
     * The type of migration (INIT, SQL, ...)
     */
    private MigrationType migrationType;

    /**
     * The name of the script to execute for this migration, relative to its classpath location.
     */
    private String script;

    /**
     * The checksum of the migration. (Optional)
     */
    private Integer checksum;

    /**
     * The timestamp when this migration was installed.
     */
    private Date installedOn;

    /**
     * The user that installed this migration.
     */
    private String installedBy;

    /**
     * The execution time (in millis) of this migration.
     */
    private int executionTime;

    /**
     * Flag indicating whether the migration was successful or not.
     */
    private boolean success;

    /**
     * Creates a new applied migration.
     *
     * @param versionRank   The position of this version amongst all others. (For easy order by sorting)
     * @param installedRank The order in which this migration was applied amongst all others. (For out of order detection)
     * @param version       The target version of this migration.
     * @param description   The description of the migration.
     * @param migrationType The type of migration (INIT, SQL, ...)
     * @param script        The name of the script to execute for this migration, relative to its classpath location.
     * @param checksum      The checksum of the migration. (Optional)
     * @param installedOn   The timestamp when this migration was installed.
     * @param installedBy   The user that installed this migration.
     * @param executionTime The execution time (in millis) of this migration.
     * @param success       Flag indicating whether the migration was successful or not.
     */
    public AppliedMigration(int versionRank, int installedRank, MigrationVersion version, String description,
                            MigrationType migrationType, String script, Integer checksum, Date installedOn,
                            String installedBy, int executionTime, boolean success) {
        this.versionRank = versionRank;
        this.installedRank = installedRank;
        this.version = version;
        this.description = description;
        this.migrationType = migrationType;
        this.script = script;
        this.checksum = checksum;
        this.installedOn = installedOn;
        this.installedBy = installedBy;
        this.executionTime = executionTime;
        this.success = success;
    }

    /**
     * @return The position of this version amongst all others. (For easy order by sorting)
     */
    public int getVersionRank() {
        return versionRank;
    }

    /**
     * @return The order in which this migration was applied amongst all others. (For out of order detection)
     */
    public int getInstalledRank() {
        return installedRank;
    }

    /**
     * @return The target version of this migration.
     */
    public MigrationVersion getVersion() {
        return version;
    }

    /**
     * @return The description of the migration.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The type of migration (INIT, SQL, ...)
     */
    public MigrationType getMigrationType() {
        return migrationType;
    }

    /**
     * @return The name of the script to execute for this migration, relative to its classpath location.
     */
    public String getScript() {
        return script;
    }

    /**
     * @return The checksum of the migration. (Optional)
     */
    public Integer getChecksum() {
        return checksum;
    }

    /**
     * @return The timestamp when this migration was installed.
     */
    public Date getInstalledOn() {
        return installedOn;
    }

    /**
     * @return The user that installed this migration.
     */
    public String getInstalledBy() {
        return installedBy;
    }

    /**
     * @return The execution time (in millis) of this migration.
     */
    public int getExecutionTime() {
        return executionTime;
    }

    /**
     * @return Flag indicating whether the migration was successful or not.
     */
    public boolean isSuccess() {
        return success;
    }
}
