/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.core.api.output;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.HtmlRenderer;
import org.flywaydb.core.extensibility.HtmlReportSummary;
import org.flywaydb.core.internal.util.FileUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.flywaydb.core.internal.util.TimeFormat.*;

public class MigrateHtmlRenderer implements HtmlRenderer<MigrateResult> {
    @Override
    public String render(MigrateResult result, Configuration config) {

        return getBody(result);
    }

    private String getBody(MigrateResult result) {
        StringBuilder html = new StringBuilder("<div>");

        if (result.warnings != null && result.warnings.size() > 0) {
            html.append("  <div><h3>Warnings</h3>\n");
            for (String warning : result.warnings) {
                html.append("    <pre>").append(warning).append("</pre>\n");
            }
            html.append("  </div>\n");
        }

        if (result.migrations != null && result.migrations.size() > 0) {
            HtmlTableRenderer tableRenderer = new HtmlTableRenderer();
            tableRenderer.addHeadings("Version", "Description", "Category", "Type", "Filepath", "ExecutionTime");
            result.migrations.forEach(output -> tableRenderer.addRow(output.version,
                                                                     output.description,
                                                                     output.category,
                                                                     output.type,
                                                                     FileUtils.getFilename(output.filepath),
                                                                     format(output.executionTime)));

            html.append(tableRenderer.render());
        } else {
            html.append("  <h3>No migrations found</h3>\n");
        }

        html.append("</div>\n");

        return html.toString();
    }

    @Override
    public String tabTitle(MigrateResult result, Configuration config) {
        return config.getDryRunOutput() == null ? "Migration report" : "DryRun report";
    }

    @Override
    public Class<MigrateResult> getType() {
        return MigrateResult.class;
    }

    @Override
    public List<HtmlReportSummary> getHtmlSummary(MigrateResult result) {
        List<HtmlReportSummary> htmlResult = new ArrayList<>();
        int migratedCount = result.migrationsExecuted;
        String databaseVersion = result.targetSchemaVersion == null
                ? result.migrations.stream().min((a, b) -> b.version.compareTo(a.version)).map(m -> m.version).orElse("0")
                : result.targetSchemaVersion;

        htmlResult.add(new HtmlReportSummary(result.targetSchemaVersion != null? "scGood" : "scError", "infoOutlined", "Database version: " + databaseVersion));
        htmlResult.add(new HtmlReportSummary(migratedCount > 0 ? "scGood" : "scWarn", "checkFilled", migratedCount + " script" + StringUtils.pluralizeSuffix(migratedCount) + " migrated"));
        if (StringUtils.hasText(result.schemaName)) {
            htmlResult.add(new HtmlReportSummary("scNote", "database", "Database Schema: " + result.schemaName));
        }
        htmlResult.add(new HtmlReportSummary("scNote", "clockOutlined", "Execution Time: " + format(result.migrations.stream().mapToInt(i -> i.executionTime).sum())));

        return htmlResult;
    }
}