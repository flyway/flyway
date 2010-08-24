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

package com.googlecode.flyway.core.runtime;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationState;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.util.TimeFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

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
     * The available migrations.
     */
    private final List<Migration> migrations;

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
     * @param migrations  The available migrations.
     * @param metaDataTable       The database metadata table.
     */
    public DbMigrator(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, DbSupport dbSupport,
                      List<Migration> migrations, MetaDataTable metaDataTable) {
        this.transactionTemplate = transactionTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.dbSupport = dbSupport;
        this.migrations = migrations;
        this.metaDataTable = metaDataTable;
    }

    /**
     * Starts the actual migration.
     *
     * @return The number of successfully applied migrations.
     * @throws Exception Thrown when a migration failed.
     */
    public int migrate() throws Exception {
        if (migrations.isEmpty()) {
            LOG.info("No migrations found");
            return 0;
        }

        int migrationSuccessCount = 0;
        while (true) {
            Migration appliedMigration = (Migration) transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Migration doInTransaction(TransactionStatus status) {
                    metaDataTable.lock();

                    Migration latestAppliedMigration = metaDataTable.latestAppliedMigration();
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

                    LOG.info("Migrating to version " + migration.getVersion() + " - " + migration.getScript());
                    migration.migrate(transactionTemplate, jdbcTemplate, dbSupport);

                    if (MigrationState.FAILED.equals(migration.getState()) && dbSupport.supportsDdlTransactions()) {
                        throw new IllegalStateException("Migration failed! Changes rolled back. Aborting!");
                    }
                    LOG.info(String.format("Finished migrating to version %s - %s (execution time %s)",
                            migration.getVersion(), migration.getScript(), TimeFormat.format(migration.getExecutionTime())));

                    metaDataTable.finishMigration(migration);

                    return migration;
                }
            });

            if (appliedMigration == null) {
                break;
            }

            appliedMigration.assertNotFailed();

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

}
