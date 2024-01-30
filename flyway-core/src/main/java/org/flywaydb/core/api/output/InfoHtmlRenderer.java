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
        pendingSummary.setSummaryText(pending + " script" + StringUtils.pluralizeSuffix(pending) + " pending");
        pendingSummary.setIcon("scriptOutlined");
        pendingSummary.setCssClass(pending > 0 ? "scInfo" : "scAmbivalent");

        htmlResult.add(pendingSummary);

        int deployed = (int) result.migrations.stream().filter(f -> "Success".equals(f.state)).count();

        HtmlReportSummary deployedSummary = new HtmlReportSummary();
        deployedSummary.setSummaryText(deployed + " script" + StringUtils.pluralizeSuffix(deployed) + " succeeded");
        deployedSummary.setIcon("checkFilled");
        deployedSummary.setCssClass(deployed > 0 ? "scGood" : "scAmbivalent");

        htmlResult.add(deployedSummary);


        return htmlResult;
    }
}