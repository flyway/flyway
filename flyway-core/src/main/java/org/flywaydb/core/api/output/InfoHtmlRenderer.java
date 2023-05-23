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
import org.flywaydb.core.internal.util.DateUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class InfoHtmlRenderer implements HtmlRenderer<InfoResult> {
    @Override
    public String render(InfoResult result, Configuration config) {
        return getBody(result);
    }

    private String getBody(InfoResult result) {
        HtmlTableRenderer tableRenderer = new HtmlTableRenderer();
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
    public String tabTitle(InfoResult result, Configuration config) {
        return "Info Report";
    }

    @Override
    public Class<InfoResult> getType() {
        return InfoResult.class;
    }

    @Override
    public List<HtmlReportSummary> getHtmlSummary(InfoResult result) {
        List<HtmlReportSummary> htmlResult = new ArrayList<>();

        int pending = (int) result.migrations.stream().filter(f -> "Pending".equals(f.state)).count();

        HtmlReportSummary pendingSummary = new HtmlReportSummary();
        pendingSummary.setSummaryText(pending + " script" + (pending != 1 ? "s" : "") + " pending");
        pendingSummary.setIcon("scriptOutlined");
        pendingSummary.setCssClass(pending > 0 ? "scInfo" : "scAmbivalent");

        htmlResult.add(pendingSummary);

        int deployed = (int) result.migrations.stream().filter(f -> "Success".equals(f.state)).count();

        HtmlReportSummary deployedSummary = new HtmlReportSummary();
        deployedSummary.setSummaryText(deployed + " script" + (deployed != 1 ? "s" : "") + " succeeded");
        deployedSummary.setIcon("checkFilled");
        deployedSummary.setCssClass(deployed > 0 ? "scGood" : "scAmbivalent");

        htmlResult.add(deployedSummary);


        return htmlResult;
    }
}