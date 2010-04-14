package com.google.code.flyway.core;

import com.google.code.flyway.core.dbsupport.DbSupport;
import com.google.code.flyway.core.dbsupport.MySqlDbSupport;
import com.google.code.flyway.core.util.MigrationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Main workflow for migrating the database.
 *
 * @author Axel Fontaine
 */
public class DbMigration implements InitializingBean {
    /**
     * Logger.
     */
    private static final Log log = LogFactory.getLog(DbMigration.class);

    /**
     * The datasource to use. Must have the necessary privileges to execute ddl.
     */
    private DataSource dataSource;

    /**
     * The schema to use.
     */
    private String schema;

    /**
     * The type of database being used.
     */
    private DatabaseType databaseType;

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
     * Spring utility for loading resources from the classpath using wildcards.
     */
    private final PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver =
            new PathMatchingResourcePatternResolver();

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
     * @param databaseType The type of database being used.
     */
    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
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
     */
    public void migrate() {
        log.debug("Schema: " + schema);

        if (!dbSupport.tableExists(schemaMetaDataTable)) {
            transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction(TransactionStatus status) {
                    dbSupport.createSchemaMetaDataTable(schemaMetaDataTable);
                    return null;
                }
            });
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
    private SchemaVersion currentSchemaVersion() {
        String version = simpleJdbcTemplate.queryForObject(
                "select version from " + schemaMetaDataTable + " where current_version=1", String.class);
        if (version == null) {
            return null;
        }
        return new SchemaVersion(version);
    }

    /**
     * Returns the list of migrations still to be performed.
     *
     * @param currentVersion The current version of the schema.
     * @return The list of migrations still to be performed.
     */
    private List<Migration> getPendingMigrations(SchemaVersion currentVersion) {
        Collection<Migration> allMigrations = new ArrayList<Migration>();
        allMigrations.addAll(findClassBasedMigrations());
        allMigrations.addAll(findSqlFileBasedMigrations());

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
     * Find all migrations based on Sql Files. The files must lie in the classpath and be named Vmajor_minor.sql
     *
     * @return The list of migrations based on Sql Files.
     */
    private Collection<Migration> findSqlFileBasedMigrations() {
        Collection<Migration> migrations = new ArrayList<Migration>();

        try {
            Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath:" + baseDir + "/V?*_?*.sql");
            for (Resource resource : resources) {
                migrations.add(new SqlFileMigration(resource));
            }
        } catch (IOException e) {
            log.error("Error loading sql migration files", e);
        }

        return migrations;
    }

    /**
     * Find all migrations based on Java Classes.
     *
     * @return The list of migrations based on Java Classes.
     */
    private Collection<Migration> findClassBasedMigrations() {
        Collection<Migration> migrations = new ArrayList<Migration>();

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(Migration.class));
        Set<BeanDefinition> components = provider.findCandidateComponents(basePackage);
        for (BeanDefinition beanDefinition : components) {
            Class<?> clazz = ClassUtils.resolveClassName(beanDefinition.getBeanClassName(), null);
            Migration migration = (Migration) BeanUtils.instantiateClass(clazz);
            migrations.add(migration);
        }

        return migrations;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);

        switch (databaseType) {
            case MYSQL: dbSupport = new MySqlDbSupport(simpleJdbcTemplate, schema);
        }
        
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionTemplate = new TransactionTemplate(transactionManager);

        migrate();
    }
}
