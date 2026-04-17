/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.reports;

import java.nio.file.Path;
import java.util.regex.Pattern;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.configuration.ConfigUtils;

public class ReportPathResolver {
    private static final String JSON_REPORT_EXTENSION = ".json";
    private static final String HTML_REPORT_EXTENSION = ".html";
    private static final String HTM_REPORT_EXTENSION = ".htm";
    private static final String SARIF_REPORT_EXTENSION = ".sarif";
    private static final Pattern REPORT_FILE_PATTERN = Pattern.compile("\\.html?$");

    public static ResolvedReportPaths resolve(final Configuration configuration) {
        final String reportFilename = configuration.getReportFilename();
        final String baseFilename = getBaseFilename(reportFilename);

        final String jsonFilename = baseFilename + JSON_REPORT_EXTENSION;
        final String htmlFilename = baseFilename + (reportFilename.endsWith(HTM_REPORT_EXTENSION)
            ? HTM_REPORT_EXTENSION
            : HTML_REPORT_EXTENSION);
        final String sarifFilename = baseFilename + "-code-review" + SARIF_REPORT_EXTENSION;

        return new ResolvedReportPaths(Path.of(ConfigUtils.getFilenameWithWorkingDirectory(jsonFilename,
            configuration)), Path.of(ConfigUtils.getFilenameWithWorkingDirectory(htmlFilename, configuration)),
            Path.of(ConfigUtils.getFilenameWithWorkingDirectory(sarifFilename, configuration)));
    }

    private static String getBaseFilename(final String filename) {
        if (REPORT_FILE_PATTERN.matcher(filename).find()) {
            return filename.replaceAll(REPORT_FILE_PATTERN.pattern(), "");
        }
        return filename;
    }
}
