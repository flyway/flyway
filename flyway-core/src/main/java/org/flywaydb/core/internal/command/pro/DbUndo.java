/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.command.pro;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.sqlscript.FlywaySqlScriptException;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Undoes the most recently applied versioned migration.
 */
public class DbUndo {
    private static final Log LOG = LogFactory.getLog(DbUndo.class);

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
    private final FlywayConfiguration configuration;

    /**
     * The callbacks to use.
     */
    private final List<FlywayCallback> effectiveCallbacks;

    /**
     * The connection to use to perform the actual database migrations.
     */
    private final Connection connectionUserObjects;

    /**
     * Creates a new undo command.
     *
     * @param database           Database-specific functionality.
     * @param schemaHistory      The database schema history table.
     * @param migrationResolver  The migration resolver.
     * @param configuration      The Flyway configuration.
     * @param effectiveCallbacks The callbacks to use.
     */
    public DbUndo(Database database,
                  SchemaHistory schemaHistory, Schema schema, MigrationResolver migrationResolver,
                  FlywayConfiguration configuration, List<FlywayCallback> effectiveCallbacks) {
        this.database = database;
        this.connectionUserObjects = database.getMigrationConnection();
        this.schemaHistory = schemaHistory;
        this.schema = schema;
        this.migrationResolver = migrationResolver;
        this.configuration = configuration;
        this.effectiveCallbacks = effectiveCallbacks;
    }

