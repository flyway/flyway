/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package org.flywaydb.core.info;


import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.metadatatable.AppliedMigration;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.util.ObjectUtils;

import java.util.Date;

/**
 * Default implementation of MigrationInfo.
 */
public class MigrationInfoImpl implements MigrationInfo {
    /**
     * The resolved migration to aggregate the info from.
     */
    private final ResolvedMigration resolvedMigration;

    /**
     * The applied migration to aggregate the info from.
     */
    private final AppliedMigration appliedMigration;

    /**
     * The current context.
     */
    private final MigrationInfoContext context;

    /**
     * Creates a new MigrationInfoImpl.
     *
     * @param resolvedMigration The resolved migration to aggregate the info from.
     * @param appliedMigration  The applied migration to aggregate the info from.
     * @param context           The current context.
     */
    public MigrationInfoImpl(ResolvedMigration resolvedMigration, AppliedMigration appliedMigration,
                             MigrationInfoContext context) {
        this.resolvedMigration = resolvedMigration;
        this.appliedMigration = appliedMigration;
        this.context = context;
    }

    /**
     * @return The resolved migration to aggregate the info from.
     */
    public ResolvedMigration getResolvedMigration() {
        return resolvedMigration;
    }

    public MigrationType getType() {
        if (appliedMigration != null) {
            return appliedMigration.getType();
        }
        return resolvedMigration.getType();
    }

    public Integer getChecksum() {
        if (appliedMigration != null) {
            return appliedMigration.getChecksum();
        }
        return resolvedMigration.getChecksum();
    }

    public MigrationVersion getVersion() {
        if (appliedMigration != null) {
            return appliedMigration.getVersion();
        }
        return resolvedMigration.getVersion();
    }

    public String getDescription() {
        if (appliedMigration != null) {
            return appliedMigration.getDescription();
        }
        return resolvedMigration.getDescription();
    }

    public String getScript() {
        if (appliedMigration != null) {
            return appliedMigration.getScript();
        }
        return resolvedMigration.getScript();
    }

    public MigrationState getState() {
        if (appliedMigration == null) {
            if (resolvedMigration.getVersion().compareTo(context.init) < 0) {
                return MigrationState.PREINIT;
            }
            if (resolvedMigration.getVersion().compareTo(context.target) > 0) {
                return MigrationState.ABOVE_TARGET;
            }
            if ((resolvedMigration.getVersion().compareTo(context.lastApplied) < 0) && !context.outOfOrder) {
                return MigrationState.IGNORED;
            }
            return MigrationState.PENDING;
        }

        if (resolvedMigration == null) {
            if (MigrationType.SCHEMA == appliedMigration.getType()) {
                return MigrationState.SUCCESS;
            }

            if (MigrationType.INIT == appliedMigration.getType()) {
                return MigrationState.SUCCESS;
            }

            if (getVersion().compareTo(context.lastResolved) < 0) {
                if (appliedMigration.isSuccess()) {
                    return MigrationState.MISSING_SUCCESS;
                }
                return MigrationState.MISSING_FAILED;
            }
            if (getVersion().compareTo(context.lastResolved) > 0) {
                if (appliedMigration.isSuccess()) {
                    return MigrationState.FUTURE_SUCCESS;
                }
                return MigrationState.FUTURE_FAILED;
            }
        }

        if (appliedMigration.isSuccess()) {
            if (appliedMigration.getVersionRank() == appliedMigration.getInstalledRank()) {
                return MigrationState.SUCCESS;
            }
            return MigrationState.OUT_OF_ORDER;
        }
        return MigrationState.FAILED;
    }

    public Date getInstalledOn() {
        if (appliedMigration != null) {
            return appliedMigration.getInstalledOn();
        }
        return null;
    }

    public Integer getExecutionTime() {
        if (appliedMigration != null) {
            return appliedMigration.getExecutionTime();
        }
        return null;
    }

    /**
     * Validates this migrationInfo for consistency.
     *
     * @return The error message, or {@code null} if everything is fine.
     */
    public String validate() {
        if ((resolvedMigration == null)
                && (appliedMigration.getType() != MigrationType.SCHEMA)
                && (appliedMigration.getType() != MigrationType.INIT)) {
            return "Detected applied migration missing on the classpath: " + getVersion();
        }

        if (resolvedMigration != null && appliedMigration != null) {
            if (getVersion().compareTo(context.init) > 0) {
                if (resolvedMigration.getType() != appliedMigration.getType()) {
                    return String.format("Migration Type mismatch for migration %s: DB=%s, Classpath=%s",
                            appliedMigration.getScript(), appliedMigration.getType(), resolvedMigration.getType());
                }
                if (!ObjectUtils.nullSafeEquals(resolvedMigration.getChecksum(), appliedMigration.getChecksum())) {
                    return String.format("Migration Checksum mismatch for migration %s: DB=%s, Classpath=%s",
                            appliedMigration.getScript(), appliedMigration.getChecksum(), resolvedMigration.getChecksum());
                }
            }
        }
        return null;
    }

    @SuppressWarnings("NullableProblems")
    public int compareTo(MigrationInfo o) {
        return getVersion().compareTo(o.getVersion());
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MigrationInfoImpl that = (MigrationInfoImpl) o;

        if (appliedMigration != null ? !appliedMigration.equals(that.appliedMigration) : that.appliedMigration != null)
            return false;
        if (!context.equals(that.context)) return false;
        return !(resolvedMigration != null ? !resolvedMigration.equals(that.resolvedMigration) : that.resolvedMigration != null);
    }

    @Override
    public int hashCode() {
        int result = resolvedMigration != null ? resolvedMigration.hashCode() : 0;
        result = 31 * result + (appliedMigration != null ? appliedMigration.hashCode() : 0);
        result = 31 * result + context.hashCode();
        return result;
    }
}
