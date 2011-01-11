/**
 * Copyright (C) 2009-2010 the original author or authors.
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

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.util.ExceptionUtils;
import com.googlecode.flyway.core.util.TimeFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;

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
    private final SchemaVersion target;

    /**
     * Database-specific functionality.
     */
    private final DbSupport dbSupport;

    /**
     * The database metadata table.
     */
    private final MetaDataTable metaDataTable;

    /**
     * The transaction template to use.
     */
    private final TransactionTemplate transactionTemplate;

    /**
     * JdbcTemplate with ddl manipulation access to the database.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Flag whether to ignore failed future migrations or not.
     */
    private final boolean ignoreFailedFutureMigration;

    /**
     * Creates a new database migrator.
     *
     * @param transactionTemplate         The transaction template to use.
     * @param jdbcTemplate                JdbcTemplate with ddl manipulation access to the database.
     * @param dbSupport                   Database-specific functionality.
     * @param metaDataTable               The database metadata table.
     * @param target                      The target version of the migration.
     * @param ignoreFailedFutureMigration Flag whether to ignore failed future migrations or not.
     */
    public DbMigrator(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, DbSupport dbSupport,
                      MetaDataTable metaDataTable, SchemaVersion target, boolean ignoreFailedFutureMigration) {
        this.transactionTemplate = transactionTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.dbSupport = dbSupport;
        this.metaDataTable = metaDataTable;
        this.target = target;
        this.ignoreFailedFutureMigration = ignoreFailedFutureMigration;
    }

    /**
     * Starts the actual migration.
     *
     * @param migrations The available migrations.
     *
     * @return The number of successfully applied migrations.
     *
     * @throws FlywayException when migration failed.
     */
    public int migrate(final List<Migration> migrations) throws FlywayException {
        if (migrations.isEmpty()) {
            LOG.info("No migrations found");
            return 0;
        }

        int migrationSuccessCount = 0;
        try {
            while (true) {
                final boolean firstRun = migrationSuccessCount == 0;
                MetaDataTableRow metaDataTableRow = (MetaDataTableRow) transactionTemplate.execute(new TransactionCallback() {
                    @Override
                    public MetaDataTableRow doInTransaction(TransactionStatus status) {
                        metaDataTable.lock();

                        SchemaVersion currentSchemaVersion = metaDataTable.getCurrentSchemaVersion();
                        if (firstRun) {
                            LOG.info("Current schema version: " + currentSchemaVersion);
                        }

                        SchemaVersion latestAvailableMigrationVersion = migrations.get(0).getVersion();
                        boolean isFutureMigration = latestAvailableMigrationVersion.compareTo(currentSchemaVersion) < 0;
                        if (isFutureMigration) {
                            LOG.warn("Database version (" + currentSchemaVersion + ") is newer than the latest available migration ("
                                    + latestAvailableMigrationVersion + ") !");
                        }

                        MigrationState currentSchemaState = metaDataTable.getCurrentSchemaState();
                        if (currentSchemaState == MigrationState.FAILED) {
                            if (isFutureMigration && ignoreFailedFutureMigration) {
                                LOG.warn("Detected failed migration to version " + currentSchemaVersion + " !");
                            } else {
                                throw new MigrationException(currentSchemaVersion, false);
                            }
                        }

                        if (isFutureMigration) {
                            return null;
                        }

                        Migration migration = getNextMigration(migrations, currentSchemaVersion);
                        if (migration == null) {
                            // No further migrations available
                            return null;
                        }

                        return applyMigration(migration);
                    }
                });

                if (metaDataTableRow == null) {
                    // No further migrations available
                    break;
                }

                if (MigrationState.FAILED == metaDataTableRow.getState()) {
                    throw new MigrationException(metaDataTableRow.getVersion(), false);
                }

                migrationSuccessCount++;
            }
        } catch (TransactionException e) {
            throw new FlywayException("Migration failed !", e);
        }

        logSummary(migrationSuccessCount);
        return migrationSuccessCount;
    }

    /**
     * Logs the summary of this migration run.
     *
     * @param migrationSuccessCount The number of successfully applied migrations.
     */
    private void logSummary(int migrationSuccessCount) {
        if (migrationSuccessCount == 0) {
            LOG.info("Schema is up to date. No migration necessary.");
        } else if (migrationSuccessCount == 1) {
            LOG.info("Migration completed. Successfully applied 1 migration.");
        } else {
            LOG.info("Migration completed. Successfully applied " + migrationSuccessCount + " migrations.");
        }
    }

    /**
     * Applies this migration to the database. The migration state and the execution time are updated accordingly.
     *
     * @param migration The migration to apply.
     *
     * @return The row that was added to the metadata table.
     *
     * @throws MigrationException when the migration failed.
     */
    public final MetaDataTableRow applyMigration(final Migration migration) throws MigrationException {
        MetaDataTableRow metaDataTableRow = new MetaDataTableRow(migration);

        LOG.info("Migrating to version " + migration.getVersion());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        MigrationRunnable migrationRunnable = new MigrationRunnable() {
            @Override
            public void run() {
                try {
                    transactionTemplate.execute(new TransactionCallback() {
                        @Override
                        public Void doInTransaction(TransactionStatus status) {
                            migration.migrate(jdbcTemplate, dbSupport);
                            return null;
                        }
                    });
                    state = MigrationState.SUCCESS;
                } catch (Exception e) {
                    LOG.error(e.toString());
                    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
                    Throwable rootCause = ExceptionUtils.getRootCause(e);
                    if (rootCause != null) {
                        LOG.error("Caused by " + rootCause.toString());
                    }
                    state = MigrationState.FAILED;
                }
            }
        };
        Thread migrationThread = new Thread(migrationRunnable, "Flyway Migration");
        migrationThread.start();
        try {
            migrationThread.join();
        } catch (InterruptedException e) {
            // Ignore
        }
        stopWatch.stop();
        int executionTime = (int) stopWatch.getLastTaskTimeMillis();

        if (MigrationState.FAILED.equals(migrationRunnable.state) && dbSupport.supportsDdlTransactions()) {
            throw new MigrationException(migration.getVersion(), true);
        }
        LOG.debug(String.format("Finished migrating to version %s (execution time %s)",
                migration.getVersion(), TimeFormat.format(executionTime)));

        metaDataTableRow.update(executionTime, migrationRunnable.state);
        metaDataTable.insert(metaDataTableRow);

        return metaDataTableRow;
    }

    /**
     * Returns the next migration to apply.
     *
     * @param currentVersion The current version of the schema.
     * @param allMigrations  All available migrations, sorted by version, newest first.
     *
     * @return The next migration to apply.
     */
    private Migration getNextMigration(List<Migration> allMigrations, SchemaVersion currentVersion) {
        if (target.compareTo(currentVersion) < 0) {
            LOG.warn("Database version (" + currentVersion + ") is newer than the target version ("
                    + target + ") !");
            return null;
        }

        Migration nextMigration = null;
        for (Migration migration : allMigrations) {
            if ((migration.getVersion().compareTo(currentVersion) > 0)) {
                nextMigration = migration;
            } else {
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

    /**
     * Runnable for migrations to lets you determine determine the final state of the migration.
     */
    private static abstract class MigrationRunnable implements Runnable {
        /**
         * The final state of the migration.
         */
        protected MigrationState state;
    }
}
