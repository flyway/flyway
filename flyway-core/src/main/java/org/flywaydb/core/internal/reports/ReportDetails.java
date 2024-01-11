package org.flywaydb.core.internal.reports;

import lombok.Data;

@Data
public class ReportDetails {
    private String jsonReportFilename;
    private String htmlReportFilename;
}