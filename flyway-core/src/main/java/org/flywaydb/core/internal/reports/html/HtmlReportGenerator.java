/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.reports.html;

import lombok.CustomLog;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.DashboardResult;
import org.flywaydb.core.api.output.HoldingResult;
import org.flywaydb.core.api.output.HtmlResult;
import org.flywaydb.core.api.output.CompositeResult;
import org.flywaydb.core.extensibility.HtmlRenderer;
import org.flywaydb.core.extensibility.HtmlReportSummary;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.FileUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.flywaydb.core.internal.util.HtmlUtils.getFormattedTimestamp;

@CustomLog
public class HtmlReportGenerator {
    private static final List<HoldingTabMetadata> HOLDING_TAB_METADATA = Arrays.asList(
            new HoldingTabMetadata("changes", "TIER3", "MASTER"),
            new HoldingTabMetadata("drift", "TIER3", "MASTER"),
            new HoldingTabMetadata("migrate", "OSS"),
            new HoldingTabMetadata("dryrun", "PRO", "ENTERPRISE", "TIER3", "MASTER"),
            new HoldingTabMetadata("code", "OSS")
                                                                                      );

    public static String generateHtml(CompositeResult<HtmlResult> result, Configuration config) {
        Map<LocalDateTime, List<HtmlResult>> groupedResults = result.individualResults.stream().collect(Collectors.groupingBy(HtmlResult::getTimestamp));
        List<LocalDateTime> timestamps = new ArrayList<>(groupedResults.keySet());

        StringBuilder content = new StringBuilder(getBeginning(timestamps));
        for (LocalDateTime timestamp : timestamps) {
            List<HtmlResult> groupedResult = groupedResults.get(timestamp);

            DashboardResult dashboardResult = new DashboardResult();
            dashboardResult.setOperation("dashboard");
            dashboardResult.setResults(groupedResult);
            dashboardResult.setTimestamp(timestamp);

            groupedResult.add(0, dashboardResult);

            String currentTier = VersionPrinter.EDITION.name();

            for (HoldingTabMetadata holdingTabMetadata : HOLDING_TAB_METADATA) {
                String holdingTab = holdingTabMetadata.getName();
                if (groupedResult.stream().noneMatch(t -> holdingTab.equals(t.getOperation()) && !t.isLicenseFailed())) {
                    String htmlFile = FileUtils.readResourceAsString("reportAssets/holdingTabs/" + holdingTab + ".html");
                    HoldingResult holdingResult = new HoldingResult();
                    if (holdingTabMetadata.getSupportedEditions().get(0) != "OSS" && !holdingTabMetadata.getSupportedEditions().contains(currentTier)) {
                        htmlFile = FileUtils.readResourceAsString("reportAssets/upgradeTabs/" + holdingTab + ".html");
                        if (groupedResult.stream().anyMatch(t -> t.getOperation().equals(holdingTab))) {
                            HtmlResult htmlResult = groupedResult.stream().filter(t -> t.getOperation().equals(holdingTab)).findFirst().get();
                            holdingResult.setException(htmlResult.exceptionObject);
                        }
                    }
                    String tabTitle = FileUtils.readResourceAsString("reportAssets/holdingTabs/" + holdingTab + ".txt");
                    holdingResult.setTimestamp(timestamp);
                    holdingResult.setTabTitle(tabTitle.trim());
                    holdingResult.setBodyText(htmlFile);
                    holdingResult.setOperation(holdingTab);
                    groupedResult.add(holdingResult);
                }
            }

            List<HtmlResult> htmlResults = new ArrayList<>();

            for (HtmlResult htmlResult : groupedResult) {
                if (!htmlResult.isLicenseFailed()) {
                    htmlResults.add(htmlResult);
                }
            }

            String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            content.append(getPage(formattedTimestamp, htmlResults, config));
        }

        return content.append(getEnd()).toString();
    }

    private static String getBeginning(List<LocalDateTime> timestamps) {
        return "<!doctype html>\n" +
                "<html lang=\"en\">\n" +
                "<head><meta charset=\"utf-8\">\n" +
                "<style>\n" +
                getCodeStyle() +
                "</style>\n</head>\n" +
                "<body>\n" +
                FileUtils.readResourceAsString("reportAssets/icons/AddFilled.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/Calendar.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/CheckFilled.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/ClockOutlined.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/Database.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/DeleteFilled.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/Document.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/EditFilled.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/ErrorFilled.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/FeedbackOutlined.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/flyway-upgrade-icon.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/InfoOutlined.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/PipelineFilled.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/ScriptOutlined.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/upgrade.svg") +
                FileUtils.readResourceAsString("reportAssets/icons/WarningFilled.svg") +
                " <div class=\"container\">" +
                "  <div class=\"header\">" +
                "    <div class=\"flywayLogo headerElement\"></div>\n" +
                "    <div class=\"headerElement leftPaddedElement\">Flyway Reports</div>" +
                //"    <div class=\"headerElement redgateBanner\">" +
                "      <div class=\"redgateText\"><a class='unstyledLink' href='https://www.redgate.com'>redgate</a></div>" +
                "      <div class=\"redgateLogo\"><a class='unstyledLink' href='https://www.redgate.com'>\uE925</a></div>" +
                //"    </div>" +
                "  </div>\n" +
                "  <div class=\"content\">\n" +
                getDropdown(timestamps);
    }

