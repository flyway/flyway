package com.google.code.flyway.core;

import com.google.code.flyway.core.dbsupport.DbSupport;
import com.google.code.flyway.core.dbsupport.MySQLDbSupport;
import com.google.code.flyway.core.dbsupport.OracleDbSupport;
import com.google.code.flyway.core.java.JavaMigrationResolver;
import com.google.code.flyway.core.sql.SqlMigrationResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
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
public class DbMigrator implements InitializingBean {
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
     * The name of the schema metadata table that will be used by flyway. (default: schema_maintenance_history)
     */
    private String schemaMetaDataTable = "schema_maintenance_history";

    /**
     * The metadata of the database.
     */
    private DatabaseMetaData databaseMetaData;

    /**
     * The target version of the migration, default is the latest version.
     */
    private final SchemaVersion targetVersion = SchemaVersion.LATEST;

    /**
     * SimpleJdbcTemplate with ddl manipulation access to the database.
     */
    private SimpleJdbcTemplate simpleJdbcTemplate;

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
     * @param dataSource The datasource to use. Must have the necessary privileges to execute ddl.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
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
     * Creates Flyway's metadata table.
     */
    private void createMetaDataTable() {
        transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                String[] statements = dbSupport.createSchemaMetaDataTableSql(schemaMetaDataTable);
                for (String statement : statements) {
                    simpleJdbcTemplate.update(statement);
                }
                return null;
            }
        });
    }

    /**
     * Checks whether Flyway's metadata table is already present in the database.
     *
     * @return {@code true} if the table exists, {@false if it doesn't}
     * @throws SQLException Thrown when the database metadata could not be read.
     */
    /* private -> for testing */
    boolean metaDataTableExists() throws SQLException {
        if (dbSupport instanceof OracleDbSupport) {
            int count = simpleJdbcTemplate.queryForInt(
                    "SELECT count(*) FROM user_tables WHERE table_name = ?", schemaMetaDataTable.toUpperCase());
            return count > 0;
        }

        ResultSet resultSet = dataSource.getConnection().getMetaData().getTables(schema, null, schemaMetaDataTable, null);
        return resultSet.next();
    }

    /**
     * Executes this migration.
     *
     * @param migration The migration to execute.
     * @throws Exception in case the migration failed.
     */
    @Transactional
    private void execute(Migration migration) throws Exception {
        migration.migrate(simpleJdbcTemplate);
        updateSchemaMaintenanceHistory(migration);
    }

    /**
     * Updates the schema maintenance history table.
     *
     * @param migration The migration that was run.
     */
    private void updateSchemaMaintenanceHistory(Migration migration) {
        simpleJdbcTemplate.update("insert into " + schemaMetaDataTable
                + " (version, script, state, current_version) values (?, ?, 'SUCCESS', '1')",
                migration.getVersion().toString(), migration.getScriptName());
    }

    /**
     * @return The version of the currently installed schema.
     */
    /* private -> for testing */
    SchemaVersion currentSchemaVersion() {
        List<Map<String, Object>> result = simpleJdbcTemplate.queryForList(
                "select VERSION from " + schemaMetaDataTable + " where current_version=1");
        if (result.isEmpty()) {
            return null;
        }
        return new SchemaVersion((String) result.get(0).get("VERSION"));
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

    @Override
    public void afterPropertiesSet() throws Exception {
        simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);

        String databaseProductName = dataSource.getConnection().getMetaData().getDatabaseProductName();
        log.debug("Database: " + databaseProductName);

        if (MySQLDbSupport.DATABASE_PRODUCT_NAME.equals(databaseProductName)) {
            dbSupport = new MySQLDbSupport();
            schema = dataSource.getConnection().getCatalog();
        } else if (OracleDbSupport.DATABASE_PRODUCT_NAME.equals(databaseProductName)) {
            dbSupport = new OracleDbSupport();
            schema = dataSource.getConnection().getMetaData().getUserName();
        }
        log.debug("Schema: " + schema);

        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionTemplate = new TransactionTemplate(transactionManager);

        migrationResolvers.add(new SqlMigrationResolver(baseDir));
        migrationResolvers.add(new JavaMigrationResolver(basePackage));

        migrate();
    }
}
