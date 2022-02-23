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
package org.flywaydb.core.internal.command;

import lombok.CustomLog;
import lombok.Getter;
import org.flywaydb.core.api.*;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.output.MigrateErrorResult;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.api.resolver.UnresolvedMigration;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.resolver.UnresolvedMigrationImpl;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.*;

import java.sql.SQLException;
import java.util.*;

@CustomLog
public class DbMigrate {

    private final Database database;
    private final SchemaHistory schemaHistory;
    /**
     * The schema containing the schema history table.
     */
    private final Schema schema;
    private final MigrationResolver migrationResolver;
    private final Configuration configuration;
    private final CallbackExecutor callbackExecutor;
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

    public DbMigrate(Database database,
                     SchemaHistory schemaHistory, Schema schema, MigrationResolver migrationResolver,
                     Configuration configuration, CallbackExecutor callbackExecutor) {
        this.database = database;
        this.connectionUserObjects = database.getMigrationConnection();
        this.schemaHistory = schemaHistory;
        this.schema = schema;
        this.migrationResolver = migrationResolver;
        this.configuration = configuration;
        this.callbackExecutor = callbackExecutor;
    }

    /**
     * Starts the actual migration.
     */
    public MigrateResult migrate() throws FlywayException {
        callbackExecutor.onMigrateOrUndoEvent(Event.BEFORE_MIGRATE);

        migrateResult = CommandResultFactory.createMigrateResult(database.getCatalog(), configuration);

        Pair<Integer, Collection<UnresolvedMigration>> count;
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            count = configuration.isGroup() ?
                    // When group is active, start the transaction boundary early to
                    // ensure that all changes to the schema history table are either committed or rolled back atomically.
                    schemaHistory.lock(this::migrateAll) :
                    // For all regular cases, proceed with the migration as usual.
                    migrateAll();

            stopWatch.stop();

            migrateResult.targetSchemaVersion = getTargetVersion();
            migrateResult.migrationsExecuted = count.getLeft();

            logSummary(count, stopWatch.getTotalTimeMillis(), migrateResult.targetSchemaVersion);
        } catch (FlywayException e) {
            callbackExecutor.onMigrateOrUndoEvent(Event.AFTER_MIGRATE_ERROR);
            throw e;
        }

        if (count.getLeft() > 0) {
            callbackExecutor.onMigrateOrUndoEvent(Event.AFTER_MIGRATE_APPLIED);
        }
        callbackExecutor.onMigrateOrUndoEvent(Event.AFTER_MIGRATE);

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

    private Pair<Integer, Collection<UnresolvedMigration>> migrateAll() {
        int total = 0;
        Collection<UnresolvedMigration> unresolvedMigrations = new HashSet<>();
        isPreviousVersioned = true;

        while (true) {
            final boolean firstRun = total == 0;
            Pair<Integer, Collection<UnresolvedMigration>> count = configuration.isGroup()
                    // With group active a lock on the schema history table has already been acquired.
                    ? migrateGroup(firstRun)
                    // Otherwise acquire the lock now. The lock will be released at the end of each migration.
                    : schemaHistory.lock(() -> migrateGroup(firstRun));

            migrateResult.migrationsExecuted += count.getLeft();
            unresolvedMigrations.addAll(count.getRight());

            total += count.getLeft();
            if (count.getLeft() == 0) {
                // No further migrations available
                break;
            } else if (configuration.getTarget() == MigrationVersion.NEXT) {
                // With target=next we only execute one migration
                break;
            }
        }

        if (isPreviousVersioned) {
            callbackExecutor.onMigrateOrUndoEvent(Event.AFTER_VERSIONED);
        }

        return Pair.of(total, unresolvedMigrations);
    }

