package org.flywaydb.core.extensibility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class HtmlReportSummary {
    private String summaryText;
    private String icon;
    private String cssClass;

    public HtmlReportSummary(){

    }
    public HtmlReportSummary(String cssClass, String icon, String summaryText) {
        this.summaryText=summaryText;
        this.icon = icon;
        this.cssClass = cssClass;
    }
}