/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.commandline.utils;

import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CompositeResult;
import org.flywaydb.core.api.output.HtmlResult;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.configuration.models.FlywayModel;
import org.flywaydb.core.internal.reports.ReportDetails;
import org.flywaydb.core.internal.reports.json.CompositeResultDeserializer;
import org.flywaydb.core.internal.util.HtmlUtils;
import org.flywaydb.core.internal.util.JsonUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OperationsReportUtils {

    public static final String DEFAULT_REPORT_FILENAME = FlywayModel.DEFAULT_REPORT_FILENAME;
    public static final String JSON_REPORT_EXTENSION = ".json";
    public static final String HTML_REPORT_EXTENSION = ".html";
    public static final String HTM_REPORT_EXTENSION = ".htm";

    private static final Pattern REPORT_FILE_PATTERN = Pattern.compile("\\.html?$");

    public static String getBaseFilename(String filename) {
        if (REPORT_FILE_PATTERN.matcher(filename).find()) {
            return filename.replaceAll(REPORT_FILE_PATTERN.pattern(), "");
        }
        return filename;
    }

    public static String createHtmlReport(Configuration configuration, CompositeResult<HtmlResult> htmlCompositeResult, String tmpHtmlReportFilename) {
        return HtmlUtils.toHtmlFile(tmpHtmlReportFilename, htmlCompositeResult, configuration);
    }

    public static String createJsonReport(CompositeResult<HtmlResult> htmlCompositeResult, String tmpJsonReportFilename) {
        return JsonUtils.jsonToFile(tmpJsonReportFilename, htmlCompositeResult);
    }

    public static ReportDetails writeReport(Configuration configuration, OperationResult filteredResults, LocalDateTime executionTime) {
        ReportDetails reportDetails = new ReportDetails();
        CompositeResult<HtmlResult> htmlCompositeResult = removeRedundantHtmlResults(flattenHtmlResults(filteredResults), configuration.isReportEnabled()) ;

        if (htmlCompositeResult != null && !htmlCompositeResult.individualResults.isEmpty()) {

            htmlCompositeResult.individualResults.forEach(r -> r.setTimestamp(executionTime));

            String reportFilename = configuration.getReportFilename();
            String baseReportFilename = getBaseFilename(reportFilename);

            String tmpJsonReportFilename = baseReportFilename + JSON_REPORT_EXTENSION;
            String tmpHtmlReportFilename = baseReportFilename + (reportFilename.endsWith(HTM_REPORT_EXTENSION) ? HTM_REPORT_EXTENSION : HTML_REPORT_EXTENSION);

            tmpJsonReportFilename = ConfigUtils.getFilenameWithWorkingDirectory(tmpJsonReportFilename, configuration);
            tmpHtmlReportFilename = ConfigUtils.getFilenameWithWorkingDirectory(tmpHtmlReportFilename, configuration);

            try {
                htmlCompositeResult = JsonUtils.appendIfExists(tmpJsonReportFilename, htmlCompositeResult, new CompositeResultDeserializer(configuration.getPluginRegister()));
                reportDetails.setJsonReportFilename(createJsonReport(htmlCompositeResult, tmpJsonReportFilename));
                reportDetails.setHtmlReportFilename(createHtmlReport(configuration, htmlCompositeResult, tmpHtmlReportFilename));
            } catch (FlywayException e) {
                if (DEFAULT_REPORT_FILENAME.equals(reportFilename)) {
                    LOG.warn("Unable to create default report files.");
                    if (LOG.isDebugEnabled()) {
                        e.printStackTrace(System.out);
                    }
                } else {
                    LOG.error("Unable to create report files", e);
                }
            }

            if (reportDetails.getHtmlReportFilename() != null) {
                LOG.info("A Flyway report has been generated here: " + reportDetails.getHtmlReportFilename());
            }
        }

        return reportDetails;
    }

    public static OperationResult filterHtmlResults(OperationResult result) {
        if (result instanceof CompositeResult<?>) {
            List<OperationResult> filteredResults = ((CompositeResult<?>) result).individualResults.stream()
                                                                                           .map(OperationsReportUtils::filterHtmlResults)
                                                                                           .filter(Objects::nonNull)
                                                                                           .collect(Collectors.toList());

            if(filteredResults.isEmpty()) {
                return null;
            }
            CompositeResult<OperationResult> htmlCompositeResult = new CompositeResult<>();
            htmlCompositeResult.individualResults.addAll(filteredResults);
            return htmlCompositeResult;
        } else if (result instanceof HtmlResult) {
            return result;
        }
        return null;
    }

    public static Exception getAggregateExceptions(OperationResult result) {
        if (result instanceof CompositeResult<?>) {
            Exception aggregate = null;
            List<Exception> exceptions = ((CompositeResult<?>) result).individualResults.stream()
                                                                                        .map(OperationsReportUtils::getAggregateExceptions)
                                                                                        .filter(Objects::nonNull)
                                                                                        .collect(Collectors.toList());
            for (Exception e : exceptions) {
                if (aggregate == null) {
                    aggregate = e;
                } else {
                    aggregate.addSuppressed(e);
                }
            }
            return aggregate;
        } else if (result instanceof HtmlResult) {
            return ((HtmlResult) result).exceptionObject;
        }
        return null;
    }

    public static CompositeResult<HtmlResult> flattenHtmlResults(OperationResult result) {
        CompositeResult<HtmlResult> htmlCompositeResult = new CompositeResult<>();
        if (result instanceof CompositeResult<?>) {
            List<HtmlResult> htmlResults = ((CompositeResult<?>) result).individualResults.stream()
                                                                                          .map(OperationsReportUtils::flattenHtmlResults)
                                                                                          .flatMap(r -> r.individualResults.stream())
                                                                                          .collect(Collectors.toList());
            htmlCompositeResult.individualResults.addAll(htmlResults);
        } else if (result instanceof HtmlResult) {
            htmlCompositeResult.individualResults.add((HtmlResult) result);
        }
        return htmlCompositeResult;
    }

    public static CompositeResult<HtmlResult> removeRedundantHtmlResults(CompositeResult<HtmlResult> htmlCompositeResult, boolean isReportEnabled) {
        if(htmlCompositeResult == null || htmlCompositeResult.individualResults == null) {
            return null;
        }

        if(!isReportEnabled) {
            htmlCompositeResult.individualResults = htmlCompositeResult.individualResults.stream()
                                                                                         .filter(r -> List.of("changes", "drift", "dryrun", "code").contains(r.getOperation().toLowerCase()))
                                                                                         .collect(Collectors.toList());
        }
        return htmlCompositeResult;
    }
}