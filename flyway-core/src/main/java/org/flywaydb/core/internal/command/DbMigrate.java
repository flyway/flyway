/*
 * Copyright 2010-2018 Boxfuse GmbH
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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.executor.Context;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.TimeFormat;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Main workflow for migrating the database.
 */
public class DbMigrate {
    private static final Log LOG = LogFactory.getLog(DbMigrate.class);

    /**
     * Database-specific functionality.
     */
    private final Database database;

    /**
     * The database schema history table.
     */
    private final SchemaHistory schemaHistory;

    /**
     * The schema containing the schema history table.
     */
    private final Schema schema;

    /**
     * The migration resolver.
     */
    private final MigrationResolver migrationResolver;

    /**
     * The Flyway configuration.
     */
    private final Configuration configuration;

    /**
     * The callback executor.
     */
    private final CallbackExecutor callbackExecutor;

    /**
     * The connection to use to perform the actual database migrations.
     */
    private final Connection connectionUserObjects;

    /**
     * Creates a new database migrator.
     *
     * @param database          Database-specific functionality.
     * @param schemaHistory     The database schema history table.
     * @param migrationResolver The migration resolver.
     * @param configuration     The Flyway configuration.
     * @param callbackExecutor  The callbacks executor.
     */
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
     *
     * @return The number of successfully applied migrations.
     * @throws FlywayException when migration failed.
     */
    public int migrate() throws FlywayException {
        callbackExecutor.onMigrateOrUndoEvent(Event.BEFORE_MIGRATE);

        int count;
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            schemaHistory.create();

            count = configuration.isGroup() ?
                    // When group is active, start the transaction boundary early to
                    // ensure that all changes to the schema history table are either committed or rolled back atomically.
                    schemaHistory.lock(new Callable<Integer>() {
                        @Override
                        public Integer call() {
                            return migrateAll();
                        }
                    }) :
                    // For all regular cases, proceed with the migration as usual.
                    migrateAll();

            stopWatch.stop();

            logSummary(count, stopWatch.getTotalTimeMillis());
        } catch (FlywayException e) {
            callbackExecutor.onMigrateOrUndoEvent(Event.AFTER_MIGRATE_ERROR);
            throw e;
        }

