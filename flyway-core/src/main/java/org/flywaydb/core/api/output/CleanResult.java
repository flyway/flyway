package org.flywaydb.core.api.output;

import java.util.ArrayList;

public class CleanResult extends OperationResultBase {
    public ArrayList<String> schemasCleaned = new ArrayList<>();
    public ArrayList<String> schemasDropped = new ArrayList<>();

    public CleanResult(String flywayVersion, String database) {
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.operation = "clean";
    }
}