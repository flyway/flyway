/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package org.flywaydb.core.internal.info;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.internal.util.DateUtils;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * Dumps migrations in an ascii-art table in the logs and the console.
 */
public class MigrationInfoDumper {
    /**
     * The minimum width (in chars) of the console we want to print the ascii table on.
     */
    private static final int MINIMUM_CONSOLE_WIDTH = 80;

    /**
     * Prevent instantiation.
     */
    private MigrationInfoDumper() {
        // Do nothing
    }

    /**
     * Dumps the info about all migrations into an ascii table.
     *
     * @param migrationInfos The list of migrationInfos to dump.
     * @return The ascii table, as one big multi-line string.
     */
    public static String dumpToAsciiTable(MigrationInfo[] migrationInfos) {
        return dumpToAsciiTable(migrationInfos, MINIMUM_CONSOLE_WIDTH);
    }

    /**
     * Dumps the info about all migrations into an ascii table.
     *
     * @param migrationInfos The list of migrationInfos to dump.
     * @param consoleWidth   The width of the console (80 or greater).
     * @return The ascii table, as one big multi-line string.
     */
    public static String dumpToAsciiTable(MigrationInfo[] migrationInfos, int consoleWidth) {
        int descriptionWidth = Math.max(consoleWidth, MINIMUM_CONSOLE_WIDTH) - 54;

        StringBuilder table = new StringBuilder();

        table.append("+----------------+-").append(StringUtils.trimOrPad("", descriptionWidth, '-')).append("-+---------------------+---------+\n");
        table.append("| Version        | ").append(StringUtils.trimOrPad("Description", descriptionWidth)).append(" | Installed on        | State   |\n");
        table.append("+----------------+-").append(StringUtils.trimOrPad("", descriptionWidth, '-')).append("-+---------------------+---------+\n");

        if (migrationInfos.length == 0) {
            table.append("| No migrations found                                                         |\n");
        } else {
            for (MigrationInfo migrationInfo : migrationInfos) {
                table.append("| ").append(StringUtils.trimOrPad(migrationInfo.getVersion().toString(), 14));
                table.append(" | ").append(StringUtils.trimOrPad(migrationInfo.getDescription(), descriptionWidth));
                table.append(" | ").append(StringUtils.trimOrPad(DateUtils.formatDateAsIsoString(migrationInfo.getInstalledOn()), 19));
                table.append(" | ").append(StringUtils.trimOrPad(migrationInfo.getState().getDisplayName(), 7));
                table.append(" |\n");
            }
        }

        table.append("+----------------+-").append(StringUtils.trimOrPad("", descriptionWidth, '-')).append("-+---------------------+---------+");
        return table.toString();
    }
}
