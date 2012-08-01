/**
 * Copyright (C) 2010-2012 the original author or authors.
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

import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.api.MigrationInfos;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.clean.DbCleaner;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.DbSupportFactory;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.info.DbInfoAggregator;
import com.googlecode.flyway.core.init.DbInit;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.migration.CompositeMigrationResolver;
import com.googlecode.flyway.core.migration.DbMigrator;
import com.googlecode.flyway.core.migration.ExecutableMigration;
import com.googlecode.flyway.core.migration.MigrationResolver;
import com.googlecode.flyway.core.migration.MigrationState;
import com.googlecode.flyway.core.migration.MigrationType;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import com.googlecode.flyway.core.util.jdbc.JdbcUtils;
import com.googlecode.flyway.core.util.jdbc.TransactionTemplate;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;
import com.googlecode.flyway.core.validation.DbValidator;
import com.googlecode.flyway.core.validation.ValidationErrorMode;
import com.googlecode.flyway.core.validation.ValidationMode;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is the centre point of Flyway, and for most users, the only class they will ever have to deal with.
 * <p/>
 * It is THE public API from which all important Flyway functions such as clean, validate and migrate can be called.
 */
public class Flyway {
    private static final Log LOG = LogFactory.getLog(Flyway.class);

    /**
     * Property name prefix for placeholders that are configured through properties.
     */
    private static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";

    /**
     * Locations on the classpath to scan recursively for migrations. Locations may contain both sql
     * and java-based migrations. (default: db/migration)
     */
    private String[] locations = new String[]{"db/migration"};

    /**
     * The base package where the Java migrations are located. (default: db.migration)
     *
     * @deprecated Uses locations instead. Will be removed in Flyway 2.0.
     */
    @Deprecated
    private String basePackage = "db/migration";

    /**
     * The base directory on the classpath where the Sql migrations are located. (default: db/migration)
     *
     * @deprecated Uses locations instead. Will be removed in Flyway 2.0.
     */
    @Deprecated
    private String baseDir = "db/migration";

    /**
     * The encoding of Sql migrations. (default: UTF-8)
     */
    private String encoding = "UTF-8";

    /**
     * The schemas managed by Flyway. The first schema in the list will be the one containing the metadata table.
     * (default: The default schema for the datasource connection)
     */
    private String[] schemas = new String[0];

    /**
     * <p>The name of the schema metadata table that will be used by Flyway. (default: schema_version)</p><p> By default
     * (single-schema mode) the metadata table is placed in the default schema for the connection provided by the
     * datasource. </p> <p> When the <i>flyway.schemas</i> property is set (multi-schema mode), the metadata table is
     * placed in the first schema of the list. </p>
     */
    private String table = "schema_version";

    /**
     * The target version up to which Flyway should run migrations. Migrations with a higher version number will not be
     * applied. (default: the latest version)
     */
    private MigrationVersion target = MigrationVersion.LATEST;

    /**
     * The map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
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
     * The file name prefix for sql migrations. (default: V)
     */
    private String sqlMigrationPrefix = "V";

    /**
     * The file name suffix for sql migrations. (default: .sql)
     */
    private String sqlMigrationSuffix = ".sql";

    /**
     * Ignores failed future migrations when reading the metadata table. These are migrations that were performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
     * (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not
     * an option. An older version of the application can then be redeployed, even though a newer one failed due to a
     * bad migration. (default: {@code false})
     */
    private boolean ignoreFailedFutureMigration;

    /**
     * Whether to automatically call validate or not when running migrate. (default: {@code false})
     */
    private boolean validateOnMigrate;

    /**
     * Whether to automatically call clean or not when a validation error occurs. (default: {@code false})<br/>
     * <p> This is exclusively intended as a convenience for development. Even tough we
     * strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a
     * way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that
     * the next migration will bring you back to the state checked into SCM.</p>
     * <p><b>Warning ! Do not enable in production !</b></p>
     */
    private boolean cleanOnValidationError;

    /**
     * The initial version to put in the database. Only used for init. (default: 0)
     */
    private MigrationVersion initialVersion = new MigrationVersion("0");

    /**
     * The description of the initial version. Only used for init. (default: << Flyway Init >>)
     */
    private String initialDescription = "<< Flyway Init >>";

