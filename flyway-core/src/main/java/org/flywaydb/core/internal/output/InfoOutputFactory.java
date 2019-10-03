/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.output;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InfoOutputFactory {
    public InfoOutput create(Configuration configuration, MigrationInfo[] migrationInfos, MigrationInfo current) {
        String databaseName = "";
        try {
            databaseName = configuration.getDataSource().getConnection().getCatalog();
        } catch (Exception e){
            // No op
        }

        Set<MigrationVersion> undoableVersions = getUndoableVersions(migrationInfos);
        migrationInfos = removeAvailableUndos(migrationInfos);

        List<MigrationOutput> migrationOutputs = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            migrationOutputs.add(
                    new MigrationOutput(getCategory(migrationInfo),
                            migrationInfo.getVersion() != null ? migrationInfo.getVersion().getVersion() : "",
                            migrationInfo.getDescription(),
                            migrationInfo.getType() != null ? migrationInfo.getType().toString() : "",
                            migrationInfo.getInstalledOn() != null ? migrationInfo.getInstalledOn().toString() : "",
                            migrationInfo.getState().getDisplayName(),
                            getUndoableStatus(migrationInfo, undoableVersions),
                            migrationInfo.getPhysicalLocation()));
        }

        MigrationVersion currentSchemaVersion = current == null ? MigrationVersion.EMPTY : current.getVersion();
        MigrationVersion schemaVersionToOutput = currentSchemaVersion == null ? MigrationVersion.EMPTY : currentSchemaVersion;

        return new InfoOutput(
                schemaVersionToOutput.getVersion(),
                databaseName,
                currentSchemaVersion.getVersion(),
                join(", ", configuration.getSchemas()),
                0,
                migrationOutputs);
    }

    private String join(String joiner, String[] strings) {
        String output = "";

        if (strings.length == 1) {
            return strings[0];
        }

        for(String s : strings) {
            output += s + joiner;
        }

        return output;
    }


    private static String getUndoableStatus(MigrationInfo migrationInfo, Set<MigrationVersion> undoableVersions) {












        return "";
    }

    private static Set<MigrationVersion> getUndoableVersions(MigrationInfo[] migrationInfos) {
        Set<MigrationVersion> result = new HashSet<>();







        return result;
    }

    private static MigrationInfo[] removeAvailableUndos(MigrationInfo[] migrationInfos) {
        List<MigrationInfo> result = new ArrayList<>();









        return result.toArray(new MigrationInfo[0]);
    }

    private String getCategory(MigrationInfo migrationInfo) {
        if (migrationInfo.getType().isSynthetic()) {
            return "";
        }
        if (migrationInfo.getVersion() == null) {
            return "Repeatable";
        }





        return "Versioned";
    }
}