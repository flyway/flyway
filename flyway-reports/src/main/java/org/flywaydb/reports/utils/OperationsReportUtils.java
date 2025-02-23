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
package org.flywaydb.reports.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.reports.ReportDetails;
import org.flywaydb.core.internal.util.FileUtils;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.JsonUtils;
import org.flywaydb.reports.json.HtmlResultDeserializer;

@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OperationsReportUtils {

    private static final String DEFAULT_REPORT_FILENAME = FlywayModel.DEFAULT_REPORT_FILENAME;
    private static final String JSON_REPORT_EXTENSION = ".json";
    private static final String HTML_REPORT_EXTENSION = ".html";
    private static final String HTM_REPORT_EXTENSION = ".htm";

    private static final Pattern REPORT_FILE_PATTERN = Pattern.compile("\\.html?$");

    static String getBaseFilename(final String filename) {
        if (REPORT_FILE_PATTERN.matcher(filename).find()) {
            return filename.replaceAll(REPORT_FILE_PATTERN.pattern(), "");
        }
        return filename;
    }

    static String createHtmlReport(final Configuration configuration,
        final CompositeResult<HtmlResult> htmlCompositeResult,
        final String tmpHtmlReportFilename) {
        return HtmlUtils.toHtmlFile(tmpHtmlReportFilename, htmlCompositeResult, configuration);
    }

    static String createJsonReport(final CompositeResult<HtmlResult> htmlCompositeResult,
        final String tmpJsonReportFilename) {
        return JsonUtils.jsonToFile(tmpJsonReportFilename, htmlCompositeResult);
    }

    public static ReportDetails writeReport(final Configuration configuration,
        final OperationResult filteredResults,
        final LocalDateTime executionTime) {
        final ReportDetails reportDetails = new ReportDetails();
        CompositeResult<HtmlResult> htmlCompositeResult = removeRedundantHtmlResults(flattenHtmlResults(filteredResults),
            configuration.isReportEnabled());

        if (htmlCompositeResult != null && !htmlCompositeResult.individualResults.isEmpty()) {
            htmlCompositeResult.individualResults.forEach(r -> r.setTimestamp(executionTime));

            final String reportFilename = configuration.getReportFilename();
            final String baseReportFilename = getBaseFilename(reportFilename);

            String tmpJsonReportFilename = baseReportFilename + JSON_REPORT_EXTENSION;
            String tmpHtmlReportFilename = baseReportFilename + (reportFilename.endsWith(HTM_REPORT_EXTENSION)
                ? HTM_REPORT_EXTENSION
                : HTML_REPORT_EXTENSION);

            tmpJsonReportFilename = ConfigUtils.getFilenameWithWorkingDirectory(tmpJsonReportFilename, configuration);
            tmpHtmlReportFilename = ConfigUtils.getFilenameWithWorkingDirectory(tmpHtmlReportFilename, configuration);

            try {
                htmlCompositeResult = appendIfExists(tmpJsonReportFilename,
                    htmlCompositeResult,
                    configuration.getPluginRegister());
                reportDetails.setJsonReportFilename(createJsonReport(htmlCompositeResult, tmpJsonReportFilename));
                reportDetails.setHtmlReportFilename(createHtmlReport(configuration,
                    htmlCompositeResult,
                    tmpHtmlReportFilename));
            } catch (final FlywayException e) {
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

    public static <T extends OperationResult> CompositeResult<T> appendIfExists(final String filename,
        final CompositeResult<T> newObject,
        final PluginRegister pluginRegister) {
        final Path path = Path.of(filename);
        if (!Files.exists(path)) {
            return newObject;
        }

        final String jsonText = FileUtils.readAsString(path);
        if (!StringUtils.hasText(jsonText)) {
            return newObject;
        }

        final JsonMapper mapper = JsonUtils.getJsonMapper();

        final ReportsDeserializer reportsDeserializer = new ReportsDeserializer(pluginRegister);
        mapper.registerModule(new SimpleModule().addDeserializer(OperationResult.class, reportsDeserializer));

        try {
            final CompositeResult<T> existingObject = mapper.readValue(jsonText, new TypeReference<>() {});
            if (existingObject == null || existingObject.individualResults.isEmpty()) {
                throw new FlywayException("Unable to deserialize existing JSON file: " + filename);
            }
            existingObject.individualResults.addAll(newObject.individualResults);
            return existingObject;
        } catch (final Exception e) {
            throw new FlywayException("Unable to read filename: " + filename, e);
        }
    }

    public static OperationResult filterHtmlResults(final OperationResult result) {
        if (result instanceof CompositeResult<?>) {
            final List<OperationResult> filteredResults = ((CompositeResult<?>) result).individualResults.stream().map(
                OperationsReportUtils::filterHtmlResults).filter(Objects::nonNull).collect(Collectors.toList());

            if (filteredResults.isEmpty()) {
                return null;
            }
            final CompositeResult<OperationResult> htmlCompositeResult = new CompositeResult<>();
            htmlCompositeResult.individualResults.addAll(filteredResults);
            return htmlCompositeResult;
        } else if (result instanceof HtmlResult) {
            return result;
        }
        return null;
    }

    public static Exception getAggregateExceptions(final OperationResult result) {
        if (result instanceof CompositeResult<?>) {
            Exception aggregate = null;
            final List<Exception> exceptions = ((CompositeResult<?>) result).individualResults.stream().map(
                OperationsReportUtils::getAggregateExceptions).filter(Objects::nonNull).collect(Collectors.toList());
            for (final Exception e : exceptions) {
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

    static CompositeResult<HtmlResult> flattenHtmlResults(final OperationResult result) {
        final CompositeResult<HtmlResult> htmlCompositeResult = new CompositeResult<>();
        if (result instanceof CompositeResult<?>) {
            final List<HtmlResult> htmlResults = ((CompositeResult<?>) result).individualResults.stream().map(
                OperationsReportUtils::flattenHtmlResults).flatMap(r -> r.individualResults.stream()).toList();
            htmlCompositeResult.individualResults.addAll(htmlResults);
        } else if (result instanceof HtmlResult) {
            htmlCompositeResult.individualResults.add((HtmlResult) result);
        }
        return htmlCompositeResult;
    }

    static CompositeResult<HtmlResult> removeRedundantHtmlResults(final CompositeResult<HtmlResult> htmlCompositeResult,
        final boolean isReportEnabled) {
        if (htmlCompositeResult == null || htmlCompositeResult.individualResults == null) {
            return null;
        }

        if (!isReportEnabled) {
            htmlCompositeResult.individualResults = htmlCompositeResult.individualResults.stream().filter(r -> List.of(
                "changes",
                "drift",
                "dryrun",
                "code").contains(r.getOperation().toLowerCase())).collect(Collectors.toList());
        }
        return htmlCompositeResult;
    }
}
