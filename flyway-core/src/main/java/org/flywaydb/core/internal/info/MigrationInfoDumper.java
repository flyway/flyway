/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.info;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.AsciiTable;
import org.flywaydb.core.internal.util.DateUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Dumps migrations in an ascii-art table in the logs and the console.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MigrationInfoDumper {

    /**
     * Dumps the info about all migrations into an ascii table.
     *
     * @param migrationInfos The list of migrationInfos to dump.
     * @return The ascii table, as one big multi-line string.
     */
    public static String dumpToAsciiTable(MigrationInfo[] migrationInfos) {
        Set<MigrationVersion> undoableVersions = getUndoableVersions(migrationInfos);
        migrationInfos = removeUndos(migrationInfos);

        List<String> columns = Arrays.asList("Category", "Version", "Description", "Type", "Installed On", "State", "Undoable");

        List<List<String>> rows = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            List<String> row = Arrays.asList(
                    getCategory(migrationInfo),
                    getVersionStr(migrationInfo),
                    migrationInfo.getDescription(),
                    migrationInfo.getType().name(),
                    DateUtils.formatDateAsIsoString(migrationInfo.getInstalledOn()),
                    migrationInfo.getState().getDisplayName(),
                    getUndoableStatus(migrationInfo, undoableVersions));
            rows.add(row);
        }

        return new AsciiTable(columns, rows, true, "", "No migrations found").render();
    }

    /**
     * Dumps the info about all migrations into a String of Migration Ids.
     *
     * @param migrationInfos The list of migrationInfos to dump.
     * @return The String containing Migration Ids, separated by comma.
     */
    public static String dumpToMigrationIds(MigrationInfo[] migrationInfos) {
        migrationInfos = removeUndos(migrationInfos);

        return Arrays.stream(migrationInfos)
                     .map(m -> m.getVersion() == null ? m.getDescription() : m.getVersion().getVersion())
                     .collect(Collectors.joining(","));
    }

    static String getCategory(MigrationInfo migrationInfo) {
        if (migrationInfo.getType().isSynthetic()) {
            return "";
        }
        if (migrationInfo.getVersion() == null) {
            return "Repeatable";
        }
        if (migrationInfo.getType().isUndo()) {
            return "Undo";
        }
        if (migrationInfo.getType().isBaseline()) {
            return "Baseline";
        }
        return "Versioned";
    }

    private static String getVersionStr(MigrationInfo migrationInfo) {
        return migrationInfo.getVersion() == null ? "" : migrationInfo.getVersion().toString();
    }

    private static String getUndoableStatus(MigrationInfo migrationInfo, Set<MigrationVersion> undoableVersions) {
        if (migrationInfo.getVersion() != null
                && !migrationInfo.getType().equals(CoreMigrationType.DELETE)
                && !migrationInfo.getState().equals(MigrationState.DELETED)
                && !migrationInfo.getType().isUndo()
                && !migrationInfo.getState().equals(MigrationState.UNDONE)) {
            if (!migrationInfo.getState().isFailed() && undoableVersions.contains(migrationInfo.getVersion())) {
                return "Yes";
            }
            return "No";
        }
        return "";
    }

    private static Set<MigrationVersion> getUndoableVersions(MigrationInfo[] migrationInfos) {
        Set<MigrationVersion> result = new HashSet<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getType().isUndo()) {
                result.add(migrationInfo.getVersion());
            }
        }
        return result;
    }

    private static MigrationInfo[] removeUndos(MigrationInfo[] migrationInfos) {
        List<MigrationInfo> result = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (!migrationInfo.getType().isUndo()) {
                result.add(migrationInfo);
            }
        }
        return result.toArray(new MigrationInfo[0]);
    }
}
