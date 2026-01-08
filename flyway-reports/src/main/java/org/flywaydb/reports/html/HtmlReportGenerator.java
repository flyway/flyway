/*-
 * ========================LICENSE_START=================================
 * flyway-reports
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.reports.html;

import static org.flywaydb.core.internal.util.ClassUtils.getInstallDir;
import static org.flywaydb.reports.utils.HtmlUtils.getFormattedTimestamp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.CustomLog;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CompositeResult;
import org.flywaydb.core.api.output.HtmlResult;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.util.CollectionsUtils;
import org.flywaydb.core.internal.util.FileUtils;
import org.flywaydb.reports.api.extensibility.HtmlRenderer;
import org.flywaydb.reports.api.extensibility.HtmlReportSummary;
import org.flywaydb.reports.output.DashboardResult;
import org.flywaydb.reports.output.HoldingResult;

@CustomLog
@ExtensionMethod(Tier.class)
public class HtmlReportGenerator {
    private static final List<HoldingTabMetadata> HOLDING_TAB_METADATA = Arrays.asList(new HoldingTabMetadata("changes",
            "ENTERPRISE"),
        new HoldingTabMetadata("drift", "ENTERPRISE"),
        new HoldingTabMetadata("migrate", "OSS"),
        new HoldingTabMetadata("dryrun", "TEAMS", "ENTERPRISE"),
        new HoldingTabMetadata("code", "OSS"));
    private static final String INSTALL_DIR = getInstallDir(HtmlReportGenerator.class);

    public static String generateHtml(final CompositeResult<? extends HtmlResult> result, final Configuration config) {
        final Map<LocalDateTime, List<HtmlResult>> groupedResults = result.individualResults.stream()
            .collect(Collectors.groupingBy(HtmlResult::getTimestamp));
        final List<LocalDateTime> timestamps = new ArrayList<>(groupedResults.keySet());

        final StringBuilder content = new StringBuilder(getBeginning(timestamps));
        for (final LocalDateTime timestamp : timestamps) {
            final List<HtmlResult> groupedResult = groupedResults.get(timestamp);

            final DashboardResult dashboardResult = new DashboardResult();
            dashboardResult.setOperation("dashboard");
            dashboardResult.setResults(groupedResult);
            dashboardResult.setTimestamp(timestamp);

            groupedResult.add(0, dashboardResult);

            groupedResult.addAll(getHoldingResults(groupedResult, timestamp, config));

            final Collection<HtmlResult> htmlResults = new ArrayList<>();

            for (final HtmlResult htmlResult : groupedResult) {
                if (!htmlResult.isLicenseFailed()) {
                    htmlResults.add(htmlResult);
                }
            }

            final String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss",
                Locale.ROOT));
            content.append(getPage(formattedTimestamp, htmlResults, config));
        }

        return content.append(getEnd()).toString();
    }

    private static Collection<HoldingResult> getHoldingResults(final Collection<? extends HtmlResult> groupedResult,
        final LocalDateTime timestamp,
        final Configuration config) {
        final String currentTier = LicenseGuard.getTierAsString(config);

        final Collection<HoldingResult> holdingResults = new ArrayList<>();
        for (final HoldingTabMetadata holdingTabMetadata : HOLDING_TAB_METADATA) {
            final String holdingTab = holdingTabMetadata.name();
            if (groupedResult.stream().noneMatch(t -> holdingTab.equals(t.getOperation()) && !t.isLicenseFailed())) {
                String htmlFile = FileUtils.readAsStringFallbackToResource(INSTALL_DIR,
                    "assets/report/holdingTabs/" + holdingTab + ".html");
                final HoldingResult holdingResult = new HoldingResult();
                if (!Objects.equals(holdingTabMetadata.supportedEditions().get(0), "OSS")
                    && !holdingTabMetadata.supportedEditions().contains(currentTier)) {
                    htmlFile = FileUtils.readAsStringFallbackToResource(INSTALL_DIR,
                        "assets/report/upgradeTabs/" + holdingTab + ".html");
                    groupedResult.stream()
                        .filter(t -> t.getOperation().equals(holdingTab))
                        .findFirst()
                        .ifPresent(x -> holdingResult.setException(x.exceptionObject));
                }
                final String tabTitle = FileUtils.readAsStringFallbackToResource(INSTALL_DIR,
                    "assets/report/holdingTabs/" + holdingTab + ".txt");
                holdingResult.setTimestamp(timestamp);
                holdingResult.setTabTitle(tabTitle.trim());
                holdingResult.setBodyText(htmlFile);
                holdingResult.setOperation(holdingTab);
                holdingResults.add(holdingResult);
            }
        }

        return holdingResults;
    }

    private static String getBeginning(final List<LocalDateTime> timestamps) {
        return "<!doctype html>\n"
            + "<html lang=\"en\">\n"
            + "<head><meta charset=\"utf-8\">\n"
            + "<style>\n"
            + getCodeStyle()
            + "</style>\n</head>\n"
            + "<body>\n"
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/AddFilled.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/Calendar.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/CheckFilled.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/ClockOutlined.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/Database.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/DeleteFilled.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/Document.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/EditFilled.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/ErrorFilled.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/FeedbackOutlined.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/flyway-upgrade-icon.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/InfoOutlined.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/PipelineFilled.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/ScriptOutlined.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/upgrade.svg")
            + FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/icons/WarningFilled.svg")
            + " <div class=\"container\">"
            + "  <div class=\"header\">"
            + "    <div class=\"flywayLogo headerElement\"></div>\n"
            + "    <div class=\"headerElement leftPaddedElement\">Flyway Reports</div>"
            + "      <div class=\"redgateText\"><a class='unstyledLink' href='https://www.redgate.com'>redgate</a></div>"
            + "  </div>\n"
            + "  <div class=\"content\">\n"
            + getDropdown(timestamps);
    }

    private static String getDropdown(final List<LocalDateTime> timestamps) {
        Collections.sort(timestamps);

        final StringBuilder options = new StringBuilder();
        for (int i = 0; i < timestamps.size(); i++) {
            final String timestamp = timestamps.get(i)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT));

            options.append("<option value=\"").append(timestamp).append("\"");

            if (i == timestamps.size() - 1) {
                options.append(" selected=\"true\"");
            }

            options.append(">").append(timestamp).append("</option>\n");
        }

        return "<div class=\"dropdown\">\n"
            + "<label for=\"dropdown\">Report generated:</label>\n"
            + "<select onchange=\"onTimestampClick(event, this.value)\" id=\"dropdown\">\n"
            + options
            + "</select>\n"
            + "</div>\n";
    }

    public static HtmlRenderer<HtmlResult> getRenderer(final HtmlResult htmlResult, final Configuration config) {
        @SuppressWarnings("unchecked") final HtmlRenderer<HtmlResult> result = config.getPluginRegister()
            .getInstancesOf(HtmlRenderer.class)
            .stream()
            .map(x -> (HtmlRenderer<HtmlResult>) x)
            .filter(t -> t.getType().isAssignableFrom(htmlResult.getClass()))
            .findFirst()
            .orElse(null);

        if (result == null) {
            System.out.println("No renderer found for " + htmlResult.getClass().getName());
        }

        return result;
    }

    private static String getPage(final String timestamp,
        final Iterable<? extends HtmlResult> results,
        final Configuration config) {
        final StringBuilder content = new StringBuilder();

        content.append("<div class=\"page ").append(timestamp).append("\">\n");
        content.append(getTabs(results, config));

        int tabCount = 0;
        for (final HtmlResult result : results) {
            content.append(renderTab(result, config, tabCount));
            tabCount++;
        }

        return content.append("</div>\n").toString();
    }

    private static String getTabs(final Iterable<? extends HtmlResult> result, final Configuration config) {
        final StringBuilder tabs = new StringBuilder();
        int tabCount = 0;
        for (final HtmlResult htmlResult : result) {
            final String id2 = htmlResult.getOperation() + "-" + tabCount + "_" + getFormattedTimestamp(htmlResult);
            final StringBuilder button = new StringBuilder("<button class=\"tab\" onclick=\"onTabClick(event, '").append(
                getTabId(htmlResult, config, tabCount)).append("','").append(id2).append("')\"");
            button.append(" id=\"").append(id2).append("\">");
            String tabTitle = "";
            final HtmlRenderer<HtmlResult> correctRenderer = getRenderer(htmlResult, config);
            if (correctRenderer != null) {
                tabTitle = correctRenderer.tabTitle(htmlResult, config);
            }
            tabs.append(button).append("<h4>").append(tabTitle).append("</h4>").append("</button>\n");
            tabCount++;
        }
        return "<div class=\"tabs\">\n" + tabs + "</div>";
    }

    private static String renderTab(final HtmlResult result, final Configuration config, final int tabCount) {
        final HtmlRenderer<HtmlResult> renderer = getRenderer(result, config);
        return getTabOpening(result, config, tabCount) + renderTabSummary(result, config) + renderer.render(result,
            config) + getTabEnding(result);
    }

    private static String renderTabSummary(final HtmlResult result, final Configuration config) {
        final HtmlRenderer<HtmlResult> renderer = getRenderer(result, config);
        final List<HtmlReportSummary> summaries = renderer.getHtmlSummary(result, config);
        if (!CollectionsUtils.hasItems(summaries)) {
            return "";
        }
        final StringBuilder html = new StringBuilder("<div class='summaryHeader'>");

        for (final HtmlReportSummary s : summaries) {
            html.append("<div class='summaryDiv ")
                .append(s.getCssClass())
                .append("'><div class='summaryDivContent'><span class='summaryIcon'><svg fill=\"none\"><use href=\"#")
                .append(s.getIcon())
                .append("\"/></svg></span><span class='summaryText'>")
                .append(s.getSummaryText())
                .append("</span></div></div>");
        }
        html.append("</div>");

        return html.toString();
    }

    private static String getTabOpening(final HtmlResult result, final Configuration config, final int tabCount) {
        return "<div id=\"" + getTabId(result, config, tabCount) + "\" class=\"tabcontent\">\n";
    }

    private static String getTabEnding(final HtmlResult result) {
        String htmlResult = "";
        if (result.getException() != null) {
            htmlResult += "<div class=\"error\">\n"
                + "<pre class=\"exception\">Flyway Exception: "
                + result.getException()
                + "</pre>\n"
                + "</div>\n";
        }
        return htmlResult + "</div>\n";
    }

    public static String getTabId(final HtmlResult result, final Configuration config, final int tabCount) {
        final HtmlRenderer<HtmlResult> correctRenderer = getRenderer(result, config);
        return (correctRenderer.tabTitle(result, config)
            + "_"
            + tabCount
            + "_"
            + getFormattedTimestamp(result)).replace(" ", "");
    }

    private static String getEnd() {
        String html = "</div>\n";
        html += FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/footer.html");
        html += "</div></body>\n" + getScript() + "</html>\n";

        return html;
    }

    private static String getScript() {
        return FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/reportScript.html");
    }

    private static String getCodeStyle() {
        return FileUtils.readAsStringFallbackToResource(INSTALL_DIR, "assets/report/report.css");
    }
}
