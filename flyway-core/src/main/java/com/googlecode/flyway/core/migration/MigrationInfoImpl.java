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
package com.googlecode.flyway.core.migration;


import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.api.MigrationState;
import com.googlecode.flyway.core.api.MigrationVersion;

import java.util.Date;

/**
 * Default implementation of MigrationInfo.
 */
public class MigrationInfoImpl implements MigrationInfo {
    /**
     * The target version of this migration.
     */
    private final MigrationVersion version;

    /**
     * The description of the migration.
     */
    private final String description;

    /**
     * The name of the script to execute for this migration, relative to its classpath location.
     */
    private final String script;

    /**
     * The checksum of the migration.
     */
    private final Integer checksum;

    /**
     * The type of migration (INIT, SQL, ...)
     */
    private final com.googlecode.flyway.core.api.MigrationType migrationType;

    /**
     * The state of the migration (PENDING, SUCCESS, ...)
     */
    private com.googlecode.flyway.core.api.MigrationState migrationState = com.googlecode.flyway.core.api.MigrationState.PENDING;

    /**
     * The timestamp when this migration was installed. (Only for applied migrations)
     */
    private Date installedOn;

    /**
     * The execution time (in millis) of this migration. (Only for applied migrations)
     */
    private Integer executionTime;

    /**
     * The physical location of the migration on disk.
     */
    private String physicalLocation;

    /**
     * The executor to run this migration.
     */
    private MigrationExecutor executor;

    /**
     * Creates a new MigrationInfo. It will be initialized in state PENDING.
     *
     * @param version       The target version of this migration.
     * @param description   The description of the migration.
     * @param script        The name of the script to execute for this migration, relative to its classpath location.
     * @param checksum      The checksum of the migration.
     * @param migrationType The type of migration (INIT, SQL, ...)
     */
    public MigrationInfoImpl(MigrationVersion version, String description, String script, Integer checksum, com.googlecode.flyway.core.api.MigrationType migrationType) {
        this.version = version;
        this.description = description;
        this.script = script;
        this.checksum = checksum;
        this.migrationType = migrationType;
    }

    public com.googlecode.flyway.core.api.MigrationType getType() {
        return migrationType;
    }

    public Integer getChecksum() {
        return checksum;
    }

    public MigrationVersion getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getScript() {
        return script;
    }

    public MigrationState getState() {
        return migrationState;
    }

    public Date getInstalledOn() {
        return installedOn;
    }

    public Integer getExecutionTime() {
        return executionTime;
    }

    /**
     * @return The physical location of the migration on disk.
     */
    public String getPhysicalLocation() {
        return physicalLocation;
    }

    /**
     * @return The executor to run this migration.
     */
    public MigrationExecutor getExecutor() {
        return executor;
    }

    /**
     * @param migrationState The state of the migration (FAILED, SUCCESS, ...)
     */
    public void setState(MigrationState migrationState) {
        this.migrationState = migrationState;
    }

    /**
     * @param installedOn The timestamp when this migration was installed.
     */
    public void setInstalledOn(Date installedOn) {
        this.installedOn = installedOn;
    }

    /**
     * @param executionTime The execution time (in millis) of this migration.
     */
    public void setExecutionTime(Integer executionTime) {
        this.executionTime = executionTime;
    }

    /**
     * @param physicalLocation The physical location of the migration on disk.
     */
    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    /**
     * @param executor The executor to run this migration.
     */
    public void setExecutor(MigrationExecutor executor) {
        this.executor = executor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MigrationInfoImpl that = (MigrationInfoImpl) o;

        if (checksum != null ? !checksum.equals(that.checksum) : that.checksum != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (executionTime != null ? !executionTime.equals(that.executionTime) : that.executionTime != null)
            return false;
        if (installedOn != null ? !installedOn.equals(that.installedOn) : that.installedOn != null) return false;
        if (migrationState != that.migrationState) return false;
        if (migrationType != that.migrationType) return false;
        if (script != null ? !script.equals(that.script) : that.script != null) return false;
        return version.equals(that.version);
    }

    @Override
    public int hashCode() {
        int result = version.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (script != null ? script.hashCode() : 0);
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        result = 31 * result + migrationType.hashCode();
        result = 31 * result + migrationState.hashCode();
        result = 31 * result + (installedOn != null ? installedOn.hashCode() : 0);
        result = 31 * result + (executionTime != null ? executionTime.hashCode() : 0);
        return result;
    }

    public int compareTo(MigrationInfo o) {
        return version.compareTo(o.getVersion());
    }
}