        callbackExecutor.onMigrateOrUndoEvent(Event.AFTER_MIGRATE);
        return count;
    }

    private int migrateAll() {
        int total = 0;
        while (true) {
            final boolean firstRun = total == 0;
            int count = configuration.isGroup()
                    // With group active a lock on the schema history table has already been acquired.
                    ? migrateGroup(firstRun)
                    // Otherwise acquire the lock now. The lock will be released at the end of each migration.
                    : schemaHistory.lock(new Callable<Integer>() {
                @Override
                public Integer call() {
                    return migrateGroup(firstRun);
                }
            });
            total += count;
            if (count == 0) {
                // No further migrations available
                break;
            }
        }
        return total;
    }

    /**
     * Migrate a group of one (group = false) or more (group = true) migrations.
     *
     * @param firstRun Where this is the first time this code runs in this migration run.
     * @return The number of newly applied migrations.
     */
    private Integer migrateGroup(boolean firstRun) {
        MigrationInfoServiceImpl infoService =
                new MigrationInfoServiceImpl(migrationResolver, schemaHistory, configuration,
                        configuration.getTarget(), configuration.isOutOfOrder(),
                        true, true, true, true);
        infoService.refresh();

        MigrationInfo current = infoService.current();
        MigrationVersion currentSchemaVersion = current == null ? MigrationVersion.EMPTY : current.getVersion();
        if (firstRun) {
            LOG.info("Current version of schema " + schema + ": " + currentSchemaVersion);

            if (configuration.isOutOfOrder()) {
                LOG.warn("outOfOrder mode is active. Migration of schema " + schema + " may not be reproducible.");
            }
        }

        MigrationInfo[] future = infoService.future();
        if (future.length > 0) {
            List<MigrationInfo> resolved = Arrays.asList(infoService.resolved());
            Collections.reverse(resolved);
            if (resolved.isEmpty()) {
                LOG.warn("Schema " + schema + " has version " + currentSchemaVersion
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

        MigrationInfo[] failed = infoService.failed();
        if (failed.length > 0) {
            if ((failed.length == 1)
                    && (failed[0].getState() == MigrationState.FUTURE_FAILED)
                    && configuration.isIgnoreFutureMigrations()) {
                LOG.warn("Schema " + schema + " contains a failed future migration to version " + failed[0].getVersion() + " !");
            } else {
                if (failed[0].getVersion() == null) {
                    throw new FlywayException("Schema " + schema + " contains a failed repeatable migration (" + failed[0].getDescription() + ") !");
                }
                throw new FlywayException("Schema " + schema + " contains a failed migration to version " + failed[0].getVersion() + " !");
            }
        }

        LinkedHashMap<MigrationInfoImpl, Boolean> group = new LinkedHashMap<>();
        for (MigrationInfoImpl pendingMigration : infoService.pending()) {
            boolean isOutOfOrder = pendingMigration.getVersion() != null
                    && pendingMigration.getVersion().compareTo(currentSchemaVersion) < 0;
            group.put(pendingMigration, isOutOfOrder);

            if (!configuration.isGroup()) {
                // Only include one pending migration if group is disabled
                break;
            }
        }

        if (!group.isEmpty()) {
            applyMigrations(group);
        }
        return group.size();
    }

    /**
     * Logs the summary of this migration run.
     *
     * @param migrationSuccessCount The number of successfully applied migrations.
     * @param executionTime         The total time taken to perform this migration run (in ms).
     */

    private void logSummary(int migrationSuccessCount, long executionTime) {
        if (migrationSuccessCount == 0) {
            LOG.info("Schema " + schema + " is up to date. No migration necessary.");
            return;
        }

        if (migrationSuccessCount == 1) {
            LOG.info("Successfully applied 1 migration to schema " + schema + " (execution time " + TimeFormat.format(executionTime) + ")");
        } else {
            LOG.info("Successfully applied " + migrationSuccessCount + " migrations to schema " + schema + " (execution time " + TimeFormat.format(executionTime) + ")");
        }
    }

    /**
     * Applies this migration to the database. The migration state and the execution time are updated accordingly.
     *
     * @param group The group of migrations to apply.
     */
    private void applyMigrations(final LinkedHashMap<MigrationInfoImpl, Boolean> group) {
        boolean executeGroupInTransaction = isExecuteGroupInTransaction(group);
        final StopWatch stopWatch = new StopWatch();
        try {
            if (executeGroupInTransaction) {
                new TransactionTemplate(connectionUserObjects.getJdbcConnection()).execute(new Callable<Object>() {
                    @Override
                    public Object call() {
                        doMigrateGroup(group, stopWatch);
                        return null;
                    }
                });
            } else {
                doMigrateGroup(group, stopWatch);
            }
        } catch (FlywayMigrateException e) {
            MigrationInfoImpl migration = e.getMigration();
            String failedMsg = "Migration of " + toMigrationText(migration, e.isOutOfOrder()) + " failed!";
            if (database.supportsDdlTransactions() && executeGroupInTransaction) {
                LOG.error(failedMsg + " Changes successfully rolled back.");
            } else {
                LOG.error(failedMsg + " Please restore backups and roll back database and code!");

                stopWatch.stop();
                int executionTime = (int) stopWatch.getTotalTimeMillis();
                schemaHistory.addAppliedMigration(migration.getVersion(), migration.getDescription(),
                        migration.getType(), migration.getScript(), migration.getResolvedMigration().getChecksum(), executionTime, false);
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
                throw new FlywayException(
                        "Detected both transactional and non-transactional migrations within the same migration group"
                                + " (even though mixed is false). First offending migration:"
                                + (resolvedMigration.getVersion() == null ? "" : " " + resolvedMigration.getVersion())
                                + (StringUtils.hasLength(resolvedMigration.getDescription()) ? " " + resolvedMigration.getDescription() : "")
                                + (inTransaction ? "" : " [non-transactional]"));
            }

            executeGroupInTransaction &= inTransaction;
        }

        return executeGroupInTransaction;
    }

    private void doMigrateGroup(LinkedHashMap<MigrationInfoImpl, Boolean> group, StopWatch stopWatch) {
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

            final String migrationText = toMigrationText(migration, isOutOfOrder);

            stopWatch.start();

            LOG.info("Migrating " + migrationText);

            connectionUserObjects.restoreOriginalState();
            connectionUserObjects.changeCurrentSchemaTo(schema);

            try {
                callbackExecutor.setMigrationInfo(migration);
                callbackExecutor.onEachMigrateOrUndoEvent(Event.BEFORE_EACH_MIGRATE);
                try {
                    migration.getResolvedMigration().getExecutor().execute(context);
                } catch (FlywayException e) {
                    callbackExecutor.onEachMigrateOrUndoEvent(Event.AFTER_EACH_MIGRATE_ERROR);
                    throw new FlywayMigrateException(migration, isOutOfOrder, e);
                } catch (SQLException e) {
                    callbackExecutor.onEachMigrateOrUndoEvent(Event.AFTER_EACH_MIGRATE_ERROR);
                    throw new FlywayMigrateException(migration, isOutOfOrder, e);
                }

                LOG.debug("Successfully completed migration of " + migrationText);
                callbackExecutor.onEachMigrateOrUndoEvent(Event.AFTER_EACH_MIGRATE);
            } finally {
                callbackExecutor.setMigrationInfo(null);
            }

            stopWatch.stop();
            int executionTime = (int) stopWatch.getTotalTimeMillis();

            schemaHistory.addAppliedMigration(migration.getVersion(), migration.getDescription(), migration.getType(),
                    migration.getScript(), migration.getResolvedMigration().getChecksum(), executionTime, true);
        }
    }

    private String toMigrationText(MigrationInfoImpl migration, boolean isOutOfOrder) {
        final MigrationExecutor migrationExecutor = migration.getResolvedMigration().getExecutor();
        final String migrationText;
        if (migration.getVersion() != null) {
            migrationText = "schema " + schema + " to version " + migration.getVersion() + " - " + migration.getDescription() +
                    (isOutOfOrder ? " [out of order]" : "") + (migrationExecutor.canExecuteInTransaction() ? "" : " [non-transactional]");
        } else {
            migrationText = "schema " + schema + " with repeatable migration " + migration.getDescription()
                    + (migrationExecutor.canExecuteInTransaction() ? "" : " [non-transactional]");
        }
        return migrationText;
    }

    public static class FlywayMigrateException extends FlywayException {
        private final MigrationInfoImpl migration;
        private final boolean outOfOrder;

        FlywayMigrateException(MigrationInfoImpl migration, boolean outOfOrder, SQLException e) {
            super(ExceptionUtils.toMessage(e), e);
            this.migration = migration;
            this.outOfOrder = outOfOrder;
        }

        FlywayMigrateException(MigrationInfoImpl migration, boolean outOfOrder, FlywayException e) {
            super(e.getMessage(), e);
            this.migration = migration;
            this.outOfOrder = outOfOrder;
        }

        public MigrationInfoImpl getMigration() {
            return migration;
        }

        public boolean isOutOfOrder() {
            return outOfOrder;
        }
    }
}