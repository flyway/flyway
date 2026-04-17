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

import org.flywaydb.core.api.FlywayException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HtmlTableRenderer {
    private List<String> fields;
    private List<List<String>> rows;

    public void addHeadings(final String... fields) {
        this.fields = Arrays.asList(fields);
    }

    public void addRow(final String... row) {
        if (row.length != fields.size()) {
            throw new FlywayException("Row must have the same number of fields as the table headings");
        }

        if (rows == null) {
            rows = new ArrayList<>();
        }

        this.rows.add(Arrays.asList(row));
    }

    public String render() {
        final StringBuilder html = new StringBuilder();
        html.append("  <table class=\"tabulardata\">\n");
        html.append("    <thead>\n");
        html.append("       <tr>\n");
        for (final String field : fields) {
            html.append("       <th>").append(field).append("</th>\n");
        }
        html.append("       </tr>\n");
        html.append("       </thead>\n");
        html.append("    <tbody>\n");

        if (rows == null) {
            html.append("<tr><td colspan=\"").append(fields.size()).append("\">No data</td></tr>\n");
        } else {
            for (final List<String> row : rows) {
                html.append("    <tr>\n");

                for (final String field : row) {
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
