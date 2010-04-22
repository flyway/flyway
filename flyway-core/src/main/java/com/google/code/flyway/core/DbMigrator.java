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
import com.google.code.flyway.core.dbsupport.MySQLDbSupport;
import com.google.code.flyway.core.dbsupport.OracleDbSupport;
import com.google.code.flyway.core.java.JavaMigrationResolver;
import com.google.code.flyway.core.sql.SqlMigrationResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
     * The datasource to use. Must have the necessary privileges to execute ddl.
     */
    private DataSource dataSource;

    /**
     * The schema to use.
     */
    private String schema;

    /**
     * The base package where the Java migrations are located. (default: db.migration)
     */
    private String basePackage = "db.migration";

    /**
     * The base directory on the classpath where the Sql migrations are located. (default: sql/location)
     */
    private String baseDir = "db/migration";

    /**
     * The name of the schema metadata table that will be used by flyway. (default: schema_version)
     */
    private String schemaMetaDataTable = "schema_version";

    /**
     * The target version of the migration, default is the latest version.
     */
    private final SchemaVersion targetVersion = SchemaVersion.LATEST;

    /**
     * SimpleJdbcTemplate with ddl manipulation access to the database.
     */
    private SimpleJdbcTemplate jdbcTemplate;

    /**
     * Database-specific functionality.
     */
    private DbSupport dbSupport;

    /**
     * The transaction template to use.
     */
    private TransactionTemplate transactionTemplate;

    /**
     * The available migration resolvers.
     */
    private Collection<MigrationResolver> migrationResolvers = new ArrayList<MigrationResolver>();

    /**
     * The available db support classes.
     */
    private Collection<DbSupport> dbSupports = new ArrayList<DbSupport>();

    /**
     * @param dataSource The datasource to use. Must have the necessary privileges to execute ddl.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @param schema The schema to use.
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * @param basePackage The base package where the migrations are located. (default: db.migration)
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * @param baseDir The base directory on the classpath where the Sql migrations are located. (default: sql/location)
     */
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * @param schemaMetaDataTable The name of the schema metadata table that will be used by flyway. (default: schema_maintenance_history)
     */
    public void setSchemaMetaDataTable(String schemaMetaDataTable) {
        this.schemaMetaDataTable = schemaMetaDataTable;
    }

    /**
     * Starts the actual migration.
     *
     * @throws SQLException Thrown when the migration failed.
     */
    public void migrate() throws SQLException {
        if (!metaDataTableExists()) {
            createMetaDataTable();
        }

        SchemaVersion currentSchemaVersion = currentSchemaVersion();
        log.debug("Current schema version: " + currentSchemaVersion);
        log.debug("Target schema version: " + targetVersion);

        List<Migration> pendingMigrations = getPendingMigrations(currentSchemaVersion);
        if (pendingMigrations.isEmpty()) {
            log.debug("Schema is up to date. No migration necessary.");
            return;
        }

        for (Migration pendingMigration : pendingMigrations) {
            log.debug("Pending migration: " + pendingMigration.getVersion() + " - " + pendingMigration.getScriptName());
        }

        log.debug("Starting migration...");
        for (Migration migration : pendingMigrations) {
            log.info("Migrating to version " + migration.getVersion());
            try {
                execute(migration);
            } catch (Exception e) {
                log.fatal("Migration failed! Please restore backups and roll back database and code!", e);
                throw new IllegalStateException("Migration failed! Please restore backups and roll back database and code!", e);
            }
        }
        log.debug("Migration completed.");
    }

    /**
     * Checks whether Flyway's metadata table is already present in the database.
     *
     * @return {@code true} if the table exists, {@code false} if it doesn't.
     * @throws SQLException Thrown when the database metadata could not be read.
     */
    public boolean metaDataTableExists() throws SQLException {
        return dbSupport.metaDataTableExists(jdbcTemplate, schema, schemaMetaDataTable);
    }

    /**
     * Creates Flyway's metadata table.
     */
    private void createMetaDataTable() {
        transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                String[] statements = dbSupport.createSchemaMetaDataTableSql(schemaMetaDataTable);
                for (String statement : statements) {
                    jdbcTemplate.update(statement);
                }
                return null;
            }
        });
    }

    /**
     * Executes this migration.
     *
     * @param migration The migration to execute.
     * @throws Exception in case the migration failed.
     */
    private void execute(final Migration migration) throws Exception {
        try {
            transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction(TransactionStatus status) {
                    long start = System.currentTimeMillis();
                    migration.migrate(jdbcTemplate);
                    long finish = System.currentTimeMillis();
                    long duration = finish - start;
                    
                    migrationSucceeded(migration, duration);
                    return null;
                }
            });
        } catch (Exception e) {
            if (!dbSupport.supportsDdlTransactions()) {
                migrationFailed(migration);
            }
            throw e;
        }
    }

    /**
     * Marks this migration as succeeded.
     *
     * @param migration     The migration that was run.
     * @param executionTime The time (in ms) it took to execute.
     */
    private void migrationSucceeded(final Migration migration, final long executionTime) {
        jdbcTemplate.update("UPDATE " + schemaMetaDataTable + " SET current_version=0");
        jdbcTemplate.update("INSERT INTO " + schemaMetaDataTable
                 + " (version, description, script, execution_time, state, current_version)"
                 + " VALUES (?, ?, ?, ?, 'SUCCESS', 1)",
                 migration.getVersion().getVersion(), migration.getVersion().getDescription(),
                 migration.getScriptName(), executionTime);
    }

    /**
     * Marks this migration as failed.
     *
     * @param migration The migration that was run.
     */
    private void migrationFailed(final Migration migration) {
        transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                jdbcTemplate.update("UPDATE " + schemaMetaDataTable + " SET current_version=0");
                jdbcTemplate.update("INSERT INTO " + schemaMetaDataTable
                        + " (version, description, script, state, current_version)"
                        + " VALUES (?, ?, ?, 'FAILED', '1')",
                        migration.getVersion().getVersion(), migration.getVersion().getDescription(),
                        migration.getScriptName());
                return null;
            }
        });
    }

    /**
     * @return The version of the currently installed schema.
     */
    public SchemaVersion currentSchemaVersion() {
        List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "select VERSION, DESCRIPTION from " + schemaMetaDataTable + " where current_version=1");
        if (result.isEmpty()) {
            return null;
        }
        return new SchemaVersion((String) result.get(0).get("VERSION"),
                (String) result.get(0).get("DESCRIPTION"));
    }

    /**
     * Returns the list of migrations still to be performed.
     *
     * @param currentVersion The current version of the schema.
     * @return The list of migrations still to be performed.
     */
    private List<Migration> getPendingMigrations(SchemaVersion currentVersion) {
        Collection<Migration> allMigrations = new ArrayList<Migration>();
        for (MigrationResolver migrationResolver : migrationResolvers) {
            allMigrations.addAll(migrationResolver.resolvesMigrations());
        }

        List<Migration> pendingMigrations = new ArrayList<Migration>();
        for (Migration migration : allMigrations) {
            if ((migration.getVersion().compareTo(currentVersion) > 0)
                    && (migration.getVersion().compareTo(targetVersion) <= 0)) {
                pendingMigrations.add(migration);
            }
        }

        Collections.sort(pendingMigrations, new Comparator<Migration>() {
            @Override
            public int compare(Migration o1, Migration o2) {
                return o1.getVersion().compareTo(o2.getVersion());
            }
        });

        return pendingMigrations;
    }

    /**
     * Registers the available migration resolvers.
     */
    protected void registerMigrationResolvers() {
        migrationResolvers.add(new SqlMigrationResolver(baseDir));
        migrationResolvers.add(new JavaMigrationResolver(basePackage));
    }

    /**
     * Registers the available db support classes.
     */
    protected void registerDbSupports() {
        dbSupports.add(new MySQLDbSupport());
        dbSupports.add(new OracleDbSupport());
    }

    /**
     * Finds the appropriate DbSupport class for the database product with this name.
     *
     * @param databaseProductName The name of the database product.
     * @return The appropriate DbSupport class.
     * @throws IllegalArgumentException Thrown when none of the available dbSupports support this databaseProductName.
     */
    private DbSupport selectDbSupport(String databaseProductName) {
        for (DbSupport aDbSupport : dbSupports) {
            if (aDbSupport.supportsDatabase(databaseProductName)) {
                return aDbSupport;
            }
        }

        throw new IllegalArgumentException("Unsupported Database: " + databaseProductName);
    }

    @PostConstruct
    public void init() throws Exception {
        registerDbSupports();
        registerMigrationResolvers();

        String databaseProductName = dataSource.getConnection().getMetaData().getDatabaseProductName();
        dbSupport = selectDbSupport(databaseProductName);
        log.debug("Database: " + databaseProductName);

        if (schema == null) {
            schema = dbSupport.getCurrentSchema(dataSource.getConnection());
        }
        log.debug("Schema: " + schema);

        jdbcTemplate = new SimpleJdbcTemplate(dataSource);

        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionTemplate = new TransactionTemplate(transactionManager);

        migrate();
    }
}
