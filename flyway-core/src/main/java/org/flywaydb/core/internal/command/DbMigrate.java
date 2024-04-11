/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.core.internal.command;

import lombok.CustomLog;
import lombok.Getter;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.MigrateErrorResult;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.*;

import java.sql.SQLException;
import java.util.*;

@CustomLog
public class DbMigrate {

    /**
     * The schema containing the schema history table.
     */
    private final Schema schema;
    /**
     * The connection to use to perform the actual database migrations.
     */
    private final Connection connectionUserObjects;
    private MigrateResult migrateResult;
    /**
     * This is used to remember the type of migration between calls to migrateGroup().
     */
    private boolean isPreviousVersioned;
    private final List<ResolvedMigration> appliedResolvedMigrations = new ArrayList<>();
    private FlywayCommandSupport flywayCommandSupport = new FlywayCommandSupport(null, null, null, null, null);

    public DbMigrate(Database database,
                     SchemaHistory schemaHistory, Schema schema, CompositeMigrationResolver migrationResolver,
                     Configuration configuration, CallbackExecutor callbackExecutor) {
        this.flywayCommandSupport.setDatabase(database);
        this.connectionUserObjects = database.getMigrationConnection();
        this.flywayCommandSupport.setSchemaHistory(schemaHistory);
        this.schema = schema;
        this.flywayCommandSupport.setMigrationResolver(migrationResolver);
        this.flywayCommandSupport.setConfiguration(configuration);
        this.flywayCommandSupport.setCallbackExecutor(callbackExecutor);
    }

    /**
     * Starts the actual migration.
     */
    public MigrateResult migrate() throws FlywayException {
        flywayCommandSupport.getCallbackExecutor().onMigrateOrUndoEvent(Event.BEFORE_MIGRATE);

        migrateResult = CommandResultFactory.createMigrateResult(flywayCommandSupport.getDatabase().getCatalog(), flywayCommandSupport.getConfiguration());

        int count;
        try {

            count = flywayCommandSupport.getConfiguration().isGroup() ?
                    // When group is active, start the transaction boundary early to
                    // ensure that all changes to the schema history table are either committed or rolled back atomically.
                    flywayCommandSupport.getSchemaHistory().lock(this::migrateAll) :
                    // For all regular cases, proceed with the migration as usual.
                    migrateAll();

            migrateResult.targetSchemaVersion = getTargetVersion();
            migrateResult.migrationsExecuted = count;

            logSummary(count, migrateResult.getTotalMigrationTime(), migrateResult.targetSchemaVersion);

        } catch (FlywayException e) {
            flywayCommandSupport.getCallbackExecutor().onMigrateOrUndoEvent(Event.AFTER_MIGRATE_ERROR);
            throw e;
        }

        if (count > 0) {
            flywayCommandSupport.getCallbackExecutor().onMigrateOrUndoEvent(Event.AFTER_MIGRATE_APPLIED);
        }
        flywayCommandSupport.getCallbackExecutor().onMigrateOrUndoEvent(Event.AFTER_MIGRATE);

        return migrateResult;
    }

    private String getTargetVersion() {
        if (!migrateResult.migrations.isEmpty()) {
            for (int i = migrateResult.migrations.size() - 1; i >= 0; i--) {
                String targetVersion = migrateResult.migrations.get(i).version;
                if (!targetVersion.isEmpty()) {
                    return targetVersion;
                }
            }
        }
        return null;
    }

    private int migrateAll() {
        int total = 0;
        isPreviousVersioned = true;

        while (true) {
            final boolean firstRun = total == 0;
            int count = flywayCommandSupport.getConfiguration().isGroup()
                    // With group active a lock on the schema history table has already been acquired.
                    ? migrateGroup(firstRun)
                    // Otherwise acquire the lock now. The lock will be released at the end of each migration.
                    : flywayCommandSupport.getSchemaHistory().lock(() -> migrateGroup(firstRun));

            migrateResult.migrationsExecuted += count;

            total += count;
            if (count == 0) {
                // No further migrations available
                break;
            } else if (flywayCommandSupport.getConfiguration().getTarget() == MigrationVersion.NEXT) {
                // With target=next we only execute one migration
                break;
            }
        }

        if (isPreviousVersioned) {
            flywayCommandSupport.getCallbackExecutor().onMigrateOrUndoEvent(Event.AFTER_VERSIONED);
        }

        return total;
    }

