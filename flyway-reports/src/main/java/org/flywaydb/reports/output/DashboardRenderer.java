/*-
 * ========================LICENSE_START=================================
 * flyway-reports
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.reports.output;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.HtmlResult;
import org.flywaydb.reports.api.extensibility.HtmlRenderer;
import org.flywaydb.reports.api.extensibility.HtmlReportSummary;
import org.flywaydb.reports.html.HtmlReportGenerator;

import java.util.List;

import static org.flywaydb.reports.utils.HtmlUtils.getFormattedTimestamp;

public class DashboardRenderer implements HtmlRenderer<DashboardResult> {
    @Override
    public String render(final DashboardResult result, final Configuration config) {
        final StringBuilder html = new StringBuilder("<div class='dashboardContainer'>");

        int tabCount = 1;
        for (final HtmlResult htmlResult : result.getResults()) {
            if (!(htmlResult instanceof DashboardResult)) {

                final HtmlRenderer<HtmlResult> resultRenderer = HtmlReportGenerator.getRenderer(htmlResult, config);
                final List<HtmlReportSummary> summaries = resultRenderer.getHtmlSummary(htmlResult, config);
                if (summaries != null) {
                    final List<HtmlReportSummary> filteredSummaries = summaries.stream()
                        .filter(x -> !x.getCssClass().contains("summaryNote")).toList();
                    html.append("<div class='dashboardSummaryGroup'>");
                    html.append("<h5>").append(resultRenderer.tabTitle(htmlResult, config)).append("</h5>");
                    for (final HtmlReportSummary s : filteredSummaries) {
                        html.append("<div class='summaryDiv ")
                            .append(s.getCssClass())
                            .append(
                                "'><div class='summaryDivContent'><span class='summaryIcon'><svg fill=\"none\"><use href=\"#")
                            .append(s.getIcon())
                            .append("\"/></svg></span><span class='summaryText'>")
                            .append(s.getSummaryText())
                            .append("</span></div></div>");
                    }
                    final String id2 = htmlResult.getOperation() + "-" + tabCount + "_" + getFormattedTimestamp(htmlResult);
                    html.append("<button class='clickable inspectButton fancyButton' onclick=\"onTabClick(event, '")
                        .append(HtmlReportGenerator.getTabId(htmlResult, config, tabCount))
                        .append("','")
                        .append(id2)
                        .append("')\">Inspect ")
                        .append(resultRenderer.tabTitle(htmlResult, config))
                        .append("</button>");
                    html.append("</div>");
                }

                tabCount++;
            }
        }

        html.append("</div>");
        return html.toString();
    }

    @Override
    public String tabTitle(final DashboardResult result, final Configuration config) {
        return "Dashboard";
    }

    @Override
    public Class<DashboardResult> getType() {
        return DashboardResult.class;
    }
}
