package org.flywaydb.core.api.output;

import java.util.ArrayList;
import java.util.List;

public class DryRunResult extends OperationResultBase {
    public String schemaName;
    public String currentSchemaVersion;
    public List<DryRunOutput> pendingMigrations;
    public int migrationCount;

    public DryRunResult(String flywayVersion, String database, String schemaName) {
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.schemaName = schemaName;
        this.operation = "dryrun";
        this.pendingMigrations = new ArrayList<>();
    }
}
