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
import org.flywaydb.core.internal.util.AsciiTable;
import org.flywaydb.core.internal.util.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dumps migrations in an ascii-art table in the logs and the console.
 */
public class MigrationInfoDumper {
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

        List<String> columns = Arrays.asList("Category", "Version", "Description", "Type", "Installed On", "State"
                // [pro]
                , "Undoable"
                // [/pro]
        );

        List<List<String>> rows = new ArrayList<List<String>>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            List<String> row = Arrays.asList(
                    getCategory(migrationInfo),
                    getVersionStr(migrationInfo),
                    migrationInfo.getDescription(),
                    migrationInfo.getType().name(),
                    DateUtils.formatDateAsIsoString(migrationInfo.getInstalledOn()),
                    migrationInfo.getState().getDisplayName()
                    // [pro]
                    , getUndoableStatus(migrationInfo, undoableVersions)
                    // [/pro]
            );
            rows.add(row);
        }

        return new AsciiTable(columns, rows, "", "No migrations found").render();
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
