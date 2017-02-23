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
package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.FlywaySqlException;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Main workflow for migrating the database.
 *
 * @author Axel Fontaine
 */
public class DbMigrate {

    private static final Log LOG = LogFactory.getLog(DbMigrate.class);

    /**
     * Database-specific functionality.
     */
    private final DbSupport dbSupport;

    /**
     * The database metadata table.
     */
    private final MetaDataTable metaDataTable;

    /**
     * The schema containing the metadata table.
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
     * The connection to use to perform the actual database migrations.
     */
    private final Connection connectionUserObjects;

    /**
     * Flag whether to ignore failed future migrations or not.
     */
    private final boolean ignoreFailedFutureMigration;

    /**
     * The DB support for the user objects connection.
     */
    private final DbSupport dbSupportUserObjects;

    /**
     * Creates a new database migrator.
     *
     * @param connectionUserObjects       The connection to use to perform the actual database migrations.
     * @param dbSupport                   Database-specific functionality.
     * @param metaDataTable               The database metadata table.
     * @param migrationResolver           The migration resolver.
     * @param ignoreFailedFutureMigration Flag whether to ignore failed future migrations or not.
     * @param configuration               The Flyway configuration.
     */
    public DbMigrate(Connection connectionUserObjects, DbSupport dbSupport,
                     MetaDataTable metaDataTable, Schema schema, MigrationResolver migrationResolver,
                     boolean ignoreFailedFutureMigration, FlywayConfiguration configuration) {
        this.connectionUserObjects = connectionUserObjects;
        this.dbSupport = dbSupport;
        this.metaDataTable = metaDataTable;
        this.schema = schema;
        this.migrationResolver = migrationResolver;
        this.ignoreFailedFutureMigration = ignoreFailedFutureMigration;
        this.configuration = configuration;

        dbSupportUserObjects = DbSupportFactory.createDbSupport(connectionUserObjects, false);
    }

