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
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.DateUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dumps migrations in an ascii-art table in the logs and the console.
 */
public class MigrationInfoDumper {
    private static final String VERSION_TITLE = "Version";
    private static final String DESCRIPTION_TITLE = "Description";
    private static final String TYPE_TITLE = "Type";
    private static final String STATE_TITLE = "State";

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
        // [pro]
        Set<MigrationVersion> undoableVersions = getUndoableVersions(migrationInfos);
        migrationInfos = removeAvailableUndos(migrationInfos);
        // [/pro]

        int versionWidth = VERSION_TITLE.length();
        int descriptionWidth = DESCRIPTION_TITLE.length();
        int typeWidth = TYPE_TITLE.length();
        int stateWidth = STATE_TITLE.length();

        for (MigrationInfo migrationInfo : migrationInfos) {
            versionWidth = Math.max(versionWidth,
                    migrationInfo.getVersion() == null
                            ? 0
                            : migrationInfo.getVersion().toString().length());
            descriptionWidth = Math.max(descriptionWidth, migrationInfo.getDescription().length());
            typeWidth = Math.max(typeWidth, migrationInfo.getType().name().length());
            stateWidth = Math.max(stateWidth, migrationInfo.getState().getDisplayName().length());
        }

        String ruler = "+------------+-"
                + StringUtils.trimOrPad("", versionWidth, '-')
                + "-+-" + StringUtils.trimOrPad("", descriptionWidth, '-')
                + "-+-" + StringUtils.trimOrPad("", typeWidth, '-')
                + "-+--------------------"
                + "-+-" + StringUtils.trimOrPad("", stateWidth, '-')
                // [pro]
                + "-+---------"
                // [/pro]
                + "-+\n";

        StringBuilder table = new StringBuilder();
        table.append(ruler);
        table.append("| Category   | ")
                .append(StringUtils.trimOrPad(VERSION_TITLE, versionWidth, ' '))
                .append(" | ").append(StringUtils.trimOrPad(DESCRIPTION_TITLE, descriptionWidth))
                .append(" | ").append(StringUtils.trimOrPad(TYPE_TITLE, typeWidth))
                .append(" | Installed on       ")
                .append(" | ").append(StringUtils.trimOrPad(STATE_TITLE, stateWidth))
                // [pro]
                .append(" | Undoable")
                // [/pro]
                .append(" |\n");
        table.append(ruler);

        if (migrationInfos.length == 0) {
            table.append(StringUtils.trimOrPad("| No migrations found", ruler.length() - 2, ' '))
                    .append("|\n");
        } else {
            for (MigrationInfo migrationInfo : migrationInfos) {
                table.append("| ").append(StringUtils.trimOrPad(getCategory(migrationInfo), 10));
                table.append(" | ").append(StringUtils.trimOrPad(getVersionStr(migrationInfo), versionWidth));
                table.append(" | ").append(StringUtils.trimOrPad(migrationInfo.getDescription(), descriptionWidth));
                table.append(" | ").append(StringUtils.trimOrPad(migrationInfo.getType().name(), typeWidth));
                table.append(" | ").append(StringUtils.trimOrPad(DateUtils.formatDateAsIsoString(migrationInfo.getInstalledOn()), 19));
                table.append(" | ").append(StringUtils.trimOrPad(migrationInfo.getState().getDisplayName(), stateWidth));
                // [pro]
                table.append(" | ").append(StringUtils.trimOrPad(getUndoableStatus(migrationInfo, undoableVersions), 8));
                // [/pro]
                table.append(" |\n");
            }
        }

        table.append(ruler);
        return table.toString();
    }

    private static String getCategory(MigrationInfo migrationInfo) {
        if (migrationInfo.getVersion() == null) {
            return "Repeatable";
        }
        // [pro]
        if (migrationInfo.getType().isUndo()) {
            return "Undo";
        }
        // [/pro]
        return "Versioned";
    }

    private static String getVersionStr(MigrationInfo migrationInfo) {
        return migrationInfo.getVersion() == null ? "" : migrationInfo.getVersion().toString();
    }

    // [pro]
    private static String getUndoableStatus(MigrationInfo migrationInfo, Set<MigrationVersion> undoableVersions) {
        if (migrationInfo.getVersion() != null
                && !migrationInfo.getType().isUndo()
                && !migrationInfo.getState().equals(MigrationState.UNDONE)) {
            if (!migrationInfo.getState().isFailed()
                    && undoableVersions.contains(migrationInfo.getVersion())) {
                return "Yes";
            }
            return "No";
        }
        return "";
    }

    private static Set<MigrationVersion> getUndoableVersions(MigrationInfo[] migrationInfos) {
        Set<MigrationVersion> result = new HashSet<MigrationVersion>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getType().isUndo()) {
                result.add(migrationInfo.getVersion());
            }
        }
        return result;
    }

    private static MigrationInfo[] removeAvailableUndos(MigrationInfo[] migrationInfos) {
        List<MigrationInfo> result = new ArrayList<MigrationInfo>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (!migrationInfo.getState().equals(MigrationState.AVAILABLE)) {
                result.add(migrationInfo);
            }
        }
        return result.toArray(new MigrationInfo[result.size()]);
    }
    // [/pro]
}
