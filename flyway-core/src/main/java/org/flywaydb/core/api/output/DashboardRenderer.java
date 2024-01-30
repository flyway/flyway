package org.flywaydb.core.api.output;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.HtmlRenderer;
import org.flywaydb.core.extensibility.HtmlReportSummary;
import org.flywaydb.core.internal.reports.html.HtmlReportGenerator;

import java.util.List;

import static org.flywaydb.core.internal.util.HtmlUtils.getFormattedTimestamp;

public class DashboardRenderer implements HtmlRenderer<DashboardResult> {
    @Override
    public String render(DashboardResult result, Configuration config) {
        String html = "<div class='dashboardContainer'>";

        int tabCount = 1;
        for (HtmlResult htmlResult : result.getResults()) {
            if (!(htmlResult instanceof DashboardResult)) {

                HtmlRenderer resultRenderer = HtmlReportGenerator.getRenderer(htmlResult, config);
                List<HtmlReportSummary> summaries = resultRenderer.getHtmlSummary(htmlResult);
                if (summaries != null) {
                    html += "<div class='dashboardSummaryGroup'>";
                    html += "<h5>" + resultRenderer.tabTitle(htmlResult, config) + "</h5>";
                    for (HtmlReportSummary s : summaries) {
                        html += "<div class='summaryDiv " + s.getCssClass() + "'><div class='summaryDivContent'><span class='summaryIcon'><svg fill=\"none\"><use href=\"#" + s.getIcon() + "\"/></svg></span><span class='summaryText'>" + s.getSummaryText() + "</span></div></div>";
                    }
                    String id2 = htmlResult.getOperation() + "-" + tabCount + "_" + getFormattedTimestamp(htmlResult);
                    html += "<button class='clickable inspectButton fancyButton' onclick=\"onTabClick(event, '" + HtmlReportGenerator.getTabId(htmlResult, config, tabCount) + "','" + id2 + "')\">Inspect " + resultRenderer.tabTitle(htmlResult, config) + "</button>";
                    html += "</div>";
                }

                tabCount++;
            }
        }

        html += "</div>";
        return html;
    }

    @Override
    public String tabTitle(DashboardResult result, Configuration config) {
        return "Dashboard";
    }

    @Override
    public Class<DashboardResult> getType() {
        return DashboardResult.class;
    }
}