    /**
     * Starts the actual migration.
     *
     * @return The number of successfully applied migrations.
     * @throws FlywayException when migration failed.
     */
    public int migrate() throws FlywayException {
        try {
            for (final FlywayCallback callback : configuration.getCallbacks()) {
                new TransactionTemplate(connectionUserObjects).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        dbSupportUserObjects.changeCurrentSchemaTo(schema);
                        callback.beforeMigrate(connectionUserObjects);
                        return null;
                    }
                });
            }

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            int migrationSuccessCount = 0;
            while (true) {
                final boolean firstRun = migrationSuccessCount == 0;
                boolean done = metaDataTable.lock(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        MigrationInfoServiceImpl infoService =
                                new MigrationInfoServiceImpl(migrationResolver, metaDataTable, configuration.getTarget(), configuration.isOutOfOrder(), true, true, true);
                        infoService.refresh();

                        MigrationVersion currentSchemaVersion = MigrationVersion.EMPTY;
                        if (infoService.current() != null) {
                            currentSchemaVersion = infoService.current().getVersion();
                        }
                        if (firstRun) {
                            LOG.info("Current version of schema " + schema + ": " + currentSchemaVersion);

                            if (configuration.isOutOfOrder()) {
                                LOG.warn("outOfOrder mode is active. Migration of schema " + schema + " may not be reproducible.");
                            }
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
                                    && (configuration.isIgnoreFutureMigrations() || ignoreFailedFutureMigration)) {
                                LOG.warn("Schema " + schema + " contains a failed future migration to version " + failed[0].getVersion() + " !");
                            } else {
                                if (failed[0].getVersion() == null) {
                                    throw new FlywayException("Schema " + schema + " contains a failed repeatable migration (" + failed[0].getDescription() + ") !");
                                }
                                throw new FlywayException("Schema " + schema + " contains a failed migration to version " + failed[0].getVersion() + " !");
                            }
                        }

                        MigrationInfoImpl[] pendingMigrations = infoService.pending();

                        if (pendingMigrations.length == 0) {
                            return true;
                        }

                        boolean isOutOfOrder = pendingMigrations[0].getVersion() != null
                                && pendingMigrations[0].getVersion().compareTo(currentSchemaVersion) < 0;
                        return applyMigration(pendingMigrations[0], isOutOfOrder);
                    }
                });
                if (done) {
                    // No further migrations available
                    break;
                }

                migrationSuccessCount++;
            }

            stopWatch.stop();

            logSummary(migrationSuccessCount, stopWatch.getTotalTimeMillis());

            for (final FlywayCallback callback : configuration.getCallbacks()) {
                new TransactionTemplate(connectionUserObjects).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        dbSupportUserObjects.changeCurrentSchemaTo(schema);
                        callback.afterMigrate(connectionUserObjects);
                        return null;
                    }
                });
            }

            return migrationSuccessCount;
        } finally {
            dbSupportUserObjects.restoreCurrentSchema();
        }
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
            LOG.info("Successfully applied 1 migration to schema " + schema + " (execution time " + TimeFormat.format(executionTime) + ").");
        } else {
            LOG.info("Successfully applied " + migrationSuccessCount + " migrations to schema " + schema + " (execution time " + TimeFormat.format(executionTime) + ").");
        }
    }

    /**
     * Applies this migration to the database. The migration state and the execution time are updated accordingly.
     *
     * @param migration    The migration to apply.
     * @param isOutOfOrder If this migration is being applied out of order.
     * @return The result of the migration.
     */
    private Boolean applyMigration(final MigrationInfoImpl migration, boolean isOutOfOrder) {
        MigrationVersion version = migration.getVersion();
        final MigrationExecutor migrationExecutor = migration.getResolvedMigration().getExecutor();
        final String migrationText;
        if (version != null) {
            migrationText = "schema " + schema + " to version " + version + " - " + migration.getDescription() +
                    (isOutOfOrder ? " [out of order]" : "") + (migrationExecutor.executeInTransaction() ? "" : " [non-transactional]");
        } else {
            migrationText = "schema " + schema + " with repeatable migration " + migration.getDescription() + (migrationExecutor.executeInTransaction() ? "" : " [non-transactional]");
        }

        LOG.info("Migrating " + migrationText);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            if (migrationExecutor.executeInTransaction()) {
                new TransactionTemplate(connectionUserObjects).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        doMigrate(migration, migrationExecutor, migrationText);
                        return null;
                    }
                });
            } else {
                try {
                    doMigrate(migration, migrationExecutor, migrationText);
                } catch (SQLException e) {
                    throw new FlywaySqlException("Unable to apply migration", e);
                }
            }
        } catch (FlywayException e) {
            String failedMsg = "Migration of " + migrationText + " failed!";
            if (dbSupport.supportsDdlTransactions() && migrationExecutor.executeInTransaction()) {
                LOG.error(failedMsg + " Changes successfully rolled back.");
            } else {
                LOG.error(failedMsg + " Please restore backups and roll back database and code!");

                stopWatch.stop();
                int executionTime = (int) stopWatch.getTotalTimeMillis();
                AppliedMigration appliedMigration = new AppliedMigration(version, migration.getDescription(),
                        migration.getType(), migration.getScript(), migration.getResolvedMigration().getChecksum(), executionTime, false);
                metaDataTable.addAppliedMigration(appliedMigration);
            }
            throw e;
        }

        stopWatch.stop();
        int executionTime = (int) stopWatch.getTotalTimeMillis();

        AppliedMigration appliedMigration = new AppliedMigration(version, migration.getDescription(),
                migration.getType(), migration.getScript(), migration.getResolvedMigration().getChecksum(), executionTime, true);
        metaDataTable.addAppliedMigration(appliedMigration);

        return false;
    }

    private void doMigrate(MigrationInfoImpl migration, MigrationExecutor migrationExecutor, String migrationText) throws SQLException {
        dbSupportUserObjects.changeCurrentSchemaTo(schema);

        for (final FlywayCallback callback : configuration.getCallbacks()) {
            callback.beforeEachMigrate(connectionUserObjects, migration);
        }

        migrationExecutor.execute(connectionUserObjects);
        LOG.debug("Successfully completed migration of " + migrationText);

        for (final FlywayCallback callback : configuration.getCallbacks()) {
            callback.afterEachMigrate(connectionUserObjects, migration);
        }
    }
}