    /**
     * Flag to disable the check that a non-empty schema has been properly initialized with init. This check ensures
     * Flyway does not migrate or clean the wrong database in case of a configuration mistake. Be careful when disabling
     * this! (default: {@code false})
     */
    private boolean disableInitCheck;

    /**
     * The dataSource to use to access the database. Must have the necessary privileges to execute ddl.
     */
    private DataSource dataSource;

    /**
     * Creates a new instance of Flyway. This is your starting point.
     */
    public Flyway() {
        // Do nothing
    }

    /**
     * Retrieves locations on the classpath to scan recursively for migrations. Locations may contain both sql
     * and java-based migrations.
     *
     * @return Locations on the classpath to scan recursively for migrations. Locations may contain both sql
     *         and java-based migrations. (default: db/migration)
     */
    public String[] getLocations() {
        return locations;
    }

    /**
     * Retrieves the base package where the Java migrations are located.
     *
     * @return The base package where the Java migrations are located. (default: db.migration)
     * @deprecated Uses getLocations instead. Will be removed in Flyway 2.0.
     */
    @Deprecated
    public String getBasePackage() {
        return basePackage;
    }

    /**
     * Retrieves the base directory on the classpath where the Sql migrations are located.
     *
     * @return The base directory on the classpath where the Sql migrations are located. (default: db/migration)
     * @deprecated Uses getLocations instead. Will be removed in Flyway 2.0.
     */
    @Deprecated
    public String getBaseDir() {
        return baseDir;
    }

    /**
     * Retrieves the encoding of Sql migrations.
     *
     * @return The encoding of Sql migrations. (default: UTF-8)
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Retrieves the schemas managed by Flyway. The first schema in the list will be the one containing the metadata
     * table.
     *
     * @return The schemas managed by Flyway. (default: The default schema for the datasource connection)
     */
    public String[] getSchemas() {
        return schemas;
    }

    /**
     * <p>Retrieves the name of the schema metadata table that will be used by Flyway.</p><p> By default (single-schema
     * mode) the metadata table is placed in the default schema for the connection provided by the datasource. </p> <p>
     * When the <i>flyway.schemas</i> property is set (multi-schema mode), the metadata table is placed in the first
     * schema of the list. </p>
     *
     * @return The name of the schema metadata table that will be used by flyway. (default: schema_version)
     */
    public String getTable() {
        return table;
    }

    /**
     * Retrieves the target version up to which Flyway should run migrations. Migrations with a higher version number
     * will not be applied.
     *
     * @return The target version up to which Flyway should run migrations. Migrations with a higher version number will
     *         not be applied. (default: the latest version)
     */
    public SchemaVersion getTarget() {
        return new SchemaVersion(target.toString());
    }

    /**
     * Retrieves the map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     *
     * @return The map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     */
    public Map<String, String> getPlaceholders() {
        return placeholders;
    }

    /**
     * Retrieves the prefix of every placeholder.
     *
     * @return The prefix of every placeholder. (default: ${ )
     */
    public String getPlaceholderPrefix() {
        return placeholderPrefix;
    }

    /**
     * Retrieves the suffix of every placeholder.
     *
     * @return The suffix of every placeholder. (default: } )
     */
    public String getPlaceholderSuffix() {
        return placeholderSuffix;
    }

    /**
     * Retrieves the file name prefix for sql migrations.
     *
     * @return The file name prefix for sql migrations. (default: V)
     */
    public String getSqlMigrationPrefix() {
        return sqlMigrationPrefix;
    }

    /**
     * Retrieves the file name suffix for sql migrations.
     *
     * @return The file name suffix for sql migrations. (default: .sql)
     */
    public String getSqlMigrationSuffix() {
        return sqlMigrationSuffix;
    }

    /**
     * Whether to ignore failed future migrations when reading the metadata table. These are migrations that
     * were performed by a newer deployment of the application that are not yet available in this version. For example:
     * we have migrations available on the classpath up to version 3.0. The metadata table indicates that a migration to
     * version 4.0 (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an
     * exception, a warning is logged and Flyway terminates normally. This is useful for situations where a database
     * rollback is not an option. An older version of the application can then be redeployed, even though a newer one
     * failed due to a bad migration.
     *
     * @return {@code true} to terminate normally and log a warning, {@code false} to fail fast with an exception.
     *         (default: {@code false})
     */
    public boolean isIgnoreFailedFutureMigration() {
        return ignoreFailedFutureMigration;
    }

