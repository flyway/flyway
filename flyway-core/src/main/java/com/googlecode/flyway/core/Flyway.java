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

package com.googlecode.flyway.core;

import com.googlecode.flyway.core.clean.DbCleaner;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.DbSupportFactory;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.init.DbInit;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.migration.DbMigrator;
import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationResolver;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.migration.java.JavaMigrationResolver;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlMigrationResolver;
import com.googlecode.flyway.core.validation.DbValidator;
import com.googlecode.flyway.core.validation.ValidationErrorMode;
import com.googlecode.flyway.core.validation.ValidationException;
import com.googlecode.flyway.core.validation.ValidationMode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Driver;
import java.util.*;

/**
 * This is the centre point of Flyway, and for most users, the only class they will ever have to deal with.
 * <p/>
 * It is THE public API from which all important Flyway functions such as clean, validate and migrate can be called.
 */
public class Flyway {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Flyway.class);

    /**
     * Property name prefix for placeholders that are configured through properties.
     */
    private static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";

    /**
     * The base package where the Java migrations are located. (default:
     * db.migration)
     */
    private String basePackage = "db.migration";

    /**
     * The base directory on the classpath where the Sql migrations are located.
     * (default: sql/location)
     */
    private String baseDir = "db/migration";

    /**
     * The encoding of Sql migrations.
     * (default: UTF-8)
     */
    private String encoding = "UTF-8";

    /**
     * The name of the schema metadata table that will be used by flyway.
     * (default: schema_version)
     */
    private String table = "schema_version";

    /**
     * The target version up to which Flyway should run migrations. Migrations with a higher version number will not be
     * applied.
     * (default: the latest version)
     */
    private SchemaVersion target = SchemaVersion.LATEST;

    /**
     * A map of <placeholder, replacementValue> to apply to sql migration
     * scripts.
     */
    private Map<String, String> placeholders = new HashMap<String, String>();

    /**
     * The prefix of every placeholder. (default: ${ )
     */
    private String placeholderPrefix = "${";

    /**
     * The suffix of every placeholder. (default: } )
     */
    private String placeholderSuffix = "}";

    /**
     * The file name prefix for sql migrations (default: V)
     */
    private String sqlMigrationPrefix = "V";

    /**
     * The file name suffix for sql migrations (default: .sql)
     */
    private String sqlMigrationSuffix = ".sql";

    /**
     * Ignores failed future migrations when reading the metadata table. These are migrations that we performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
     * (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not
     * an option. An older version of the application can then be redeployed, even though a newer one failed due to a
     * bad migration. (default: false)
     */
    private boolean ignoreFailedFutureMigration;

    /**
     * The mode for validation
     */
    private ValidationMode validationMode = ValidationMode.NONE;

    /**
     * The error mode for validation
     */
    private ValidationErrorMode validationErrorMode = ValidationErrorMode.FAIL;

    /**
     * JdbcTemplate with ddl manipulation access to the database.
     */
    /* private -> for testing */
    JdbcTemplate jdbcTemplate;

    /**
     * The transaction template to use.
     */
    private TransactionTemplate transactionTemplate;

    /**
     * Database-specific functionality.
     */
    private DbSupport dbSupport;

    /**
     * Ignores failed future migrations when reading the metadata table. These are migrations that we performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
     * (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not
     * an option. An older version of the application can then be redeployed, even though a newer one failed due to a
     * bad migration.
     * (default: false)
     *
     * @param ignoreFailedFutureMigration {@code true} to terminate normally and log a warning, {@code false} to fail
     * fast with an exception.
     */
    public void setIgnoreFailedFutureMigration(boolean ignoreFailedFutureMigration) {
        this.ignoreFailedFutureMigration = ignoreFailedFutureMigration;
    }

    /**
     * @param validationMode The ValidationMode for checksum validation
     */
    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    /**
     * @param validationErrorMode The error mode for validation
     */
    public void setValidationErrorMode(ValidationErrorMode validationErrorMode) {
        this.validationErrorMode = validationErrorMode;
    }

    /**
     * @param basePackage The base package where the migrations are located. (default:
     *                    db.migration)
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * @param baseDir The base directory on the classpath where the Sql migrations
     *                are located. (default: sql/location)
     */
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    /**
     * @param encoding The encoding of Sql migrations.
     *                 (default: UTF-8)
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @param table The name of the schema metadata table that will be used by flyway. (default: schema_version)
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * @param target  The target version up to which Flyway should run migrations. Migrations with a higher version
     * number will not be applied. (default: the latest version)
     */
    public void setTarget(SchemaVersion target) {
        this.target = target;
    }

    /**
     * @param placeholders A map of <placeholder, replacementValue> to apply to sql migration scripts.
     */
    public void setPlaceholders(Map<String, String> placeholders) {
        this.placeholders = placeholders;
    }

    /**
     * @param placeholderPrefix The prefix of every placeholder. (default: ${ )
     */
    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    /**
     * @param placeholderSuffix The suffix of every placeholder. (default: } )
     */
    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * @param sqlMigrationPrefix The file name prefix for sql migrations (default: V)
     */
    public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
        this.sqlMigrationPrefix = sqlMigrationPrefix;
    }

    /**
     * @param sqlMigrationSuffix The file name suffix for sql migrations (default: .sql)
     */
    public void setSqlMigrationSuffix(String sqlMigrationSuffix) {
        this.sqlMigrationSuffix = sqlMigrationSuffix;
    }

    /**
     * @param dataSource The datasource to use. Must have the necessary privileges to execute ddl.
     */
    public void setDataSource(DataSource dataSource) {
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionTemplate = new TransactionTemplate(transactionManager);
        jdbcTemplate = new JdbcTemplate(dataSource);

        dbSupport = DbSupportFactory.createDbSupport(jdbcTemplate);
        LOG.debug("Schema: " + dbSupport.getCurrentSchema());
    }

    /**
     * Starts the database migration. All pending migrations will be applied in order.
     *
     * @return The number of successfully applied migrations.
     * @throws FlywayException Thrown when the migration failed.
     */
    public int migrate() throws FlywayException {
        validate();

        MetaDataTable metaDataTable = new MetaDataTable(transactionTemplate, jdbcTemplate, dbSupport, table);
        metaDataTable.createIfNotExists();

        DbMigrator dbMigrator =
                new DbMigrator(transactionTemplate, jdbcTemplate, dbSupport, metaDataTable, target, ignoreFailedFutureMigration);
        return dbMigrator.migrate(findAvailableMigrations());
    }

    /**
     * Validate applied migration with classpath migrations to detect accidental changes.
     * Uses validation type ALL if NONE is set.
     *
     * @throws FlywayException thrown when the validation failed.
     */
    public void validate() throws FlywayException {
        MetaDataTable metaDataTable = new MetaDataTable(transactionTemplate, jdbcTemplate, dbSupport, table);
        DbValidator dbValidator = new DbValidator(validationMode, metaDataTable);
        final String validationError = dbValidator.validate(findAvailableMigrations());

        if (validationError != null) {
            final String msg = "Validate failed. Found differences between applied migrations and available migrations: " + validationError;
            if (ValidationErrorMode.CLEAN.equals(validationErrorMode)) {
                LOG.warn(msg + " running clean and migrate again.");
                clean();
            } else {
                throw new ValidationException(msg);
            }
        }
    }

    /**
     * Finds all available migrations using all migration resolvers (sql, java, ...).
     *
     * @return The available migrations, sorted by version, newest first. An
     *         empty list is returned when no migrations can be found.
     *
     * @throws FlywayException when the available migrations have overlapping versions.
     */
    private List<Migration> findAvailableMigrations() throws FlywayException {
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer(placeholders, placeholderPrefix, placeholderSuffix);

        Collection<MigrationResolver> migrationResolvers = new ArrayList<MigrationResolver>();
        migrationResolvers.add(new SqlMigrationResolver(baseDir, placeholderReplacer, encoding, sqlMigrationPrefix, sqlMigrationSuffix));
        migrationResolvers.add(new JavaMigrationResolver(basePackage));

        List<Migration> allMigrations = new ArrayList<Migration>();
        for (MigrationResolver migrationResolver : migrationResolvers) {
            allMigrations.addAll(migrationResolver.resolveMigrations());
        }

        if (allMigrations.isEmpty()) {
            return allMigrations;
        }

        Collections.sort(allMigrations);
        Collections.reverse(allMigrations);

        // check for more than one migration with same version
        for (int i = 0; i < allMigrations.size() - 1; i++) {
            Migration current = allMigrations.get(i);
            Migration next = allMigrations.get(i+1);
            if (current.compareTo(next) == 0) {
                throw new ValidationException("Found more than one migration with version: " + current.getVersion());
            }
        }

        return allMigrations;
    }

    /**
     * Drops all objects (tables, views, procedures, triggers, ...) in the current schema.
     */
    public void clean() {
        new DbCleaner(transactionTemplate, jdbcTemplate, dbSupport).clean();
    }

    /**
     * Returns the status (current version) of the database.
     *
     * @return The latest applied migration, or {@code null} if no migration has been applied yet.
     */
    public MetaDataTableRow status() {
        MetaDataTable metaDataTable = new MetaDataTable(transactionTemplate, jdbcTemplate, dbSupport, table);
        return metaDataTable.latestAppliedMigration();
    }

    /**
     * Returns the history (all applied migrations) of the database.
     *
     * @return All migrations applied to the database, sorted, oldest first. An empty list if none.
     */
    public List<MetaDataTableRow> history() {
        MetaDataTable metaDataTable = new MetaDataTable(transactionTemplate, jdbcTemplate, dbSupport, table);
        return metaDataTable.allAppliedMigrations();
    }

    /**
     * Creates and initializes the Flyway metadata table.
     *
     * @param initialVersion (Optional) The initial version to put in the metadata table. Only migrations with a version number
     *                       higher than this one will be considered for this database.
     * @param description    (Optional) The description of the initial version.
     *
     * @throws FlywayException when the schema initialization failed.
     */
    public void init(SchemaVersion initialVersion, String description) throws FlywayException {
        MetaDataTable metaDataTable = new MetaDataTable(transactionTemplate, jdbcTemplate, dbSupport, table);
        new DbInit(transactionTemplate, metaDataTable).init(initialVersion, description);
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration.
     * Property names are documented in the flyway maven plugin.
     *
     * @param properties Properties used for configuration.
     */
    public void configure(Properties properties) {
        String driver = properties.getProperty("flyway.driver");
        String url = properties.getProperty("flyway.url");
        String user = properties.getProperty("flyway.user");
        String password = properties.getProperty("flyway.password");

        if ((driver != null) && (url != null) && (user != null) && (password != null)) {
            // All datasource properties set
            Driver driverClazz;
            try {
                driverClazz = (Driver) Class.forName(driver).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Error instantiating database driver: " + driver, e);
            }

            setDataSource(new SimpleDriverDataSource(driverClazz, url, user, password));
        } else if ((driver != null) || (url != null) || (user != null) || (password != null)) {
            // Some, but not all datasource properties set
            LOG.warn("Discarding INCOMPLETE dataSource configuration!" +
                    " At least one of flyway.driver, flyway.url, flyway.user or flyway.password missing.");
        }


        String baseDirProp = properties.getProperty("flyway.baseDir");
        if (baseDirProp != null) {
            setBaseDir(baseDirProp);
        }
        String placeholderPrefixProp = properties.getProperty("flyway.placeholderPrefix");
        if (placeholderPrefixProp != null) {
            setPlaceholderPrefix(placeholderPrefixProp);
        }
        String placeholderSuffixProp = properties.getProperty("flyway.placeholderSuffix");
        if (placeholderSuffixProp != null) {
            setPlaceholderSuffix(placeholderSuffixProp);
        }
        String sqlMigrationPrefixProp = properties.getProperty("flyway.sqlMigrationPrefix");
        if (sqlMigrationPrefixProp != null) {
            setSqlMigrationPrefix(sqlMigrationPrefixProp);
        }
        String sqlMigrationSuffixProp = properties.getProperty("flyway.sqlMigrationSuffix");
        if (sqlMigrationSuffixProp != null) {
            setSqlMigrationSuffix(sqlMigrationSuffixProp);
        }
        String basePackageProp = properties.getProperty("flyway.basePackage");
        if (basePackageProp != null) {
            setBasePackage(basePackageProp);
        }
        String encodingProp = properties.getProperty("flyway.encoding");
        if (encodingProp != null) {
            setEncoding(encodingProp);
        }
        String tableProp = properties.getProperty("flyway.table");
        if (tableProp != null) {
            setTable(tableProp);
        }
        String validationErrorModeProp = properties.getProperty("flyway.validationErrorMode");
        if (validationErrorModeProp != null) {
            setValidationErrorMode(ValidationErrorMode.valueOf(validationErrorModeProp));
        }
        String validationModeProp = properties.getProperty("flyway.validationMode");
        if (validationErrorModeProp != null) {
            setValidationMode(ValidationMode.valueOf(validationModeProp));
        }

        Map<String, String> placeholdersFromProps = new HashMap<String, String>();
        for (Object property : properties.keySet()) {
            String propertyName = (String) property;
            if (propertyName.startsWith(PLACEHOLDERS_PROPERTY_PREFIX)
                    && propertyName.length() > PLACEHOLDERS_PROPERTY_PREFIX.length()) {
                String placeholderName = propertyName.substring(PLACEHOLDERS_PROPERTY_PREFIX.length());
                String placeholderValue = properties.getProperty(propertyName);
                placeholdersFromProps.put(placeholderName, placeholderValue);
            }
        }
        setPlaceholders(placeholdersFromProps);
    }
}
