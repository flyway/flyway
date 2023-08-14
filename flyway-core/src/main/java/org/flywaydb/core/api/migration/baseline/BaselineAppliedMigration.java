/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.core.api.migration.baseline;

import lombok.NoArgsConstructor;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.extensibility.AppliedMigration;
import org.flywaydb.core.internal.info.MigrationInfoContext;
import org.flywaydb.core.internal.schemahistory.BaseAppliedMigration;

import java.util.Arrays;
import java.util.Date;

@NoArgsConstructor
public class BaselineAppliedMigration extends BaseAppliedMigration {
    public BaselineAppliedMigration(int installedRank,
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
        this.type = BaselineMigrationType.fromString(type);
        this.script = script;
        this.checksum = checksum;
        this.installedOn = installedOn;
        this.installedBy = installedBy;
        this.executionTime = executionTime;
        this.success = success;
    }

    @Override
    public boolean handlesType(String type) {
        return Arrays.stream(BaselineMigrationType.values())
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
        return new BaselineAppliedMigration(installedRank, version, description, type, script, checksum, installedOn, installedBy, executionTime, success);
    }

    @Override
    public MigrationState getState(MigrationInfoContext context, boolean outOfOrder, ResolvedMigration resolvedMigration) {
        MigrationState migrationState = super.getState(context, outOfOrder, resolvedMigration);
        MigrationState missingState = super.getMissingState(context, resolvedMigration);
        if (migrationState == MigrationState.BASELINE) {
            if (!isSuccess()) {
                return MigrationState.FAILED;
            }
            if (missingState != null) {
                return missingState;
            }
        }
        return migrationState;
    }
}