    /**
     * Migrate a group of one (group = false) or more (group = true) migrations.
     *
     * @param firstRun Whether this is the first time this code runs in this migration run.
     * @return The number of newly applied migrations.
     */
    private Integer migrateGroup(boolean firstRun) {
        MigrationInfoServiceImpl infoService =
                new MigrationInfoServiceImpl(flywayCommandSupport.getMigrationResolver(), flywayCommandSupport.getSchemaHistory(), flywayCommandSupport.getDatabase(), flywayCommandSupport.getConfiguration(),
                                             flywayCommandSupport.getConfiguration().getTarget(), flywayCommandSupport.getConfiguration().isOutOfOrder(), ValidatePatternUtils.getIgnoreAllPattern(), flywayCommandSupport.getConfiguration().getCherryPick());
        infoService.refresh();

        MigrationInfo current = infoService.current();
        MigrationVersion currentSchemaVersion = current == null ? MigrationVersion.EMPTY : current.getVersion();
        if (firstRun) {
            LOG.info("Current version of schema " + schema + ": " + currentSchemaVersion);

            MigrationVersion schemaVersionToOutput = currentSchemaVersion == null ? MigrationVersion.EMPTY : currentSchemaVersion;
            migrateResult.initialSchemaVersion = schemaVersionToOutput.getVersion();

            if (flywayCommandSupport.getConfiguration().isOutOfOrder()) {
                String outOfOrderWarning = "outOfOrder mode is active. Migration of schema " + schema + " may not be reproducible.";
                LOG.warn(outOfOrderWarning);
                migrateResult.addWarning(outOfOrderWarning);
            }
        }

        MigrationInfo[] future = infoService.future();
        if (future.length > 0) {
            List<MigrationInfo> resolved = Arrays.asList(infoService.resolved());
            Collections.reverse(resolved);
            if (resolved.isEmpty()) {
                LOG.error("Schema " + schema + " has version " + currentSchemaVersion
                                  + ", but no migration could be resolved in the configured locations !");
            } else {
                for (MigrationInfo migrationInfo : resolved) {
                    // Only consider versioned migrations
                    if (migrationInfo.getVersion() != null) {
                        LOG.warn("Schema " + schema + " has a version (" + currentSchemaVersion
                                         + ") that is newer than the latest available migration ("
                                         + migrationInfo.getVersion() + ") !");
                        break;
                    }
                }
            }
        }

        MigrationInfoImpl[] failed = infoService.failed();
        if (failed.length > 0) {
            if ((failed.length == 1)
                    && (failed[0].getState() == MigrationState.FUTURE_FAILED)
                    && ValidatePatternUtils.isFutureIgnored(flywayCommandSupport.getConfiguration().getIgnoreMigrationPatterns())) {
                LOG.warn("Schema " + schema + " contains a failed future migration to version " + failed[0].getVersion() + " !");
            } else {
                final boolean inTransaction = failed[0].canExecuteInTransaction();
                if (failed[0].getVersion() == null) {
                    throw new FlywayMigrateException(failed[0], "Schema " + schema + " contains a failed repeatable migration (" + doQuote(failed[0].getDescription()) + ") !", inTransaction, migrateResult);
                }
                throw new FlywayMigrateException(failed[0], "Schema " + schema + " contains a failed migration to version " + failed[0].getVersion() + " !", inTransaction, migrateResult);
            }
        }

        LinkedHashMap<MigrationInfoImpl, Boolean> group = new LinkedHashMap<>();
        for (MigrationInfoImpl pendingMigration : infoService.pending()) {
            if (appliedResolvedMigrations.contains(pendingMigration.getResolvedMigration())) {
                continue;
            }

            boolean isOutOfOrder = pendingMigration.getVersion() != null
                    && pendingMigration.getVersion().compareTo(currentSchemaVersion) < 0;

            group.put(pendingMigration, isOutOfOrder);

            if (!flywayCommandSupport.getConfiguration().isGroup()) {
                // Only include one pending migration if group is disabled
                break;
            }
        }

        if (!group.isEmpty()) {
            boolean skipExecutingMigrations = false;



            applyMigrations(group, skipExecutingMigrations);
        }
        return group.size();
    }

    private void logSummary(int migrationSuccessCount, long executionTime, String targetVersion) {
        if (migrationSuccessCount == 0) {
            LOG.info("Schema " + schema + " is up to date. No migration necessary.");
            return;
        }

        String targetText = (targetVersion != null) ? ", now at version v" + targetVersion : "";

        String migrationText = "migration" + StringUtils.pluralizeSuffix(migrationSuccessCount);

        LOG.info("Successfully applied " + migrationSuccessCount + " " + migrationText + " to schema " + schema
                         + targetText + " (execution time " + TimeFormat.format(executionTime) + ")");
    }

