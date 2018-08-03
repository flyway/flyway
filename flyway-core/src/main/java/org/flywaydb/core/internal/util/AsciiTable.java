/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.util;

import java.util.ArrayList;
import java.util.List;

/**
 * An Ascii table.
 */
public class AsciiTable {
    private final List<String> columns;
    private final List<List<String>> rows;
    private final boolean printHeader;
    private final String nullText;
    private final String emptyText;

    /**
     * Creates a new Ascii table.
     *
     * @param columns     The column titles.
     * @param rows        The data rows
     * @param printHeader Whether to print the header row or not.
     * @param nullText    The text to use for a {@code null} value.
     * @param emptyText   The text to include in the table if it has no rows.
     */
    public AsciiTable(List<String> columns, List<List<String>> rows, boolean printHeader, String nullText, String emptyText) {
        this.columns = columns;
        this.rows = rows;
        this.printHeader = printHeader;
        this.nullText = nullText;
        this.emptyText = emptyText;
    }

    /**
     * @return The table rendered with column header and row data.
     */
    public String render() {
        List<Integer> widths = new ArrayList<>();
        for (String column : columns) {
            widths.add(column.length());
        }

        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                widths.set(i, Math.max(widths.get(i), getValue(row, i).length()));
            }
        }

        StringBuilder ruler = new StringBuilder("+");
        for (Integer width : widths) {
            ruler.append("-").append(StringUtils.trimOrPad("", width, '-')).append("-+");
        }
        ruler.append("\n");

        StringBuilder result = new StringBuilder();

        if (printHeader) {
            StringBuilder header = new StringBuilder("|");
            for (int i = 0; i < widths.size(); i++) {
                header.append(" ").append(StringUtils.trimOrPad(columns.get(i), widths.get(i), ' ')).append(" |");
            }
            header.append("\n");

            result.append(ruler);
            result.append(header);
        }

        result.append(ruler);

        if (rows.isEmpty()) {
            result.append("| ").append(StringUtils.trimOrPad(emptyText, ruler.length() - 5)).append(" |\n");
        } else {
            for (List<String> row : rows) {
                StringBuilder r = new StringBuilder("|");
                for (int i = 0; i < widths.size(); i++) {
                    r.append(" ").append(StringUtils.trimOrPad(getValue(row, i), widths.get(i), ' ')).append(" |");
                }
                r.append("\n");
                result.append(r);
            }
        }

        result.append(ruler);
        return result.toString();
    }

    private String getValue(List<String> row, int i) {
        String value = row.get(i);
        if (value == null) {
            value = nullText;
        }
        return value;
    }
}