package org.flywaydb.core.api.output;

public class OperationResultJsonBase implements OperationResult {
    public String jsonReport;
    public String htmlReport;

    public OperationResultJsonBase(String jsonReport, String htmlReport) {
        this.jsonReport = jsonReport;
        this.htmlReport = htmlReport;
    }

}