    /**
     * Starts the actual migration.
     *
     * @return The number of successfully applied migrations.
     * @throws FlywayException when migration failed.
     */
    public int undo() throws FlywayException {
        try {
            for (final FlywayCallback callback : effectiveCallbacks) {
                new TransactionTemplate(connectionUserObjects.getJdbcConnection()).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        connectionUserObjects.changeCurrentSchemaTo(schema);
                        callback.beforeUndo(connectionUserObjects.getJdbcConnection());
                        return null;
                    }
                });
            }

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            int count = 0;
            if (schemaHistory.exists()) {
                count = configuration.isGroup() ?
                        // When group is active, start the transaction boundary early to
                        // ensure that all changes to the schema history table are either committed or rolled back atomically.
                        schemaHistory.lock(new Callable<Integer>() {
                            @Override
                            public Integer call() {
                                return undoAll();
                            }
                        }) :
                        // For all regular cases, proceed with the migration as usual.
                        undoAll();
            }

            stopWatch.stop();

            logSummary(count, stopWatch.getTotalTimeMillis());

            for (final FlywayCallback callback : effectiveCallbacks) {
                new TransactionTemplate(connectionUserObjects.getJdbcConnection()).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        connectionUserObjects.changeCurrentSchemaTo(schema);
                        callback.afterUndo(connectionUserObjects.getJdbcConnection());
                        return null;
                    }
                });
            }

            return count;
        } finally {
            connectionUserObjects.restoreCurrentSchema();
        }
    }

    private int undoAll() {
        int total = 0;
        while (true) {
            final boolean firstRun = total == 0;
            int count = configuration.isGroup()
                    // With group active a lock on the schema history table has already been acquired.
                    ? undoGroup(firstRun)
                    // Otherwise acquire the lock now. The lock will be released at the end of each undo migration.
                    : schemaHistory.lock(new Callable<Integer>() {
                @Override
                public Integer call() {
                    return undoGroup(firstRun);
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
     * Undo a group of one (group = false) or more (group = true) migrations.
     *
     * @param firstRun Where this is the first time this code runs in this undo run.
     * @return The number of newly undone migrations.
     */
    private Integer undoGroup(boolean firstRun) {
        if (configuration.getTarget() != null && !firstRun) {
            // Only undo one migration if no target has been set.
            return 0;
        }

        MigrationInfoServiceImpl infoService =
                new MigrationInfoServiceImpl(migrationResolver,
                        schemaHistory, configuration.getTarget(), configuration.isOutOfOrder(), true, true, true);
        infoService.refresh();

        MigrationVersion currentSchemaVersion = MigrationVersion.EMPTY;
        if (infoService.current() != null) {
            currentSchemaVersion = infoService.current().getVersion();
        }
        if (firstRun) {
            LOG.info("Current version of schema " + schema + ": " + currentSchemaVersion);
        }

        MigrationInfo[] future = infoService.future();
        if (future.length > 0) {
            MigrationInfo[] resolved = infoService.resolved();
            if (resolved.length == 0) {
                LOG.warn("Schema " + schema + " has version " + currentSchemaVersion
                        + ", but no migration could be resolved in the configured locations !");
            } else {
                int offset = resolved.length - 1;
                while (resolved[offset].getVersion() == null) {
                    // Skip repeatable migrations
                    offset--;
                }
                LOG.warn("Schema " + schema + " has a version (" + currentSchemaVersion
                        + ") that is newer than the latest available migration ("
                        + resolved[offset].getVersion() + ") !");
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

        List<MigrationInfoImpl> undoCandidates = new ArrayList<MigrationInfoImpl>();
        for (MigrationInfoImpl migrationInfo : infoService.applied()) {
            if (migrationInfo.getVersion() != null
                    && !migrationInfo.getType().isSynthetic()
                    && !migrationInfo.getType().isUndo()
                    && migrationInfo.getState() != MigrationState.UNDONE) {
                if (configuration.getTarget() == null
                        || configuration.getTarget().compareTo(migrationInfo.getVersion()) >= 0) {
                    undoCandidates.add(migrationInfo);
                } else {
                    break;
                }
            }
        }
        Collections.reverse(undoCandidates);

        MigrationInfoImpl[] undos = infoService.undo();

        Map<ResolvedMigration, MigrationInfo> group = new LinkedHashMap<ResolvedMigration, MigrationInfo>();
        for (MigrationInfoImpl undoCandidate : undoCandidates) {
            ResolvedMigration undo = findUndo(undos, undoCandidate.getVersion());
            if (undo == null) {
                throw new FlywayException("Unable to undo migration to version " + undoCandidate.getVersion()
                        + " as no corresponding undo migration has been found.");
            }
            group.put(undo, undoCandidate);

            if (!configuration.isGroup() || configuration.getTarget() == null) {
                // Only include one undo migration if group is disabled or no target has been set
                break;
            }
        }

        if (!group.isEmpty()) {
            undoMigrations(group);
        }
        return group.size();
    }

    private ResolvedMigration findUndo(MigrationInfoImpl[] undos, MigrationVersion version) {
        for (MigrationInfoImpl undo : undos) {
            if (undo.getVersion().equals(version)) {
                return undo.getResolvedMigration();
            }
        }
        return null;
    }

    /**
     * Logs the summary of this migration run.
     *
     * @param migrationSuccessCount The number of successfully applied migrations.
     * @param executionTime         The total time taken to perform this migration run (in ms).
     */

    private void logSummary(int migrationSuccessCount, long executionTime) {
        if (migrationSuccessCount == 0) {
            LOG.info("Schema " + schema + " has no migrations to undo.");
            return;
        }

        if (migrationSuccessCount == 1) {
            LOG.info("Successfully undid 1 migration to schema " + schema + " (execution time " + TimeFormat.format(executionTime) + ").");
        } else {
            LOG.info("Successfully undid " + migrationSuccessCount + " migrations to schema " + schema + " (execution time " + TimeFormat.format(executionTime) + ").");
        }
    }

    /**
     * Undoes these migration from the database. The migration state and the execution time are updated accordingly.
     *
     * @param group The group of migrations to apply.
     */
    private void undoMigrations(final Map<ResolvedMigration, MigrationInfo> group) {
        boolean executeGroupInTransaction = isExecuteGroupInTransaction(group);
        final StopWatch stopWatch = new StopWatch();
        try {
            if (executeGroupInTransaction) {
                new TransactionTemplate(connectionUserObjects.getJdbcConnection()).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        doUndoGroup(group, stopWatch);
                        return null;
                    }
                });
            } else {
                doUndoGroup(group, stopWatch);
            }
        } catch (FlywayUndoSqlException e) {
            ResolvedMigration migration = e.getMigration();
            String failedMsg = "Undo of " + toMigrationText(migration) + " failed!";
            if (database.supportsDdlTransactions() && executeGroupInTransaction) {
                LOG.error(failedMsg + " Changes successfully rolled back.");
            } else {
                LOG.error(failedMsg + " Please restore backups and roll back database and code!");

                stopWatch.stop();
                int executionTime = (int) stopWatch.getTotalTimeMillis();
                schemaHistory.addAppliedMigration(migration.getVersion(), migration.getDescription(),
                        migration.getType(),
                        migration.getScript(),
                        migration.getChecksum(),
                        executionTime, false);
            }
            throw e;
        }
    }

    private boolean isExecuteGroupInTransaction(Map<ResolvedMigration, MigrationInfo> group) {
        boolean executeGroupInTransaction = true;
        boolean first = true;
        for (ResolvedMigration undoMigration : group.keySet()) {
            boolean inTransaction = undoMigration.getExecutor().executeInTransaction();
            if (first) {
                executeGroupInTransaction = inTransaction;
                first = false;
            } else {
                if (!configuration.isMixed() && executeGroupInTransaction != inTransaction) {
                    throw new FlywayException(
                            "Detected both transactional and non-transactional undo migrations within the same undo migration group"
                                    + " (even though mixed is false). First offending migration:"
                                    + undoMigration.getVersion()
                                    + (StringUtils.hasLength(undoMigration.getDescription()) ? " " + undoMigration.getDescription() : "")
                                    + (inTransaction ? "" : " [non-transactional]"));
                }
                executeGroupInTransaction = executeGroupInTransaction && inTransaction;
            }
        }
        return executeGroupInTransaction;
    }

    private void doUndoGroup(Map<ResolvedMigration, MigrationInfo> group, StopWatch stopWatch) {
        for (ResolvedMigration migration : group.keySet()) {
            final String migrationText = toMigrationText(migration);

            stopWatch.start();

            LOG.info("Undoing " + migrationText);

            connectionUserObjects.changeCurrentSchemaTo(schema);

            for (final FlywayCallback callback : effectiveCallbacks) {
                callback.beforeEachUndo(connectionUserObjects.getJdbcConnection(), group.get(migration));
            }

            try {
                migration.getExecutor().execute(connectionUserObjects.getJdbcConnection());
            } catch (FlywaySqlScriptException e) {
                throw new FlywayUndoSqlException(migration, e);
            } catch (SQLException e) {
                throw new FlywayUndoSqlException(migration, e);
            }
            LOG.debug("Successfully completed undo of " + migrationText);

            for (final FlywayCallback callback : effectiveCallbacks) {
                callback.afterEachUndo(connectionUserObjects.getJdbcConnection(), group.get(migration));
            }

            stopWatch.stop();
            int executionTime = (int) stopWatch.getTotalTimeMillis();

            schemaHistory.addAppliedMigration(migration.getVersion(),
                    migration.getDescription(),
                    migration.getType(),
                    migration.getScript(),
                    migration.getChecksum(),
                    executionTime, true);
        }
    }

    private String toMigrationText(ResolvedMigration migration) {
        return "schema " + schema + " to version " + migration.getVersion()
                + " - " + migration.getDescription()
                + (migration.getExecutor().executeInTransaction() ? "" : " [non-transactional]");
    }

    public static class FlywayUndoSqlException extends FlywaySqlScriptException {
        private final ResolvedMigration migration;

        FlywayUndoSqlException(ResolvedMigration migration, SQLException e) {
            super(null, null, e);
            this.migration = migration;
        }

        FlywayUndoSqlException(ResolvedMigration migration, FlywaySqlScriptException e) {
            super(e.getResource(), e.getSqlStatement(), (SQLException) e.getCause());
            this.migration = migration;
        }

        public ResolvedMigration getMigration() {
            return migration;
        }
    }
}
