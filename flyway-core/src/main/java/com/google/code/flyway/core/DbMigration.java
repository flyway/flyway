package com.google.code.flyway.core;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;

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
     * The base package where the migrations are located.
     */
    private String basePackage;

    /**
     * The name of the database.
     */
    private String database;

    /**
     * The target version of the migration, default is the latest version.
     */
    private SchemaVersion targetVersion = SchemaVersion.LATEST;

    /**
     * Username of the user with admin privileges (ddl modification) on this database.
     */
    private String adminUsername;

    /**
     * Password of the user with admin privileges (ddl modification) on this database.
     */
    private String adminPassword;

    /**
     * Username of the user with user privileges (data modification) on this database.
     */
    private String userUsername;

    /**
     * Password of the user with user privileges (data modification) on this database.
     */
    private String userPassword;

    /**
     * SimpleJdbcTemplate with root access to the database.
     */
    private SimpleJdbcTemplate rootJdbcTemplate;

    /**
     * SimpleJdbcTemplate with ddl manipulation access to the database.
     */
    private SimpleJdbcTemplate ddlJdbcTemplate;

    /**
     * Spring utility for loading resources from the classpath using wildcards.
     */
    private final PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver =
            new PathMatchingResourcePatternResolver();

    /**
     * @param rootJdbcTemplate SimpleJdbcTemplate with root access to the database.
     */
    public void setRootJdbcTemplate(SimpleJdbcTemplate rootJdbcTemplate) {
        this.rootJdbcTemplate = rootJdbcTemplate;
    }

    /**
     * @param ddlJdbcTemplate SimpleJdbcTemplate with ddl manipulation access to the database.
     */
    public void setDdlJdbcTemplate(SimpleJdbcTemplate ddlJdbcTemplate) {
        this.ddlJdbcTemplate = ddlJdbcTemplate;
    }

    /**
     * @param basePackage The base package where the migrations are located.
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * @param database The name of the database.
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * @param targetVersion The target version of the migration, default is the latest version.
     */
    public void setTargetVersion(String targetVersion) {
        this.targetVersion = new SchemaVersion(targetVersion);
    }

    /**
     * @param adminUsername Username of the user with admin privileges (ddl modification) on this database.
     */
    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    /**
     * @param adminPassword Password of the user with admin privileges (ddl modification) on this database.
     */
    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    /**
     * @param userUsername Username of the user with user privileges (data modification) on this database.
     */
    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    /**
     * @param userPassword Password of the user with user privileges (data modification) on this database.
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * Starts the actual migration.
     */
    public void migrate() {
        log.debug("Database: " + database);
        log.debug("Admin user: " + adminUsername);
        log.debug("Normal user: " + userUsername);

        boolean adminExists = userExists(adminUsername);
        log.debug("Admin user exists: " + adminExists);
        if (!adminExists) {
            createUser(adminUsername, adminPassword);
            log.info("Admin user created: " + adminUsername);
        }

        boolean userExists = userExists(userUsername);
        log.debug("Normal user exists: " + userExists);
        if (!userExists) {
            createUser(userUsername, userPassword);
            log.info("Normal user created: " + userUsername);
        }

        boolean databaseExists = databaseExists();
        log.debug("Database exists: " + databaseExists);

        if (!databaseExists) {
            createDatabase();
            initSchemaMetadataTables();
        }

        SchemaVersion currentSchemaVersion = currentSchemaVersion();
        log.debug("Current schema version: " + currentSchemaVersion);
        log.debug("Target schema version: " + targetVersion);

        List<Migration> pendingMigrations = getPendingMigrations(currentSchemaVersion);
        for (Migration pendingMigration : pendingMigrations) {
            log.debug("Pending migration: " + pendingMigration.getVersion() + " - " + pendingMigration.getScriptName());
        }

        if (pendingMigrations.isEmpty()) {
            log.debug("Schema is up to date. No migration necessary.");
            return;
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
     * Initializes the schema metadata tables.
     */
    private void initSchemaMetadataTables() {
        MigrationUtils.executeSqlScript(ddlJdbcTemplate, new ClassPathResource("sql/initSchemaMetadata.sql"));
        ddlJdbcTemplate.update("insert into schema_version (major,minor) values (?,?)", 0, 0);
    }

    /**
     * Executes this migration.
     *
     * @param migration The migration to execute.
     * @throws Exception in case the migration failed.
     */
    @Transactional
    private void execute(Migration migration) throws Exception {
        migration.migrate(ddlJdbcTemplate);
        updateSchemaVersion(migration.getVersion());
        updateSchemaMaintenanceHistory(migration.getScriptName());
    }

    /**
     * Updates the schema maintenance history table.
     *
     * @param scriptName The name of the script that was run.
     */
    private void updateSchemaMaintenanceHistory(String scriptName) {
        ddlJdbcTemplate.update("insert into schema_maintenance_history (script) values (?)",
                scriptName);
    }

    /**
     * Updates the schema version table to this version.
     *
     * @param version The version of the schema.
     */
    private void updateSchemaVersion(SchemaVersion version) {
        ddlJdbcTemplate.update("update schema_version set major=?, minor=?, installed_on=?",
                version.getMajor(), version.getMinor(), new Date());
    }

    /**
     * Checks whether this database exists.
     *
     * @return {@code true} if it exists, {@code false} if not.
     */
    private boolean databaseExists() {
        int count = rootJdbcTemplate.queryForInt(
                "select count(schema_name) from information_schema.schemata where schema_name=?",
                database);
        return count == 1;
    }

    /**
     * Checks whether this user exists.
     *
     * @param username The user.
     * @return {@code true} if it exists, {@code false} if not.
     */
    private boolean userExists(String username) {
        int count = rootJdbcTemplate.queryForInt("select count(user) from mysql.user where user=?",
                username);
        return count > 0;
    }

    /**
     * @return The version of the currently installed schema.
     */
    private SchemaVersion currentSchemaVersion() {
        int major = ddlJdbcTemplate.queryForInt("select major from schema_version");
        int minor = ddlJdbcTemplate.queryForInt("select minor from schema_version");
        return new SchemaVersion(major, minor);
    }

    /**
     * Creates a new user with this username and this password.
     *
     * @param username The username.
     * @param password The password.
     */
    private void createUser(String username, String password) {
        rootJdbcTemplate.update("CREATE USER ?@'localhost' IDENTIFIED BY ?", username, password);
    }

    /**
     * Creates the database.
     */
    private void createDatabase() {
        rootJdbcTemplate.update("CREATE DATABASE " + database
                + " DEFAULT CHARACTER SET 'utf8' DEFAULT COLLATE 'utf8_bin'");
        log.info("Database created: " + database);

        rootJdbcTemplate.update("GRANT all ON " + database + ".* TO ?@'localhost' IDENTIFIED BY ?",
                adminUsername, adminPassword);
        rootJdbcTemplate.update("GRANT select,insert,update,delete ON " + database
                + ".* TO ?@'localhost' IDENTIFIED BY ?", userUsername, userPassword);
        rootJdbcTemplate.update("FLUSH PRIVILEGES");
        log.info("Admin access granted to " + adminUsername);
        log.info("User access granted to " + userUsername);
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
            Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:sql/V?*_?*.sql");
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
        migrate();
    }
}
