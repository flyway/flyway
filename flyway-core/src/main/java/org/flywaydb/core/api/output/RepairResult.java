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
        if (completedRepairActions.removedFailedMigrations) {
            repairActions.add(completedRepairActions.removedMessage());
        }
        if (completedRepairActions.deletedMissingMigrations) {
            repairActions.add(completedRepairActions.deletedMessage());
        }
        if (completedRepairActions.alignedAppliedMigrationChecksums) {
            repairActions.add(completedRepairActions.alignedMessage());
        }
    }
}