package org.flywaydb.core.api.output;

import java.util.ArrayList;
import java.util.List;

public class DryRunResult extends OperationResultBase {
    public String schemaName;
    public String currentSchemaVersion;
    public List<DryRunOutput> pendingMigrations;

    public DryRunResult(String flywayVersion, String database, String schemaName) {
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.schemaName = schemaName;
        this.operation = "dryrun";
        this.pendingMigrations = new ArrayList<>();
    }

    /** Returns the number of pending migrations. Prefer this over a cached count field. */
    public int getMigrationCount() {
        return pendingMigrations.size();
    }
}
