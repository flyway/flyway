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
package org.flywaydb.reports;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.output.CompositeResult;
import org.flywaydb.core.api.output.HtmlResult;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.configuration.models.FlywayModel;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.reports.ReportDetails;
import org.flywaydb.core.internal.reports.ReportGenerationOutput;
import org.flywaydb.core.internal.reports.ResultReportGenerator;
import org.flywaydb.core.internal.util.FileUtils;
import org.flywaydb.core.internal.util.JsonUtils;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.reports.utils.HtmlUtils;
import org.flywaydb.reports.utils.ReportsDeserializer;

@CustomLog
public class OperationResultReportGenerator implements ResultReportGenerator {
    private static final String DEFAULT_REPORT_FILENAME = FlywayModel.DEFAULT_REPORT_FILENAME;
    private static final String JSON_REPORT_EXTENSION = ".json";
    private static final String HTML_REPORT_EXTENSION = ".html";
    private static final String HTM_REPORT_EXTENSION = ".htm";

    private static final Pattern REPORT_FILE_PATTERN = Pattern.compile("\\.html?$");
    private static final Set<String> ALWAYS_REPORT_OPERATIONS = Set.of("changes", "drift", "dryrun", "code");

    @Override
    public ReportGenerationOutput generateReport(final OperationResult operationResult,
        final Configuration configuration) {
        ReportDetails reportDetails = new ReportDetails();
        Exception aggregateException = null;

        final Collection<HtmlResult> flattenedResults = flattenResults(operationResult).filter(HtmlResult.class::isInstance)
            .map(HtmlResult.class::cast)
            .toList();
        final Collection<HtmlResult> filteredResults = flattenedResults.stream()
            .filter(result -> configuration.isReportEnabled() || ALWAYS_REPORT_OPERATIONS.contains(result.getOperation()
                .toLowerCase(Locale.ROOT)))
            .toList();

        if (!filteredResults.isEmpty()) {
            reportDetails = writeReport(configuration, filteredResults);
        }

        if (!flattenedResults.isEmpty()) {
            aggregateException = getAggregateExceptions(flattenedResults);
        }

        return new ReportGenerationOutput(reportDetails, aggregateException);
    }

    private ReportDetails writeReport(final Configuration configuration,
        final Collection<? extends HtmlResult> filteredResults) {
        final ReportDetails reportDetails = new ReportDetails();
        final String reportFilename = configuration.getReportFilename();
        final String baseReportFilename = getBaseFilename(reportFilename);

        final String jsonReportFilename = baseReportFilename + JSON_REPORT_EXTENSION;
        final String htmlReportFilename = baseReportFilename + (reportFilename.endsWith(HTM_REPORT_EXTENSION)
            ? HTM_REPORT_EXTENSION
            : HTML_REPORT_EXTENSION);

        final String jsonReportAbsolutePath = ConfigUtils.getFilenameWithWorkingDirectory(jsonReportFilename,
            configuration);
        final String htmlReportAbsolutePath = ConfigUtils.getFilenameWithWorkingDirectory(htmlReportFilename,
            configuration);

        try {
            final Collection<HtmlResult> combinedResults = combineWithExistingResultsIfFileExists(jsonReportAbsolutePath,
                filteredResults,
                configuration.getPluginRegister());
            final Collection<HtmlResult> resultsPerOperation = restrictToOneResultPerOperation(combinedResults);
            reportDetails.setJsonReportFilename(JsonUtils.jsonToFile(jsonReportAbsolutePath,
                new CompositeResult<>(resultsPerOperation)));
            reportDetails.setHtmlReportFilename(HtmlUtils.toHtmlFile(htmlReportAbsolutePath,
                resultsPerOperation,
                configuration));
        } catch (final FlywayException e) {
            if (DEFAULT_REPORT_FILENAME.equals(reportFilename)) {
                LOG.warn("Unable to create default report files.");
                if (LogFactory.isDebugEnabled()) {
                    e.printStackTrace(System.out);
                }
            } else {
                LOG.error("Unable to create report files", e);
            }
        }

        return reportDetails;
    }

    private Stream<OperationResult> flattenResults(final OperationResult result) {
        return result instanceof final CompositeResult<?> compositeResult ? compositeResult.individualResults()
            .stream()
            .flatMap(this::flattenResults) : Stream.of(result);
    }

    private static String getBaseFilename(final String filename) {
        if (REPORT_FILE_PATTERN.matcher(filename).find()) {
            return filename.replaceAll(REPORT_FILE_PATTERN.pattern(), "");
        }
        return filename;
    }

    private static Collection<HtmlResult> combineWithExistingResultsIfFileExists(final String filename,
        final Collection<? extends HtmlResult> newResults,
        final PluginRegister pluginRegister) {
        final Collection<HtmlResult> existingResults = deserializeExistingFileIfExists(filename, pluginRegister);
        return Stream.concat(existingResults.stream(), newResults.stream()).toList();
    }

    private static <T extends OperationResult> Collection<T> deserializeExistingFileIfExists(final String filename,
        final PluginRegister pluginRegister) {
        final Path path = Path.of(filename);
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }

        final String jsonText = FileUtils.readAsString(path);
        if (!StringUtils.hasText(jsonText)) {
            return Collections.emptyList();
        }

        final ReportsDeserializer reportsDeserializer = new ReportsDeserializer(pluginRegister);
        final JsonMapper mapper = JsonUtils.getJsonMapper()
                .rebuild()
                .addModule(new SimpleModule().addDeserializer(OperationResult.class, reportsDeserializer))
                .build();

        final CompositeResult<T> existingObject;
        try {
            existingObject = mapper.readValue(jsonText, new TypeReference<>() {});
        } catch (final Exception e) {
            throw new FlywayException("Unable to read filename: " + filename, e);
        }

        if (existingObject == null) {
            throw new FlywayException("Unable to deserialize existing JSON file: " + filename);
        }

        return existingObject.individualResults();
    }

    private static <T extends HtmlResult> Collection<T> restrictToOneResultPerOperation(final Collection<? extends T> results) {
        return results.stream()
            .collect(Collectors.<T, String>groupingBy(HtmlResult::getOperation))
            .values()
            .stream()
            .map(resultsForOperation -> resultsForOperation.stream()
                .max(Comparator.comparing(HtmlResult::getTimestamp)))
            .flatMap(Optional::stream)
            .toList();
    }

    private Exception getAggregateExceptions(final Collection<? extends HtmlResult> results) {
        Exception aggregate = null;
        final var exceptions = results.stream().map(x -> x.exceptionObject).filter(Objects::nonNull).toList();
        for (final Exception e : exceptions) {
            if (aggregate == null) {
                aggregate = e;
            } else {
                aggregate.addSuppressed(e);
            }
        }
        return aggregate;
    }
}
