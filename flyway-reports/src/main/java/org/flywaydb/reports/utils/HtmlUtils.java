/*-
 * ========================LICENSE_START=================================
 * flyway-reports
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
package org.flywaydb.reports.utils;

import org.apache.commons.text.StringEscapeUtils;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.HtmlResult;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.output.CompositeResult;

import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;

import static org.flywaydb.reports.html.HtmlReportGenerator.generateHtml;
import static org.flywaydb.core.internal.util.FileUtils.createDirIfNotExists;

public class HtmlUtils {
    public static String toHtmlFile(String filename, CompositeResult<HtmlResult> results, Configuration config) {
        String fileContents = generateHtml(results, config);

        File file = new File(filename);

        createDirIfNotExists(file);

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(fileContents);
            return file.getCanonicalPath();
        } catch (Exception e) {
            throw new FlywayException("Unable to write HTML to file: " + e.getMessage());
        }

    }

    public static String getFormattedTimestamp(HtmlResult result) {
        if (result == null || result.getTimestamp() == null) {
            return "--";
        }
        return result.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String htmlEncode(String input) {
        return StringEscapeUtils.escapeHtml4(input);
    }
}
