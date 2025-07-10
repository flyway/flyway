/*-
 * ========================LICENSE_START=================================
 * flyway-verb-migrate
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
package org.flywaydb.verb.migrate.migrators;

import static org.flywaydb.core.internal.util.FileUtils.getParentDir;
import static org.flywaydb.nc.utils.VerbUtils.toMigrationText;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import lombok.CustomLog;
import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.api.LoadableMigrationInfo;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.nc.NativeConnectorsDatabase;
import org.flywaydb.core.internal.exception.FlywayMigrateException;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.nc.callbacks.CallbackManager;
import org.flywaydb.nc.utils.ErrorUtils;
import org.flywaydb.nc.executors.NonJdbcExecutorExecutionUnit;
import org.flywaydb.nc.executors.ExecutorFactory;
import org.flywaydb.verb.migrate.MigrationExecutionGroup;
import org.flywaydb.core.internal.nc.Executor;
import org.flywaydb.core.internal.nc.Reader;
import org.flywaydb.nc.readers.ReaderFactory;

@CustomLog
public class ExecutableMigrator extends Migrator {
    @Override
    public List<MigrationExecutionGroup> createGroups(final MigrationInfo[] allPendingMigrations,
        final Configuration configuration,
        final NativeConnectorsDatabase experimentalDatabase,
        final MigrateResult migrateResult,
        final ParsingContext parsingContext) {

        return Arrays.stream(allPendingMigrations).map(x -> new MigrationExecutionGroup(List.of(x), true)).toList();
    }

    @Override
    public int doExecutionGroup(final Configuration configuration,
        final MigrationExecutionGroup executionGroup,
        final NativeConnectorsDatabase experimentalDatabase,
        final MigrateResult migrateResult,
        final ParsingContext parsingContext,
        final int installedRank,
        final CallbackManager callbackManager,
        final ProgressLogger progress) {

        final boolean executeInTransaction = configuration.isExecuteInTransaction()
            && executionGroup.shouldExecuteInTransaction();
        if (executeInTransaction) {
            experimentalDatabase.startTransaction();
        }

        doIndividualMigration(executionGroup.migrations().get(0),
            experimentalDatabase,
            configuration,
            migrateResult,
            installedRank,
            parsingContext,
            callbackManager,
            progress);

        return installedRank + 1;
    }

    private void doIndividualMigration(final MigrationInfo migrationInfo,
        final NativeConnectorsDatabase experimentalDatabase,
        final Configuration configuration,
        final MigrateResult migrateResult,
        final int installedRank,
        final ParsingContext parsingContext,
        final CallbackManager callbackManager,
        final ProgressLogger progress) {
        final StopWatch watch = new StopWatch();
        watch.start();

        final boolean outOfOrder = migrationInfo.getState() == MigrationState.OUT_OF_ORDER
            && configuration.isOutOfOrder();
        final String migrationText = toMigrationText(migrationInfo, false, experimentalDatabase, outOfOrder);
        final Executor<NonJdbcExecutorExecutionUnit> executor = ExecutorFactory.getExecutor(experimentalDatabase,
            configuration);
        final Reader<String> reader = ReaderFactory.getReader(experimentalDatabase, configuration);

        try {
            if (configuration.isSkipExecutingMigrations()) {
                LOG.debug("Skipping execution of migration of " + migrationText);
                progress.log("Skipping migration of " + migrationInfo.getScript());
            } else {
                LOG.debug("Starting migration of " + migrationText + " ...");
                progress.log("Starting migration of " + migrationInfo.getScript() + " ...");
                if (!migrationInfo.getType().isUndo()) {
                    callbackManager.handleEvent(Event.BEFORE_EACH_MIGRATE,
                        experimentalDatabase,
                        configuration,
                        parsingContext);
                }
                if (!migrationInfo.getType().isUndo()) {
                    LOG.info("Migrating " + migrationText);
                    progress.log("Migrating " + migrationInfo.getScript());
                } else {
                    LOG.info("Undoing migration of " + migrationText);
                }

                if (migrationInfo instanceof final LoadableMigrationInfo loadableMigrationInfo) {
                    final NonJdbcExecutorExecutionUnit nonJdbcExecutorExecutionUnit = new NonJdbcExecutorExecutionUnit(
                        reader.read(configuration,
                            experimentalDatabase,
                            parsingContext,
                            loadableMigrationInfo.getLoadableResource(),
                            null).findFirst().get(),
                        getParentDir(loadableMigrationInfo.getLoadableResource().getAbsolutePath()));
                    executor.execute(experimentalDatabase, nonJdbcExecutorExecutionUnit, configuration);
                    executor.finishExecution(experimentalDatabase, configuration);
                }

                if (!migrationInfo.getType().isUndo()) {
                    callbackManager.handleEvent(Event.AFTER_EACH_MIGRATE,
                        experimentalDatabase,
                        configuration,
                        parsingContext);
                }
            }
        } catch (final Exception e) {
            watch.stop();
            final int totalTimeMillis = (int) watch.getTotalTimeMillis();
            handleMigrationError(e,
                experimentalDatabase,
                migrationInfo,
                migrateResult,
                configuration.getTable(),
                configuration.isOutOfOrder(),
                installedRank,
                experimentalDatabase.getInstalledBy(configuration),
                totalTimeMillis,
                configuration.getCurrentEnvironmentName());
        }

        watch.stop();

        progress.log("Successfully completed migration of " + migrationInfo.getScript());
        migrateResult.migrationsExecuted += 1;
        final int totalTimeMillis = (int) watch.getTotalTimeMillis();
        migrateResult.putSuccessfulMigration(migrationInfo, totalTimeMillis);
        if (migrationInfo.isVersioned()) {
            migrateResult.targetSchemaVersion = migrationInfo.getVersion().getVersion();
        }
        migrateResult.migrations.add(CommandResultFactory.createMigrateOutput(migrationInfo, totalTimeMillis, null));
        updateSchemaHistoryTable(configuration.getTable(),
            migrationInfo,
            totalTimeMillis,
            installedRank,
            experimentalDatabase,
            experimentalDatabase.getInstalledBy(configuration),
            true);
    }

    private void handleMigrationError(final Exception e,
        final NativeConnectorsDatabase experimentalDatabase,
        final MigrationInfo migrationInfo,
        final MigrateResult migrateResult,
        final String schemaHistoryTableName,
        final boolean outOfOrder,
        final int installedRank,
        final String installedBy,
        final int totalTimeMillis,
        final String environment) {
        final String migrationText = toMigrationText(migrationInfo, false, experimentalDatabase, outOfOrder);
        final String failedMsg;
        if (!migrationInfo.getType().isUndo()) {
            failedMsg = "Migration of " + migrationText + " failed!";
        } else {
            failedMsg = "Undo of migration of " + migrationText + " failed!";
        }

        migrateResult.putFailedMigration(migrationInfo, totalTimeMillis);
        migrateResult.setSuccess(false);

        LOG.error(failedMsg + " Please restore backups and roll back database and code!");
        updateSchemaHistoryTable(schemaHistoryTableName,
            migrationInfo,
            totalTimeMillis,
            installedRank,
            experimentalDatabase,
            installedBy,
            false);

        final String message = experimentalDatabase.redactUrl(e.getMessage());
        throw new FlywayMigrateException(migrationInfo,
            calculateErrorMessage(message, migrationInfo, environment),
            true,
            migrateResult);
    }

    private String calculateErrorMessage(final String message,
        final MigrationInfo migrationInfo,
        final String environment) {

        final String title = ErrorUtils.getScriptExecutionErrorMessageTitle(Paths.get(migrationInfo.getScript())
            .getFileName(), environment);

        LoadableResource loadableResource = null;
        if (migrationInfo instanceof final LoadableMigrationInfo loadableMigrationInfo) {
            loadableResource = loadableMigrationInfo.getLoadableResource();
        }

        return ErrorUtils.calculateErrorMessage(title,
            loadableResource,
            migrationInfo.getPhysicalLocation(),
            null,
            null,
            "Message    : " + message + "\n");
    }
}
