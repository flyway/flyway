/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.migration;

import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.util.ExceptionUtils;
import com.googlecode.flyway.core.util.StopWatch;
import com.googlecode.flyway.core.util.TimeFormat;
import com.googlecode.flyway.core.util.jdbc.JdbcTemplate;
import com.googlecode.flyway.core.util.jdbc.TransactionCallback;
import com.googlecode.flyway.core.util.jdbc.TransactionException;
import com.googlecode.flyway.core.util.jdbc.TransactionTemplate;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

/**
 * Main workflow for migrating the database.
 *
 * @author Axel Fontaine
 */
public class DbMigrator {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(DbMigrator.class);

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
     * The connection to use.
     */
    private final Connection connection;

    /**
     * The connection to use to perform the actual database migrations.
     */
    private final Connection connectionForMigrations;

    /**
     * Flag whether to ignore failed future migrations or not.
     */
    private final boolean ignoreFailedFutureMigration;

    /**
     * Creates a new database migrator.
     *
     * @param connection                  The connection to use.
     * @param connectionForMigrations     The connection to use to perform the actual database migrations.
     * @param dbSupport                   Database-specific functionality.
     * @param metaDataTable               The database metadata table.
     * @param target                      The target version of the migration.
     * @param ignoreFailedFutureMigration Flag whether to ignore failed future migrations or not.
     */
    public DbMigrator(Connection connection, Connection connectionForMigrations, DbSupport dbSupport,
                      MetaDataTable metaDataTable, MigrationVersion target, boolean ignoreFailedFutureMigration) {
        this.connection = connection;
        this.connectionForMigrations = connectionForMigrations;
        this.dbSupport = dbSupport;
        this.metaDataTable = metaDataTable;
        this.target = target;
        this.ignoreFailedFutureMigration = ignoreFailedFutureMigration;
    }

    /**
     * Starts the actual migration.
     *
     * @param migrations The available migrations.
     * @return The number of successfully applied migrations.
     * @throws FlywayException when migration failed.
     */
    public int migrate(final List<MigrationInfoImpl> migrations) throws FlywayException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int migrationSuccessCount = 0;
        try {
            while (true) {
                final boolean firstRun = migrationSuccessCount == 0;
                MigrationInfo migrationInfo =
                        new TransactionTemplate(connection).execute(new TransactionCallback<MigrationInfo>() {
                            public MigrationInfo doInTransaction() {
                                metaDataTable.lock();

                                MigrationVersion currentSchemaVersion = metaDataTable.getCurrentSchemaVersion();
                                if (firstRun) {
                                    LOG.info("Current schema version: " + currentSchemaVersion);
                                }

                                MigrationInfoImpl latestAvailableMigration = migrations.get(migrations.size() - 1);
                                MigrationVersion latestAvailableMigrationVersion = latestAvailableMigration.getVersion();
                                boolean isFutureMigration = latestAvailableMigrationVersion.compareTo(currentSchemaVersion) < 0;
                                if (isFutureMigration) {
                                    LOG.warn("Database version (" + currentSchemaVersion + ") is newer than the latest available migration ("
                                            + latestAvailableMigrationVersion + ") !");
                                }

                                com.googlecode.flyway.core.api.MigrationState currentSchemaState = metaDataTable.getCurrentSchemaState();
                                if (currentSchemaState == com.googlecode.flyway.core.api.MigrationState.FAILED) {
                                    if (isFutureMigration && ignoreFailedFutureMigration) {
                                        LOG.warn("Detected failed migration to version " + currentSchemaVersion + " !");
                                    } else {
                                        throw new MigrationException(currentSchemaVersion, false);
                                    }
                                }

                                if (isFutureMigration) {
                                    return null;
                                }

                                MigrationInfoImpl migration = getNextMigration(migrations, currentSchemaVersion);
                                if (migration == null) {
                                    // No further migrations available
                                    return null;
                                }

                                return applyMigration(migration);
                            }
                        });

                if (migrationInfo == null) {
                    // No further migrations available
                    break;
                }

                if (com.googlecode.flyway.core.api.MigrationState.FAILED == migrationInfo.getState()) {
                    throw new MigrationException(migrationInfo.getVersion(), false);
                }

                migrationSuccessCount++;
            }
        } catch (TransactionException e) {
            throw new FlywayException("Migration failed !", e);
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
            LOG.info("Schema is up to date. No migration necessary.");
            return;
        }

        String executionTimeStr = "(execution time " + TimeFormat.format(executionTime) + ").";

        if (migrationSuccessCount == 1) {
            LOG.info("Successfully applied 1 migration " + executionTimeStr);
        } else {
            LOG.info("Successfully applied " + migrationSuccessCount + " migrations " + executionTimeStr);
        }
    }

    /**
     * Applies this migration to the database. The migration state and the execution time are updated accordingly.
     *
     * @param migration The migration to apply.
     * @return The row that was added to the metadata table.
     * @throws MigrationException when the migration failed.
     */
    private MigrationInfo applyMigration(final MigrationInfoImpl migration) throws MigrationException {
        MigrationVersion version = migration.getVersion();
        LOG.info("Migrating to version " + version);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        com.googlecode.flyway.core.api.MigrationState state;
        try {
            final JdbcTemplate jdbcTemplate = new JdbcTemplate(connectionForMigrations);
            new TransactionTemplate(connectionForMigrations).execute(new TransactionCallback<Void>() {
                public Void doInTransaction() {
                    migration.getExecutor().execute(jdbcTemplate, dbSupport);
                    return null;
                }
            });
            LOG.debug("Successfully completed and committed DB migration to version " + version);
            state = com.googlecode.flyway.core.api.MigrationState.SUCCESS;
        } catch (Exception e) {
            LOG.error(e.toString());

            @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause != null) {
                LOG.error("Caused by " + rootCause.toString());
            }
            state = com.googlecode.flyway.core.api.MigrationState.FAILED;
        }

        stopWatch.stop();
        int executionTime = (int) stopWatch.getTotalTimeMillis();

        if (com.googlecode.flyway.core.api.MigrationState.FAILED.equals(state) && dbSupport.supportsDdlTransactions()) {
            throw new MigrationException(version, true);
        }
        LOG.debug(String.format("Finished migrating to version %s (execution time %s)",
                version, TimeFormat.format(executionTime)));

        migration.setInstalledOn(new Date());
        migration.setExecutionTime(executionTime);
        migration.setState(state);
        metaDataTable.insert(migration);
        LOG.debug("MetaData table successfully updated to reflect changes");

        return migration;
    }

    /**
     * Returns the next migration to apply.
     *
     * @param currentVersion The current version of the schema.
     * @param allMigrations  All available migrations, sorted by version, newest first.
     * @return The next migration to apply.
     */
    private MigrationInfoImpl getNextMigration(List<MigrationInfoImpl> allMigrations, MigrationVersion currentVersion) {
        if (target.compareTo(currentVersion) < 0) {
            LOG.warn("Database version (" + currentVersion + ") is newer than the target version ("
                    + target + ") !");
            return null;
        }

        MigrationInfoImpl nextMigration = null;
        for (MigrationInfoImpl migration : allMigrations) {
            if ((migration.getVersion().compareTo(currentVersion) > 0)) {
                nextMigration = migration;
                break;
            }
        }

        if (nextMigration == null) {
            return null;
        }

        if (target.compareTo(nextMigration.getVersion()) < 0) {
            return null;
        }

        return nextMigration;
    }
}