    /**
     * Applies this migration to the database. The migration state and the execution time are updated accordingly.
     */
    private void applyMigrations(final LinkedHashMap<MigrationInfoImpl, Boolean> group, boolean skipExecutingMigrations) {
        boolean executeGroupInTransaction = isExecuteGroupInTransaction(group);
        final StopWatch stopWatch = new StopWatch();
        try {
            if (executeGroupInTransaction) {
                ExecutionTemplateFactory.createExecutionTemplate(connectionUserObjects.getJdbcConnection(), flywayCommandSupport.getDatabase()).execute(() -> {
                    doMigrateGroup(group, stopWatch, skipExecutingMigrations, true);
                    return null;
                });
            } else {
                doMigrateGroup(group, stopWatch, skipExecutingMigrations, false);
            }
        } catch (FlywayMigrateException e) {
            MigrationInfo migration = e.getMigration();

            String failedMsg = "Migration of " + toMigrationText(migration, e.isExecutableInTransaction(), e.isOutOfOrder()) + " failed!";
            if (flywayCommandSupport.getDatabase().supportsDdlTransactions() && executeGroupInTransaction) {
                LOG.error(failedMsg + " Changes successfully rolled back.");
            } else {
                LOG.error(failedMsg + " Please restore backups and roll back database and code!");

                stopWatch.stop();
                int executionTime = (int) stopWatch.getTotalTimeMillis();
                flywayCommandSupport.getSchemaHistory().addAppliedMigration(migration.getVersion(), migration.getDescription(),
                                                  migration.getType(), migration.getScript(), migration.getChecksum(), executionTime, false);
            }
            throw e;
        }
    }

    private boolean isExecuteGroupInTransaction(LinkedHashMap<MigrationInfoImpl, Boolean> group) {
        boolean executeGroupInTransaction = true;
        boolean first = true;

        for (Map.Entry<MigrationInfoImpl, Boolean> entry : group.entrySet()) {
            ResolvedMigration resolvedMigration = entry.getKey().getResolvedMigration();
            boolean inTransaction = resolvedMigration.getExecutor().canExecuteInTransaction();

            if (first) {
                executeGroupInTransaction = inTransaction;
                first = false;
                continue;
            }

            if (!flywayCommandSupport.getConfiguration().isMixed() && executeGroupInTransaction != inTransaction) {
                throw new FlywayMigrateException(entry.getKey(),
                                                 "Detected both transactional and non-transactional migrations within the same migration group"
                                                         + " (even though mixed is false). First offending migration: "
                                                         + doQuote((resolvedMigration.getVersion() == null ? "" : resolvedMigration.getVersion())
                                                                           + (StringUtils.hasLength(resolvedMigration.getDescription()) ? " " + resolvedMigration.getDescription() : ""))
                                                         + (inTransaction ? "" : " [non-transactional]"),
                                                 inTransaction,
                                                 migrateResult);
            }

            executeGroupInTransaction &= inTransaction;
        }

        return executeGroupInTransaction;
    }

