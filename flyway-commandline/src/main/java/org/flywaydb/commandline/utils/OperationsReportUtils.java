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
package org.flywaydb.commandline.utils;

import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CompositeResult;
import org.flywaydb.core.api.output.HtmlResult;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.internal.util.HtmlUtils;
import org.flywaydb.core.internal.util.JsonUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OperationsReportUtils {

    private static final Pattern REPORT_FILE_PATTERN = Pattern.compile("\\.html?$");

    public static String getBaseFilename(String filename) {
        if (REPORT_FILE_PATTERN.matcher(filename).find()) {
            return filename.replaceAll(REPORT_FILE_PATTERN.pattern(), "");
        }
        return filename;
    }

    public static String createHtmlReport(Configuration configuration, CompositeResult<HtmlResult> htmlCompositeResult, String tmpHtmlReportFilename) {
        try {
            return HtmlUtils.toHtmlFile(tmpHtmlReportFilename, htmlCompositeResult, configuration);
        } catch (FlywayException e) {
            LOG.error("Unable to create HTML report", e);
            return null;
        }
    }

    public static String createJsonReport(CompositeResult<HtmlResult> htmlCompositeResult, String tmpJsonReportFilename) {
        try {
            return JsonUtils.jsonToFile(tmpJsonReportFilename, htmlCompositeResult);
        } catch (FlywayException e) {
            LOG.error("Unable to create JSON report", e);
            return null;
        }
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
}