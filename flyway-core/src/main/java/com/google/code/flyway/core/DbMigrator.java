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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
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
    private static final Log log = LogFactory.getLog(DbMigrator.class);

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
    private TransactionTemplate transactionTemplate;

    /**
     * SimpleJdbcTemplate with ddl manipulation access to the database.
     */
    private SimpleJdbcTemplate jdbcTemplate;

    /**
     * Creates a new database migration.
     *
     * @param transactionTemplate The transaction template to use.
     * @param jdbcTemplate        SimpleJdbcTemplate with ddl manipulation access to the database.
     * @param dbSupport           Database-specific functionality.
     * @param migrationResolvers  The migration. resolvers
     * @param metaDataTable       The database metadata table.
     */
    public DbMigrator(TransactionTemplate transactionTemplate, SimpleJdbcTemplate jdbcTemplate, DbSupport dbSupport,
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

        Migration latestAppliedMigration = metaDataTable.latestAppliedMigration();
        log.info("Current schema version: " + latestAppliedMigration.getVersion());

        if (MigrationState.FAILED.equals(latestAppliedMigration.getState())) {
            throw new IllegalStateException("A previous migration failed! Please restore backups and roll back database!");
        }

        List<Migration> pendingMigrations = getPendingMigrations(latestAppliedMigration.getVersion());
        if (pendingMigrations.isEmpty()) {
            log.info("Schema is up to date. No migration necessary.");
            return 0;
        }

        for (Migration pendingMigration : pendingMigrations) {
            log.debug("Pending migration: " + pendingMigration.getVersion() + " - " + pendingMigration.getScriptName());
        }

        log.debug("Starting migration...");
        for (Migration migration : pendingMigrations) {
            log.info("Migrating to version " + migration.getVersion() + " - " + migration.getScriptName());
            executeInTransaction(migration);

            if (MigrationState.FAILED.equals(migration.getState()) && dbSupport.supportsDdlTransactions()) {
                throw new IllegalStateException("Migration failed! Changes rolled back. Aborting!");
            }

            metaDataTable.migrationFinished(migration);

            if (MigrationState.FAILED.equals(migration.getState())) {
                throw new IllegalStateException("Migration failed! Please restore backups and roll back database and code!");
            }
        }

        if (pendingMigrations.size() == 1) {
            log.info("Migration completed. Successfully applied 1 migration.");
        } else {
            log.info("Migration completed. Successfully applied " + pendingMigrations.size() + " migrations.");
        }

        return pendingMigrations.size();
    }

    /**
     * Executes this migration in a transaction.
     *
     * @param migration The migration to execute.
     */
    private void executeInTransaction(final Migration migration) {
        transactionTemplate.execute(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction(TransactionStatus status) {
                migration.migrate(jdbcTemplate);
                return null;
            }
        });
    }

    /**
     * Returns the list of migrations still to be performed.
     *
     * @param currentVersion The current version of the schema.
     * @return The list of migrations still to be performed.
     */
    private List<Migration> getPendingMigrations(SchemaVersion currentVersion) {
        List<Migration> allMigrations = new ArrayList<Migration>();
        for (MigrationResolver migrationResolver : migrationResolvers) {
            allMigrations.addAll(migrationResolver.resolvesMigrations());
        }

        if (allMigrations.isEmpty()) {
            log.warn("No migrations found!");
            return allMigrations;
        }

        Collections.sort(allMigrations, new Comparator<Migration>() {
            @Override
            public int compare(Migration o1, Migration o2) {
                //newest migration first
                return o2.getVersion().compareTo(o1.getVersion());
            }
        });

        SchemaVersion newestMigrationVersion = allMigrations.get(0).getVersion();
        if (newestMigrationVersion.compareTo(currentVersion) < 0) {
            log.warn("Database version (" + currentVersion.getVersion() + ") is newer than the latest migration ("
                    + newestMigrationVersion + ") !");
            return new ArrayList<Migration>();
        }

        ArrayList<Migration> pendingMigrations = new ArrayList<Migration>();
        for (Migration migration : allMigrations) {
            if ((migration.getVersion().compareTo(currentVersion) > 0)
                    && (migration.getVersion().compareTo(targetVersion) <= 0)) {
                pendingMigrations.add(migration);
            }
        }

        Collections.reverse(pendingMigrations);

        return pendingMigrations;
    }
}
