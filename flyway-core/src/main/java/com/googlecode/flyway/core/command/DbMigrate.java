/**
 * Copyright (C) 2010-2013 the original author or authors.
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
package com.googlecode.flyway.core.command;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.api.MigrationResult;
import com.googlecode.flyway.core.api.MigrationState;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.info.MigrationInfoImpl;
import com.googlecode.flyway.core.info.MigrationInfoServiceImpl;
import com.googlecode.flyway.core.metadatatable.AppliedMigration;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.resolver.MigrationResolver;
import com.googlecode.flyway.core.resolver.ResolvedMigration;
import com.googlecode.flyway.core.util.ExceptionUtils;
import com.googlecode.flyway.core.util.StopWatch;
import com.googlecode.flyway.core.util.TimeFormat;
import com.googlecode.flyway.core.util.jdbc.TransactionCallback;
import com.googlecode.flyway.core.util.jdbc.TransactionTemplate;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.Connection;

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
    private boolean outOfOrder;

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
                     MigrationVersion target, boolean ignoreFailedFutureMigration, boolean outOfOrder) {
        this.connectionMetaDataTable = connectionMetaDataTable;
        this.connectionUserObjects = connectionUserObjects;
        this.dbSupport = dbSupport;
        this.metaDataTable = metaDataTable;
        this.schema = schema;
        this.migrationResolver = migrationResolver;
        this.target = target;
        this.ignoreFailedFutureMigration = ignoreFailedFutureMigration;
        this.outOfOrder = outOfOrder;
    }

    /**
     * Starts the actual migration.
     *
     * @return The number of successfully applied migrations.
     * @throws FlywayException when migration failed.
     */
    public int migrate() throws FlywayException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int migrationSuccessCount = 0;
        while (true) {
            final boolean firstRun = migrationSuccessCount == 0;
            final MigrationResult result =
                    new TransactionTemplate(connectionMetaDataTable).execute(new TransactionCallback<MigrationResult>() {
                        public MigrationResult doInTransaction() {
                            metaDataTable.lock();

                            MigrationInfoServiceImpl infoService =
                                    new MigrationInfoServiceImpl(migrationResolver, metaDataTable, target, outOfOrder);
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
                                    return MigrationResult.createFailed(failed[0].getVersion(), null);
                                }
                            }

                            MigrationInfoImpl[] pendingMigrations = infoService.pending();

                            if (pendingMigrations.length == 0) {
                                return null;
                            }

                            boolean isOutOfOrder = pendingMigrations[0].getVersion().compareTo(currentSchemaVersion) < 0;
                            return applyMigration(pendingMigrations[0].getResolvedMigration(), isOutOfOrder);
                        }
                    });

            if (result == null) {
                // No further migrations available
                break;
            }

            if (!result.isSuccess()) {
                throw new FlywayException("Migration of schema " + schema + " to version " + result.getMigrationVersion() + " failed! Please restore backups and roll back database and code!", result.getErrorCause());
            }

            migrationSuccessCount++;
        }

        stopWatch.stop();

        logSummary(migrationSuccessCount, stopWatch.getTotalTimeMillis());
        return migrationSuccessCount;
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
    private MigrationResult applyMigration(final ResolvedMigration migration, boolean isOutOfOrder) {
        MigrationVersion version = migration.getVersion();
        if (isOutOfOrder) {
            LOG.info("Migrating schema " + schema + " to version " + version + " (out of order)");
        } else {
            LOG.info("Migrating schema " + schema + " to version " + version);
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        MigrationResult migrationResult;
        try {
            new TransactionTemplate(connectionUserObjects).execute(new TransactionCallback<Void>() {
                public Void doInTransaction() {
                    migration.getExecutor().execute(new JdbcTemplate(connectionUserObjects, 0), dbSupport);
                    return null;
                }
            });
            LOG.debug("Successfully completed and committed migration of schema " + schema + " to version " + version);
            migrationResult = MigrationResult.createSuccess(version);
        } catch (Exception e) {
            LOG.error(e.toString());

            @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause != null) {
                LOG.error("Caused by " + rootCause.toString());
            }
            migrationResult = MigrationResult.createFailed(version, rootCause);
        }

        stopWatch.stop();
        int executionTime = (int) stopWatch.getTotalTimeMillis();

        if (migrationResult.isSuccess() && dbSupport.supportsDdlTransactions()) {
            throw new FlywayException("Migration of schema " + schema + " to version " + version + " failed! Changes successfully rolled back.");
        }
        LOG.debug(String.format("Finished migrating schema %s to version %s (execution time %s)",
                schema, version, TimeFormat.format(executionTime)));

        AppliedMigration appliedMigration = new AppliedMigration(version, migration.getDescription(),
                migration.getType(), migration.getScript(), migration.getChecksum(), executionTime, migrationResult.isSuccess());
        metaDataTable.addAppliedMigration(appliedMigration);

        return migrationResult;
    }
}
