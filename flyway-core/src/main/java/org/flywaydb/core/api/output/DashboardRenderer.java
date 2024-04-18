/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
