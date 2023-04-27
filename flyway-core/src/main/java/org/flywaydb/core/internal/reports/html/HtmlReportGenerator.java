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
import org.flywaydb.core.api.output.HtmlResult;
import org.flywaydb.core.api.output.CompositeResult;
import org.flywaydb.core.extensibility.HtmlRenderer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CustomLog
public class HtmlReportGenerator {
    public static String generateHtml(CompositeResult<HtmlResult> result, Configuration config) {
        Map<LocalDateTime, List<HtmlResult>> groupedResults = result.individualResults.stream().collect(Collectors.groupingBy(r -> r.timestamp));
        List<LocalDateTime> timestamps = new ArrayList<>(groupedResults.keySet());

        StringBuilder content = new StringBuilder(getBeginning(timestamps));
        for (LocalDateTime timestamp : timestamps) {
            List<HtmlResult> groupedResult = groupedResults.get(timestamp);
            String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            content.append(getPage(formattedTimestamp, groupedResult, config));
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
                "<body style=\"font-family: sans-serif\">\n" +
                "<div style=\"margin: 30px;\">\n" +
                "<div style=\"float: right\">\n" +
                "<a href=\"https://flywaydb.org/\" target=\"_newtab\"><img src=\"" + getBase64EncodedLogo() + "\" width=72 height=72/></a>\n" +
                "</div>\n" +
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

        return "<div class=\"dropdown\" style=\"float: right; clear: both; margin-top: 50px\">\n" +
                "<label for=\"dropdown\">Report generated:</label>\n" +
                "<select onchange=\"onTimestampClick(event, this.value)\" id=\"dropdown\">\n" +
                options +
                "</select>\n" +
                "</div>\n";
    }

    private static HtmlRenderer<HtmlResult> getRenderer(HtmlResult htmlResult, Configuration config) {
        return config.getPluginRegister().getPlugins(HtmlRenderer.class).stream()
                     .filter(t -> t.getType().isAssignableFrom(htmlResult.getClass()))
                     .findFirst()
                     .orElse(null);
    }

    private static String getPage(String timestamp, List<HtmlResult> results, Configuration config) {
        StringBuilder content = new StringBuilder();

        content.append("<div class=\"page ").append(timestamp).append("\">\n");
        content.append(getTabs(results, config));

        for (HtmlResult result : results) {
            HtmlRenderer<HtmlResult> correctRenderer = getRenderer(result, config);
            if (correctRenderer != null) {
                content.append(correctRenderer.render(result, config));
            } else {
                //todo - signpost?
            }
        }

        return content.append("</div>\n").toString();
    }

    private static String getTabs(List<HtmlResult> result, Configuration config) {
        StringBuilder tabs = new StringBuilder();
        for (int i = 0; i < result.size(); i++) {
            HtmlResult htmlResult = result.get(i);

            String button = "<button class=\"tab\" onclick=\"onTabClick(event, '" + getTabId(htmlResult, config) + "')\"";
            if (i == result.size() - 1) {
                button += " id=\"open-" + getFormattedTimestamp(htmlResult) + "\"";
            }
            button += ">";
            String tabTitle = "";
            HtmlRenderer<HtmlResult> correctRenderer = getRenderer(htmlResult, config);
            if (correctRenderer != null) {
                tabTitle = correctRenderer.tabTitle(htmlResult);
            }
            tabs.append(button).append("<h4>").append(tabTitle).append("</h4>").append("</button>\n");
        }
        return "<div class=\"tabs\">\n" + tabs + "</div>";
    }

    public static String getTabOpening(HtmlResult result, Configuration config) {
        return "<div id=\"" + getTabId(result, config) + "\" class=\"tabcontent\">\n";
    }

    public static String getTabEnding(HtmlResult result) {
        String htmlResult = "";
        if(result.exception != null) {
            htmlResult += "<div class=\"error\">\n" +
                    "<pre style=\"color:red\">Flyway Exception: " + result.exception + "</pre>\n" +
                    "</div>\n";
        }
        return htmlResult + "</div>\n";
    }

    private static String getTabId(HtmlResult result, Configuration config) {
        HtmlRenderer<HtmlResult> correctRenderer = getRenderer(result, config);
        return (correctRenderer.tabTitle(result) + getFormattedTimestamp(result)).replace(" ", "");
    }

    private static String getFormattedTimestamp(HtmlResult result) {
        return result.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static String getEnd() {
        return "</div>\n</body>\n" + getScript() + "</html>\n";
    }

    public static String getReportType(HtmlResult result, Configuration config) {
        return getRenderer(result, config).tabTitle(result);
    }

    public static String getSummary(HtmlResult result) {
        String separator = String.join("", Collections.nCopies(8, "&nbsp;"));
        return "<div " + getSummaryStyle() + ">\n" +
                "<div>" + getSummaryText(result, separator, true) + "</div>" +
                "<div>" + getInformationalText() + "</div>\n" +
                "</div>\n";
    }

    public static String getSummaryText(HtmlResult result, String separator, boolean addStyles) {
        return "TODO"; //result.accept(new HtmlSummaryTextVisitor(separator, addStyles)); //TODO
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

    public static String getInformationalText() {
        return "<img style=\"vertical-align: top; margin-right: 6px\" width=16 height=16 src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACYAAAAmCAYAAACoPemuAAACyElEQVRYhe2YT1IaQRSHf68bsw1HGE+QMWSXitoniJ7AZJdKUTo3EE4QiJSVpTmBeIIBszRq3yDjDSZrfP2yCJARGmRmQFzk23VXz6uvXs/rf8AzhZYVKPwUB9m2/WaSMvEKiYVRXAVjT0PvCMkuCQXegSQWQpaFL2zHdFcmFkZxVbnKEYlEAKp5vgWQAOjxgJuLZHNhsdeHP44LCk0hRI2br++apcTCKA4qrnIuImFZoQkSHrCZlb25YuHnONRax1hClmaQKOb9q1NjFxZ7AqkRqWI2k3JesTCKA+10DIG/2pZPygPeyk6r8o0qJCXS3hhsbCrWWyQy98f2UNUv1Hm2Yypjw+pr5Ax8dn2y/THbUav3z0B0kCeIiDRvOjsNYCJjYRQHBaRAQt89vbkWVAAgoqMwiqtTYpr1cd5gAOCcTE27UKGiqap7FQETU1k7vPxV6IcnsaycsS2TAn93CO30bcHiSVnz5lisVr/cA+F83hePkIjId6XppTh8QIllhoRN5V8L70tIAUBARMfiSkYBIKQPKplWCJIy0SIh+Q0AcKiSwpcSbruZjJXbC52o7m3n7R0wXKBZlxEL1ChQGalV4F35nwP/xfKiAMC2yl0cVsE4Y0J4NnJEZMdi5KS/TpksImIzGaPeOmUeILgYiznNXQDpGnVGJNed7e5YzLZMKiLtdRoN6QETy4WruBbWnDUecBPwHa3r/QYR5T4wCsmDqp75bDAHEmn+HB6tvbek2mH/FkLLvuA+RnJ9sr05anhXflZuH087pQkP2GQ7vGK2ZRLFbPA0cikz708+FczcK69OjVXMBqvdEVJmNtbzRDB3E786NZbVauSIyPKAt3xSQI5nqDf1fkMKVKuHlETao+qbRd6Hu0DfqwYU7RS4mqUk0r6vuNbomrc0sSy1+uUeIHtCFBLwyjdGgDsS9ITQc5q7iwiN+AOQ/zVlelSVVQAAAABJRU5ErkJggg==\" />" +
                " You can read more about the check command and other available report types <a href=\"https://flywaydb.org/documentation/command/check\">here</a>.\n";
    }

    private static String getScript() {
        return "<script>\n" +
                "function onTimestampClick(event, timestamp) {\n" +
                "    let pages = document.getElementsByClassName(\"page\");\n" +
                "    for (let i = 0; i < pages.length; i++) {\n" +
                "        pages[i].style.display = \"none\";\n" +
                "    }\n" +

                "    let selectedPage = document.getElementsByClassName(\"page \" + timestamp)[0];\n" +
                "    selectedPage.style.display = \"block\";\n" +

                "    document.getElementById(\"open-\" + timestamp).click();\n" +
                "}\n" +

                "function onTabClick(event, id) {\n" +
                "  // Get all elements with class=\"tabcontent\" and hide them\n" +
                "  let tabcontents = document.getElementsByClassName(\"tabcontent\");\n" +
                "  for (let i = 0; i < tabcontents.length; i++) {\n" +
                "    tabcontents[i].style.display = \"none\";\n" +
                "  }\n" +

                "  // Get all elements with class=\"tab\" and remove the class \"active\"\n" +
                "  let tabs = document.getElementsByClassName(\"tab\");\n" +
                "  for (let i = 0; i < tabs.length; i++) {\n" +
                "    tabs[i].className = tabs[i].className.replace(\" active\", \"\");\n" +
                "  }\n" +

                "  // Show the current tab, and add an \"active\" class to the button that opened the tab\n" +
                "  document.getElementById(id).style.display = \"block\";\n" +
                "  event.currentTarget.className += \" active\";\n" +
                "}\n" +

                "let timestamp = document.getElementById(\"dropdown\").value;\n" +
                "onTimestampClick(null, timestamp);\n" +
                "</script>\n";
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
        return "pre {\n" +
                "    counter-reset: line;\n" +
                "    font-size: large;\n" +
                "}\n" +
                "code {\n" +
                "    counter-increment: line;\n" +
                "}\n" +
                "code:before {\n" +
                "    content: counter(line);\n" +
                "    color: #808080;\n" +
                "    margin-right: 10px;\n" +
                "    margin-left: 5px;\n" +
                "    font-size: 14px;\n" +
                "    -webkit-user-select: none;\n" +
                "}\n" +
                ".added {\n" +
                "    background-color: #45EA85\n" +
                "}\n" +
                ".keyword {\n" +
                "    color: #0000ff;\n" +
                "}\n" +
                ".comment {\n" +
                "    color: #808080;\n" +
                "}\n" +
                ".after {\n" +
                "    background-color: #E6FFEc;\n" +
                "}\n" +
                ".before {\n" +
                "    background-color: #FFEBE9;\n" +
                "}\n" +
                ".textInsert {\n" +
                "   background-color: #ABF2BC\n" +
                "}\n" +
                ".textDelete {\n" +
                "   background-color: #FF818266\n" +
                "}\n" +
                ".tabs {\n" +
                "   overflow: hidden;\n" +
                "   margin: 0 100px 30px 0;\n" +
                "   border-bottom: 1px solid #BBBBBB;\n" +
                "}\n" +
                ".tabs button {\n" +
                "   background-color: transparent;\n" +
                "   border: none;\n" +
                "   cursor: pointer;\n" +
                "   padding: 0px 40px;\n" +
                "   max-width: 200px;\n" +
                "}\n" +
                ".tabs button:hover, .tabs button.active {\n" +
                "   border-bottom: 2px solid #CC0000;\n" +
                "}\n" +
                ".tabcontent, .page {\n" +
                "    display: none;\n" +
                "}\n" +
                ".dropdown>select {\n" +
                "    padding: 8px 12px;\n" +
                "}\n" +
                "summary {\n" +
                "   display: inline;\n" +
                "   list-style-image: url(data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGZpbGw9Im5vbmUiIHZpZXdCb3g9IjAgMCAyNCAyNCIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZT0iY3VycmVudENvbG9yIiBjbGFzcz0idy02IGgtNiI+DQogIDxwYXRoIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIgZD0iTTguMjUgNC41bDcuNSA3LjUtNy41IDcuNSIgLz4NCjwvc3ZnPg0K);\n" +
                "}\n" +
                "summary>h3, summary>h4 {\n" +
                "   display: list-item;\n" +
                "   list-style-position: inside;\n" +
                "}\n" +
                "details[open]>summary {\n" +
                "    list-style-image: url(data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIGZpbGw9Im5vbmUiIHZpZXdCb3g9IjAgMCAyNCAyNCIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZT0iY3VycmVudENvbG9yIiBjbGFzcz0idy02IGgtNiI+DQogIDxwYXRoIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIgc3Ryb2tlLWxpbmVqb2luPSJyb3VuZCIgZD0iTTE5LjUgOC4yNWwtNy41IDcuNS03LjUtNy41IiAvPg0KPC9zdmc+DQo=);\n" +
                "}\n" +
                "h3::marker {\n" +
                "   font-size: 30px;\n" +
                "}\n" +
                "h4::marker {\n" +
                "   font-size: 24px;\n" +
                "}\n" +
                "details {\n" +
                "   border: 2px solid #F2F2F2;\n" +
                "   border-radius: 5px;\n" +
                "   padding: 0 6px;\n" +
                "   margin-top: 10px;\n" +
                "}\n" +
                "details>details {\n" +
                "    background-color: #F2F2F2;\n" +
                "    padding: 0 4px 4px 6px;\n" +
                "    margin: 0 10px 10px 0;\n" +
                "}\n";
    }
}