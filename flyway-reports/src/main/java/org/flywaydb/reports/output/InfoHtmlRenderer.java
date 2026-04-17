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
package org.flywaydb.reports.output;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.InfoResult;
import org.flywaydb.reports.api.extensibility.HtmlRenderer;
import org.flywaydb.reports.api.extensibility.HtmlReportSummary;
import org.flywaydb.core.internal.util.DateUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class InfoHtmlRenderer implements HtmlRenderer<InfoResult> {
    @Override
    public String render(final InfoResult result, final Configuration config) {
        return getBody(result);
    }

    private String getBody(final InfoResult result) {
        final HtmlTableRenderer tableRenderer = new HtmlTableRenderer();
        tableRenderer.addHeadings("Version", "Category", "Description", "Type", "Installed On", "State", "Undoable");

        result.migrations.forEach(migration -> tableRenderer.addRow(migration.version,
                                                                    migration.category,
                                                                    migration.description,
                                                                    migration.type,
                                                                    StringUtils.hasText(migration.installedOnUTC) ? DateUtils.formatStringAsIsoDateString(migration.installedOnUTC) : "--",
                                                                    migration.state, migration.undoable));

        return tableRenderer.render();
    }

    @Override
    public String tabTitle(final InfoResult result, final Configuration config) {
        return "Info Report";
    }

    @Override
    public Class<InfoResult> getType() {
        return InfoResult.class;
    }

    @Override
    public List<HtmlReportSummary> getHtmlSummary(final InfoResult result, final Configuration config) {
        final List<HtmlReportSummary> htmlResult = new ArrayList<>();

        final int pending = (int) result.migrations.stream().filter(f -> "Pending".equals(f.state)).count();

        final HtmlReportSummary pendingSummary = new HtmlReportSummary();
        pendingSummary.setSummaryText(pending + " script" + StringUtils.pluralizeSuffix(pending) + " pending");
        pendingSummary.setIcon("scriptOutlined");
        pendingSummary.setCssClass(pending > 0 ? "scInfo" : "scAmbivalent");

        htmlResult.add(pendingSummary);

        final int deployed = (int) result.migrations.stream().filter(f -> "Success".equals(f.state)).count();

        final HtmlReportSummary deployedSummary = new HtmlReportSummary();
        deployedSummary.setSummaryText(deployed + " script" + StringUtils.pluralizeSuffix(deployed) + " succeeded");
        deployedSummary.setIcon("checkFilled");
        deployedSummary.setCssClass(deployed > 0 ? "scGood" : "scAmbivalent");

        htmlResult.add(deployedSummary);

        if (!"default".equals(config.getCurrentEnvironmentName())) {
            htmlResult.add(new HtmlReportSummary("summaryNote",
                "infoOutlined",
                "Environment: " + config.getCurrentEnvironmentName()));
        }


        return htmlResult;
    }
}
