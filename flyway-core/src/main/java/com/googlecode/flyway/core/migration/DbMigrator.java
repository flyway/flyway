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
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.migration.init.InitMigration;
import com.googlecode.flyway.core.util.ExceptionUtils;
import com.googlecode.flyway.core.util.TimeFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
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
     * The target version of the migration, default is the latest version.
     */
    private final SchemaVersion targetVersion = SchemaVersion.LATEST;

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
     * Creates a new database migrator.
     *
     * @param transactionTemplate The transaction template to use.
     * @param jdbcTemplate        JdbcTemplate with ddl manipulation access to the
     *                            database.
     * @param dbSupport           Database-specific functionality.
     * @param metaDataTable       The database metadata table.
     */
    public DbMigrator(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, DbSupport dbSupport,
                      MetaDataTable metaDataTable) {
        this.transactionTemplate = transactionTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.dbSupport = dbSupport;
        this.metaDataTable = metaDataTable;
    }

    /**
     * Initializes the metadata table with this version.
     *
     * @param schemaVersion The version to initialize the metadata table with.
     */
    public void init(SchemaVersion schemaVersion) {
        if (metaDataTable.hasRows()) {
            throw new IllegalStateException(
                    "Schema already initialized. Current Version: " + metaDataTable.latestAppliedMigration().getVersion());
        }

        metaDataTable.createIfNotExists();

        final Migration initialMigration = new InitMigration(schemaVersion);

        final MetaDataTableRow metaDataTableRow = new MetaDataTableRow(initialMigration);
        metaDataTableRow.update(0, MigrationState.SUCCESS);

        transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Void doInTransaction(TransactionStatus status) {
                metaDataTable.insert(metaDataTableRow);
                return null;
            }
        });

        LOG.info("Schema initialized with version: " + schemaVersion);
    }

    /**
     * Starts the actual migration.
     *
     * @param migrations          The available migrations.

     * @return The number of successfully applied migrations.
     * @throws Exception Thrown when a migration failed.
     */
    public int migrate(final List<Migration> migrations) throws Exception {
        if (migrations.isEmpty()) {
            LOG.info("No migrations found");
            return 0;
        }

        int migrationSuccessCount = 0;
        while (true) {
            MetaDataTableRow metaDataTableRow = (MetaDataTableRow) transactionTemplate.execute(new TransactionCallback() {
                @Override
                public MetaDataTableRow doInTransaction(TransactionStatus status) {
                    metaDataTable.lock();

                    MetaDataTableRow latestAppliedMigration = metaDataTable.latestAppliedMigration();
                    SchemaVersion currentSchemaVersion;
                    if (latestAppliedMigration == null) {
                        currentSchemaVersion = SchemaVersion.EMPTY;
                    } else {
                        latestAppliedMigration.assertNotFailed();
                        currentSchemaVersion = latestAppliedMigration.getVersion();
                    }

                    LOG.info("Current schema version: " + currentSchemaVersion);

                    Migration migration = getNextMigration(migrations, currentSchemaVersion);
                    if (migration == null) {
                        return null;
                    }

                    return applyMigration(migration, transactionTemplate, jdbcTemplate, dbSupport);
                }
            });

            if (metaDataTableRow == null) {
                break;
            }

            metaDataTableRow.assertNotFailed();

            migrationSuccessCount++;
        }

        if (migrationSuccessCount == 0) {
            LOG.info("Schema is up to date. No migration necessary.");
        } else if (migrationSuccessCount == 1) {
            LOG.info("Migration completed. Successfully applied 1 migration.");
        } else {
            LOG.info("Migration completed. Successfully applied " + migrationSuccessCount + " migrations.");
        }

        return migrationSuccessCount;
    }

    /**
     * Applies this migration to the database. The migration state and the execution time are
     * updated accordingly.
     *
     * @param migration           The migration to apply.
     * @param transactionTemplate The transaction template to use.
     * @param jdbcTemplate        To execute the migration statements.
     * @param dbSupport           The support for database-specific extensions.
     * @return The row that was added to the metadata table.
     */
    public final MetaDataTableRow applyMigration(final Migration migration, final TransactionTemplate transactionTemplate, final JdbcTemplate jdbcTemplate, final DbSupport dbSupport) {
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
                        LOG.error(rootCause.toString());
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
            throw new IllegalStateException("Migration failed! Changes rolled back. Aborting!");
        }
        LOG.info(String.format("Finished migrating to version %s (execution time %s)",
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
     * @return The next migration to apply.
     */
    private Migration getNextMigration(List<Migration> allMigrations, SchemaVersion currentVersion) {
        SchemaVersion newestMigrationVersion = allMigrations.get(0).getVersion();
        if (newestMigrationVersion.compareTo(currentVersion) < 0) {
            LOG.warn("Database version (" + currentVersion.getVersion() + ") is newer than the latest migration ("
                    + newestMigrationVersion + ") !");
            return null;
        }

        Migration nextMigration = null;
        for (Migration migration : allMigrations) {
            if ((migration.getVersion().compareTo(currentVersion) > 0)
                    && (migration.getVersion().compareTo(targetVersion) <= 0)) {
                nextMigration = migration;
            } else {
                return nextMigration;
            }
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