    /**
     * Retrieves the mode for validation. Only used for migrate. When using validate validationMode is always ALL.
     *
     * @return The mode for validation. (default: NONE)
     * @deprecated Use isValidateOnMigrate instead. Will be removed in Flyway 2.0.
     */
    @Deprecated
    public ValidationMode getValidationMode() {
        LOG.warn("validationMode has been deprecated and will be removed in Flyway 2.0. Use validateOnMigrate instead.");
        if (validateOnMigrate) {
            return ValidationMode.ALL;
        }
        return ValidationMode.NONE;
    }

    /**
     * Retrieves the error mode for validation.
     *
     * @return The error mode for validation. (default: FAIL)
     * @deprecated Use isCleanOnValidationError instead. Will be removed in Flyway 2.0.
     */
    @Deprecated
    public ValidationErrorMode getValidationErrorMode() {
        LOG.warn("validationErrorMode has been deprecated and will be removed in Flyway 2.0. Use cleanOnValidationError instead.");
        if (cleanOnValidationError) {
            return ValidationErrorMode.CLEAN;
        }
        return ValidationErrorMode.FAIL;
    }

    /**
     * Whether to automatically call validate or not when running migrate.
     *
     * @return {@code true} if validate should be called. {@code false} if not. (default: {@code false})
     */
    public boolean isValidateOnMigrate() {
        return validateOnMigrate;
    }

    /**
     * Whether to automatically call clean or not when a validation error occurs.<br/>
     * <p> This is exclusively intended as a convenience for development. Even tough we
     * strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a
     * way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that
     * the next migration will bring you back to the state checked into SCM.</p>
     * <p><b>Warning ! Do not enable in production !</b></p>
     *
     * @return {@code true} if clean should be called. {@code false} if not. (default: {@code false})
     */
    public boolean isCleanOnValidationError() {
        return cleanOnValidationError;
    }

    /**
     * Retrieves the initial version to put in the database. Only used for init.
     *
     * @return The initial version to put in the database. (default: 0)
     */
    public SchemaVersion getInitialVersion() {
        return new SchemaVersion(initialVersion.toString());
    }

    /**
     * Retrieves the description of the initial version. Only used for init.
     *
     * @return The description of the initial version. (default: << Flyway Init >>)
     */
    public String getInitialDescription() {
        return initialDescription;
    }

    /**
     * Flag to disable the check that a non-empty schema has been properly initialized with init. This check ensures
     * Flyway does not migrate or clean the wrong database in case of a configuration mistake. Be careful when disabling
     * this!
     *
     * @return {@code true} if the check is disabled. {@code false} if it is active. (default: {@code false})
     */
    public boolean isDisableInitCheck() {
        return disableInitCheck;
    }

    /**
     * Retrieves the dataSource to use to access the database. Must have the necessary privileges to execute ddl.
     *
     * @return The dataSource to use to access the database. Must have the necessary privileges to execute ddl.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Ignores failed future migrations when reading the metadata table. These are migrations that were performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
     * (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not
     * an option. An older version of the application can then be redeployed, even though a newer one failed due to a
     * bad migration.
     *
     * @param ignoreFailedFutureMigration {@code true} to terminate normally and log a warning, {@code false} to fail
     *                                    fast with an exception. (default: {@code false})
     */
    public void setIgnoreFailedFutureMigration(boolean ignoreFailedFutureMigration) {
        this.ignoreFailedFutureMigration = ignoreFailedFutureMigration;
    }

    /**
     * Sets the mode for validation. Only used for migrate. When using validate validationMode is always ALL.
     *
     * @param validationMode The mode for validation. (default: NONE)
     * @deprecated Use setValidateOnMigrate instead. Will be removed in Flyway 2.0.
     */
    @Deprecated
    public void setValidationMode(ValidationMode validationMode) {
        LOG.warn("validationMode has been deprecated and will be removed in Flyway 2.0. Use validateOnMigrate instead.");
        validateOnMigrate = ValidationMode.ALL == validationMode;
    }

