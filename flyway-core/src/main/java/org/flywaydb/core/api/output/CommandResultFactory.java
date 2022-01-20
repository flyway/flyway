/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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

import org.flywaydb.core.api.*;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

public class CommandResultFactory {
    public static InfoResult createInfoResult(Configuration configuration, Database database, MigrationInfo[] migrationInfos, MigrationInfo current, boolean allSchemasEmpty) {
        String flywayVersion = VersionPrinter.getVersion();
        String databaseName = getDatabaseName(configuration, database);
        Set<MigrationInfo> undoableMigrations = getUndoMigrations(migrationInfos);





        List<InfoOutput> infoOutputs = new ArrayList<>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            infoOutputs.add(createInfoOutput(undoableMigrations, migrationInfo));
        }

        MigrationVersion currentSchemaVersion = current == null ? MigrationVersion.EMPTY : current.getVersion();
        MigrationVersion schemaVersionToOutput = currentSchemaVersion == null ? MigrationVersion.EMPTY : currentSchemaVersion;
        String schemaVersion = schemaVersionToOutput.getVersion();

        return new InfoResult(
                flywayVersion,
                databaseName,
                schemaVersion,
                String.join(", ", configuration.getSchemas()),
                infoOutputs,
                allSchemasEmpty);
    }

    public static MigrateResult createMigrateResult(String databaseName, Configuration configuration) {
        String flywayVersion = VersionPrinter.getVersion();
        return new MigrateResult(flywayVersion, databaseName, String.join(", ", configuration.getSchemas()));
    }

    public static CleanResult createCleanResult(String databaseName) {
        String flywayVersion = VersionPrinter.getVersion();
        return new CleanResult(flywayVersion, databaseName);
    }

    public static UndoResult createUndoResult(String databaseName, Configuration configuration) {
        String flywayVersion = VersionPrinter.getVersion();
        return new UndoResult(flywayVersion, databaseName, String.join(", ", configuration.getSchemas()));
    }

    public static BaselineResult createBaselineResult(String databaseName) {
        String flywayVersion = VersionPrinter.getVersion();
        return new BaselineResult(flywayVersion, databaseName);
    }

    public static ValidateResult createValidateResult(String databaseName, ErrorDetails validationError, int validationCount, List<ValidateOutput> invalidMigrations, List<String> warnings) {
        String flywayVersion = VersionPrinter.getVersion();
        boolean validationSuccessful = validationError == null;
        List<ValidateOutput> invalidMigrationsList = invalidMigrations == null ? new ArrayList<>() : invalidMigrations;

        return new ValidateResult(flywayVersion, databaseName, validationError, validationSuccessful, validationCount, invalidMigrationsList, warnings);
    }

    public static RepairResult createRepairResult(String databaseName) {
        String flywayVersion = VersionPrinter.getVersion();
        return new RepairResult(flywayVersion, databaseName);
    }

    public static InfoOutput createInfoOutput(Set<MigrationInfo> undoableMigrations, MigrationInfo migrationInfo) {
        return new InfoOutput(getCategory(migrationInfo),
                              migrationInfo.getVersion() != null ? migrationInfo.getVersion().getVersion() : "",
                              migrationInfo.getDescription(),
                              migrationInfo.getType() != null ? migrationInfo.getType().toString() : "",
                              migrationInfo.getInstalledOn() != null ? migrationInfo.getInstalledOn().toInstant().toString() : "",
                              migrationInfo.getState().getDisplayName(),
                              getUndoableStatus(migrationInfo, undoableMigrations),
                              migrationInfo.getPhysicalLocation() != null ? migrationInfo.getPhysicalLocation() : "",
                              getUndoablePath(migrationInfo, undoableMigrations),
                              migrationInfo.getInstalledBy() != null ? migrationInfo.getInstalledBy() : "",
                              migrationInfo.getExecutionTime() != null ? migrationInfo.getExecutionTime() : 0);
    }

    public static MigrateOutput createMigrateOutput(MigrationInfo migrationInfo, int executionTime) {
        return new MigrateOutput(getCategory(migrationInfo),
                                 migrationInfo.getVersion() != null ? migrationInfo.getVersion().getVersion() : "",
                                 migrationInfo.getDescription(),
                                 migrationInfo.getType() != null ? migrationInfo.getType().toString() : "",
                                 migrationInfo.getPhysicalLocation() != null ? migrationInfo.getPhysicalLocation() : "",
                                 executionTime);
    }

    public static UndoOutput createUndoOutput(ResolvedMigration migrationInfo, int executionTime) {
        return new UndoOutput(
                migrationInfo.getVersion().getVersion(),
                migrationInfo.getDescription(),
                migrationInfo.getPhysicalLocation() != null ? migrationInfo.getPhysicalLocation() : "",
                executionTime);
    }

    public static ValidateOutput createValidateOutput(MigrationInfo migrationInfo, ErrorDetails validateError) {
        return new ValidateOutput(
                migrationInfo.getVersion() != null ? migrationInfo.getVersion().getVersion() : "",
                migrationInfo.getDescription(),
                migrationInfo.getPhysicalLocation() != null ? migrationInfo.getPhysicalLocation() : "",
                validateError);
    }

    public static RepairOutput createRepairOutput(MigrationInfo migrationInfo) {
        return new RepairOutput(
                migrationInfo.getVersion() != null ? migrationInfo.getVersion().getVersion() : "",
                migrationInfo.getDescription(),
                migrationInfo.getPhysicalLocation() != null ? migrationInfo.getPhysicalLocation() : "");
    }

    public static RepairOutput createRepairOutput(AppliedMigration am) {
        return new RepairOutput(am.getVersion() != null ? am.getVersion().getVersion() : "", am.getDescription(), "");
    }

    private static String getUndoableStatus(MigrationInfo migrationInfo, Set<MigrationInfo> undoableMigrations) {








        return "";
    }

    private static String getUndoablePath(MigrationInfo migrationInfo, Set<MigrationInfo> undoableMigrations) {











        return "";
    }

    private static Set<MigrationInfo> getUndoMigrations(MigrationInfo[] migrationInfos) {
        Set<MigrationInfo> result = Collections.emptySet();





        return result;
    }

    private static MigrationInfo[] removeAvailableUndoMigrations(MigrationInfo[] migrationInfos) {
        return Arrays.stream(migrationInfos)
                .filter(m -> !m.getState().equals(MigrationState.AVAILABLE))
                .toArray(MigrationInfo[]::new);
    }

    private static String getDatabaseName(Configuration configuration, Database database) {
        try {
            return database.getCatalog();
        } catch (Exception e) {
            try (Connection connection = configuration.getDataSource().getConnection()) {
                String catalog = connection.getCatalog();
                return catalog != null ? catalog : "";
            } catch (Exception e1) {
                return "";
            }
        }
    }

    private static String getCategory(MigrationInfo migrationInfo) {
        if (migrationInfo.getType().isSynthetic()) {
            return "";
        }
        if (migrationInfo.getVersion() == null) {
            return "Repeatable";
        }





        return "Versioned";
    }
}