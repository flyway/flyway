package org.flywaydb.core.api.output;

public class BaselineResult extends OperationResultBase {
    public boolean successfullyBaselined;
    public String baselineVersion = null;

    public BaselineResult(String flywayVersion, String database) {
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.operation = "baseline";
    }
}