    /**
     * Sets the error mode for validation.
     *
     * @param validationErrorMode The error mode for validation. (default: FAIL)
     * @deprecated Use setCleanOnValidationError instead. Will be removed in Flyway 2.0.
     */
    @Deprecated
    public void setValidationErrorMode(ValidationErrorMode validationErrorMode) {
        LOG.warn("validationErrorMode has been deprecated and will be removed in Flyway 2.0. Use cleanOnValidationError instead.");
        cleanOnValidationError = ValidationErrorMode.CLEAN == validationErrorMode;
    }

    /**
     * Whether to automatically call validate or not when running migrate.
     *
     * @param validateOnMigrate {@code true} if validate should be called. {@code false} if not. (default: {@code false})
     */
    public void setValidateOnMigrate(boolean validateOnMigrate) {
        this.validateOnMigrate = validateOnMigrate;
    }

    /**
     * Whether to automatically call clean or not when a validation error occurs.<br/>
     * <p> This is exclusively intended as a convenience for development. Even tough we
     * strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a
     * way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that
     * the next migration will bring you back to the state checked into SCM.</p>
     * <p><b>Warning ! Do not enable in production !</b></p>
     *
     * @param cleanOnValidationError {@code true} if clean should be called. {@code false} if not. (default: {@code false})
     */
    public void setCleanOnValidationError(boolean cleanOnValidationError) {
        this.cleanOnValidationError = cleanOnValidationError;
    }

    /**
     * Sets the locations on the classpath to scan recursively for migrations. Locations may contain both sql
     * and java-based migrations. (default: db.migration)
     *
     * @param locations Locations on the classpath to scan recursively for migrations. Locations may contain both sql
     *                  and java-based migrations. (default: db/migration)
     */
    public void setLocations(String... locations) {
        this.locations = new String[locations.length];
        for (int i = 0; i < locations.length; i++) {
            this.locations[i] = normalizeLocation(locations[i]);
        }
    }

    /**
     * Sets the base package where the migrations are located.
     *
     * @param basePackage The base package where the migrations are located. (default: db.migration)
     * @deprecated Use setLocations instead. Will be removed in Flyway 2.0.
     */
    @Deprecated
    public void setBasePackage(String basePackage) {
        LOG.warn("Flyway.setBasePackage is deprecated. Use Flyway.setLocations instead.");
        this.basePackage = normalizeLocation(basePackage);
    }

    /**
     * Sets the base directory on the classpath where the Sql migrations are located.
     *
     * @param baseDir The base directory on the classpath where the Sql migrations are located. (default: db/migration)
     * @deprecated Use setLocations instead. Will be removed in Flyway 2.0.
     */
    @Deprecated
    public void setBaseDir(String baseDir) {
        LOG.warn("Flyway.setBaseDir is deprecated. Use Flyway.setLocations instead.");
        this.baseDir = normalizeLocation(baseDir);
    }


    /**
     * Normalizes this classpath location by
     * <ul>
     * <li>eliminating all leading and trailing spaces</li>
     * <li>eliminating all leading and trailing slashes</li>
     * <li>turning all separators into slashes</li>
     * </ul>
     *
     * @param location The location to normalize.
     * @return The normalized location.
     */
    private String normalizeLocation(String location) {
        String directory = location.trim().replace(".", "/").replace("\\", "/");
        if (directory.startsWith("/")) {
            directory = directory.substring(1);
        }
        if (directory.endsWith("/")) {
            directory = directory.substring(0, directory.length() - 1);
        }
        return directory;
    }

    /**
     * Sets the encoding of Sql migrations.
     *
     * @param encoding The encoding of Sql migrations. (default: UTF-8)
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the schemas managed by Flyway. The first schema in the list will be the one containing the metadata table.
     *
     * @param schemas The schemas managed by Flyway. May not be {@code null}. Must contain at least one element.
     */
    public void setSchemas(String... schemas) {
        this.schemas = schemas;
    }

