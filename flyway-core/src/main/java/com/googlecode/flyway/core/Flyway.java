/**
 * Copyright (C) 2010-2013 the original author or authors.
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

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.api.MigrationInfoService;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.command.DbClean;
import com.googlecode.flyway.core.command.DbInit;
import com.googlecode.flyway.core.command.DbMigrate;
import com.googlecode.flyway.core.command.DbSchemas;
import com.googlecode.flyway.core.command.DbValidate;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.DbSupportFactory;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.info.MigrationInfoServiceImpl;
import com.googlecode.flyway.core.metadatatable.MetaDataTable;
import com.googlecode.flyway.core.metadatatable.MetaDataTableImpl;
import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import com.googlecode.flyway.core.migration.SchemaVersion;
import com.googlecode.flyway.core.resolver.CompositeMigrationResolver;
import com.googlecode.flyway.core.resolver.MigrationResolver;
import com.googlecode.flyway.core.util.Locations;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import com.googlecode.flyway.core.util.jdbc.JdbcUtils;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;
import com.googlecode.flyway.core.validation.ValidationErrorMode;
import com.googlecode.flyway.core.validation.ValidationMode;

import javax.sql.DataSource;
import java.sql.Connection;
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
    private Locations locations = new Locations("db/migration");

    /**
     * The encoding of Sql migrations. (default: UTF-8)
     */
    private String encoding = "UTF-8";

    /**
     * The schemas managed by Flyway.  These schema names are case-sensitive. (default: The default schema for the datasource connection)
     * <p>Consequences:</p>
     * <ul>
     * <li>The first schema in the list will be automatically set as the default one during the migration.</li>
     * <li>The first schema in the list will also be the one containing the metadata table.</li>
     * <li>The schemas will be cleaned in the order of this list.</li>
     * </ul>
     */
    private String[] schemaNames = new String[0];

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
     * The version to tag an existing schema with when executing init. (default: 1)
     */
    private MigrationVersion initVersion = new MigrationVersion("1");

    /**
     * The description to tag an existing schema with when executing init. (default: << Flyway Init >>)
     */
    private String initDescription = "<< Flyway Init >>";

    /**
     * Flag to disable the check that a non-empty schema has been properly initialized with init. This check ensures
     * Flyway does not migrate the wrong database in case of a configuration mistake. Be careful when disabling
     * this! (default: {@code false})
     *
     * @deprecated Use initOnMigrate instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    private boolean disableInitCheck;

    /**
     * <p>
     * Whether to automatically call init when migrate is executed against a non-empty schema with no metadata table.
     * This schema will then be initialized with the {@code initialVersion} before executing the migrations.
     * Only migrations above {@code initialVersion} will then be applied.
     * </p>
     * <p>
     * This is useful for initial Flyway production deployments on projects with an existing DB.
     * </p>
     * <p>
     * Be careful when enabling this as it removes the safety net that ensures
     * Flyway does not migrate the wrong database in case of a configuration mistake! (default: {@code false})
     * </p>
     */
    private boolean initOnMigrate;

    /**
     * Allows migrations to be run "out of order".
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     * <p>(default: {@code false})</p>
     */
    private boolean outOfOrder;

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
        return locations.getLocations().toArray(new String[locations.getLocations().size()]);
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
     * Retrieves the schemas managed by Flyway.  These schema names are case-sensitive.
     * <p>Consequences:</p>
     * <ul>
     * <li>The first schema in the list will be automatically set as the default one during the migration.</li>
     * <li>The first schema in the list will also be the one containing the metadata table.</li>
     * <li>The schemas will be cleaned in the order of this list.</li>
     * </ul>
     *
     * @return The schemas managed by Flyway. (default: The default schema for the datasource connection)
     */
    public String[] getSchemas() {
        return schemaNames;
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
    public MigrationVersion getTarget() {
        return target;
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
     * @deprecated Use isValidateOnMigrate instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public ValidationMode getValidationMode() {
        LOG.warn("validationMode has been deprecated and will be removed in Flyway 3.0. Use validateOnMigrate instead.");
        if (validateOnMigrate) {
            return ValidationMode.ALL;
        }
        return ValidationMode.NONE;
    }

    /**
     * Retrieves the error mode for validation.
     *
     * @return The error mode for validation. (default: FAIL)
     * @deprecated Use isCleanOnValidationError instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public ValidationErrorMode getValidationErrorMode() {
        LOG.warn("validationErrorMode has been deprecated and will be removed in Flyway 3.0. Use cleanOnValidationError instead.");
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
     * Retrieves the version to tag an existing schema with when executing init.
     *
     * @return The version to tag an existing schema with when executing init. (default: 1)
     * @deprecated Use getInitVersion() instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public MigrationVersion getInitialVersion() {
        LOG.warn("Flyway.getInitialVersion() has been deprecated. Use getInitVersion() instead. Will be removed in Flyway 3.0.");
        return initVersion;
    }

    /**
     * Retrieves the description to tag an existing schema with when executing init.
     *
     * @return The description to tag an existing schema with when executing init. (default: << Flyway Init >>)
     * @deprecated Use getInitDescription() instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public String getInitialDescription() {
        LOG.warn("Flyway.getInitialDescription() has been deprecated. Use getInitDescription() instead. Will be removed in Flyway 3.0.");
        return initDescription;
    }

    /**
     * Retrieves the version to tag an existing schema with when executing init.
     *
     * @return The version to tag an existing schema with when executing init. (default: 1)
     */
    public MigrationVersion getInitVersion() {
        return initVersion;
    }

    /**
     * Retrieves the description to tag an existing schema with when executing init.
     *
     * @return The description to tag an existing schema with when executing init. (default: << Flyway Init >>)
     */
    public String getInitDescription() {
        return initDescription;
    }

    /**
     * Flag to disable the check that a non-empty schema has been properly initialized with init. This check ensures
     * Flyway does not migrate or clean the wrong database in case of a configuration mistake. Be careful when disabling
     * this!
     *
     * @return {@code true} if the check is disabled. {@code false} if it is active. (default: {@code false})
     * @deprecated Use initOnMigrate instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public boolean isDisableInitCheck() {
        return disableInitCheck;
    }

    /**
     * <p>
     * Whether to automatically call init when migrate is executed against a non-empty schema with no metadata table.
     * This schema will then be initialized with the {@code initialVersion} before executing the migrations.
     * Only migrations above {@code initialVersion} will then be applied.
     * </p>
     * <p>
     * This is useful for initial Flyway production deployments on projects with an existing DB.
     * </p>
     * <p>
     * Be careful when enabling this as it removes the safety net that ensures
     * Flyway does not migrate the wrong database in case of a configuration mistake!
     * </p>
     *
     * @return {@code true} if init should be called on migrate for non-empty schemas, {@code false} if not. (default: {@code false})
     */
    public boolean isInitOnMigrate() {
        return initOnMigrate;
    }

    /**
     * Allows migrations to be run "out of order".
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     *
     * @return {@code true} if outOfOrder migrations should be applied, {@code false} if not. (default: {@code false})
     */
    public boolean isOutOfOrder() {
        return outOfOrder;
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
     * @deprecated Use setValidateOnMigrate instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setValidationMode(ValidationMode validationMode) {
        LOG.warn("validationMode has been deprecated and will be removed in Flyway 3.0. Use validateOnMigrate instead.");
        validateOnMigrate = ValidationMode.ALL == validationMode;
    }

    /**
     * Sets the error mode for validation.
     *
     * @param validationErrorMode The error mode for validation. (default: FAIL)
     * @deprecated Use setCleanOnValidationError instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setValidationErrorMode(ValidationErrorMode validationErrorMode) {
        LOG.warn("validationErrorMode has been deprecated and will be removed in Flyway 3.0. Use cleanOnValidationError instead.");
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
        this.locations = new Locations(locations);
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
     * Sets the schemas managed by Flyway. These schema names are case-sensitive. (default: The default schema for the datasource connection)
     * <p>Consequences:</p>
     * <ul>
     * <li>The first schema in the list will be automatically set as the default one during the migration.</li>
     * <li>The first schema in the list will also be the one containing the metadata table.</li>
     * <li>The schemas will be cleaned in the order of this list.</li>
     * </ul>
     *
     * @param schemas The schemas managed by Flyway. May not be {@code null}. Must contain at least one element.
     */
    public void setSchemas(String... schemas) {
        this.schemaNames = schemas;
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
     * @deprecated Use setTarget(MigrationVersion) instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setTarget(SchemaVersion target) {
        LOG.warn("Flyway.setTarget(SchemaVersion) has been deprecated. Use setTarget(MigrationVersion) instead. Will be removed in Flyway 3.0.");
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
     * Sets the target version up to which Flyway should run migrations. Migrations with a higher version number will
     * not be applied.
     *
     * @param target The target version up to which Flyway should run migrations. Migrations with a higher version
     *               number will not be applied. (default: the latest version)
     */
    public void setTarget(String target) {
        this.target = new MigrationVersion(target);
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
     * Sets the datasource to use. Must have the necessary privileges to execute ddl.
     *
     * @param url      The JDBC URL of the database.
     * @param user     The user of the database.
     * @param password The password of the database.
     */
    public void setDataSource(String url, String user, String password) {
        this.dataSource = new DriverDataSource(null, url, user, password);
    }

    /**
     * Sets the version to tag an existing schema with when executing init. (default: 1)
     *
     * @param initialVersion The version to tag an existing schema with when executing init. (default: 1)
     * @deprecated Use setInitVersion(MigrationVersion) instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setInitialVersion(SchemaVersion initialVersion) {
        LOG.warn("Flyway.setInitialVersion(SchemaVersion) has been deprecated. Use setInitVersion(MigrationVersion) instead. Will be removed in Flyway 3.0.");
        this.initVersion = new MigrationVersion(initialVersion.toString());
    }

    /**
     * Sets the version to tag an existing schema with when executing init.
     *
     * @param initialVersion The version to tag an existing schema with when executing init. (default: 1)
     * @deprecated Use setInitVersion(MigrationVersion) instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setInitialVersion(MigrationVersion initialVersion) {
        LOG.warn("Flyway.setInitialVersion(MigrationVersion) has been deprecated. Use setInitVersion(MigrationVersion) instead. Will be removed in Flyway 3.0.");
        this.initVersion = initialVersion;
    }

    /**
     * Sets the version to tag an existing schema with when executing init.
     *
     * @param initialVersion The version to tag an existing schema with when executing init. (default: 1)
     * @deprecated Use setInitVersion(String) instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setInitialVersion(String initialVersion) {
        LOG.warn("Flyway.setInitialVersion(String) has been deprecated. Use setInitVersion(String) instead. Will be removed in Flyway 3.0.");
        this.initVersion = new MigrationVersion(initialVersion);
    }

    /**
     * Sets the description to tag an existing schema with when executing init.
     *
     * @param initialDescription The description to tag an existing schema with when executing init. (default: << Flyway Init >>)
     * @deprecated Use setInitDescription(String) instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setInitialDescription(String initialDescription) {
        LOG.warn("Flyway.setInitialDescription(String) has been deprecated. Use setInitDescription(String instead. Will be removed in Flyway 3.0.");
        this.initDescription = initialDescription;
    }

    /**
     * Sets the version to tag an existing schema with when executing init.
     *
     * @param initVersion The version to tag an existing schema with when executing init. (default: 1)
     */
    public void setInitVersion(MigrationVersion initVersion) {
        this.initVersion = initVersion;
    }

    /**
     * Sets the version to tag an existing schema with when executing init.
     *
     * @param initVersion The version to tag an existing schema with when executing init. (default: 1)
     */
    public void setInitVersion(String initVersion) {
        this.initVersion = new MigrationVersion(initVersion);
    }

    /**
     * Sets the description to tag an existing schema with when executing init.
     *
     * @param initDescription The description to tag an existing schema with when executing init. (default: << Flyway Init >>)
     */
    public void setInitDescription(String initDescription) {
        this.initDescription = initDescription;
    }

    /**
     * Flag to disable the check that a non-empty schema has been properly initialized with init. This check ensures
     * Flyway does not migrate or clean the wrong database in case of a configuration mistake. Be careful when disabling
     * this!
     *
     * @param disableInitCheck {@code true} if the check is disabled. {@code false} if it is active. (default: {@code false})
     * @deprecated Use initOnMigrate instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public void setDisableInitCheck(boolean disableInitCheck) {
        this.disableInitCheck = disableInitCheck;
    }

    /**
     * <p>
     * Whether to automatically call init when migrate is executed against a non-empty schema with no metadata table.
     * This schema will then be initialized with the {@code initialVersion} before executing the migrations.
     * Only migrations above {@code initialVersion} will then be applied.
     * </p>
     * <p>
     * This is useful for initial Flyway production deployments on projects with an existing DB.
     * </p>
     * <p>
     * Be careful when enabling this as it removes the safety net that ensures
     * Flyway does not migrate the wrong database in case of a configuration mistake!
     * </p>
     *
     * @param initOnMigrate {@code true} if init should be called on migrate for non-empty schemas, {@code false} if not. (default: {@code false})
     */
    public void setInitOnMigrate(boolean initOnMigrate) {
        this.initOnMigrate = initOnMigrate;
    }

    /**
     * Allows migrations to be run "out of order".
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     *
     * @param outOfOrder {@code true} if outOfOrder migrations should be applied, {@code false} if not. (default: {@code false})
     */
    public void setOutOfOrder(boolean outOfOrder) {
        this.outOfOrder = outOfOrder;
    }

    /**
     * Starts the database migration. All pending migrations will be applied in order.
     * Calling migrate on an up-to-date database has no effect.
     *
     * @return The number of successfully applied migrations.
     * @throws FlywayException when the migration failed.
     */
    public int migrate() throws FlywayException {
        return execute(new Command<Integer>() {
            public Integer execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport, Schema[] schemas) {
                MigrationResolver migrationResolver = createMigrationResolver();
                MetaDataTable metaDataTable =
                        new MetaDataTableImpl(dbSupport, schemas[0].getTable(table), migrationResolver);

                new DbSchemas(connectionMetaDataTable, schemas, metaDataTable).create();

                if (validateOnMigrate) {
                    doValidate(connectionMetaDataTable, dbSupport, migrationResolver, metaDataTable, schemas);
                }

                if (!metaDataTable.hasSchemasMarker() && !metaDataTable.hasInitMarker() && !metaDataTable.hasAppliedMigrations()) {
                    List<Schema> nonEmptySchemas = new ArrayList<Schema>();
                    for (Schema schema : schemas) {
                        if (!schema.empty()) {
                            nonEmptySchemas.add(schema);
                        }
                    }

                    if (initOnMigrate || disableInitCheck || nonEmptySchemas.isEmpty()) {
                        if (initOnMigrate && !nonEmptySchemas.isEmpty()) {
                            new DbInit(connectionMetaDataTable, metaDataTable, initVersion, initDescription).init();
                        }
                    } else {
                        if (nonEmptySchemas.size() == 1) {
                            Schema schema = nonEmptySchemas.get(0);
                            //Check whether we only have an empty metadata table in an otherwise empty schema
                            if (schema.allTables().length != 1 || !schema.getTable(table).exists()) {
                                throw new FlywayException("Found non-empty schema " + schema
                                        + " without metadata table! Use init() first to initialize the metadata table.");
                            }
                        } else {
                            throw new FlywayException("Found non-empty schemas "
                                    + StringUtils.collectionToCommaDelimitedString(nonEmptySchemas)
                                    + " without metadata table! Use init() first to initialize the metadata table.");
                        }
                    }
                }

                if (!schemas[0].equals(dbSupport.getCurrentSchema())) {
                    DbSupportFactory.createDbSupport(connectionUserObjects).setCurrentSchema(schemas[0]);
                }

                DbMigrate dbMigrator =
                        new DbMigrate(connectionMetaDataTable, connectionUserObjects, dbSupport, metaDataTable,
                                schemas[0], migrationResolver, target, ignoreFailedFutureMigration, outOfOrder);
                return dbMigrator.migrate();
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
            public Void execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport, Schema[] schemas) {
                MigrationResolver migrationResolver = createMigrationResolver();
                MetaDataTable metaDataTable =
                        new MetaDataTableImpl(dbSupport, schemas[0].getTable(table), migrationResolver);

                doValidate(connectionMetaDataTable, dbSupport, migrationResolver, metaDataTable, schemas);
                return null;
            }
        });
    }

    /**
     * Performs the actual validation. All set up must have taken place beforehand.
     *
     * @param connectionMetaDataTable The database connection for the metadata table.
     * @param dbSupport               The database-specific support for these connections.
     * @param migrationResolver       The migration resolver;
     * @param metaDataTable           The metadata table.
     * @param schemas                 The schemas managed by Flyway.
     */
    private void doValidate(Connection connectionMetaDataTable, DbSupport dbSupport, MigrationResolver migrationResolver,
                            MetaDataTable metaDataTable, Schema[] schemas) {
        String validationError =
                new DbValidate(connectionMetaDataTable, metaDataTable, migrationResolver, target, outOfOrder).validate();

        if (validationError != null) {
            if (cleanOnValidationError) {
                new DbClean(connectionMetaDataTable, metaDataTable, schemas).clean();
            } else {
                throw new FlywayException("Validate failed. Found differences between applied migrations and available migrations: " + validationError);
            }
        }
    }

    /**
     * Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas.
     * The schemas are cleaned in the order specified by the {@code schemas} property.
     *
     * @throws FlywayException when the clean fails.
     */
    public void clean() {
        execute(new Command<Void>() {
            public Void execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport, Schema[] schemas) {
                MetaDataTableImpl metaDataTable =
                        new MetaDataTableImpl(dbSupport, schemas[0].getTable(table), createMigrationResolver());
                new DbClean(connectionMetaDataTable, metaDataTable, schemas).clean();
                return null;
            }
        });
    }

    /**
     * Returns the status (current version) of the database.
     *
     * @return The latest applied migration, or {@code null} if no migration has been applied yet.
     * @deprecated Use flyway.info().current() instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public MetaDataTableRow status() {
        LOG.warn("Flyway.status() has been deprecated and will be removed in Flyway 3.0. Use Flyway.info().current() instead.");
        MigrationInfo current = info().current();
        if (current == null) {
            return null;
        }
        return new MetaDataTableRow(current);
    }

    /**
     * Returns the history (all applied migrations) of the database.
     *
     * @return All migrations applied to the database, sorted, oldest first. An empty list if none.
     * @deprecated Use flyway.info().applied() instead. Will be removed in Flyway 3.0.
     */
    @Deprecated
    public List<MetaDataTableRow> history() {
        LOG.warn("Flyway.history() has been deprecated and will be removed in Flyway 3.0. Use Flyway.info().applied() instead.");
        MigrationInfo[] migrationInfos = info().applied();

        List<MetaDataTableRow> metaDataTableRows = new ArrayList<MetaDataTableRow>();
        for (MigrationInfo migrationInfo : migrationInfos) {
            metaDataTableRows.add(new MetaDataTableRow(migrationInfo));
        }
        return metaDataTableRows;
    }

    /**
     * Retrieves the complete information about all the migrations including applied, pending and current migrations with
     * details and status.
     *
     * @return All migrations sorted by version, oldest first.
     * @throws FlywayException when the info retrieval failed.
     */
    public MigrationInfoService info() {
        return execute(new Command<MigrationInfoService>() {
            public MigrationInfoService execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport, Schema[] schemas) {
                MigrationResolver migrationResolver = createMigrationResolver();
                MetaDataTable metaDataTable =
                        new MetaDataTableImpl(dbSupport, schemas[0].getTable(table), migrationResolver);

                MigrationInfoServiceImpl migrationInfoService = new MigrationInfoServiceImpl(migrationResolver, metaDataTable, target, outOfOrder);
                migrationInfoService.refresh();
                return migrationInfoService;
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
            public Void execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport, Schema[] schemas) {
                MigrationResolver migrationResolver = createMigrationResolver();
                MetaDataTable metaDataTable =
                        new MetaDataTableImpl(dbSupport, schemas[0].getTable(table), migrationResolver);
                new DbSchemas(connectionMetaDataTable, schemas, metaDataTable).create();
                new DbInit(connectionMetaDataTable, metaDataTable, initVersion, initDescription).init();
                return null;
            }
        });
    }

    /**
     * Repairs the Flyway metadata table after a failed migration. User objects left behind must still be cleaned up
     * manually.
     *
     * @throws FlywayException when the metadata table repair failed.
     */
    public void repair() throws FlywayException {
        execute(new Command<Void>() {
            public Void execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport, Schema[] schemas) {
                new MetaDataTableImpl(dbSupport, schemas[0].getTable(table), createMigrationResolver()).repair();
                return null;
            }
        });
    }

    /**
     * @return A new, fully configured, MigrationResolver instance.
     */
    private MigrationResolver createMigrationResolver() {
        return new CompositeMigrationResolver(locations, encoding, sqlMigrationPrefix, sqlMigrationSuffix, placeholders, placeholderPrefix, placeholderSuffix);
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

        if (StringUtils.hasText(urlProp)) {
            setDataSource(new DriverDataSource(driverProp, urlProp, userProp, passwordProp));
        } else if (!StringUtils.hasText(urlProp) &&
                (StringUtils.hasText(driverProp) || StringUtils.hasText(userProp) || StringUtils.hasText(passwordProp))) {
            LOG.warn("Discarding INCOMPLETE dataSource configuration! flyway.url must be set.");
        }

        String locationsProp = properties.getProperty("flyway.locations");
        if (locationsProp != null) {
            setLocations(StringUtils.tokenizeToStringArray(locationsProp, ","));
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
        String initVersionProp = properties.getProperty("flyway.initVersion");
        if (initVersionProp != null) {
            setInitVersion(new MigrationVersion(initVersionProp));
        }
        String initDescriptionProp = properties.getProperty("flyway.initDescription");
        if (initDescriptionProp != null) {
            setInitDescription(initDescriptionProp);
        }
        String disableInitCheckProp = properties.getProperty("flyway.disableInitCheck");
        if (disableInitCheckProp != null) {
            setDisableInitCheck(Boolean.parseBoolean(disableInitCheckProp));
        }
        String initOnMigrateProp = properties.getProperty("flyway.initOnMigrate");
        if (initOnMigrateProp != null) {
            setInitOnMigrate(Boolean.parseBoolean(initOnMigrateProp));
        }
        String ignoreFailedFutureMigrationProp = properties.getProperty("flyway.ignoreFailedFutureMigration");
        if (ignoreFailedFutureMigrationProp != null) {
            setIgnoreFailedFutureMigration(Boolean.parseBoolean(ignoreFailedFutureMigrationProp));
        }
        String targetProp = properties.getProperty("flyway.target");
        if (targetProp != null) {
            setTarget(new MigrationVersion(targetProp));
        }
        String outOfOrderProp = properties.getProperty("flyway.outOfOrder");
        if (outOfOrderProp != null) {
            setOutOfOrder(Boolean.parseBoolean(outOfOrderProp));
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
            LOG.debug("DDL Transactions Supported: " + dbSupport.supportsDdlTransactions());

            if (schemaNames.length == 0) {
                setSchemas(dbSupport.getCurrentSchema().getName());
            }

            if (schemaNames.length == 1) {
                LOG.debug("Schema: " + schemaNames[0]);
            } else {
                LOG.debug("Schemas: " + StringUtils.arrayToCommaDelimitedString(schemaNames));
            }

            Schema[] schemas = new Schema[schemaNames.length];
            for (int i = 0; i < schemaNames.length; i++) {
                schemas[i] = dbSupport.getSchema(schemaNames[i]);
            }

            result = command.execute(connectionMetaDataTable, connectionUserObjects, dbSupport, schemas);
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
         * @param schemas                 The schemas managed by Flyway.
         * @return The result of the operation.
         */
        T execute(Connection connectionMetaDataTable, Connection connectionUserObjects, DbSupport dbSupport, Schema[] schemas);
    }
}