    /**
     * Migrate a group of one (group = false) or more (group = true) migrations.
     *
     * @param firstRun Whether this is the first time this code runs in this migration run.
     * @return The number of newly applied migrations.
     */
    private Pair<Integer, Collection<UnresolvedMigration>> migrateGroup(boolean firstRun) {
        MigrationInfoServiceImpl infoService =
                new MigrationInfoServiceImpl(migrationResolver, schemaHistory, database, configuration,
                                             configuration.getTarget(), configuration.isOutOfOrder(), configuration.getCherryPick(),
                                             true, true, true, true);
        infoService.refresh();

        MigrationInfo current = infoService.current();
        MigrationVersion currentSchemaVersion = current == null ? MigrationVersion.EMPTY : current.getVersion();
        if (firstRun) {
            LOG.info("Current version of schema " + schema + ": " + currentSchemaVersion);

            MigrationVersion schemaVersionToOutput = currentSchemaVersion == null ? MigrationVersion.EMPTY : currentSchemaVersion;
            migrateResult.initialSchemaVersion = schemaVersionToOutput.getVersion();

            if (configuration.isOutOfOrder()) {
                String outOfOrderWarning = "outOfOrder mode is active. Migration of schema " + schema + " may not be reproducible.";
                LOG.warn(outOfOrderWarning);
                migrateResult.warnings.add(outOfOrderWarning);
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
                    && configuration.isIgnoreFutureMigrations()) {
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

            if (!configuration.isGroup()) {
                // Only include one pending migration if group is disabled
                break;
            }
        }

        if (!group.isEmpty()) {
            boolean skipExecutingMigrations = false;



            applyMigrations(group, skipExecutingMigrations);
        }
        return Pair.of(group.size(), infoService.unresolved());
    }

    private void logSummary(Pair<Integer, Collection<UnresolvedMigration>> migrationsCount, long executionTime, String targetVersion) {
        if (migrationsCount.getLeft() == 0) {
            LOG.info("Schema " + schema + " is up to date. No migration necessary.");
            if(migrationsCount.getRight().size() > 0) {
                LOG.warn("Schema " + schema + " has " + migrationsCount.getRight().size() + " unresolved migrations with the following outout:");
                for(UnresolvedMigration unresolvedMigration : migrationsCount.getRight()) {
                    LOG.warn(unresolvedMigration.getValidityMessage());
                }
            }
            return;
        }

        String targetText = (targetVersion != null) ? ", now at version v" + targetVersion : "";

        String migrationText = (migrationsCount.getLeft() == 1) ? "migration" : "migrations";

        LOG.info("Successfully applied " + migrationsCount + " " + migrationText + " to schema " + schema
                         + targetText + " (execution time " + TimeFormat.format(executionTime) + ")");
        if(migrationsCount.getRight().size() > 0) {
            LOG.warn("Found " + migrationsCount.getRight().size() + " unresolved migrations with the following output:");
            for(UnresolvedMigration unresolvedMigration : migrationsCount.getRight()) {
                LOG.warn(unresolvedMigration.getValidityMessage());
            }
            LOG.warn("If these should have been resolved, perhaps try the -validateMigrationNaming=\"true\" " +
                    "command-line option to fail this command instead of ignoring unresolved migrations.");
        }
    }

    /**
     * Applies this migration to the database. The migration state and the execution time are updated accordingly.
     */
    private void applyMigrations(final LinkedHashMap<MigrationInfoImpl, Boolean> group, boolean skipExecutingMigrations) {
        boolean executeGroupInTransaction = isExecuteGroupInTransaction(group);
        final StopWatch stopWatch = new StopWatch();
        try {
            if (executeGroupInTransaction) {
                ExecutionTemplateFactory.createExecutionTemplate(connectionUserObjects.getJdbcConnection(), database).execute(() -> {
                    doMigrateGroup(group, stopWatch, skipExecutingMigrations, true);
                    return null;
                });
            } else {
                doMigrateGroup(group, stopWatch, skipExecutingMigrations, false);
            }
        } catch (FlywayMigrateException e) {
            MigrationInfo migration = e.getMigration();

            String failedMsg = "Migration of " + toMigrationText(migration, e.isExecutableInTransaction(), e.isOutOfOrder()) + " failed!";
            if (database.supportsDdlTransactions() && executeGroupInTransaction) {
                LOG.error(failedMsg + " Changes successfully rolled back.");
            } else {
                LOG.error(failedMsg + " Please restore backups and roll back database and code!");

                stopWatch.stop();
                int executionTime = (int) stopWatch.getTotalTimeMillis();
                schemaHistory.addAppliedMigration(migration.getVersion(), migration.getDescription(),
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

            if (!configuration.isMixed() && executeGroupInTransaction != inTransaction) {
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
                return configuration;
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
                callbackExecutor.onMigrateOrUndoEvent(Event.AFTER_VERSIONED);
                callbackExecutor.onMigrateOrUndoEvent(Event.BEFORE_REPEATABLES);
                isPreviousVersioned = false;
            }

            if (skipExecutingMigrations) {
                LOG.debug("Skipping execution of migration of " + migrationText);
            } else {
                LOG.debug("Starting migration of " + migrationText + " ...");

                connectionUserObjects.restoreOriginalState();
                connectionUserObjects.changeCurrentSchemaTo(schema);

                try {
                    callbackExecutor.setMigrationInfo(migration);
                    callbackExecutor.onEachMigrateOrUndoEvent(Event.BEFORE_EACH_MIGRATE);
                    try {
                        LOG.info("Migrating " + migrationText);

                        // With single connection databases we need to manually disable the transaction for the
                        // migration as it is turned on for schema history changes
                        boolean oldAutoCommit = context.getConnection().getAutoCommit();
                        if (database.useSingleConnection() && !isExecuteInTransaction) {
                            context.getConnection().setAutoCommit(true);
                        }
                        migration.getResolvedMigration().getExecutor().execute(context);
                        if (database.useSingleConnection() && !isExecuteInTransaction) {
                            context.getConnection().setAutoCommit(oldAutoCommit);
                        }

                        appliedResolvedMigrations.add(migration.getResolvedMigration());
                    } catch (FlywayException e) {
                        callbackExecutor.onEachMigrateOrUndoEvent(Event.AFTER_EACH_MIGRATE_ERROR);
                        throw new FlywayMigrateException(migration, isOutOfOrder, e, migration.canExecuteInTransaction(), migrateResult);
                    } catch (SQLException e) {
                        callbackExecutor.onEachMigrateOrUndoEvent(Event.AFTER_EACH_MIGRATE_ERROR);
                        throw new FlywayMigrateException(migration, isOutOfOrder, e, migration.canExecuteInTransaction(), migrateResult);
                    }

                    LOG.debug("Successfully completed migration of " + migrationText);
                    callbackExecutor.onEachMigrateOrUndoEvent(Event.AFTER_EACH_MIGRATE);
                } finally {
                    callbackExecutor.setMigrationInfo(null);
                }
            }

            stopWatch.stop();
            int executionTime = (int) stopWatch.getTotalTimeMillis();

            migrateResult.migrations.add(CommandResultFactory.createMigrateOutput(migration, executionTime));

            schemaHistory.addAppliedMigration(migration.getVersion(), migration.getDescription(), migration.getType(),
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