    /**
     * <p>Sets the name of the schema metadata table that will be used by Flyway.</p><p> By default (single-schema mode)
     * the metadata table is placed in the default schema for the connection provided by the datasource. </p> <p> When
     * the <i>flyway.schemas</i> property is set (multi-schema mode), the metadata table is placed in the first schema
     * of the list. </p>
     *
     * @param table The name of the schema metadata table that will be used by flyway. (default: schema_version)
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * Sets the target version up to which Flyway should run migrations. Migrations with a higher version number will
     * not be applied.
     *
     * @param target The target version up to which Flyway should run migrations. Migrations with a higher version
     *               number will not be applied. (default: the latest version)
     * @deprecated Use setTarget(MigrationVersion) instead. Will be removed in Flyway 2.0.
     */
    @Deprecated
    public void setTarget(SchemaVersion target) {
        LOG.warn("Flyway.setTarget(SchemaVersion) has been deprecated. Use setTarget(MigrationVersion) instead. Will be removed in Flyway 2.0.");
        this.target = new MigrationVersion(target.toString());
    }

    /**
     * Sets the target version up to which Flyway should run migrations. Migrations with a higher version number will
     * not be applied.
     *
     * @param target The target version up to which Flyway should run migrations. Migrations with a higher version
     *               number will not be applied. (default: the latest version)
     */
    public void setTarget(MigrationVersion target) {
        this.target = target;
    }

    /**
     * Sets the placeholders to replace in sql migration scripts.
     *
     * @param placeholders The map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     */
    public void setPlaceholders(Map<String, String> placeholders) {
        this.placeholders = placeholders;
    }

