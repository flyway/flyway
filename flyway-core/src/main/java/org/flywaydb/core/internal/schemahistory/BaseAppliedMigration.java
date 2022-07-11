/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.core.internal.schemahistory;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.MigrationPattern;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.extensibility.AppliedMigration;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.internal.info.MigrationInfoContext;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
public class BaseAppliedMigration implements AppliedMigration {
    protected int installedRank;
    protected MigrationVersion version;
    protected String description;
    protected MigrationType type;
    protected String script;
    protected Integer checksum;
    protected Date installedOn;
    protected String installedBy;
    protected int executionTime;
    protected boolean success;

    public BaseAppliedMigration(int installedRank,
                                MigrationVersion version,
                                String description,
                                String type,
                                String script,
                                Integer checksum,
                                Date installedOn,
                                String installedBy,
                                int executionTime,
                                boolean success) {
        this.installedRank = installedRank;
        this.version = version;
        this.description = description;
        this.type = CoreMigrationType.fromString(type);
        this.script = script;
        this.checksum = checksum;
        this.installedOn = installedOn;
        this.installedBy = installedBy;
        this.executionTime = executionTime;
        this.success = success;
    }

    @Override
    public int getInstalledRank() {
        return installedRank;
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
    public MigrationType getType() {
        return type;
    }

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public Integer getChecksum() {
        return checksum;
    }

    @Override
    public Date getInstalledOn() {
        return installedOn;
    }

    @Override
    public String getInstalledBy() {
        return installedBy;
    }

    @Override
    public int getExecutionTime() {
        return executionTime;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public boolean handlesType(String type) {
        return Arrays.stream(CoreMigrationType.values())
                .map(Enum::toString)
                .anyMatch(t -> t.equalsIgnoreCase(type));
    }

    @Override
    public AppliedMigration create(int installedRank,
                                   MigrationVersion version,
                                   String description,
                                   String type,
                                   String script,
                                   Integer checksum,
                                   Date installedOn,
                                   String installedBy,
                                   int executionTime,
                                   boolean success) {
        return new BaseAppliedMigration(installedRank, version, description, type, script, checksum, installedOn, installedBy, executionTime, success);
    }

    private boolean isMigrationNotInList(MigrationPattern[] cherryPick, MigrationVersion version, String description) {
        boolean inMigrationList = false;
        for (MigrationPattern migration : cherryPick) {
            if (migration.matches(version, description)) {
                inMigrationList = true;
                break;
            }
        }
        return !inMigrationList;
    }

    @Override
    public MigrationState getState(MigrationInfoContext context, boolean outOfOrder, ResolvedMigration resolvedMigration) {
        if (CoreMigrationType.DELETE == getType()) {
            return MigrationState.SUCCESS;
        }

        if (getType().isBaseline()) {
            return MigrationState.BASELINE;
        }

        MigrationState missingState = getMissingState(context, resolvedMigration);
        if (missingState != null) {
            return missingState;
        }









        if (!isSuccess()) {
            return MigrationState.FAILED;
        }

        MigrationState repeatableState = getRepeatableState(context, resolvedMigration);
        if (repeatableState != null) {
            return repeatableState;
        }

        if (outOfOrder) {
            return MigrationState.OUT_OF_ORDER;
        }

        return MigrationState.SUCCESS;
    }

    private MigrationState getRepeatableState(MigrationInfoContext context, ResolvedMigration resolvedMigration) {
        if (getVersion() == null) {
            if (getInstalledRank() == context.latestRepeatableRuns.get(getDescription())) {
                if (resolvedMigration != null && resolvedMigration.checksumMatches(getChecksum())) {
                    return MigrationState.SUCCESS;
                }
                return MigrationState.OUTDATED;
            }
            return MigrationState.SUPERSEDED;
        }
        return null;
    }

    protected MigrationState getMissingState(MigrationInfoContext context, ResolvedMigration resolvedMigration) {
        if (resolvedMigration == null && isRepeatableLatest(context)) {








            if (CoreMigrationType.SCHEMA == getType()) {
                return MigrationState.SUCCESS;
            }

            if ((getVersion() == null) || getVersion().compareTo(context.lastResolved) < 0) {
                if (isSuccess()) {
                    return MigrationState.MISSING_SUCCESS;
                }
                return MigrationState.MISSING_FAILED;
            } else {
                if (isSuccess()) {
                    return MigrationState.FUTURE_SUCCESS;
                }
                return MigrationState.FUTURE_FAILED;
            }
        }
        return null;
    }

    private boolean isRepeatableLatest(MigrationInfoContext context) {
        if (getVersion() != null) {
            return true;
        }

        Integer latestRepeatableRank = context.latestRepeatableRuns.get(getDescription());
        return latestRepeatableRank == null || getInstalledRank() == latestRepeatableRank;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseAppliedMigration that = (BaseAppliedMigration) o;

        if (executionTime != that.executionTime) {
            return false;
        }
        if (installedRank != that.installedRank) {
            return false;
        }
        if (success != that.success) {
            return false;
        }
        if (checksum != null ? !checksum.equals(that.checksum) : that.checksum != null) {
            return false;
        }
        if (!description.equals(that.description)) {
            return false;
        }
        if (installedBy != null ? !installedBy.equals(that.installedBy) : that.installedBy != null) {
            return false;
        }
        if (installedOn != null ? !installedOn.equals(that.installedOn) : that.installedOn != null) {
            return false;
        }
        if (!script.equals(that.script)) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        return Objects.equals(version, that.version);
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
}