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

package com.google.code.flyway.core;

import com.google.code.flyway.core.dbsupport.DbSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
     * The available migration resolvers.
     */
    private final Collection<MigrationResolver> migrationResolvers;

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
     * Creates a new database migration.
     *
     * @param transactionTemplate The transaction template to use.
     * @param jdbcTemplate        JdbcTemplate with ddl manipulation access to the
     *                            database.
     * @param dbSupport           Database-specific functionality.
     * @param migrationResolvers  The migration. resolvers
     * @param metaDataTable       The database metadata table.
     */
    public DbMigrator(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, DbSupport dbSupport,
                      Collection<MigrationResolver> migrationResolvers, MetaDataTable metaDataTable) {
        this.transactionTemplate = transactionTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.dbSupport = dbSupport;
        this.migrationResolvers = migrationResolvers;
        this.metaDataTable = metaDataTable;
    }

    /**
     * Starts the actual migration.
     *
     * @return The number of successfully applied migrations.
     * @throws Exception Thrown when a migration failed.
     */
    public int migrate() throws Exception {
        if (!metaDataTable.exists()) {
            metaDataTable.create();
        }

        final List<Migration> allMigrations = findAvailableMigrations();

        int migrationSuccessCount = 0;
        while (true) {
            int result = transactionTemplate.execute(new TransactionCallback<Integer>() {
                @Override
                public Integer doInTransaction(TransactionStatus status) {
                    metaDataTable.lock();

                    Migration latestAppliedMigration = metaDataTable.latestAppliedMigration();
                    LOG.info("Current schema version: " + latestAppliedMigration.getVersion());

                    latestAppliedMigration.assertNotFailed();

                    Migration migration = getNextMigration(allMigrations, latestAppliedMigration.getVersion());
                    if (migration == null) {
                        LOG.info("Schema is up to date. No migration necessary.");
                        return 0;
                    }

                    LOG.info("Migrating to version " + migration.getVersion() + " - " + migration.getScriptName());
                    migration.migrate(transactionTemplate, jdbcTemplate, dbSupport);

                    if (MigrationState.FAILED.equals(migration.getState()) && dbSupport.supportsDdlTransactions()) {
                        throw new IllegalStateException("Migration failed! Changes rolled back. Aborting!");
                    }

                    metaDataTable.migrationFinished(migration);

                    migration.assertNotFailed();

                    return 1;
                }
            });

            if (result == 0) {
                break;
            }
            migrationSuccessCount += result;
        }

        if (migrationSuccessCount == 0) {
            LOG.info("Migration completed.");
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

    /**
     * Finds all available migrations using all migration resolvers (sql, java,
     * ...).
     *
     * @return The available migrations, sorted by version, newest first. An
     *         empty list is returned when no migrations can be found.
     */
    private List<Migration> findAvailableMigrations() {
        List<Migration> allMigrations = new ArrayList<Migration>();
        for (MigrationResolver migrationResolver : migrationResolvers) {
            allMigrations.addAll(migrationResolver.resolvesMigrations());
        }

        if (allMigrations.isEmpty()) {
            LOG.warn("No migrations found!");
            return allMigrations;
        }

        Collections.sort(allMigrations, new Comparator<Migration>() {
            @Override
            public int compare(Migration o1, Migration o2) {
                // newest migration first
                return o2.getVersion().compareTo(o1.getVersion());
			}
		});
		return allMigrations;
	}
}
