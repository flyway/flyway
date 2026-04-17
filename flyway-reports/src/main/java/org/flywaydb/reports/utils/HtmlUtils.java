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
package org.flywaydb.reports.utils;

import static org.flywaydb.core.internal.util.FileUtils.createDirIfNotExists;
import static org.flywaydb.reports.html.HtmlReportGenerator.generateHtml;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import lombok.CustomLog;
import org.apache.commons.text.StringEscapeUtils;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.HtmlResult;

@CustomLog
public class HtmlUtils {
    public static String toHtmlFile(final String filename,
        final Collection<? extends HtmlResult> results,
        final Configuration config) {
        final String fileContents = generateHtml(results, config);

        final File file = new File(filename);

        createDirIfNotExists(file);

        try (final FileWriter fileWriter = new FileWriter(file, StandardCharsets.UTF_8)) {
            fileWriter.write(fileContents);
            final var canonicalPath = file.getCanonicalPath();
            LOG.info("A Flyway report has been generated here: " + canonicalPath);
            return canonicalPath;
        } catch (final Exception e) {
            throw new FlywayException("Unable to write HTML to file: " + e.getMessage(), e);
        }
    }

    public static String htmlEncode(final String input) {
        return StringEscapeUtils.escapeHtml4(input);
    }
}
