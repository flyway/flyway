/**
 * Copyright 2010-2015 Axel Fontaine
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
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.metadatatable.AppliedMigration;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.jdbc.TransactionCallback;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Main workflow for migrating the database.
 *
 * @author Axel Fontaine
 */
public class DbMigrate {

    private static final Log LOG = LogFactory.getLog(DbMigrate.class);

    /**
     * The target version of the migration.
     */
    private final MigrationVersion target;

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
     * The connection to use.
     */
    private final Connection connectionMetaDataTable;

    /**
     * The connection to use to perform the actual database migrations.
     */
    private final Connection connectionUserObjects;

    /**
     * Flag whether to ignore failed future migrations or not.
     */
    private final boolean ignoreFailedFutureMigration;

    /**
     * Allows migrations to be run "out of order".
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     * <p>(default: {@code false})</p>
     */
    private final boolean outOfOrder;

    /**
     * This is a list of callbacks that fire before or after the migrate task is executed.
     * You can add as many callbacks as you want.  These should be set on the Flyway class
     * by the end user as Flyway will set them automatically for you here.
     */
    private final FlywayCallback[] callbacks;

    /**
     * The DB support for the user objects connection.
     */
    private final DbSupport dbSupportUserObjects;

    /**
     * Creates a new database migrator.
     *
     * @param connectionMetaDataTable     The connection to use.
     * @param connectionUserObjects       The connection to use to perform the actual database migrations.
     * @param dbSupport                   Database-specific functionality.
     * @param metaDataTable               The database metadata table.
     * @param migrationResolver           The migration resolver.
     * @param target                      The target version of the migration.
     * @param ignoreFailedFutureMigration Flag whether to ignore failed future migrations or not.
     * @param outOfOrder                  Allows migrations to be run "out of order".
     */
    public DbMigrate(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport,
                     MetaDataTable metaDataTable, Schema schema, MigrationResolver migrationResolver,
                     MigrationVersion target, boolean ignoreFailedFutureMigration, boolean outOfOrder,
                     FlywayCallback[] callbacks) {
        this.connectionMetaDataTable = connectionMetaDataTable;
        this.connectionUserObjects = connectionUserObjects;
        this.dbSupport = dbSupport;
        this.metaDataTable = metaDataTable;
        this.schema = schema;
        this.migrationResolver = migrationResolver;
        this.target = target;
        this.ignoreFailedFutureMigration = ignoreFailedFutureMigration;
        this.outOfOrder = outOfOrder;
        this.callbacks = callbacks;

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
            for (final FlywayCallback callback : callbacks) {
                new TransactionTemplate(connectionUserObjects).execute(new TransactionCallback<Object>() {
                    @Override
                    public Object doInTransaction() throws SQLException {
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
                MigrationVersion result = new TransactionTemplate(connectionMetaDataTable, false).execute(new TransactionCallback<MigrationVersion>() {
                    public MigrationVersion doInTransaction() {
                        metaDataTable.lock();

                        MigrationInfoServiceImpl infoService =
                                new MigrationInfoServiceImpl(migrationResolver, metaDataTable, target, outOfOrder, true);
                        infoService.refresh();

                        MigrationVersion currentSchemaVersion = MigrationVersion.EMPTY;
                        if (infoService.current() != null) {
                            currentSchemaVersion = infoService.current().getVersion();
                        }
                        if (firstRun) {
                            LOG.info("Current version of schema " + schema + ": " + currentSchemaVersion);

                            if (outOfOrder) {
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
                                LOG.warn("Schema " + schema + " has a version (" + currentSchemaVersion
                                        + ") that is newer than the latest available migration ("
                                        + resolved[resolved.length - 1].getVersion() + ") !");
                            }
                        }

                        MigrationInfo[] failed = infoService.failed();
                        if (failed.length > 0) {
                            if ((failed.length == 1)
                                    && (failed[0].getState() == MigrationState.FUTURE_FAILED)
                                    && ignoreFailedFutureMigration) {
                                LOG.warn("Schema " + schema + " contains a failed future migration to version " + failed[0].getVersion() + " !");
                            } else {
                                throw new FlywayException("Schema " + schema + " contains a failed migration to version " + failed[0].getVersion() + " !");
                            }
                        }

                        MigrationInfoImpl[] pendingMigrations = infoService.pending();

                        if (pendingMigrations.length == 0) {
                            return null;
                        }

                        boolean isOutOfOrder = pendingMigrations[0].getVersion().compareTo(currentSchemaVersion) < 0;
                        return applyMigration(pendingMigrations[0], isOutOfOrder);
                    }
                });
                if (result == null) {
                    // No further migrations available
                    break;
                }

                migrationSuccessCount++;
            }

            stopWatch.stop();

            logSummary(migrationSuccessCount, stopWatch.getTotalTimeMillis());

            for (final FlywayCallback callback : callbacks) {
                new TransactionTemplate(connectionUserObjects).execute(new TransactionCallback<Object>() {
                    @Override
                    public Object doInTransaction() throws SQLException {
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
    private MigrationVersion applyMigration(final MigrationInfoImpl migration, boolean isOutOfOrder) {
        MigrationVersion version = migration.getVersion();
        LOG.info("Migrating schema " + schema + " to version " + version + " - " + migration.getDescription() +
                (isOutOfOrder ? " (out of order)" : ""));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            for (final FlywayCallback callback : callbacks) {
                new TransactionTemplate(connectionUserObjects).execute(new TransactionCallback<Object>() {
                    @Override
                    public Object doInTransaction() throws SQLException {
                        dbSupportUserObjects.changeCurrentSchemaTo(schema);
                        callback.beforeEachMigrate(connectionUserObjects, migration);
                        return null;
                    }
                });
            }

            final MigrationExecutor migrationExecutor = migration.getResolvedMigration().getExecutor();
            if (migrationExecutor.executeInTransaction()) {
                new TransactionTemplate(connectionUserObjects).execute(new TransactionCallback<Void>() {
                    public Void doInTransaction() throws SQLException {
                        dbSupportUserObjects.changeCurrentSchemaTo(schema);
                        migrationExecutor.execute(connectionUserObjects);
                        return null;
                    }
                });
            } else {
                try {
                    dbSupportUserObjects.changeCurrentSchemaTo(schema);
                    migrationExecutor.execute(connectionUserObjects);
                } catch (SQLException e) {
                    throw new FlywayException("Unable to apply migration", e);
                }
            }
            LOG.debug("Successfully completed and committed migration of schema " + schema + " to version " + version);

            for (final FlywayCallback callback : callbacks) {
                new TransactionTemplate(connectionUserObjects).execute(new TransactionCallback<Object>() {
                    @Override
                    public Object doInTransaction() throws SQLException {
                        dbSupportUserObjects.changeCurrentSchemaTo(schema);
                        callback.afterEachMigrate(connectionUserObjects, migration);
                        return null;
                    }
                });
            }
        } catch (FlywayException e) {
            String failedMsg = "Migration of schema " + schema + " to version " + version + " failed!";
            if (dbSupport.supportsDdlTransactions()) {
                LOG.error(failedMsg + " Changes successfully rolled back.");
            } else {
                LOG.error(failedMsg + " Please restore backups and roll back database and code!");

                stopWatch.stop();
                int executionTime = (int) stopWatch.getTotalTimeMillis();
                AppliedMigration appliedMigration = new AppliedMigration(version, migration.getDescription(),
                        migration.getType(), migration.getScript(), migration.getChecksum(), executionTime, false);
                metaDataTable.addAppliedMigration(appliedMigration);
            }
            throw e;
        }

        stopWatch.stop();
        int executionTime = (int) stopWatch.getTotalTimeMillis();

        AppliedMigration appliedMigration = new AppliedMigration(version, migration.getDescription(),
                migration.getType(), migration.getScript(), migration.getChecksum(), executionTime, true);
        metaDataTable.addAppliedMigration(appliedMigration);

        return version;
    }
}
