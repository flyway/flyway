/**
 * Copyright 2010-2016 Boxfuse GmbH
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
import org.flywaydb.core.internal.batch.MigrationBatchService;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.batch.MigrationBatchResult;
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

    private final MigrationBatchService migrationBatchService;

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
     * Flag whether to ignore future migrations or not.
     */
    private final boolean ignoreFutureMigrations;

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
     * @param migrationBatchService       The migration batch service to use
     * @param connectionMetaDataTable     The connection to use.
     * @param connectionUserObjects       The connection to use to perform the actual database migrations.
     * @param dbSupport                   Database-specific functionality.
     * @param metaDataTable               The database metadata table.
     * @param migrationResolver           The migration resolver.
     * @param target                      The target version of the migration.
     * @param ignoreFutureMigrations      Flag whether to ignore future migrations or not.
     * @param ignoreFailedFutureMigration Flag whether to ignore failed future migrations or not.
     * @param outOfOrder                  Allows migrations to be run "out of order".
     */
    public DbMigrate(MigrationBatchService migrationBatchService,
                     Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport,
                     MetaDataTable metaDataTable, Schema schema, MigrationResolver migrationResolver,
                     MigrationVersion target, boolean ignoreFutureMigrations, boolean ignoreFailedFutureMigration, boolean outOfOrder,
                     FlywayCallback[] callbacks) {
        this.migrationBatchService = migrationBatchService;
        this.connectionMetaDataTable = connectionMetaDataTable;
        this.connectionUserObjects = connectionUserObjects;
        this.dbSupport = dbSupport;
        this.metaDataTable = metaDataTable;
        this.schema = schema;
        this.migrationResolver = migrationResolver;
        this.target = target;
        this.ignoreFutureMigrations = ignoreFutureMigrations;
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
                MigrationBatchResult result = new TransactionTemplate(connectionMetaDataTable, false).execute(new TransactionCallback<MigrationBatchResult>() {
                    public MigrationBatchResult doInTransaction() {
                        return migrateBatch();
                    }
                });
                if (result.isDone()) {
                    // No further migrations available
                    break;
                }
                migrationSuccessCount += result.getNumberOfAppliedMigrations();
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
     *
     * @return
     */
    private MigrationBatchResult migrateBatch(){
        metaDataTable.lock();

        final MigrationBatchResult result = new MigrationBatchResult();
        Integer migrationBatchSuccessCount = new TransactionTemplate(connectionUserObjects).execute(new TransactionCallback<Integer>() {
            @Override
            public Integer doInTransaction() throws SQLException {
                int migrationBatchSuccessCount = 0;
                while(true){
                    MigrationInfoServiceImpl infoService =
                            new MigrationInfoServiceImpl(migrationResolver, metaDataTable, target, outOfOrder, true, true);
                    infoService.refresh();

                    MigrationVersion currentSchemaVersion = MigrationVersion.EMPTY;
                    if (infoService.current() != null) {
                        currentSchemaVersion = infoService.current().getVersion();
                    }
                    if (migrationBatchSuccessCount == 0) {
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
                                && (ignoreFutureMigrations || ignoreFailedFutureMigration)) {
                            LOG.warn("Schema " + schema + " contains a failed future migration to version " + failed[0].getVersion() + " !");
                        } else {
                            throw new FlywayException("Schema " + schema + " contains a failed migration to version " + failed[0].getVersion() + " !");
                        }
                    }

                    MigrationInfoImpl[] pendingMigrations = infoService.pending();

                    if (pendingMigrations.length == 0) {
                        result.setDone(true);
                        break;
                    }

                    boolean isOutOfOrder = pendingMigrations[0].getVersion() != null
                            && pendingMigrations[0].getVersion().compareTo(currentSchemaVersion) < 0;
                    MigrationInfoImpl migrationInfo = pendingMigrations[0];
                    applyMigration(migrationInfo, isOutOfOrder);
                    migrationBatchSuccessCount++;
                    if(migrationBatchService.isLastOfBatch(dbSupport, migrationInfo)){
                        result.setDone(false);
                        break;
                    }
                }
                return migrationBatchSuccessCount;
            }
        });

        result.setNumberOfAppliedMigrations(migrationBatchSuccessCount);
        return result;
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
     */
    private void applyMigration(final MigrationInfoImpl migration, boolean isOutOfOrder) {
        MigrationVersion version = migration.getVersion();
        final String migrationText;
        if (version != null) {
            migrationText = "schema " + schema + " to version " + version + " - " + migration.getDescription() +
                    (isOutOfOrder ? " (out of order)" : "");
        } else {
            migrationText = "schema " + schema + " with repeatable migration " + migration.getDescription();
        }
        LOG.info("Migrating " + migrationText);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            final MigrationExecutor migrationExecutor = migration.getResolvedMigration().getExecutor();
            try {
                doMigrate(migration, migrationExecutor, migrationText);
            } catch (SQLException e) {
                throw new FlywayException("Unable to apply migration", e);
            }
        } catch (FlywayException e) {
            String failedMsg = "Migration of " + migrationText + " failed!";
            if (dbSupport.supportsDdlTransactions()) {
                LOG.error(failedMsg);
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
    }

    private void doMigrate(MigrationInfoImpl migration, MigrationExecutor migrationExecutor, String migrationText) throws SQLException {
        dbSupportUserObjects.changeCurrentSchemaTo(schema);

        for (final FlywayCallback callback : callbacks) {
            callback.beforeEachMigrate(connectionUserObjects, migration);
        }

        migrationExecutor.execute(connectionUserObjects);
        LOG.debug("Successfully completed migration of " + migrationText);

        for (final FlywayCallback callback : callbacks) {
            callback.afterEachMigrate(connectionUserObjects, migration);
        }
    }
}
