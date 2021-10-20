/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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

import org.flywaydb.core.api.*;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.AbbreviationUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.stream.IntStream;

public class MigrationInfoImpl implements MigrationInfo {
    private final ResolvedMigration resolvedMigration;
    private final AppliedMigration appliedMigration;
    private final MigrationInfoContext context;
    private final boolean outOfOrder;
    private final boolean deleted;
    private final boolean shouldNotExecuteMigration;




    MigrationInfoImpl(ResolvedMigration resolvedMigration, AppliedMigration appliedMigration,
                      MigrationInfoContext context, boolean outOfOrder, boolean deleted, boolean undone) {
        this.resolvedMigration = resolvedMigration;
        this.appliedMigration = appliedMigration;
        this.context = context;
        this.outOfOrder = outOfOrder;
        this.deleted = deleted;
        this.shouldNotExecuteMigration = shouldNotExecuteMigration(resolvedMigration);



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






        if (deleted) {
            return MigrationState.DELETED;
        }

        if (appliedMigration == null) {








            if (shouldNotExecuteMigration) {
                return MigrationState.IGNORED;
            }

            if (resolvedMigration.getVersion() != null) {

                if (resolvedMigration.getVersion().compareTo(context.baseline) < 0) {
                    return MigrationState.BELOW_BASELINE;
                }





                if (context.target != null && context.target != MigrationVersion.NEXT && resolvedMigration.getVersion().compareTo(context.target) > 0) {
                    return MigrationState.ABOVE_TARGET;
                }
                if ((resolvedMigration.getVersion().compareTo(context.lastApplied) < 0) && !context.outOfOrder) {
                    return MigrationState.IGNORED;
                }
                if (resolvedMigration.getVersion().compareTo(context.latestBaselineMigration) < 0 ||
                        (resolvedMigration.getVersion().compareTo(context.latestBaselineMigration) == 0 && !resolvedMigration.getType().isBaselineMigration())) {
                        return MigrationState.IGNORED;
                }
            }
            return MigrationState.PENDING;
        }

        if (MigrationType.DELETE == appliedMigration.getType()) {
            return MigrationState.SUCCESS;
        }

        if (MigrationType.BASELINE == appliedMigration.getType()) {
            return MigrationState.BASELINE;
        }









        if (resolvedMigration == null && isRepeatableLatest()) {








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
                if (resolvedMigration != null && resolvedMigration.checksumMatches(appliedMigration.getChecksum())) {
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

    private boolean isRepeatableLatest() {
        if (appliedMigration.getVersion() != null) {
            return true;
        }

        Integer latestRepeatableRank = context.latestRepeatableRuns.get(appliedMigration.getDescription());
        return latestRepeatableRank == null || appliedMigration.getInstalledRank() == latestRepeatableRank;
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

    @Override
    public String getPhysicalLocation() {
        if (resolvedMigration != null) {
            return resolvedMigration.getPhysicalLocation();
        }
        return "";
    }

    /**
     * @return The error code with the relevant validation message, or {@code null} if everything is fine.
     */
    public ErrorDetails validate() {
        MigrationState state = getState();







        if (MigrationState.ABOVE_TARGET.equals(state)) {
            return null;
        }

        if (MigrationState.DELETED.equals(state)) {
            return null;
        }

        if (Arrays.stream(context.ignorePatterns).anyMatch(p -> p.matchesMigration(getVersion() != null, state))) {
            return null;
        }

        if (state.isFailed() && (!context.future || MigrationState.FUTURE_FAILED != state)) {
            if (getVersion() == null) {
                String errorMessage = "Detected failed repeatable migration: " + getDescription() + ". Please remove any half-completed changes then run repair to fix the schema history.";
                return new ErrorDetails(ErrorCode.FAILED_REPEATABLE_MIGRATION, errorMessage);
            }
            String errorMessage = "Detected failed migration to version " + getVersion() + " (" + getDescription() + ")" + ". Please remove any half-completed changes then run repair to fix the schema history.";
            return new ErrorDetails(ErrorCode.FAILED_VERSIONED_MIGRATION, errorMessage);
        }







        if ((resolvedMigration == null)
                && !appliedMigration.getType().isSynthetic()




                && (MigrationState.SUPERSEDED != state)
                && (!context.missing || (MigrationState.MISSING_SUCCESS != state && MigrationState.MISSING_FAILED != state))
                && (!context.future || (MigrationState.FUTURE_SUCCESS != state && MigrationState.FUTURE_FAILED != state))) {
            if (appliedMigration.getVersion() != null) {
                String errorMessage = "Detected applied migration not resolved locally: " + getVersion() + ". If you removed this migration intentionally, run repair to mark the migration as deleted.";
                return new ErrorDetails(ErrorCode.APPLIED_VERSIONED_MIGRATION_NOT_RESOLVED, errorMessage);
            } else {
                String errorMessage = "Detected applied migration not resolved locally: " + getDescription() + ". If you removed this migration intentionally, run repair to mark the migration as deleted.";
                return new ErrorDetails(ErrorCode.APPLIED_REPEATABLE_MIGRATION_NOT_RESOLVED, errorMessage);
            }
        }

        if (!context.ignored && MigrationState.IGNORED == state) {
            if (shouldNotExecuteMigration) {
                return null;
            }
            if (getVersion() != null) {
                String errorMessage = "Detected resolved migration not applied to database: " + getVersion() + ". To ignore this migration, set -ignoreIgnoredMigrations=true. To allow executing this migration, set -outOfOrder=true.";
                return new ErrorDetails(ErrorCode.RESOLVED_VERSIONED_MIGRATION_NOT_APPLIED, errorMessage);
            }
            String errorMessage = "Detected resolved repeatable migration not applied to database: " + getDescription() + ". To ignore this migration, set -ignoreIgnoredMigrations=true.";
            return new ErrorDetails(ErrorCode.RESOLVED_REPEATABLE_MIGRATION_NOT_APPLIED, errorMessage);
        }

        if (!context.pending && MigrationState.PENDING == state) {
            if (getVersion() != null) {
                String errorMessage = "Detected resolved migration not applied to database: " + getVersion() + ". To fix this error, either run migrate, or set -ignorePendingMigrations=true.";
                return new ErrorDetails(ErrorCode.RESOLVED_VERSIONED_MIGRATION_NOT_APPLIED, errorMessage);
            }
            String errorMessage = "Detected resolved repeatable migration not applied to database: " + getDescription() + ". To fix this error, either run migrate, or set -ignorePendingMigrations=true.";
            return new ErrorDetails(ErrorCode.RESOLVED_REPEATABLE_MIGRATION_NOT_APPLIED, errorMessage);
        }

        if (!context.pending && MigrationState.OUTDATED == state) {
            String errorMessage = "Detected outdated resolved repeatable migration that should be re-applied to database: " + getDescription() + ". Run migrate to execute this migration.";
            return new ErrorDetails(ErrorCode.OUTDATED_REPEATABLE_MIGRATION, errorMessage);
        }

        if (resolvedMigration != null && appliedMigration != null
                && getType() != MigrationType.DELETE




        ) {
            String migrationIdentifier = appliedMigration.getVersion() == null ?
                    // Repeatable migrations
                    appliedMigration.getScript() :
                    // Versioned migrations
                    "version " + appliedMigration.getVersion();
            if (getVersion() == null || getVersion().compareTo(context.baseline) > 0) {
                if (resolvedMigration.getType() != appliedMigration.getType()) {
                    String mismatchMessage = createMismatchMessage("type", migrationIdentifier,
                            appliedMigration.getType(), resolvedMigration.getType());
                    return new ErrorDetails(ErrorCode.TYPE_MISMATCH, mismatchMessage);
                }
                if (resolvedMigration.getVersion() != null
                        || (context.pending && MigrationState.OUTDATED != state && MigrationState.SUPERSEDED != state)) {
                    if (!resolvedMigration.checksumMatches(appliedMigration.getChecksum())) {
                        String mismatchMessage = createMismatchMessage("checksum", migrationIdentifier,
                                appliedMigration.getChecksum(), resolvedMigration.getChecksum());
                        return new ErrorDetails(ErrorCode.CHECKSUM_MISMATCH, mismatchMessage);
                    }
                }
                if (descriptionMismatch(resolvedMigration, appliedMigration)) {
                    String mismatchMessage = createMismatchMessage("description", migrationIdentifier,
                            appliedMigration.getDescription(), resolvedMigration.getDescription());
                    return new ErrorDetails(ErrorCode.DESCRIPTION_MISMATCH, mismatchMessage);
                }
            }
        }

        // Perform additional validation for pending migrations. This is not performed for previously applied migrations
        // as it is assumed that if the checksum is unchanged, previous positive validation results still hold true.
        // #2392: Migrations above target are also ignored as the user explicitly asked for them to not be taken into account.
        if (!context.pending && MigrationState.PENDING == state && resolvedMigration instanceof ResolvedMigrationImpl) {
            ((ResolvedMigrationImpl) resolvedMigration).validate();
        }

        return null;
    }

    private boolean shouldNotExecuteMigration(ResolvedMigration resolvedMigration) {
        return resolvedMigration != null && resolvedMigration.getExecutor() != null && !resolvedMigration.getExecutor().shouldExecute();
    }

    private boolean descriptionMismatch(ResolvedMigration resolvedMigration, AppliedMigration appliedMigration) {
        // For some databases, we can't put an empty description into the history table
        if (SchemaHistory.NO_DESCRIPTION_MARKER.equals(appliedMigration.getDescription())) {
            return !"".equals(resolvedMigration.getDescription());
        }
        return (!AbbreviationUtils.abbreviateDescription(resolvedMigration.getDescription())
                .equals(appliedMigration.getDescription()));
    }

    private String createMismatchMessage(String mismatch, String migrationIdentifier, Object applied, Object resolved) {
        return String.format("Migration " + mismatch + " mismatch for migration %s\n" +
                        "-> Applied to database : %s\n" +
                        "-> Resolved locally    : %s" +
                ". Either revert the changes to the migration, or run repair to update the schema history.",
                migrationIdentifier, applied, resolved);
    }

    public boolean canExecuteInTransaction() {
        return resolvedMigration != null && resolvedMigration.getExecutor().canExecuteInTransaction();
    }









    @Override
    public int compareTo(MigrationInfo o) {






        if ((getInstalledRank() != null) && (o.getInstalledRank() != null)) {
            return getInstalledRank().compareTo(o.getInstalledRank());
        }

        MigrationState state = getState();
        MigrationState oState = o.getState();

        // Below baseline migrations come before applied ones
        if (state == MigrationState.BELOW_BASELINE && oState.isApplied()) {
            return -1;
        }
        if (state.isApplied() && oState == MigrationState.BELOW_BASELINE) {
            return 1;
        }

        // Sort installed before pending
        if (getInstalledRank() != null) {
            return -1;
        }
        if (o.getInstalledRank() != null) {
            return 1;
        }

        return compareVersion(o);
    }

    @Override
    public int compareVersion(MigrationInfo o) {
        if (getVersion() != null && o.getVersion() != null) {
            int v = getVersion().compareTo(o.getVersion());
            if (v != 0) {
                return v;
            }











            return 0;
        }

        // One versioned and one repeatable migration: versioned migration goes before repeatable
        if (getVersion() != null) {
            return -1;
        }
        if (o.getVersion() != null) {
            return 1;
        }

        // Two repeatable migrations: sort by description
        return getDescription().compareTo(o.getDescription());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MigrationInfoImpl that = (MigrationInfoImpl) o;

        if (!Objects.equals(appliedMigration, that.appliedMigration)) return false;
        if (!context.equals(that.context)) return false;
        return Objects.equals(resolvedMigration, that.resolvedMigration);
    }

    @Override
    public int hashCode() {
        int result = resolvedMigration != null ? resolvedMigration.hashCode() : 0;
        result = 31 * result + (appliedMigration != null ? appliedMigration.hashCode() : 0);
        result = 31 * result + context.hashCode();
        return result;
    }
}