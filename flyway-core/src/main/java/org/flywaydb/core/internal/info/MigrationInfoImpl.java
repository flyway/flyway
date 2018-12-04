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
package org.flywaydb.core.internal.info;


import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.util.AbbreviationUtils;
import org.flywaydb.core.internal.util.ObjectUtils;

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
     * Whether this migration was applied out of order.
     */
    private final boolean outOfOrder;








    /**
     * Creates a new MigrationInfoImpl.
     *
     * @param resolvedMigration The resolved migration to aggregate the info from.
     * @param appliedMigration  The applied migration to aggregate the info from.
     * @param context           The current context.
     * @param outOfOrder        Whether this migration was applied out of order.



     */
    MigrationInfoImpl(ResolvedMigration resolvedMigration, AppliedMigration appliedMigration,
                      MigrationInfoContext context, boolean outOfOrder



    ) {
        this.resolvedMigration = resolvedMigration;
        this.appliedMigration = appliedMigration;
        this.context = context;
        this.outOfOrder = outOfOrder;



    }

    /**
     * @return The resolved migration to aggregate the info from.
     */
    public ResolvedMigration getResolvedMigration() {
        return resolvedMigration;
    }

    /**
     * @return The applied migration to aggregate the info from.
     */
    public AppliedMigration getAppliedMigration() {
        return appliedMigration;
    }

    @Override
    public MigrationType getType() {
        if (appliedMigration != null) {
            return appliedMigration.getType();
        }
        return resolvedMigration.getType();
    }

    @Override
    public Integer getChecksum() {
        if (appliedMigration != null) {
            return appliedMigration.getChecksum();
        }
        return resolvedMigration.getChecksum();
    }

    @Override
    public MigrationVersion getVersion() {
        if (appliedMigration != null) {
            return appliedMigration.getVersion();
        }
        return resolvedMigration.getVersion();
    }

    @Override
    public String getDescription() {
        if (appliedMigration != null) {
            return appliedMigration.getDescription();
        }
        return resolvedMigration.getDescription();
    }

    @Override
    public String getScript() {
        if (appliedMigration != null) {
            return appliedMigration.getScript();
        }
        return resolvedMigration.getScript();
    }

    @Override
    public MigrationState getState() {
        if (appliedMigration == null) {
            if (resolvedMigration.getVersion() != null) {
                if (resolvedMigration.getVersion().compareTo(context.baseline) < 0) {
                    return MigrationState.BELOW_BASELINE;
                }





                if (context.target != null && resolvedMigration.getVersion().compareTo(context.target) > 0) {
                    return MigrationState.ABOVE_TARGET;
                }
                if ((resolvedMigration.getVersion().compareTo(context.lastApplied) < 0) && !context.outOfOrder) {
                    return MigrationState.IGNORED;
                }
            }
            return MigrationState.PENDING;
        }

        if (MigrationType.BASELINE == appliedMigration.getType()) {
            return MigrationState.BASELINE;
        }

        if (resolvedMigration == null) {
            if (MigrationType.SCHEMA == appliedMigration.getType()) {
                return MigrationState.SUCCESS;
            }

            if ((appliedMigration.getVersion() == null) || getVersion().compareTo(context.lastResolved) < 0) {
                if (appliedMigration.isSuccess()) {
                    return MigrationState.MISSING_SUCCESS;
                }
                return MigrationState.MISSING_FAILED;
            } else {
                if (appliedMigration.isSuccess()) {
                    return MigrationState.FUTURE_SUCCESS;
                }
                return MigrationState.FUTURE_FAILED;
            }
        }

        if (!appliedMigration.isSuccess()) {
            return MigrationState.FAILED;
        }

        if (appliedMigration.getVersion() == null) {
            if (appliedMigration.getInstalledRank() == context.latestRepeatableRuns.get(appliedMigration.getDescription())) {
                if (ObjectUtils.nullSafeEquals(appliedMigration.getChecksum(), resolvedMigration.getChecksum())) {
                    return MigrationState.SUCCESS;
                }
                return MigrationState.OUTDATED;
            }
            return MigrationState.SUPERSEDED;
        }






        if (outOfOrder) {
            return MigrationState.OUT_OF_ORDER;
        }
        return MigrationState.SUCCESS;
    }

    @Override
    public Date getInstalledOn() {
        if (appliedMigration != null) {
            return appliedMigration.getInstalledOn();
        }
        return null;
    }

    @Override
    public String getInstalledBy() {
        if (appliedMigration != null) {
            return appliedMigration.getInstalledBy();
        }
        return null;
    }

    @Override
    public Integer getInstalledRank() {
        if (appliedMigration != null) {
            return appliedMigration.getInstalledRank();
        }
        return null;
    }

    @Override
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
        MigrationState state = getState();

        // Ignore any migrations above the current target as they are out of scope.
        if (MigrationState.ABOVE_TARGET.equals(state)) {
            return null;
        }

        if (state.isFailed() && (!context.future || MigrationState.FUTURE_FAILED != state)) {
            if (getVersion() == null) {
                return "Detected failed repeatable migration: " + getDescription();
            }
            return "Detected failed migration to version " + getVersion() + " (" + getDescription() + ")";
        }

        if ((resolvedMigration == null)
                && (appliedMigration.getType() != MigrationType.SCHEMA)
                && (appliedMigration.getType() != MigrationType.BASELINE)
                && (appliedMigration.getVersion() != null)
                && (!context.missing || (MigrationState.MISSING_SUCCESS != state && MigrationState.MISSING_FAILED != state))
                && (!context.future || (MigrationState.FUTURE_SUCCESS != state && MigrationState.FUTURE_FAILED != state))) {
            return "Detected applied migration not resolved locally: " + getVersion();
        }

        if (!context.pending && MigrationState.PENDING == state || (!context.ignored && MigrationState.IGNORED == state)) {
            if (getVersion() != null) {
                return "Detected resolved migration not applied to database: " + getVersion();
            }
            return "Detected resolved repeatable migration not applied to database: " + getDescription();
        }

        if (!context.pending && MigrationState.OUTDATED == state) {
            return "Detected outdated resolved repeatable migration that should be re-applied to database: " + getDescription();
        }

        if (resolvedMigration != null && appliedMigration != null) {
            String migrationIdentifier = appliedMigration.getVersion() == null ?
                    // Repeatable migrations
                    appliedMigration.getScript() :
                    // Versioned migrations
                    "version " + appliedMigration.getVersion();
            if (getVersion() == null || getVersion().compareTo(context.baseline) > 0) {
                if (resolvedMigration.getType() != appliedMigration.getType()) {
                    return createMismatchMessage("type", migrationIdentifier,
                            appliedMigration.getType(), resolvedMigration.getType());
                }



                    if (resolvedMigration.getVersion() != null
                            || (context.pending && MigrationState.OUTDATED != state && MigrationState.SUPERSEDED != state)) {
                        if (!ObjectUtils.nullSafeEquals(resolvedMigration.getChecksum(), appliedMigration.getChecksum())) {
                            return createMismatchMessage("checksum", migrationIdentifier,
                                    appliedMigration.getChecksum(), resolvedMigration.getChecksum());
                        }
                    }



                if (!AbbreviationUtils.abbreviateDescription(resolvedMigration.getDescription())
                        .equals(appliedMigration.getDescription())) {
                    return createMismatchMessage("description", migrationIdentifier,
                            appliedMigration.getDescription(), resolvedMigration.getDescription());
                }
            }
        }
        return null;
    }

    /**
     * Creates a message for a mismatch.
     *
     * @param mismatch            The type of mismatch.
     * @param migrationIdentifier The offending version.
     * @param applied             The applied value.
     * @param resolved            The resolved value.
     * @return The message.
     */
    private String createMismatchMessage(String mismatch, String migrationIdentifier, Object applied, Object resolved) {
        return String.format("Migration " + mismatch + " mismatch for migration %s\n" +
                        "-> Applied to database : %s\n" +
                        "-> Resolved locally    : %s",
                migrationIdentifier, applied, resolved);
    }

    @Override
    public int compareTo(MigrationInfo o) {
        if ((getInstalledRank() != null) && (o.getInstalledRank() != null)) {
            return getInstalledRank() - o.getInstalledRank();
        }

        MigrationState state = getState();
        MigrationState oState = o.getState();

        // Below baseline migrations come before applied ones
        if (state == MigrationState.BELOW_BASELINE && oState.isApplied()) {
            return Integer.MIN_VALUE;
        }
        if (state.isApplied() && oState == MigrationState.BELOW_BASELINE) {
            return Integer.MAX_VALUE;
        }

        if (state == MigrationState.IGNORED && oState.isApplied()) {
            if (getVersion() != null && o.getVersion() != null) {
                return getVersion().compareTo(o.getVersion());
            }
            return Integer.MIN_VALUE;
        }
        if (state.isApplied() && oState == MigrationState.IGNORED) {
            if (getVersion() != null && o.getVersion() != null) {
                return getVersion().compareTo(o.getVersion());
            }
            return Integer.MAX_VALUE;
        }

        // Sort installed before pending
        if (getInstalledRank() != null) {
            return Integer.MIN_VALUE;
        }
        if (o.getInstalledRank() != null) {
            return Integer.MAX_VALUE;
        }

        // No migration installed, sort according to other criteria
        // Two versioned migrations: sort by version
        if (getVersion() != null && o.getVersion() != null) {
            int v = getVersion().compareTo(o.getVersion());
            if (v != 0) {
                return v;
            }











            return 0;
        }

        // One versioned and one repeatable migration: versioned migration goes before repeatable one
        if (getVersion() != null) {
            return Integer.MIN_VALUE;
        }
        if (o.getVersion() != null) {
            return Integer.MAX_VALUE;
        }

        // Two repeatable migrations: sort by description
        return getDescription().compareTo(o.getDescription());
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