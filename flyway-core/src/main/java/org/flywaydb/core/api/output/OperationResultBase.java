package org.flywaydb.core.api.output;

import java.util.LinkedList;
import java.util.List;

public abstract class OperationResultBase implements OperationResult {
    public String flywayVersion;
    public String database;
    public List<String> warnings;
    public String operation;

    public OperationResultBase() {
        this.warnings = new LinkedList<>();
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }
}