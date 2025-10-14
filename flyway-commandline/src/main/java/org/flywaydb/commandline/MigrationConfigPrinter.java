/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.commandline;

import java.util.Arrays;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.migration.baseline.BaselineMigrationConfigurationExtension;

public class MigrationConfigPrinter {
    public static void print(final Log log, final Configuration configuration) {
        final Location[] locations = configuration.getLocations();
        final String locationsValue = String.join(", ", Arrays.stream(locations).map(Location::toString).toList());

        final Location[] callbackLocations = configuration.getCallbackLocations();
        final String callbackLocationsValue = String.join(", ",
            Arrays.stream(callbackLocations).map(Location::toString).toList());

        final String workingDirectory = configuration.getWorkingDirectory();
        final String workingDirectoryValue = (workingDirectory != null && !workingDirectory.isEmpty())
            ? workingDirectory
            : System.getProperty("user.dir");

        final String repeatablePrefixValue = configuration.getRepeatableSqlMigrationPrefix();

        final String sqlPrefixValue = configuration.getSqlMigrationPrefix();

        final BaselineMigrationConfigurationExtension baselineExt = configuration.getPluginRegister()
            .getExact(BaselineMigrationConfigurationExtension.class);
        final String baselinePrefixValue = baselineExt.getBaselineMigrationPrefix();

        final String sqlSeparatorValue = configuration.getSqlMigrationSeparator();

        final String[] sqlSuffixes = configuration.getSqlMigrationSuffixes();
        final String sqlSuffixesValue = String.join(", ", sqlSuffixes);

        final String[][] rows = { { "locations", locationsValue },
                                  { "callbackLocations", callbackLocationsValue },
                                  { "workingDirectory", workingDirectoryValue },
                                  { "repeatableSqlMigrationPrefix", repeatablePrefixValue },
                                  { "sqlMigrationPrefix", sqlPrefixValue },
                                  { "baselineMigrationPrefix", baselinePrefixValue },
                                  { "sqlMigrationSeparator", sqlSeparatorValue },
                                  { "sqlMigrationSuffixes", sqlSuffixesValue } };

        log.info("\n" + buildTable(rows));
    }

    private static String buildTable(final String[][] rows) {
        final int[] colWidths = { "Setting".length(), "Current value".length() };
        for (final String[] row : rows) {
            if (row[0].length() > colWidths[0]) {
                colWidths[0] = row[0].length();
            }
            if (row[1].length() > colWidths[1]) {
                colWidths[1] = row[1].length();
            }
        }
        colWidths[0] += 2;
        colWidths[1] += 2;

        final StringBuilder table = new StringBuilder();
        table.append(String.format("| %s | %s |\n", pad("Setting", colWidths[0]), pad("Value", colWidths[1])));
        table.append(String.format("|%s|%s|\n", makeLine(colWidths[0] + 2), makeLine(colWidths[1] + 2)));
        for (final String[] row : rows) {
            table.append(String.format("| %s | %s |\n", pad(row[0], colWidths[0]), pad(row[1], colWidths[1])));
        }
        return table.toString();
    }

    private static String pad(final String s, final int len) {
        if (s.length() >= len) {
            return s;
        }
        return String.format("%-" + len + "s", s);
    }

    private static String makeLine(final int count) {
        return "-".repeat(Math.max(0, count));
    }
}
