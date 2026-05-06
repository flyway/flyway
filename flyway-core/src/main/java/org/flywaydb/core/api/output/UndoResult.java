package org.flywaydb.core.api.output;

import java.util.ArrayList;
import java.util.List;

public class UndoResult extends OperationResultBase {
    public String schemaName;
    public String initialSchemaVersion;
    public String targetSchemaVersion;
    public List<UndoOutput> undoneScripts;
    public int migrationsUndone;
    public boolean success;

    public UndoResult(String flywayVersion, String database, String schemaName) {
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.schemaName = schemaName;
        this.operation = "undo";
        this.success = true;
        this.undoneScripts = new ArrayList<>();
    }
}
