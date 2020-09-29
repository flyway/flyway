/*
 * Copyright © Red Gate Software Ltd 2010-2020
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
package org.flywaydb.core.api.output;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.license.VersionPrinter;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandResultFactory {
    public InfoResult createInfoResult(Configuration configuration, MigrationInfo[] migrationInfos, MigrationInfo current, boolean allSchemasEmpty) {
        String flywayVersion = VersionPrinter.getVersion();
        String databaseName = getDatabaseName(configuration);

        Set<MigrationVersion> undoableVersions = getUndoableVersions(migrationInfos);





        List<InfoOutput> infoOutputs = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            infoOutputs.add(createInfoOutput(undoableVersions, migrationInfo));
        }

        MigrationVersion currentSchemaVersion = current == null ? MigrationVersion.EMPTY : current.getVersion();
        MigrationVersion schemaVersionToOutput = currentSchemaVersion == null ? MigrationVersion.EMPTY : currentSchemaVersion;
        String schemaVersion =  schemaVersionToOutput.getVersion();

        return new InfoResult(
                flywayVersion,
                databaseName,
                schemaVersion,
                String.join(", ", configuration.getSchemas()),
                infoOutputs,
                allSchemasEmpty);
    }

    public MigrateResult createMigrateResult(String databaseName, Configuration configuration) {
        String flywayVersion = VersionPrinter.getVersion();

        return new MigrateResult(
                flywayVersion,
                databaseName,
                String.join(", ", configuration.getSchemas()));
    }

    public CleanResult createCleanResult(String databaseName) {
        String flywayVersion = VersionPrinter.getVersion();

        return new CleanResult(flywayVersion, databaseName);
    }

    public UndoResult createUndoResult(String databaseName, Configuration configuration) {
        String flywayVersion = VersionPrinter.getVersion();

        return new UndoResult(flywayVersion, databaseName, String.join(", ", configuration.getSchemas()));
    }

    public BaselineResult createBaselineResult(String databaseName) {
        String flywayVersion = VersionPrinter.getVersion();

        return new BaselineResult(flywayVersion, databaseName);
    }

    public ValidateResult createValidateResult(String databaseName, String validationError, int validationCount, List<String> warnings) {
        String flywayVersion = VersionPrinter.getVersion();
        boolean validationSuccessful = validationError == null;

        return new ValidateResult(flywayVersion, databaseName, validationError, validationSuccessful, validationCount, warnings);
    }

    public RepairResult createRepairResult(String databaseName) {
        String flywayVersion = VersionPrinter.getVersion();

        return new RepairResult(flywayVersion, databaseName);
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

    public InfoOutput createInfoOutput(Set<MigrationVersion> undoableVersions, MigrationInfo migrationInfo) {
        return new InfoOutput(getCategory(migrationInfo),
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

    public MigrateOutput createMigrateOutput(MigrationInfo migrationInfo) {
        return new MigrateOutput(getCategory(migrationInfo),
                migrationInfo.getVersion() != null ? migrationInfo.getVersion().getVersion() : "",
                migrationInfo.getDescription(),
                migrationInfo.getType() != null ? migrationInfo.getType().toString() : "",
                migrationInfo.getPhysicalLocation() != null ? migrationInfo.getPhysicalLocation() : "",
                migrationInfo.getExecutionTime() != null ? migrationInfo.getExecutionTime() : 0);
    }

    public UndoOutput createUndoOutput(ResolvedMigration undoMigrationInfo) {
        return new UndoOutput(
                undoMigrationInfo.getVersion().getVersion(),
                undoMigrationInfo.getDescription(),
                undoMigrationInfo.getPhysicalLocation() != null ? undoMigrationInfo.getPhysicalLocation() : "");
    }

    public RepairOutput createRepairOutput(MigrationInfo RepairMigrationInfo) {
        return new RepairOutput(
                RepairMigrationInfo.getVersion() != null ? RepairMigrationInfo.getVersion().getVersion() : "",
                RepairMigrationInfo.getDescription(),
                RepairMigrationInfo.getPhysicalLocation() != null ? RepairMigrationInfo.getPhysicalLocation() : "");
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
        if (migrationInfo.getType().isSynthetic()) return "";
        if (migrationInfo.getVersion() == null) return "Repeatable";



        return "Versioned";
    }
}