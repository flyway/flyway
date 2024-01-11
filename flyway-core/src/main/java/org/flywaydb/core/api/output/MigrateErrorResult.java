package org.flywaydb.core.api.output;

public class MigrateErrorResult extends MigrateResult {
    public ErrorOutput.ErrorOutputItem error;

    public MigrateErrorResult(MigrateResult migrateResult, Exception e) {
        super(migrateResult);
        this.success = false;
        this.error = ErrorOutput.fromException(e).error;
    }
}