    /**
     * Sets the prefix of every placeholder.
     *
     * @param placeholderPrefix The prefix of every placeholder. (default: ${ )
     */
    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    /**
     * Sets the suffix of every placeholder.
     *
     * @param placeholderSuffix The suffix of every placeholder. (default: } )
     */
    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * Sets the file name prefix for sql migrations.
     *
     * @param sqlMigrationPrefix The file name prefix for sql migrations (default: V)
     */
    public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
        this.sqlMigrationPrefix = sqlMigrationPrefix;
    }

    /**
     * Sets the file name suffix for sql migrations.
     *
     * @param sqlMigrationSuffix The file name suffix for sql migrations (default: .sql)
     */
    public void setSqlMigrationSuffix(String sqlMigrationSuffix) {
        this.sqlMigrationSuffix = sqlMigrationSuffix;
    }

    /**
     * Sets the datasource to use. Must have the necessary privileges to execute ddl.
     *
     * @param dataSource The datasource to use. Must have the necessary privileges to execute ddl.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * The initial version to put in the database. Only used for init.
     *
     * @param initialVersion The initial version to put in the database. (default: 0)
     * @deprecated Use setInitialVersion(MigrationVersion) instead. Will be removed in Flyway 2.0.
     */
    @Deprecated
    public void setInitialVersion(SchemaVersion initialVersion) {
        LOG.warn("Flyway.setInitialVersion(SchemaVersion) has been deprecated. Use setInitialVersion(MigrationVersion) instead. Will be removed in Flyway 2.0.");
        this.initialVersion = new MigrationVersion(initialVersion.toString());
    }

    /**
     * The initial version to put in the database. Only used for init.
     *
     * @param initialVersion The initial version to put in the database. (default: 0)
     */
    public void setInitialVersion(MigrationVersion initialVersion) {
        this.initialVersion = initialVersion;
    }

    /**
     * The description of the initial version. Only used for init.
     *
     * @param initialDescription The description of the initial version. (default: << Flyway Init >>)
     */
    public void setInitialDescription(String initialDescription) {
        this.initialDescription = initialDescription;
    }

    /**
     * Flag to disable the check that a non-empty schema has been properly initialized with init. This check ensures
     * Flyway does not migrate or clean the wrong database in case of a configuration mistake. Be careful when disabling
     * this!
     *
     * @param disableInitCheck {@code true} if the check is disabled. {@code false} if it is active. (default: {@code false})
     */
    public void setDisableInitCheck(boolean disableInitCheck) {
        this.disableInitCheck = disableInitCheck;
    }

    /**
     * Starts the database migration. All pending migrations will be applied in order.
     *
     * @return The number of successfully applied migrations.
     * @throws FlywayException when the migration failed.
     */
    public int migrate() throws FlywayException {
        return execute(new Command<Integer>() {
            public Integer execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport) {
                List<ExecutableMigration> availableMigrations = createMigrationResolver().resolveMigrations();
                if (availableMigrations.isEmpty()) {
                    return 0;
                }

                MetaDataTable metaDataTable = createMetaDataTable(connectionMetaDataTable, dbSupport);

                if (validateOnMigrate) {
                    doValidate(connectionUserObjects, dbSupport, availableMigrations, metaDataTable);
                }

                metaDataTable.createIfNotExists();

                DbMigrator dbMigrator =
                        new DbMigrator(connectionMetaDataTable, connectionUserObjects, dbSupport, metaDataTable, target, ignoreFailedFutureMigration);
                return dbMigrator.migrate(availableMigrations);
            }
        });
    }

    /**
     * Validate applied migration with classpath migrations to detect accidental changes.
     *
     * @throws FlywayException when the validation failed.
     */
    public void validate() throws FlywayException {
        execute(new Command<Void>() {
            public Void execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport) {
                List<ExecutableMigration> availableMigrations = createMigrationResolver().resolveMigrations();

                MetaDataTable metaDataTable = createMetaDataTable(connectionMetaDataTable, dbSupport);

                doValidate(connectionUserObjects, dbSupport, availableMigrations, metaDataTable);
                return null;
            }
        });
    }

    /**
     * Performs the actual validation. All set up must have taken place beforehand.
     *
     * @param connectionUserObjects The database connection for user object changes.
     * @param dbSupport             The database-specific support for these connections.
     * @param availableMigrations   The available migrations on the classpath.
     * @param metaDataTable         The metadata table.
     */
    private void doValidate(Connection connectionUserObjects, DbSupport dbSupport, List<ExecutableMigration> availableMigrations, MetaDataTable metaDataTable) {
        if (MigrationVersion.EMPTY.equals(metaDataTable.getCurrentSchemaVersion()) && !disableInitCheck) {
            for (String schema : schemas) {
                try {
                    if (!dbSupport.isSchemaEmpty(schema)) {
                        throw new FlywayException("Found non-empty schema '" + schema
                                + "' without metadata table! Use init() first to initialize the metadata table.");
                    }
                } catch (SQLException e) {
                    throw new FlywayException("Error while checking whether schema '" + schema + "' is empty", e);
                }
            }
        }

        DbValidator dbValidator = new DbValidator(metaDataTable);
        final String validationError = dbValidator.validate(availableMigrations);

        if (validationError != null) {
            final String msg = "Validate failed. Found differences between applied migrations and available migrations: " + validationError;
            if (cleanOnValidationError) {
                doClean(connectionUserObjects, dbSupport);
            } else {
                throw new FlywayException(msg);
            }
        }
    }

    /**
     * Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas.
     *
     * @throws FlywayException when the clean fails.
     */
    public void clean() {
        execute(new Command<Void>() {
            public Void execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport) {
                doClean(connectionUserObjects, dbSupport);
                return null;
            }
        });
    }

    /**
     * Cleans the configured schemas.
     *
     * @param connectionUserObjects The database connection for user object changes.
     * @param dbSupport             The database-specific support for these connections.
     */
    private void doClean(Connection connectionUserObjects, DbSupport dbSupport) {
        new DbCleaner(new TransactionTemplate(connectionUserObjects), dbSupport, schemas).clean();
    }

    /**
     * Returns the status (current version) of the database.
     *
     * @return The latest applied migration, or {@code null} if no migration has been applied yet.
     * @deprecated Use flyway.info() instead. Will be removed in Flyway 2.0.
     */
    @Deprecated
    public MetaDataTableRow status() {
        LOG.warn("Flyway.status() has been deprecated and will be removed in Flyway 2.0. Use Flyway.info() instead.");
        return execute(new Command<MetaDataTableRow>() {
            public MetaDataTableRow execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport) {
                MetaDataTable metaDataTable = createMetaDataTable(connectionMetaDataTable, dbSupport);
                return toMetaDataTableRow(metaDataTable.latestAppliedMigration());
            }
        });
    }

    /**
     * Returns the history (all applied migrations) of the database.
     *
     * @return All migrations applied to the database, sorted, oldest first. An empty list if none.
     * @deprecated Use flyway.info() instead. Will be removed in Flyway 2.0.
     */
    @Deprecated
    public List<MetaDataTableRow> history() {
        LOG.warn("Flyway.history() has been deprecated and will be removed in Flyway 2.0. Use Flyway.info() instead.");
        return execute(new Command<List<MetaDataTableRow>>() {
            public List<MetaDataTableRow> execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport) {
                MetaDataTable metaDataTable = createMetaDataTable(connectionMetaDataTable, dbSupport);
                List<MigrationInfo> migrationInfos = metaDataTable.allAppliedMigrations();

                List<MetaDataTableRow> metaDataTableRows = new ArrayList<MetaDataTableRow>(migrationInfos.size());
                for (MigrationInfo migrationInfo : migrationInfos) {
                    metaDataTableRows.add(toMetaDataTableRow(migrationInfo));
                }
                return metaDataTableRows;
            }
        });
    }

    /**
     * Converts this migrationInfo into a metaDataTableRow.
     *
     * @param migrationInfo The migration info to convert.
     * @return The matching metaDataTableRow.
     */
    private MetaDataTableRow toMetaDataTableRow(MigrationInfo migrationInfo) {
        if (migrationInfo == null) {
            return null;
        }

        SchemaVersion version = new SchemaVersion(migrationInfo.getVersion().toString());
        MigrationType type = MigrationType.valueOf(migrationInfo.getType().name());
        MigrationState state = MigrationState.valueOf(migrationInfo.getState().name());

        return new MetaDataTableRow(version, migrationInfo.getDescription(), type, migrationInfo.getScript(),
                migrationInfo.getChecksum(), migrationInfo.getInstalledOn(), migrationInfo.getExecutionTime(), state);
    }

    /**
     * Retrieves the complete information about the migrations including applied, pending and current migrations with
     * details and status.
     *
     * @return All migrations sorted by version, oldest first.
     * @throws FlywayException when the info retrieval failed.
     */
    public MigrationInfos info() {
        return execute(new Command<MigrationInfos>() {
            public MigrationInfos execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport) {
                MetaDataTable metaDataTable = createMetaDataTable(connectionMetaDataTable, dbSupport);
                return new DbInfoAggregator(createMigrationResolver(), metaDataTable, target).aggregateMigrationInfo();
            }
        });
    }

    /**
     * Creates and initializes the Flyway metadata table.
     *
     * @throws FlywayException when the schema initialization failed.
     */
    public void init() throws FlywayException {
        execute(new Command<Void>() {
            public Void execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport) {
                MetaDataTable metaDataTable = createMetaDataTable(connectionMetaDataTable, dbSupport);
                new DbInit(new TransactionTemplate(connectionMetaDataTable), metaDataTable).init(initialVersion, initialDescription);
                return null;
            }
        });
    }

    /**
     * @return A new, fully configured, MetaDataTable instance.
     */
    private MetaDataTable createMetaDataTable(Connection connectionMetaDataTable, DbSupport dbSupport) {
        return new MetaDataTable(connectionMetaDataTable, dbSupport, schemas[0], table);
    }

    /**
     * @return A new, fully configured, MigrationResolver instance.
     */
    private MigrationResolver createMigrationResolver() {
        return new CompositeMigrationResolver(locations, basePackage, baseDir, encoding, sqlMigrationPrefix, sqlMigrationSuffix, placeholders, placeholderPrefix, placeholderSuffix);
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration. Property names are
     * documented in the flyway maven plugin.
     *
     * @param properties Properties used for configuration.
     * @throws FlywayException when the configuration failed.
     */
    public void configure(Properties properties) {
        String driverProp = properties.getProperty("flyway.driver");
        String urlProp = properties.getProperty("flyway.url");
        String userProp = properties.getProperty("flyway.user");
        String passwordProp = properties.getProperty("flyway.password");

        if (StringUtils.hasText(driverProp) && StringUtils.hasText(urlProp) && StringUtils.hasText(userProp)
                && (passwordProp != null)) {
            // All datasource properties set
            setDataSource(new DriverDataSource(driverProp, urlProp, userProp, passwordProp));
        } else if (StringUtils.hasText(driverProp) || StringUtils.hasText(urlProp) || StringUtils.hasText(userProp)
                || (passwordProp != null)) {
            // Some, but not all datasource properties set
            LOG.warn("Discarding INCOMPLETE dataSource configuration!" +
                    " At least one of flyway.driver, flyway.url, flyway.user or flyway.password missing.");
        }


        String locationsProp = properties.getProperty("flyway.locations");
        if (locationsProp != null) {
            setLocations(StringUtils.tokenizeToStringArray(locationsProp, ","));
        }
        String baseDirProp = properties.getProperty("flyway.baseDir");
        if (baseDirProp != null) {
            setBaseDir(baseDirProp);
        }
        String basePackageProp = properties.getProperty("flyway.basePackage");
        if (basePackageProp != null) {
            setBasePackage(basePackageProp);
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
        String encodingProp = properties.getProperty("flyway.encoding");
        if (encodingProp != null) {
            setEncoding(encodingProp);
        }
        String schemasProp = properties.getProperty("flyway.schemas");
        if (schemasProp != null) {
            setSchemas(StringUtils.tokenizeToStringArray(schemasProp, ","));
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
        if (validationModeProp != null) {
            setValidationMode(ValidationMode.valueOf(validationModeProp));
        }
        String cleanOnValidationErrorProp = properties.getProperty("flyway.cleanOnValidationError");
        if (cleanOnValidationErrorProp != null) {
            setCleanOnValidationError(Boolean.parseBoolean(cleanOnValidationErrorProp));
        }
        String validateOnMigrateProp = properties.getProperty("flyway.validateOnMigrate");
        if (validateOnMigrateProp != null) {
            setValidateOnMigrate(Boolean.parseBoolean(validateOnMigrateProp));
        }
        String initialVersionProp = properties.getProperty("flyway.initialVersion");
        if (initialVersionProp != null) {
            setInitialVersion(new MigrationVersion(initialVersionProp));
        }
        String initialDescriptionProp = properties.getProperty("flyway.initialDescription");
        if (initialDescriptionProp != null) {
            setInitialDescription(initialDescriptionProp);
        }
        String disableInitCheckProp = properties.getProperty("flyway.disableInitCheck");
        if (disableInitCheckProp != null) {
            setDisableInitCheck(Boolean.parseBoolean(disableInitCheckProp));
        }
        String ignoreFailedFutureMigrationProp = properties.getProperty("flyway.ignoreFailedFutureMigration");
        if (ignoreFailedFutureMigrationProp != null) {
            setIgnoreFailedFutureMigration(Boolean.parseBoolean(ignoreFailedFutureMigrationProp));
        }
        String targetProp = properties.getProperty("flyway.target");
        if (targetProp != null) {
            setTarget(new MigrationVersion(targetProp));
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

    /**
     * Executes this command with proper resource handling and cleanup.
     *
     * @param command The command to execute.
     * @param <T>     The type of the result.
     * @return The result of the command.
     */
    /*private -> testing*/ <T> T execute(Command<T> command) {
        T result;

        Connection connectionMetaDataTable = null;
        Connection connectionUserObjects = null;

        try {
            if (dataSource == null) {
                throw new FlywayException("DataSource not set! Check your configuration!");
            }

            connectionMetaDataTable = JdbcUtils.openConnection(dataSource);
            connectionUserObjects = JdbcUtils.openConnection(dataSource);

            DbSupport dbSupport = DbSupportFactory.createDbSupport(connectionMetaDataTable);
            if (schemas.length == 0) {
                try {
                    setSchemas(dbSupport.getCurrentSchema());
                } catch (SQLException e) {
                    throw new FlywayException("Error retrieving current schema", e);
                }
            }

            if (schemas.length == 1) {
                LOG.debug("Schema: " + schemas[0]);
            } else {
                LOG.debug("Schemas: " + StringUtils.arrayToCommaDelimitedString(schemas));
            }

            result = command.execute(connectionMetaDataTable, connectionUserObjects, dbSupport);
        } finally {
            JdbcUtils.closeConnection(connectionUserObjects);
            JdbcUtils.closeConnection(connectionMetaDataTable);
        }
        return result;
    }

    /**
     * A Flyway command that can be executed.
     *
     * @param <T> The result type of the command.
     */
    /*private -> testing*/ interface Command<T> {
        /**
         * Execute the operation.
         *
         * @param connectionMetaDataTable The database connection for the metadata table changes.
         * @param connectionUserObjects   The database connection for user object changes.
         * @param dbSupport               The database-specific support for these connections.
         * @return The result of the operation.
         */
        T execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport);
    }
}
