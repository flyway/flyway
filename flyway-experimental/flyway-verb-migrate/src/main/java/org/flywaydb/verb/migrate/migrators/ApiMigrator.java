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
import java.util.Iterator;
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
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.nc.callbacks.CallbackManager;
import org.flywaydb.nc.utils.ErrorUtils;
import org.flywaydb.nc.executors.NonJdbcExecutorExecutionUnit;
import org.flywaydb.nc.executors.ExecutorFactory;
import org.flywaydb.verb.migrate.MigrationExecutionGroup;
import org.flywaydb.core.internal.nc.Executor;
import org.flywaydb.core.internal.nc.Reader;
import org.flywaydb.nc.readers.ReaderFactory;

@CustomLog
public class ApiMigrator extends Migrator {

    @Override
    public List<MigrationExecutionGroup> createGroups(final MigrationInfo[] allPendingMigrations,
        final Configuration configuration,
        final NativeConnectorsDatabase experimentalDatabase,
        final MigrateResult migrateResult,
        final ParsingContext parsingContext) {
        final List<MigrationInfo> currentGroup = Arrays.asList(allPendingMigrations);
        final List<Pair<MigrationInfo, Boolean>> migrationTransactionPairs = currentGroup.stream()
            .map(x -> Pair.of(x, shouldExecuteInTransaction(x, configuration)))
            .toList();
        if (!configuration.isGroup()) {
            return migrationTransactionPairs.stream()
                .map(x -> new MigrationExecutionGroup(List.of(x.getLeft()), x.getRight()))
                .toList();
        }

        for (final Pair<MigrationInfo, Boolean> pair : migrationTransactionPairs) {
            final MigrationInfo migrationInfo = pair.getLeft();
            final boolean shouldExecuteMigrationInTransaction = pair.getRight();
            if (configuration.isExecuteInTransaction() != shouldExecuteMigrationInTransaction) {
                if (configuration.isMixed()) {
                    return migrationTransactionPairs.stream()
                        .map(x -> new MigrationExecutionGroup(List.of(x.getLeft()), x.getRight()))
                        .toList();
                } else {
                    throw new FlywayMigrateException(migrationInfo,
                        "Detected both transactional and non-transactional migrations within the same migration group"
                            + " (even though mixed is false). First offending migration: "
                            + experimentalDatabase.doQuote((migrationInfo.isVersioned() ? migrationInfo.getVersion()
                            .getVersion() : "") + (StringUtils.hasLength(migrationInfo.getDescription()) ? " "
                            + migrationInfo.getDescription() : ""))
                            + (shouldExecuteMigrationInTransaction ? "" : " [non-transactional]"),
                        shouldExecuteMigrationInTransaction,
                        migrateResult);
                }
            }
        }
        if (!configuration.isExecuteInTransaction()) {
            return Arrays.stream(allPendingMigrations)
                .map(x -> new MigrationExecutionGroup(List.of(x), false))
                .toList();
        }
        return List.of(new MigrationExecutionGroup(currentGroup, true));
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
        int rank = installedRank;
        final boolean executeInTransaction = executionGroup.shouldExecuteInTransaction();
        if (executeInTransaction) {
            experimentalDatabase.startTransaction();
        }

        Iterator<MigrationInfo> it = executionGroup.migrations().iterator();
        while (it.hasNext()) {
            MigrationInfo migrationInfo = it.next();
            boolean isLast = !it.hasNext();
            doIndividualMigration(migrationInfo,
                experimentalDatabase,
                configuration,
                migrateResult,
                rank,
                parsingContext,
                callbackManager,
                executeInTransaction,
                progress,
                isLast);
            rank++;
        }
        if (executeInTransaction) {
            experimentalDatabase.commitTransaction();
        }
        return rank;
    }

    private void doIndividualMigration(final MigrationInfo migrationInfo,
        final NativeConnectorsDatabase experimentalDatabase,
        final Configuration configuration,
        final MigrateResult migrateResult,
        final int installedRank,
        final ParsingContext parsingContext,
        final CallbackManager callbackManager,
        final boolean executeInTransaction,
        final ProgressLogger progress,
        final boolean isLast) {
        final StopWatch watch = new StopWatch();
        watch.start();

        final boolean outOfOrder = migrationInfo.getState() == MigrationState.OUT_OF_ORDER
            && configuration.isOutOfOrder();
        final String migrationText = toMigrationText(migrationInfo,
            executeInTransaction,
            experimentalDatabase,
            outOfOrder);
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
                        getParentDir(loadableMigrationInfo.getLoadableResource().getAbsolutePath()), executeInTransaction);
                    executor.execute(experimentalDatabase, nonJdbcExecutorExecutionUnit, configuration);
                    if(isLast) {
                        executor.finishExecution(experimentalDatabase, configuration);
                    }
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
                executeInTransaction,
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
        final boolean executeInTransaction,
        final int totalTimeMillis,
        final String environment) {
        final String migrationText = toMigrationText(migrationInfo,
            executeInTransaction,
            experimentalDatabase,
            outOfOrder);
        final String failedMsg;
        if (!migrationInfo.getType().isUndo()) {
            failedMsg = "Migration of " + migrationText + " failed!";
        } else {
            failedMsg = "Undo of migration of " + migrationText + " failed!";
        }

        if (executeInTransaction && experimentalDatabase.transactionAsBatch()) {
            LOG.warn("When running transactions in bulk, the reported failed migration may be incorrect. "
                + "Flyway always flags the last migration in the bulk as failed");
        }

        migrateResult.putFailedMigration(migrationInfo, totalTimeMillis);
        migrateResult.setSuccess(false);

        if (executeInTransaction) {
            experimentalDatabase.rollbackTransaction();
            LOG.error(failedMsg + " Changes successfully rolled back.");
        } else {
            LOG.error(failedMsg + " Please restore backups and roll back database and code!");
            updateSchemaHistoryTable(schemaHistoryTableName,
                migrationInfo,
                totalTimeMillis,
                installedRank,
                experimentalDatabase,
                installedBy,
                false);
        }

        throw new FlywayMigrateException(migrationInfo,
            calculateErrorMessage(e.getMessage(), migrationInfo, environment),
            executeInTransaction,
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

    private boolean shouldExecuteInTransaction(final MigrationInfo migrationInfo, final Configuration configuration) {
        if (migrationInfo instanceof final LoadableMigrationInfo loadableMigrationInfo) {
            if (loadableMigrationInfo.getSqlScriptMetadata() != null
                && loadableMigrationInfo.getSqlScriptMetadata().executeInTransaction() != null) {
                return loadableMigrationInfo.getSqlScriptMetadata().executeInTransaction();
            }
        }
        return configuration.isExecuteInTransaction();
    }
}
