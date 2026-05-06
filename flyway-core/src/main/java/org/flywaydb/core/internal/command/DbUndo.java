/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.core.internal.command;

import lombok.CustomLog;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.output.UndoOutput;
import org.flywaydb.core.api.output.UndoResult;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.ValidatePatternUtils;

import java.sql.SQLException;
import java.util.Arrays;

@CustomLog
public class DbUndo {

    private final Database database;
    private final SchemaHistory schemaHistory;
    private final Schema schema;
    private final CompositeMigrationResolver migrationResolver;
    private final Configuration configuration;
    private final CallbackExecutor<Event> callbackExecutor;
    private final Connection connectionUserObjects;

    public DbUndo(Database database, SchemaHistory schemaHistory, Schema schema,
                  CompositeMigrationResolver migrationResolver, Configuration configuration,
                  CallbackExecutor<Event> callbackExecutor) {
        this.database = database;
        this.schemaHistory = schemaHistory;
        this.schema = schema;
        this.migrationResolver = migrationResolver;
        this.configuration = configuration;
        this.callbackExecutor = callbackExecutor;
        this.connectionUserObjects = database.getMigrationConnection();
    }

    public UndoResult undo() throws FlywayException {
        callbackExecutor.onMigrateOrUndoEvent(Event.BEFORE_UNDO);

        UndoResult result = new UndoResult(
                VersionPrinter.getVersion(),
                database.getCatalog(),
                String.join(", ", configuration.getSchemas()));

        try {
            schemaHistory.lock(() -> {
                doUndo(result);
                return null;
            });
        } catch (FlywayException e) {
            callbackExecutor.onMigrateOrUndoEvent(Event.AFTER_UNDO_ERROR);
            result.success = false;
            throw e;
        }

        callbackExecutor.onMigrateOrUndoEvent(Event.AFTER_UNDO);
        return result;
    }

    private void doUndo(UndoResult result) {
        MigrationInfoServiceImpl infoService = new MigrationInfoServiceImpl(
                migrationResolver, schemaHistory, database, configuration,
                MigrationVersion.LATEST, false, ValidatePatternUtils.getIgnoreAllPattern(), null);
        infoService.refresh();

        MigrationInfo target = infoService.current();
        result.initialSchemaVersion = target == null || target.getVersion() == null
                ? null : target.getVersion().getVersion();

        if (target == null) {
            LOG.info("Schema " + schema + " has no versioned migrations applied. Nothing to undo.");
            return;
        }

        MigrationVersion targetVersion = target.getVersion();
        MigrationInfoImpl undoMigration = Arrays.stream(infoService.undo())
                .filter(m -> m.getState() == MigrationState.AVAILABLE
                        && m.getVersion() != null
                        && m.getVersion().compareTo(targetVersion) == 0)
                .findFirst()
                .orElse(null);

        if (undoMigration == null) {
            throw new FlywayException(
                    "No undo script found for version " + targetVersion + " (" + target.getDescription() + ")."
                    + " Create U" + targetVersion.getVersion() + "__"
                    + target.getDescription().replace(" ", "_") + ".sql");
        }

        LOG.info("Undoing migration of schema " + schema + " to version \""
                + targetVersion + " - " + target.getDescription() + "\"");

        Context executionContext = new Context() {
            @Override
            public Configuration getConfiguration() {
                return configuration;
            }

            @Override
            public java.sql.Connection getConnection() {
                return connectionUserObjects.getJdbcConnection();
            }
        };

        connectionUserObjects.restoreOriginalState();
        connectionUserObjects.changeCurrentSchemaTo(schema);

        boolean executeInTransaction = undoMigration.canExecuteInTransaction();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            if (executeInTransaction) {
                ExecutionTemplateFactory.createExecutionTemplate(
                        connectionUserObjects.getJdbcConnection(), database).execute(() -> {
                    undoMigration.getResolvedMigration().getExecutor().execute(executionContext);
                    return null;
                });
            } else {
                undoMigration.getResolvedMigration().getExecutor().execute(executionContext);
            }

            stopWatch.stop();
            int executionTime = (int) stopWatch.getTotalTimeMillis();

            schemaHistory.addAppliedMigration(
                    targetVersion,
                    target.getDescription(),
                    CoreMigrationType.UNDO_SCRIPT,
                    undoMigration.getScript(),
                    undoMigration.getChecksum(),
                    executionTime,
                    true);

            result.undoneScripts.add(new UndoOutput(
                    targetVersion.getVersion(),
                    target.getDescription(),
                    CoreMigrationType.UNDO_SCRIPT.toString(),
                    undoMigration.getPhysicalLocation() != null ? undoMigration.getPhysicalLocation() : "",
                    executionTime));
            result.migrationsUndone++;

            // Refresh to get the new current version after undo
            infoService.refresh();
            MigrationInfo newCurrent = infoService.current();
            result.targetSchemaVersion = newCurrent == null || newCurrent.getVersion() == null
                    ? null : newCurrent.getVersion().getVersion();

            LOG.info("Successfully undid migration of schema " + schema + " to version " + targetVersion
                    + " (execution time " + TimeFormat.format(executionTime) + ")");

        } catch (FlywayException e) {
            stopWatch.stop();
            int executionTime = (int) stopWatch.getTotalTimeMillis();
            schemaHistory.addAppliedMigration(
                    targetVersion,
                    target.getDescription(),
                    CoreMigrationType.UNDO_SCRIPT,
                    undoMigration.getScript(),
                    undoMigration.getChecksum(),
                    executionTime,
                    false);
            throw e;
        } catch (SQLException e) {
            stopWatch.stop();
            int executionTime = (int) stopWatch.getTotalTimeMillis();
            schemaHistory.addAppliedMigration(
                    targetVersion,
                    target.getDescription(),
                    CoreMigrationType.UNDO_SCRIPT,
                    undoMigration.getScript(),
                    undoMigration.getChecksum(),
                    executionTime,
                    false);
            throw new FlywayException("Unable to undo migration to version " + targetVersion, e);
        }
    }
}
