/*
 * Copyright 2010-2020 Redgate Software Ltd
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
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.license.VersionPrinter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InfoOutputFactory {
    public InfoOutput create(Configuration configuration, MigrationInfo[] migrationInfos, MigrationInfo current) {
        String databaseName = getDatabaseName(configuration);

        Set<MigrationVersion> undoableVersions = getUndoableVersions(migrationInfos);





        List<MigrationOutput> migrationOutputs = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            migrationOutputs.add(createMigrationOutput(undoableVersions, migrationInfo));
        }

        MigrationVersion currentSchemaVersion = current == null ? MigrationVersion.EMPTY : current.getVersion();
        MigrationVersion schemaVersionToOutput = currentSchemaVersion == null ? MigrationVersion.EMPTY : currentSchemaVersion;
        String schemaVersion =  schemaVersionToOutput.getVersion();
        String flywayVersion = VersionPrinter.getVersion();

        return new InfoOutput(
                flywayVersion,
                databaseName,
                schemaVersion,
                join(", ", configuration.getSchemas()),
                migrationOutputs);
    }

    private String getDatabaseName(Configuration configuration) {
        try {
            Connection connection = configuration.getDataSource().getConnection();
            String catalog = connection.getCatalog();
            connection.close();
            return catalog;
        } catch (Exception e) {
            return "";
        }
    }

    private MigrationOutput createMigrationOutput(Set<MigrationVersion> undoableVersions, MigrationInfo migrationInfo) {
        return new MigrationOutput(getCategory(migrationInfo),
                migrationInfo.getVersion() != null ? migrationInfo.getVersion().getVersion() : "",
                migrationInfo.getDescription(),
                migrationInfo.getType() != null ? migrationInfo.getType().toString() : "",
                migrationInfo.getInstalledOn() != null ? migrationInfo.getInstalledOn().toString() : "",
                migrationInfo.getState().getDisplayName(),
                getUndoableStatus(migrationInfo, undoableVersions),
                migrationInfo.getPhysicalLocation() != null ? migrationInfo.getPhysicalLocation() : "",
                migrationInfo.getInstalledBy() != null ? migrationInfo.getInstalledBy() : "",
                migrationInfo.getExecutionTime() != null ? migrationInfo.getExecutionTime() : 0);
    }

    private String join(String joiner, String[] strings) {
        if (strings.length == 1) {
            return strings[0];
        }

        StringBuilder output = new StringBuilder();

        for(String s : strings) {
            output.append(s).append(joiner);
        }

        return output.toString();
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

        for (MigrationInfo migrationInfo : migrationInfos) {
            if (!migrationInfo.getState().equals(MigrationState.AVAILABLE)) {
                result.add(migrationInfo);
            }
        }

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