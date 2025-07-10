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

import static org.flywaydb.nc.utils.VerbUtils.toMigrationText;

import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import lombok.CustomLog;
import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.LoadableMigrationInfo;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.nc.ConnectionType;
import org.flywaydb.core.internal.nc.NativeConnectorsDatabase;
import org.flywaydb.core.internal.exception.FlywayMigrateException;
import org.flywaydb.core.internal.parser.Parser;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;
import org.flywaydb.core.internal.sqlscript.SqlStatement;
import org.flywaydb.core.internal.sqlscript.SqlStatementIterator;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.nc.callbacks.CallbackManager;
import org.flywaydb.nc.utils.ErrorUtils;
import org.flywaydb.core.internal.nc.Executor;
import org.flywaydb.nc.executors.ExecutorFactory;
import org.flywaydb.verb.migrate.MigrationExecutionGroup;
import org.flywaydb.core.internal.nc.Reader;
import org.flywaydb.nc.readers.ReaderFactory;

@CustomLog
public class JdbcMigrator extends Migrator {

    @Override
    public List<MigrationExecutionGroup> createGroups(final MigrationInfo[] allPendingMigrations,
        final Configuration configuration,
        final NativeConnectorsDatabase experimentalDatabase,
        final MigrateResult migrateResult,
        final ParsingContext parsingContext) {
        if (experimentalDatabase.getDatabaseMetaData().connectionType() != ConnectionType.JDBC) {
            return List.of(new MigrationExecutionGroup(List.of(allPendingMigrations), true));
        }
        final List<MigrationInfo> currentGroup = Arrays.asList(allPendingMigrations);
        final List<Pair<MigrationInfo, Boolean>> migrationTransactionPairs = currentGroup.stream()
            .map(x -> Pair.of(x, shouldExecuteInTransaction(x, configuration, experimentalDatabase, parsingContext)))
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

        final List<Pair<MigrationInfo, Boolean>> migrationContainsNonTransactionalStatements = currentGroup.stream()
            .map(x -> Pair.of(x,
                containsNonTransactionalStatements(configuration, experimentalDatabase, x, parsingContext)))
            .toList();
        for (final Pair<MigrationInfo, Boolean> pair : migrationContainsNonTransactionalStatements) {
            final MigrationInfo migrationInfo = pair.getLeft();
            if (migrationInfo instanceof final LoadableMigrationInfo loadableMigrationInfo) {
                final boolean containsNonTransactionalStatements = containsNonTransactionalStatements(configuration,
                    experimentalDatabase,
                    loadableMigrationInfo,
                    parsingContext);
                if (containsNonTransactionalStatements) {
                    if (configuration.isMixed()) {
                        return Arrays.stream(allPendingMigrations)
                            .map(x -> new MigrationExecutionGroup(List.of(x), pair.getRight()))
                            .toList();
                    }
                    throw new FlywayMigrateException(migrationInfo,
                        "Detected both transactional and non-transactional migrations within the same migration group"
                            + " (even though mixed is false). First offending migration: "
                            + experimentalDatabase.doQuote((migrationInfo.isVersioned() ? migrationInfo.getVersion()
                            .getVersion() : "") + (StringUtils.hasLength(migrationInfo.getDescription()) ? " "
                            + migrationInfo.getDescription() : ""))
                            + (" [non-transactional]"),
                        false,
                        migrateResult);
                }
            }
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
        for (final MigrationInfo migrationInfo : executionGroup.migrations()) {
            if (!configuration.isMixed() && configuration.isExecuteInTransaction()) {
                validateMixedStatements(configuration, experimentalDatabase, migrationInfo, parsingContext);
            }
            doIndividualMigration(migrationInfo,
                experimentalDatabase,
                configuration,
                migrateResult,
                parsingContext,
                callbackManager,
                rank,
                executeInTransaction,
                progress);
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
        final ParsingContext parsingContext,
        final CallbackManager callbackManager,
        final int installedRank,
        final boolean executeInTransaction,
        final ProgressLogger progress) {
        final StopWatch watch = new StopWatch();
        watch.start();

        final AtomicReference<SqlStatement> sqlStatement = new AtomicReference<>();
        final boolean outOfOrder = migrationInfo.getState() == MigrationState.OUT_OF_ORDER
            && configuration.isOutOfOrder();
        final String migrationText = toMigrationText(migrationInfo,
            executeInTransaction,
            experimentalDatabase,
            outOfOrder);
        final Executor<SqlStatement> executor = ExecutorFactory.getExecutor(experimentalDatabase, configuration);
        final Reader<SqlStatement> reader = ReaderFactory.getReader(experimentalDatabase, configuration);

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
                    final Stream<SqlStatement> executionUnits = reader.read(configuration,
                        experimentalDatabase,
                        parsingContext,
                        loadableMigrationInfo.getLoadableResource(),
                        loadableMigrationInfo.getSqlScriptMetadata());

                    executionUnits.forEach(x -> {
                        sqlStatement.set(x);
                        executor.execute(experimentalDatabase, x, configuration);
                    });
                    executor.finishExecution(experimentalDatabase, configuration);
                }

                if (!migrationInfo.getType().isUndo()) {
                    callbackManager.handleEvent(Event.AFTER_EACH_MIGRATE,
                        experimentalDatabase,
                        configuration,
                        parsingContext);
                }
            }
        } catch (final FlywayException e) {
            watch.stop();
            final int totalTimeMillis = (int) watch.getTotalTimeMillis();
            handleMigrationError(e,
                experimentalDatabase,
                migrationInfo,
                executor,
                sqlStatement.get(),
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

    private boolean containsNonTransactionalStatements(final Configuration configuration,
        final NativeConnectorsDatabase experimentalDatabase,
        final MigrationInfo migrationInfo,
        final ParsingContext parsingContext) {
        if (migrationInfo instanceof final LoadableMigrationInfo loadableMigrationInfo) {
            try (final SqlStatementIterator sqlStatementIterator = getSqlStatementIterator(experimentalDatabase,
                configuration,
                loadableMigrationInfo,
                parsingContext)) {
                while (sqlStatementIterator.hasNext()) {
                    final SqlStatement sqlStatement = sqlStatementIterator.next();
                    if (!sqlStatement.canExecuteInTransaction()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean shouldExecuteInTransaction(final MigrationInfo migrationInfo,
        final Configuration configuration,
        final NativeConnectorsDatabase experimentalDatabase,
        final ParsingContext parsingContext) {
        if (migrationInfo instanceof final LoadableMigrationInfo loadableMigrationInfo) {
            if (loadableMigrationInfo.getSqlScriptMetadata() != null
                && loadableMigrationInfo.getSqlScriptMetadata().executeInTransaction() != null) {
                return loadableMigrationInfo.getSqlScriptMetadata().executeInTransaction();
            }
        }
        return configuration.isExecuteInTransaction() && !containsNonTransactionalStatements(configuration,
            experimentalDatabase,
            migrationInfo,
            parsingContext);
    }

    private SqlStatementIterator getSqlStatementIterator(final NativeConnectorsDatabase experimentalDatabase,
        final Configuration configuration,
        final LoadableMigrationInfo loadableMigrationInfo,
        final ParsingContext parsingContext) {
        final Parser parser = (Parser) experimentalDatabase.getParser().apply(configuration, parsingContext);
        final SqlScriptMetadata metadata = loadableMigrationInfo.getSqlScriptMetadata();
        return parser.parse(loadableMigrationInfo.getLoadableResource(), metadata);
    }

    private void handleMigrationError(final FlywayException e,
        final NativeConnectorsDatabase experimentalDatabase,
        final MigrationInfo migrationInfo,
        final Executor<SqlStatement> executor,
        final SqlStatement sqlStatement,
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

        migrateResult.putFailedMigration(migrationInfo, totalTimeMillis);
        migrateResult.setSuccess(false);
        if (executeInTransaction) {
            experimentalDatabase.rollbackTransaction();
        }
        if (experimentalDatabase.supportsTransactions() && executeInTransaction) {
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
        if (sqlStatement == null) {
            throw new FlywayMigrateException(migrationInfo, e.getMessage(), executeInTransaction, migrateResult);
        } else {
            final String message = calculateErrorMessage(e, migrationInfo, executor, sqlStatement, environment);
            throw new FlywayMigrateException(migrationInfo,
                outOfOrder,
                message,
                e,
                executeInTransaction,
                migrateResult,
                sqlStatement);
        }
    }

    private String calculateErrorMessage(final Exception e,
        final MigrationInfo migrationInfo,
        final Executor<SqlStatement> executor,
        final SqlStatement sqlStatement,
        final String environment) {

        final String title = ErrorUtils.getScriptExecutionErrorMessageTitle(Paths.get(migrationInfo.getScript()).getFileName(), environment);

        String message = null;
        if (e.getCause() instanceof final SQLException sqlException) {
            message = ExceptionUtils.toMessage(sqlException);
        }

        LoadableResource loadableResource = null;
        if (migrationInfo instanceof final LoadableMigrationInfo loadableMigrationInfo) {
            loadableResource = loadableMigrationInfo.getLoadableResource();
        }

        return ErrorUtils.calculateErrorMessage(title,
            loadableResource,
            migrationInfo.getPhysicalLocation(),
            executor,
            sqlStatement,
            message);
    }

    private void validateMixedStatements(final Configuration configuration,
        final NativeConnectorsDatabase experimentalDatabase,
        final MigrationInfo migrationInfo,
        final ParsingContext parsingContext) {
        if (migrationInfo instanceof final LoadableMigrationInfo loadableMigrationInfo) {
            try (final SqlStatementIterator sqlStatementIterator = getSqlStatementIterator(experimentalDatabase,
                configuration,
                loadableMigrationInfo,
                parsingContext)) {
                boolean haveFoundNonTransactionalStatements = false;
                boolean haveFoundTransactionalStatements = false;
                while (sqlStatementIterator.hasNext()) {
                    final SqlStatement sqlStatement = sqlStatementIterator.next();
                    if (sqlStatement.canExecuteInTransaction()) {
                        haveFoundTransactionalStatements = true;
                    } else {
                        haveFoundNonTransactionalStatements = true;
                    }
                    if (haveFoundTransactionalStatements && haveFoundNonTransactionalStatements) {
                        throw new FlywayException(
                            "Detected both transactional and non-transactional statements within the same migration"
                                + " (even though mixed is false). Offending statement found at line "
                                + sqlStatement.getLineNumber()
                                + ": "
                                + sqlStatement.getSql()
                                + (sqlStatement.canExecuteInTransaction() ? "" : " [non-transactional]"));
                    }
                }
            }
        }
    }
}
