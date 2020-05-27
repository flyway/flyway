/*
 * Copyright 2010-2020 Redgate Software Ltd
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

import java.util.Objects;

/**
 * A migration available on the classpath.
 */
public class ResolvedMigrationImpl implements ResolvedMigration {
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
     * The equivalent checksum of the migration. For versioned migrations, this is the same as the checksum.
     * For repeatable migrations, it is the checksum calculated prior to placeholder replacement.
     */
    private final Integer equivalentChecksum;

    /**
     * The checksum of the migration.
     */
    private final Integer checksum;

    /**
     * The type of migration (INIT, SQL, ...)
     */
    private final MigrationType type;

    /**
     * The physical location of the migration on disk.
     */
    private final String physicalLocation;

    /**
     * The executor to run this migration.
     */
    private final MigrationExecutor executor;

    /**
     * Creates a new resolved migration.
     *
     * @param version               The target version of this migration.
     * @param description           The description of the migration.
     * @param script                The name of the script to execute for this migration, relative to its classpath location.
     * @param checksum              The checksum of the migration.
     * @param equivalentChecksum    The equivalent checksum of the migration.
     * @param type                  The type of migration (SQL, ...)
     * @param physicalLocation      The physical location of the migration on disk.
     * @param executor              The executor to run this migration.
     */
    public ResolvedMigrationImpl(MigrationVersion version, String description, String script,
                                 Integer checksum, Integer equivalentChecksum,
                                 MigrationType type, String physicalLocation, MigrationExecutor executor) {
        this.version = version;
        this.description = description;
        this.script = script;
        this.checksum = checksum;
        this.equivalentChecksum = equivalentChecksum;
        this.type = type;
        this.physicalLocation = physicalLocation;
        this.executor = executor;
    }

    @Override
    public MigrationVersion getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public Integer getChecksum() {
        return checksum == null ?
                equivalentChecksum :
                checksum;
    }

    @Override
    public MigrationType getType() {
        return type;
    }

    @Override
    public String getPhysicalLocation() {
        return physicalLocation;
    }

    @Override
    public MigrationExecutor getExecutor() {
        return executor;
    }

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
        if (equivalentChecksum != null ? !equivalentChecksum.equals(migration.equivalentChecksum) : migration.equivalentChecksum != null) return false;
        if (description != null ? !description.equals(migration.description) : migration.description != null)
            return false;
        if (script != null ? !script.equals(migration.script) : migration.script != null) return false;
        if (type != migration.type) return false;
        return Objects.equals(version, migration.version);
    }

    @Override
    public int hashCode() {
        int result = (version != null ? version.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (script != null ? script.hashCode() : 0);
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        result = 31 * result + (equivalentChecksum != null ? equivalentChecksum.hashCode() : 0);
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ResolvedMigrationImpl{" +
                "version=" + version +
                ", description='" + description + '\'' +
                ", script='" + script + '\'' +
                ", checksum=" + getChecksum() +
                ", type=" + type +
                ", physicalLocation='" + physicalLocation + '\'' +
                ", executor=" + executor +
                '}';
    }

    /**
     * Validates this resolved migration.
     */
    public void validate() {
        // Do nothing by default.
    }

    @Override
    public boolean checksumMatches(Integer checksum) {
        return Objects.equals(checksum, this.checksum) ||
                Objects.equals(checksum, this.equivalentChecksum);
    }

    @Override
    public boolean checksumMatchesWithoutBeingIdentical(Integer checksum) {
        // The checksum in the database matches the one calculated without replacement, but not the one with.
        // That is, the script has placeholders and the checksum was originally calculated ignoring their values.
        return Objects.equals(checksum, this.equivalentChecksum)
                && !Objects.equals(checksum, this.checksum);
    }
}