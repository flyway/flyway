/*
 * Copyright © Red Gate Software Ltd 2010-2020
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
package org.flywaydb.core.api.output;

import org.flywaydb.core.internal.command.DbRepair;

import java.util.ArrayList;
import java.util.List;

public class RepairResult extends OperationResultBase {

    public List<String> repairActions;
    public List<RepairOutput> migrationsRemoved;
    public List<RepairOutput> migrationsDeleted;
    public List<RepairOutput> migrationsAligned;

    public RepairResult(String flywayVersion, String database) {
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.repairActions = new ArrayList<>();
        this.migrationsRemoved = new ArrayList<>();
        this.migrationsDeleted = new ArrayList<>();
        this.migrationsAligned = new ArrayList<>();
        this.operation = "repair";
    }

    public void setRepairActions(DbRepair.CompletedRepairActions completedRepairActions) {
        if (completedRepairActions.removedFailedMigrations) repairActions.add(completedRepairActions.removedMessage());
        if (completedRepairActions.deletedMissingMigrations) repairActions.add(completedRepairActions.deletedMessage());
        if (completedRepairActions.alignedAppliedMigrationChecksums) repairActions.add(completedRepairActions.alignedMessage());
    }

}