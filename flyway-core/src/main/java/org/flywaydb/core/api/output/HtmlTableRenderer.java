package org.flywaydb.core.api.output;

import org.flywaydb.core.api.FlywayException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HtmlTableRenderer {
    private List<String> fields;
    private List<List<String>> rows;

    public void addHeadings(String... fields) {
        this.fields = Arrays.asList(fields);
    }

    public void addRow(String... row) {
        if (row.length != fields.size()) {
            throw new FlywayException("Row must have the same number of fields as the table headings");
        }

        if (rows == null) {
            rows = new ArrayList<>();
        }

        this.rows.add(Arrays.asList(row));
    }

    public String render() {
        StringBuilder html = new StringBuilder();
        html.append("  <table class=\"tabulardata\">\n");
        html.append("    <thead>\n");
        html.append("       <tr>\n");
        for (String field : fields) {
            html.append("       <th>").append(field).append("</th>\n");
        }
        html.append("       </tr>\n");
        html.append("       </thead>\n");
        html.append("    <tbody>\n");

        if (rows == null) {
            html.append("<tr><td colspan=\"").append(fields.size()).append("\">No data</td></tr>\n");
        } else {
            for (List<String> row : rows) {
                html.append("    <tr>\n");

                for (String field : row) {
                    html.append("      <td>").append(field).append("</td>\n");
                }

                html.append("    </tr>\n");
            }
        }

        html.append("    </tbody>\n");
        html.append("  </table>\n");

        return html.toString();
    }
}