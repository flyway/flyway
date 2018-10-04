/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.util.ObjectUtils;

/**
 * A migration available on the classpath.
 */
public class ResolvedMigrationImpl implements ResolvedMigration {
    /**
     * The target version of this migration.
     */
    private MigrationVersion version;

    /**
     * The description of the migration.
     */
    private String description;

    /**
     * The name of the script to execute for this migration, relative to its classpath location.
     */
    private String script;

    /**
     * The checksum of the migration.
     */
    private Integer checksum;

    /**
     * The type of migration (INIT, SQL, ...)
     */
    private MigrationType type;

    /**
     * The physical location of the migration on disk.
     */
    private String physicalLocation;

    /**
     * The executor to run this migration.
     */
    private MigrationExecutor executor;

    @Override
    public MigrationVersion getVersion() {
        return version;
    }

    /**
     * @param version The target version of this migration.
     */
    public void setVersion(MigrationVersion version) {
        this.version = version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description of the migration.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getScript() {
        return script;
    }

    /**
     * @param script The name of the script to execute for this migration, relative to its classpath location.
     */
    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public Integer getChecksum() {
        return checksum;
    }

    /**
     * @param checksum The checksum of the migration.
     */
    public void setChecksum(Integer checksum) {
        this.checksum = checksum;
    }

    @Override
    public MigrationType getType() {
        return type;
    }

    /**
     * @param type The type of migration (INIT, SQL, ...)
     */
    public void setType(MigrationType type) {
        this.type = type;
    }

    @Override
    public String getPhysicalLocation() {
        return physicalLocation;
    }

    /**
     * @param physicalLocation The physical location of the migration on disk.
     */
    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    @Override
    public MigrationExecutor getExecutor() {
        return executor;
    }

    /**
     * @param executor The executor to run this migration.
     */
    public void setExecutor(MigrationExecutor executor) {
        this.executor = executor;
    }

    @SuppressWarnings("NullableProblems")
    public int compareTo(ResolvedMigrationImpl o) {
        return version.compareTo(o.version);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResolvedMigrationImpl migration = (ResolvedMigrationImpl) o;

        if (checksum != null ? !checksum.equals(migration.checksum) : migration.checksum != null) return false;
        if (description != null ? !description.equals(migration.description) : migration.description != null)
            return false;
        if (script != null ? !script.equals(migration.script) : migration.script != null) return false;
        if (type != migration.type) return false;
        return ObjectUtils.nullSafeEquals(version, migration.version);
    }

    @Override
    public int hashCode() {
        int result = (version != null ? version.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (script != null ? script.hashCode() : 0);
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ResolvedMigrationImpl{" +
                "version=" + version +
                ", description='" + description + '\'' +
                ", script='" + script + '\'' +
                ", checksum=" + checksum +
                ", type=" + type +
                ", physicalLocation='" + physicalLocation + '\'' +
                ", executor=" + executor +
                '}';
    }
}