    private static String getDropdown(List<LocalDateTime> timestamps) {
        Collections.sort(timestamps);

        StringBuilder options = new StringBuilder();
        for (int i = 0; i < timestamps.size(); i++) {
            String timestamp = timestamps.get(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            options.append("<option value=\"").append(timestamp).append("\"");

            if (i == timestamps.size() - 1) {
                options.append(" selected=\"true\"");
            }

            options.append(">").append(timestamp).append("</option>\n");
        }

        return "<div class=\"dropdown\">\n" +
                "<label for=\"dropdown\">Report generated:</label>\n" +
                "<select onchange=\"onTimestampClick(event, this.value)\" id=\"dropdown\">\n" +
                options +
                "</select>\n" +
                "</div>\n";
    }

    public static HtmlRenderer<HtmlResult> getRenderer(HtmlResult htmlResult, Configuration config) {
        HtmlRenderer result = config.getPluginRegister().getPlugins(HtmlRenderer.class).stream()
                                    .filter(t -> t.getType().isAssignableFrom(htmlResult.getClass()))
                                    .findFirst()
                                    .orElse(null);

        if (result == null) {
            System.out.println("No renderer found for " + htmlResult.getClass().getName());
        }

        return result;
    }

    private static String getPage(String timestamp, List<HtmlResult> results, Configuration config) {
        StringBuilder content = new StringBuilder();

        content.append("<div class=\"page ").append(timestamp).append("\">\n");
        content.append(getTabs(results, config));

        int tabCount = 0;
        for (HtmlResult result : results) {
            content.append(renderTab(result, config, tabCount));
            tabCount++;
        }

        return content.append("</div>\n").toString();
    }

    private static String getTabs(List<HtmlResult> result, Configuration config) {
        StringBuilder tabs = new StringBuilder();
        int tabCount = 0;
        for (HtmlResult htmlResult : result) {
            String id2 = htmlResult.getOperation() + "-" + tabCount + "_" + getFormattedTimestamp(htmlResult);
            String button = "<button class=\"tab\" onclick=\"onTabClick(event, '" + getTabId(htmlResult, config, tabCount) + "','" + id2 + "')\"";
            button += " id=\"" + id2 + "\">";
            String tabTitle = "";
            HtmlRenderer<HtmlResult> correctRenderer = getRenderer(htmlResult, config);
            if (correctRenderer != null) {
                tabTitle = correctRenderer.tabTitle(htmlResult, config);
            }
            tabs.append(button).append("<h4>").append(tabTitle).append("</h4>").append("</button>\n");
            tabCount++;
        }
        return "<div class=\"tabs\">\n" + tabs + "</div>";
    }

    public static String renderTab(HtmlResult result, Configuration config, int tabCount) {
        HtmlRenderer renderer = getRenderer(result, config);
        return getTabOpening(result, config, tabCount) + renderTabSummary(result, config) + renderer.render(result, config) + getTabEnding(result);
    }

    public static String renderTabSummary(HtmlResult result, Configuration config) {
        HtmlRenderer renderer = getRenderer(result, config);
        List<HtmlReportSummary> summaries = renderer.getHtmlSummary(result);
        if (summaries == null) {
            return "";
        }
        String html = "<div class='summaryHeader'>";

        for (HtmlReportSummary s : summaries) {
            html += "<div class='summaryDiv " + s.getCssClass() + "'><div class='summaryDivContent'><span class='summaryIcon'><svg fill=\"none\"><use href=\"#" + s.getIcon() + "\"/></svg></span><span class='summaryText'>" + s.getSummaryText() + "</span></div></div>";
        }

        try {
            String informationalText = FileUtils.readResourceAsString("reportAssets/infoBlobs/" + result.getOperation() + ".html");

            if (StringUtils.hasText(informationalText)) {
                html += "<div class='summaryDiv summaryNote'><div class='summaryDivContent'><span class='summaryIcon'><svg fill=\"none\"><use href=\"#infoOutlined\"/></svg></span><span class='summaryText'>" + informationalText + "</span></div></div>";
            }
        } catch (Exception ignore) {
        }
        html += "</div>";

        return html;
    }

    public static String getTabOpening(HtmlResult result, Configuration config, int tabCount) {
        return "<div id=\"" + getTabId(result, config, tabCount) + "\" class=\"tabcontent\">\n";
    }

    public static String getTabEnding(HtmlResult result) {
        String htmlResult = "";
        if (result.getException() != null) {
            htmlResult += "<div class=\"error\">\n" +
                    "<pre class=\"exception\">Flyway Exception: " + result.getException() + "</pre>\n" +
                    "</div>\n";
        }
        return htmlResult + "</div>\n";
    }

    public static String getTabId(HtmlResult result, Configuration config, int tabCount) {
        HtmlRenderer<HtmlResult> correctRenderer = getRenderer(result, config);
        return (correctRenderer.tabTitle(result, config) + "_" + tabCount + "_" + getFormattedTimestamp(result)).replace(" ", "");
    }

    private static String getEnd() {
        String html = "</div>\n";
        html += FileUtils.readResourceAsString("reportAssets/footer.html");
        html += "</div></body>\n" + getScript() + "</html>\n";

        return html;
    }

    public static String getSummaryValueStyled(int value, String color, String backgroundColor) {
        return getSummaryValueStyled(value, color, "black", backgroundColor, "white");
    }

    public static String getSummaryValueStyled(int value, String color, String defaultColor, String backgroundColor, String defaultBackgroundColor) {
        color = value > 0 ? color : defaultColor;
        backgroundColor = value > 0 ? backgroundColor : defaultBackgroundColor;
        return "<span style=\"background-color: " + backgroundColor + "; padding: 4px 12px; margin-left: 12px; border-radius: 14px; color: " + color + "\">" + value + "</span>";
    }

    public static String getSummaryStyle() {
        return "style=\"background-color: rgb(240 240 240); padding: 20px; border-radius: 5px; color: black; display: flex; justify-content: space-between; clear: both; margin-top: 20px;\"";
    }

    public static String getInformationalText(String txt) {
        return "<img style=\"vertical-align: top; margin-right: 6px\" width=16 height=16 src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACYAAAAmCAYAAACoPemuAAACyElEQVRYhe2YT1IaQRSHf68bsw1HGE+QMWSXitoniJ7AZJdKUTo3EE4QiJSVpTmBeIIBszRq3yDjDSZrfP2yCJARGmRmQFzk23VXz6uvXs/rf8AzhZYVKPwUB9m2/WaSMvEKiYVRXAVjT0PvCMkuCQXegSQWQpaFL2zHdFcmFkZxVbnKEYlEAKp5vgWQAOjxgJuLZHNhsdeHP44LCk0hRI2br++apcTCKA4qrnIuImFZoQkSHrCZlb25YuHnONRax1hClmaQKOb9q1NjFxZ7AqkRqWI2k3JesTCKA+10DIG/2pZPygPeyk6r8o0qJCXS3hhsbCrWWyQy98f2UNUv1Hm2Yypjw+pr5Ax8dn2y/THbUav3z0B0kCeIiDRvOjsNYCJjYRQHBaRAQt89vbkWVAAgoqMwiqtTYpr1cd5gAOCcTE27UKGiqap7FQETU1k7vPxV6IcnsaycsS2TAn93CO30bcHiSVnz5lisVr/cA+F83hePkIjId6XppTh8QIllhoRN5V8L70tIAUBARMfiSkYBIKQPKplWCJIy0SIh+Q0AcKiSwpcSbruZjJXbC52o7m3n7R0wXKBZlxEL1ChQGalV4F35nwP/xfKiAMC2yl0cVsE4Y0J4NnJEZMdi5KS/TpksImIzGaPeOmUeILgYiznNXQDpGnVGJNed7e5YzLZMKiLtdRoN6QETy4WruBbWnDUecBPwHa3r/QYR5T4wCsmDqp75bDAHEmn+HB6tvbek2mH/FkLLvuA+RnJ9sr05anhXflZuH087pQkP2GQ7vGK2ZRLFbPA0cikz708+FczcK69OjVXMBqvdEVJmNtbzRDB3E786NZbVauSIyPKAt3xSQI5nqDf1fkMKVKuHlETao+qbRd6Hu0DfqwYU7RS4mqUk0r6vuNbomrc0sSy1+uUeIHtCFBLwyjdGgDsS9ITQc5q7iwiN+AOQ/zVlelSVVQAAAABJRU5ErkJggg==\" />" +
                txt + ".\n";
    }

    private static String getScript() {
        return FileUtils.readResourceAsString("reportAssets/reportScript.html");
    }

    private static String getBase64EncodedLogo() {
        InputStream logo = HtmlReportGenerator.class.getClassLoader().getResourceAsStream("logo_base64.txt");
        if (logo == null) {
            LOG.debug("Unable to load logo for check report");
            return "";
        }
        return new BufferedReader(new InputStreamReader(logo)).lines().collect(Collectors.toList()).get(0);
    }

    private static String getCodeStyle() {
        return FileUtils.readResourceAsString("reportAssets/report.css");
    }
}