    private void doMigrateGroup(LinkedHashMap<MigrationInfoImpl, Boolean> group, StopWatch stopWatch, boolean skipExecutingMigrations, boolean isExecuteInTransaction) {
        Context context = new Context() {
            @Override
            public Configuration getConfiguration() {
                return flywayCommandSupport.getConfiguration();
            }

            @Override
            public java.sql.Connection getConnection() {
                return connectionUserObjects.getJdbcConnection();
            }
        };

        for (Map.Entry<MigrationInfoImpl, Boolean> entry : group.entrySet()) {
            final MigrationInfoImpl migration = entry.getKey();
            boolean isOutOfOrder = entry.getValue();

            final String migrationText = toMigrationText(migration, migration.canExecuteInTransaction(), isOutOfOrder);

            stopWatch.start();

            if (isPreviousVersioned && migration.getVersion() == null) {
                flywayCommandSupport.getCallbackExecutor().onMigrateOrUndoEvent(Event.AFTER_VERSIONED);
                flywayCommandSupport.getCallbackExecutor().onMigrateOrUndoEvent(Event.BEFORE_REPEATABLES);
                isPreviousVersioned = false;
            }

            if (skipExecutingMigrations) {
                LOG.debug("Skipping execution of migration of " + migrationText);
            } else {
                LOG.debug("Starting migration of " + migrationText + " ...");

                connectionUserObjects.restoreOriginalState();
                connectionUserObjects.changeCurrentSchemaTo(schema);

                try {
                    flywayCommandSupport.getCallbackExecutor().setMigrationInfo(migration);
                    flywayCommandSupport.getCallbackExecutor().onEachMigrateOrUndoEvent(Event.BEFORE_EACH_MIGRATE);
                    try {
                        LOG.info("Migrating " + migrationText);

                        // With single connection databases we need to manually disable the transaction for the
                        // migration as it is turned on for schema history changes
                        boolean oldAutoCommit = context.getConnection().getAutoCommit();
                        if (flywayCommandSupport.getDatabase().useSingleConnection() && !isExecuteInTransaction) {
                            context.getConnection().setAutoCommit(true);
                        }
                        migration.getResolvedMigration().getExecutor().execute(context);
                        if (flywayCommandSupport.getDatabase().useSingleConnection() && !isExecuteInTransaction) {
                            context.getConnection().setAutoCommit(oldAutoCommit);
                        }

                        appliedResolvedMigrations.add(migration.getResolvedMigration());
                    } catch (FlywayException e) {
                        flywayCommandSupport.getCallbackExecutor().onEachMigrateOrUndoEvent(Event.AFTER_EACH_MIGRATE_ERROR);
                        throw new FlywayMigrateException(migration, isOutOfOrder, e, migration.canExecuteInTransaction(), migrateResult);
                    } catch (SQLException e) {
                        flywayCommandSupport.getCallbackExecutor().onEachMigrateOrUndoEvent(Event.AFTER_EACH_MIGRATE_ERROR);
                        throw new FlywayMigrateException(migration, isOutOfOrder, e, migration.canExecuteInTransaction(), migrateResult);
                    }

                    LOG.debug("Successfully completed migration of " + migrationText);
                    flywayCommandSupport.getCallbackExecutor().onEachMigrateOrUndoEvent(Event.AFTER_EACH_MIGRATE);
                } finally {
                    flywayCommandSupport.getCallbackExecutor().setMigrationInfo(null);
                }
            }

            stopWatch.stop();
            int executionTime = (int) stopWatch.getTotalTimeMillis();

            migrateResult.migrations.add(CommandResultFactory.createMigrateOutput(migration, executionTime));

            flywayCommandSupport.getSchemaHistory().addAppliedMigration(migration.getVersion(), migration.getDescription(), migration.getType(),
                                              migration.getScript(), migration.getResolvedMigration().getChecksum(), executionTime, true);
        }
    }

    private String toMigrationText(MigrationInfo migration, boolean canExecuteInTransaction, boolean isOutOfOrder) {
        final String migrationText;
        if (migration.getVersion() != null) {
            migrationText = "schema " + schema + " to version " + doQuote(migration.getVersion()
                                                                                  + (StringUtils.hasLength(migration.getDescription()) ? " - " + migration.getDescription() : ""))
                    + (isOutOfOrder ? " [out of order]" : "")
                    + (canExecuteInTransaction ? "" : " [non-transactional]");
        } else {
            migrationText = "schema " + schema + " with repeatable migration " + doQuote(migration.getDescription())
                    + (canExecuteInTransaction ? "" : " [non-transactional]");
        }
        return migrationText;
    }

    private String doQuote(String text) {
        return "\"" + text + "\"";
    }

    @Getter
    public static class FlywayMigrateException extends FlywayException {
        private final MigrationInfo migration;
        private final boolean executableInTransaction;
        private final boolean outOfOrder;
        private final MigrateErrorResult errorResult;

        public ErrorCode getMigrationErrorCode() {
            if (migration.getVersion() != null) {
                return CoreErrorCode.FAILED_VERSIONED_MIGRATION;
            } else {
                return CoreErrorCode.FAILED_REPEATABLE_MIGRATION;
            }
        }

        FlywayMigrateException(MigrationInfo migration, boolean outOfOrder, SQLException e, boolean canExecuteInTransaction, MigrateResult partialResult) {
            super(ExceptionUtils.toMessage(e), e);
            this.migration = migration;
            this.outOfOrder = outOfOrder;
            this.executableInTransaction = canExecuteInTransaction;
            this.errorResult = new MigrateErrorResult(partialResult, this);
        }

        FlywayMigrateException(MigrationInfo migration, String message, boolean canExecuteInTransaction, MigrateResult partialResult) {
            super(message);
            this.outOfOrder = false;
            this.migration = migration;
            this.executableInTransaction = canExecuteInTransaction;
            this.errorResult = new MigrateErrorResult(partialResult, this);
        }

        FlywayMigrateException(MigrationInfo migration, boolean outOfOrder, FlywayException e, boolean canExecuteInTransaction, MigrateResult partialResult) {
            super(e.getMessage(), e);
            this.migration = migration;
            this.outOfOrder = outOfOrder;
            this.executableInTransaction = canExecuteInTransaction;
            this.errorResult = new MigrateErrorResult(partialResult, this);
        }
    }
}