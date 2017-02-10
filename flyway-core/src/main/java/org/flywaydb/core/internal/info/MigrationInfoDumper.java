/*
 * Copyright 2010-2017 Boxfuse GmbH
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
    private static final String VERSION_TITLE = "Version";
    private static final String DESCRIPTION_TITLE = "Description";

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
        int versionWidth = VERSION_TITLE.length();
        int descriptionWidth = DESCRIPTION_TITLE.length();

        for (MigrationInfo migrationInfo : migrationInfos) {
            versionWidth = Math.max(versionWidth, migrationInfo.getVersion() == null ? 0 : migrationInfo.getVersion().toString().length());
            descriptionWidth = Math.max(descriptionWidth, migrationInfo.getDescription().length());
        }

        String ruler = "+-" + StringUtils.trimOrPad("", versionWidth, '-')
                + "-+-" + StringUtils.trimOrPad("", descriptionWidth, '-') + "-+---------------------+---------+\n";

        StringBuilder table = new StringBuilder();
        table.append(ruler);
        table.append("| ").append(StringUtils.trimOrPad(VERSION_TITLE, versionWidth, ' '))
                .append(" | ").append(StringUtils.trimOrPad(DESCRIPTION_TITLE, descriptionWidth))
                .append(" | Installed on        | State   |\n");
        table.append(ruler);

        if (migrationInfos.length == 0) {
            table.append(StringUtils.trimOrPad("| No migrations found", ruler.length() - 2, ' ')).append("|\n");
        } else {
            for (MigrationInfo migrationInfo : migrationInfos) {
                String versionStr = migrationInfo.getVersion() == null ? "" : migrationInfo.getVersion().toString();
                table.append("| ").append(StringUtils.trimOrPad(versionStr, versionWidth));
                table.append(" | ").append(StringUtils.trimOrPad(migrationInfo.getDescription(), descriptionWidth));
                table.append(" | ").append(StringUtils.trimOrPad(DateUtils.formatDateAsIsoString(migrationInfo.getInstalledOn()), 19));
                table.append(" | ").append(StringUtils.trimOrPad(migrationInfo.getState().getDisplayName(), 7));
                table.append(" |\n");
            }
        }

        table.append(ruler);
        return table.toString();
    }
}
