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

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.ObjectUtils;

import java.util.Date;

/**
 * A migration applied to the database (maps to a row in the metadata table).
 */
public class AppliedMigration implements Comparable<AppliedMigration> {
    /**
     * The order in which this migration was applied amongst all others. (For out of order detection)
     */
    private int installedRank;

    /**
     * The target version of this migration. {@code null} if it is a repeatable migration.
     */
    private MigrationVersion version;

    /**
     * The description of the migration.
     */
    private String description;

    /**
     * The type of migration (BASELINE, SQL, ...)
     */
    private MigrationType type;

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
     * Creates a new applied migration. Only called from the RowMapper.
     *
     * @param installedRank The order in which this migration was applied amongst all others. (For out of order detection)
     * @param version       The target version of this migration.
     * @param description   The description of the migration.
     * @param type          The type of migration (INIT, SQL, ...)
     * @param script        The name of the script to execute for this migration, relative to its classpath location.
     * @param checksum      The checksum of the migration. (Optional)
     * @param installedOn   The timestamp when this migration was installed.
     * @param installedBy   The user that installed this migration.
     * @param executionTime The execution time (in millis) of this migration.
     * @param success       Flag indicating whether the migration was successful or not.
     */
    public AppliedMigration(int installedRank, MigrationVersion version, String description,
                     MigrationType type, String script, Integer checksum, Date installedOn,
                     String installedBy, int executionTime, boolean success) {
        this.installedRank = installedRank;
        this.version = version;
        this.description = description;
        this.type = type;
        this.script = script;
        this.checksum = checksum;
        this.installedOn = installedOn;
        this.installedBy = installedBy;
        this.executionTime = executionTime;
        this.success = success;
    }

    /**
     * Creates a new applied migration.
     *
     * @param version       The target version of this migration.
     * @param description   The description of the migration.
     * @param type          The type of migration (BASELINE, SQL, ...)
     * @param script        The name of the script to execute for this migration, relative to its classpath location.
     * @param checksum      The checksum of the migration. (Optional)
     * @param executionTime The execution time (in millis) of this migration.
     * @param success       Flag indicating whether the migration was successful or not.
     */
    public AppliedMigration(MigrationVersion version, String description, MigrationType type, String script,
                            Integer checksum, int executionTime, boolean success) {
        this.version = version;
        this.description = abbreviateDescription(description);
        this.type = type;
        this.script = abbreviateScript(script);
        this.checksum = checksum;
        this.executionTime = executionTime;
        this.success = success;
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

        if (description.length() <= 200) {
            return description;
        }

        return description.substring(0, 197) + "...";
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

        if (script.length() <= 1000) {
            return script;
        }

        return "..." + script.substring(3, 1000);
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
     * @return The type of migration (BASELINE, SQL, ...)
     */
    public MigrationType getType() {
        return type;
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

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppliedMigration that = (AppliedMigration) o;

        if (executionTime != that.executionTime) return false;
        if (installedRank != that.installedRank) return false;
        if (success != that.success) return false;
        if (checksum != null ? !checksum.equals(that.checksum) : that.checksum != null) return false;
        if (!description.equals(that.description)) return false;
        if (installedBy != null ? !installedBy.equals(that.installedBy) : that.installedBy != null) return false;
        if (installedOn != null ? !installedOn.equals(that.installedOn) : that.installedOn != null) return false;
        if (!script.equals(that.script)) return false;
        if (type != that.type) return false;
        return ObjectUtils.nullSafeEquals(version, that.version);
    }

    @Override
    public int hashCode() {
        int result = installedRank;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + description.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + script.hashCode();
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        result = 31 * result + (installedOn != null ? installedOn.hashCode() : 0);
        result = 31 * result + (installedBy != null ? installedBy.hashCode() : 0);
        result = 31 * result + executionTime;
        result = 31 * result + (success ? 1 : 0);
        return result;
    }

    @SuppressWarnings("NullableProblems")
    public int compareTo(AppliedMigration o) {
        return installedRank - o.installedRank;
    }
}
