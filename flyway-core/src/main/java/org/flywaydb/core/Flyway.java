/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core;


import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.SqlScriptFlywayCallback;
import org.flywaydb.core.internal.command.DbBaseline;
import org.flywaydb.core.internal.command.DbClean;
import org.flywaydb.core.internal.command.DbMigrate;
import org.flywaydb.core.internal.command.DbRepair;
import org.flywaydb.core.internal.command.DbSchemas;
import org.flywaydb.core.internal.command.DbValidate;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.metadatatable.MetaDataTableImpl;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.ConfigurationInjectionUtils;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.VersionPrinter;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Scanner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * This is the centre point of Flyway, and for most users, the only class they will ever have to deal with.
 * <p>
 * It is THE public API from which all important Flyway functions such as clean, validate and migrate can be called.
 * </p>
 */
public class Flyway implements FlywayConfiguration {
    private static final Log LOG = LogFactory.getLog(Flyway.class);

    /**
     * Property name prefix for placeholders that are configured through properties.
     */
    private static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";

    /**
     * The locations to scan recursively for migrations.
     * <p/>
     * <p>The location type is determined by its prefix.
     * Unprefixed locations or locations starting with {@code classpath:} point to a package on the classpath and may
     * contain both sql and java-based migrations.
     * Locations starting with {@code filesystem:} point to a directory on the filesystem and may only contain sql
     * migrations.</p>
     * <p/>
     * (default: db/migration)
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
     * The target version up to which Flyway should consider migrations. Migrations with a higher version number will
     * be ignored. The special value {@code current} designates the current version of the schema (default: the latest version)
     */
    private MigrationVersion target = MigrationVersion.LATEST;

    /**
     * Whether placeholders should be replaced. (default: true)
     */
    private boolean placeholderReplacement = true;

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
     * <p/>
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     */
    private String sqlMigrationPrefix = "V";

    /**
     * The file name prefix for repeatable sql migrations. (default: R)
     * <p/>
     * <p>Repeatable sql migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to R__My_description.sql</p>
     */
    private String repeatableSqlMigrationPrefix = "R";

    /**
     * The file name separator for sql migrations. (default: __)
     * <p/>
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     */
    private String sqlMigrationSeparator = "__";

    /**
     * The file name suffix for sql migrations. (default: .sql)
     * <p/>
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     */
    private String sqlMigrationSuffix = ".sql";

    /**
     * Ignore missing migrations when reading the metadata table. These are migrations that were performed by an
     * older deployment of the application that are no longer available in this version. For example: we have migrations
     * available on the classpath with versions 1.0 and 3.0. The metadata table indicates that a migration with version 2.0
     * (unknown to us) has also been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to deploy
     * a newer version of the application even though it doesn't contain migrations included with an older one anymore.
     *
     * {@code true} to continue normally and log a warning, {@code false} to fail fast with an exception.
     * (default: {@code false})
     */
    private boolean ignoreMissingMigrations;

    /**
     * Ignore future migrations when reading the metadata table. These are migrations that were performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
     * (unknown to us) has already been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to redeploy
     * an older version of the application after the database has been migrated by a newer one. (default: {@code true})
     */
    private boolean ignoreFutureMigrations = true;

    /**
     * Ignores failed future migrations when reading the metadata table. These are migrations that were performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
     * (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not
     * an option. An older version of the application can then be redeployed, even though a newer one failed due to a
     * bad migration. (default: {@code false})
     *
     * @deprecated Use the more generic <code>ignoreFutureMigrations</code> instead. Will be removed in Flyway 5.0.
     */
    @Deprecated
    private boolean ignoreFailedFutureMigration;

    /**
     * Whether to automatically call validate or not when running migrate. (default: {@code true})
     */
    private boolean validateOnMigrate = true;

    /**
     * Whether to automatically call clean or not when a validation error occurs. (default: {@code false})
     * <p> This is exclusively intended as a convenience for development. Even tough we
     * strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a
     * way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that
     * the next migration will bring you back to the state checked into SCM.</p>
     * <p><b>Warning ! Do not enable in production !</b></p>
     */
    private boolean cleanOnValidationError;

    /**
     * Whether to disable clean. (default: {@code false})
     * <p>This is especially useful for production environments where running clean can be quite a career limiting move.</p>
     */
    private boolean cleanDisabled;

    /**
     * The version to tag an existing schema with when executing baseline. (default: 1)
     */
    private MigrationVersion baselineVersion = MigrationVersion.fromVersion("1");

    /**
     * The description to tag an existing schema with when executing baseline. (default: &lt;&lt; Flyway Baseline &gt;&gt;)
     */
    private String baselineDescription = "<< Flyway Baseline >>";

    /**
     * <p>
     * Whether to automatically call baseline when migrate is executed against a non-empty schema with no metadata table.
     * This schema will then be initialized with the {@code baselineVersion} before executing the migrations.
     * Only migrations above {@code baselineVersion} will then be applied.
     * </p>
     * <p>
     * This is useful for initial Flyway production deployments on projects with an existing DB.
     * </p>
     * <p>
     * Be careful when enabling this as it removes the safety net that ensures
     * Flyway does not migrate the wrong database in case of a configuration mistake! (default: {@code false})
     * </p>
     */
    private boolean baselineOnMigrate;

    /**
     * Allows migrations to be run "out of order".
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     * <p>(default: {@code false})</p>
     */
    private boolean outOfOrder;

    /**
     * This is a list of custom callbacks that fire before and after tasks are executed.  You can
     * add as many custom callbacks as you want. (default: none)
     */
    private FlywayCallback[] callbacks = new FlywayCallback[0];

    /**
     * Whether Flyway should skip the default callbacks. If true, only custom callbacks are used.
     * <p>(default: false)</p>
     */
    private boolean skipDefaultCallbacks;

    /**
     * The custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
     * <p>(default: none)</p>
     */
    private MigrationResolver[] resolvers = new MigrationResolver[0];

    /**
     * Whether Flyway should skip the default resolvers. If true, only custom resolvers are used.
     * <p>(default: false)</p>
     */
    private boolean skipDefaultResolvers;

    /**
     * The dataSource to use to access the database. Must have the necessary privileges to execute ddl.
     */
    private DataSource dataSource;

    /**
     * The ClassLoader to use for resolving migrations on the classpath. (default: Thread.currentThread().getContextClassLoader() )
     */
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    /**
     * Whether the database connection info has already been printed in the logs.
     */
    private boolean dbConnectionInfoPrinted;

    /**
     * Whether to allow mixing transactional and non-transactional statements within the same migration.
     * <p>
     * {@code true} if mixed migrations should be allowed. {@code false} if an error should be thrown instead. (default: {@code false})
     */
    private boolean allowMixedMigrations;

    /**
     * The username that will be recorded in the metadata table as having applied the migration.
     * <p>
     * {@code null} for the current database user of the connection. (default: {@code null}).
     */
    private String installedBy;

    /**
     * Creates a new instance of Flyway. This is your starting point.
     */
    public Flyway() {
        // Do nothing
    }

    @Override
    public String[] getLocations() {
        String[] result = new String[locations.getLocations().size()];
        for (int i = 0; i < locations.getLocations().size(); i++) {
            result[i] = locations.getLocations().get(i).toString();
        }
        return result;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public String[] getSchemas() {
        return schemaNames;
    }

    @Override
    public String getTable() {
        return table;
    }

    @Override
    public MigrationVersion getTarget() {
        return target;
    }

    @Override
    public boolean isPlaceholderReplacement() {
        return placeholderReplacement;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return placeholders;
    }

    @Override
    public String getPlaceholderPrefix() {
        return placeholderPrefix;
    }

    @Override
    public String getPlaceholderSuffix() {
        return placeholderSuffix;
    }

    @Override
    public String getSqlMigrationPrefix() {
        return sqlMigrationPrefix;
    }

    @Override
    public String getRepeatableSqlMigrationPrefix() {
        return repeatableSqlMigrationPrefix;
    }

    @Override
    public String getSqlMigrationSeparator() {
        return sqlMigrationSeparator;
    }

    @Override
    public String getSqlMigrationSuffix() {
        return sqlMigrationSuffix;
    }

    @Override
    public boolean isIgnoreMissingMigrations() {
        return ignoreMissingMigrations;
    }

    @Override
    public boolean isIgnoreFutureMigrations() {
        return ignoreFutureMigrations;
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
     * (default: {@code false})
     * @deprecated Use the more generic <code>isIgnoreFutureMigration()</code> instead. Will be removed in Flyway 5.0.
     */
    @Deprecated
    public boolean isIgnoreFailedFutureMigration() {
        LOG.warn("ignoreFailedFutureMigration has been deprecated and will be removed in Flyway 5.0. Use the more generic ignoreFutureMigrations instead.");
        return ignoreFailedFutureMigration;
    }

    @Override
    public boolean isValidateOnMigrate() {
        return validateOnMigrate;
    }

    @Override
    public boolean isCleanOnValidationError() {
        return cleanOnValidationError;
    }

    @Override
    public boolean isCleanDisabled() {
        return cleanDisabled;
    }

    @Override
    public MigrationVersion getBaselineVersion() {
        return baselineVersion;
    }

    @Override
    public String getBaselineDescription() {
        return baselineDescription;
    }

    @Override
    public boolean isBaselineOnMigrate() {
        return baselineOnMigrate;
    }

    @Override
    public boolean isOutOfOrder() {
        return outOfOrder;
    }

    @Override
    public MigrationResolver[] getResolvers() {
        return resolvers;
    }

    @Override
    public boolean isSkipDefaultResolvers() {
        return skipDefaultResolvers;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public boolean isAllowMixedMigrations() {
        return allowMixedMigrations;
    }

    @Override
    public String getInstalledBy() {
        return installedBy;
    }

    /**
     * The username that will be recorded in the metadata table as having applied the migration.
     *
     * @param installedBy The username or {@code null} for the current database user of the connection. (default: {@code null}).
     */
    public void setInstalledBy(String installedBy) {
        if ("".equals(installedBy)) {
            installedBy = null;
        }
        this.installedBy = installedBy;
    }

    /**
     * Whether to allow mixing transactional and non-transactional statements within the same migration.
     *
     * @param allowMixedMigrations {@code true} if mixed migrations should be allowed. {@code false} if an error should be thrown instead. (default: {@code false})
     */
    public void setAllowMixedMigrations(boolean allowMixedMigrations) {
        this.allowMixedMigrations = allowMixedMigrations;
    }

    /**
     * Ignore missing migrations when reading the metadata table. These are migrations that were performed by an
     * older deployment of the application that are no longer available in this version. For example: we have migrations
     * available on the classpath with versions 1.0 and 3.0. The metadata table indicates that a migration with version 2.0
     * (unknown to us) has also been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to deploy
     * a newer version of the application even though it doesn't contain migrations included with an older one anymore.
     *
     * @param ignoreMissingMigrations {@code true} to continue normally and log a warning, {@code false} to fail fast with an exception.
     * (default: {@code false})
     */
    public void setIgnoreMissingMigrations(boolean ignoreMissingMigrations) {
        this.ignoreMissingMigrations = ignoreMissingMigrations;
    }

    /**
     * Whether to ignore future migrations when reading the metadata table. These are migrations that were performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0
     * (unknown to us) has already been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to redeploy
     * an older version of the application after the database has been migrated by a newer one.
     *
     * @param ignoreFutureMigrations {@code true} to continue normally and log a warning, {@code false} to fail
     *                               fast with an exception. (default: {@code true})
     */
    public void setIgnoreFutureMigrations(boolean ignoreFutureMigrations) {
        this.ignoreFutureMigrations = ignoreFutureMigrations;
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
     * @deprecated Use the more generic <code>setIgnoreFutureMigrations()</code> instead. Will be removed in Flyway 5.0.
     */
    @Deprecated
    public void setIgnoreFailedFutureMigration(boolean ignoreFailedFutureMigration) {
        LOG.warn("ignoreFailedFutureMigration has been deprecated and will be removed in Flyway 5.0. Use the more generic ignoreFutureMigrations instead.");
        this.ignoreFailedFutureMigration = ignoreFailedFutureMigration;
    }

    /**
     * Whether to automatically call validate or not when running migrate.
     *
     * @param validateOnMigrate {@code true} if validate should be called. {@code false} if not. (default: {@code true})
     */
    public void setValidateOnMigrate(boolean validateOnMigrate) {
        this.validateOnMigrate = validateOnMigrate;
    }

    /**
     * Whether to automatically call clean or not when a validation error occurs.
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
     * Whether to disable clean.
     * <p>This is especially useful for production environments where running clean can be quite a career limiting move.</p>
     *
     * @param cleanDisabled {@code true} to disabled clean. {@code false} to leave it enabled.  (default: {@code false})
     */
    public void setCleanDisabled(boolean cleanDisabled) {
        this.cleanDisabled = cleanDisabled;
    }

    /**
     * Sets the locations to scan recursively for migrations.
     * <p/>
     * <p>The location type is determined by its prefix.
     * Unprefixed locations or locations starting with {@code classpath:} point to a package on the classpath and may
     * contain both sql and java-based migrations.
     * Locations starting with {@code filesystem:} point to a directory on the filesystem and may only contain sql
     * migrations.</p>
     *
     * @param locations Locations to scan recursively for migrations. (default: db/migration)
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
     * Sets the target version up to which Flyway should consider migrations. Migrations with a higher version number will
     * be ignored.
     *
     * @param target The target version up to which Flyway should consider migrations. (default: the latest version)
     */
    public void setTarget(MigrationVersion target) {
        this.target = target;
    }

    /**
     * Sets the target version up to which Flyway should consider migrations.
     * Migrations with a higher version number will be ignored.
     *
     * @param target The target version up to which Flyway should consider migrations.
     *               The special value {@code current} designates the current version of the schema. (default: the latest
     *               version)
     */
    public void setTargetAsString(String target) {
        this.target = MigrationVersion.fromVersion(target);
    }

    /**
     * Sets whether placeholders should be replaced.
     *
     * @param placeholderReplacement Whether placeholders should be replaced. (default: true)
     */
    public void setPlaceholderReplacement(boolean placeholderReplacement) {
        this.placeholderReplacement = placeholderReplacement;
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
        if (!StringUtils.hasLength(placeholderPrefix)) {
            throw new FlywayException("placeholderPrefix cannot be empty!");
        }
        this.placeholderPrefix = placeholderPrefix;
    }

    /**
     * Sets the suffix of every placeholder.
     *
     * @param placeholderSuffix The suffix of every placeholder. (default: } )
     */
    public void setPlaceholderSuffix(String placeholderSuffix) {
        if (!StringUtils.hasLength(placeholderSuffix)) {
            throw new FlywayException("placeholderSuffix cannot be empty!");
        }
        this.placeholderSuffix = placeholderSuffix;
    }

    /**
     * Sets the file name prefix for sql migrations.
     * <p/>
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     *
     * @param sqlMigrationPrefix The file name prefix for sql migrations (default: V)
     */
    public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
        this.sqlMigrationPrefix = sqlMigrationPrefix;
    }

    /**
     * Sets the file name prefix for repeatable sql migrations.
     * <p/>
     * <p>Repeatable sql migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to R__My_description.sql</p>
     *
     * @param repeatableSqlMigrationPrefix The file name prefix for repeatable sql migrations (default: R)
     */
    public void setRepeatableSqlMigrationPrefix(String repeatableSqlMigrationPrefix) {
        this.repeatableSqlMigrationPrefix = repeatableSqlMigrationPrefix;
    }

    /**
     * Sets the file name separator for sql migrations.
     * <p/>
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     *
     * @param sqlMigrationSeparator The file name separator for sql migrations (default: __)
     */
    public void setSqlMigrationSeparator(String sqlMigrationSeparator) {
        if (!StringUtils.hasLength(sqlMigrationSeparator)) {
            throw new FlywayException("sqlMigrationSeparator cannot be empty!");
        }

        this.sqlMigrationSeparator = sqlMigrationSeparator;
    }

    /**
     * Sets the file name suffix for sql migrations.
     * <p/>
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
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
     * <p/>
     * <p>To use a custom ClassLoader, setClassLoader() must be called prior to calling this method.</p>
     *
     * @param url      The JDBC URL of the database.
     * @param user     The user of the database.
     * @param password The password of the database.
     * @param initSqls The (optional) sql statements to execute to initialize a connection immediately after obtaining it.
     */
    public void setDataSource(String url, String user, String password, String... initSqls) {
        this.dataSource = new DriverDataSource(classLoader, null, url, user, password, null, initSqls);
    }

    /**
     * Sets the ClassLoader to use for resolving migrations on the classpath.
     *
     * @param classLoader The ClassLoader to use for resolving migrations on the classpath. (default: Thread.currentThread().getContextClassLoader() )
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Sets the version to tag an existing schema with when executing baseline.
     *
     * @param baselineVersion The version to tag an existing schema with when executing baseline. (default: 1)
     */
    public void setBaselineVersion(MigrationVersion baselineVersion) {
        this.baselineVersion = baselineVersion;
    }

    /**
     * Sets the version to tag an existing schema with when executing baseline.
     *
     * @param baselineVersion The version to tag an existing schema with when executing baseline. (default: 1)
     */
    public void setBaselineVersionAsString(String baselineVersion) {
        this.baselineVersion = MigrationVersion.fromVersion(baselineVersion);
    }

    /**
     * Sets the description to tag an existing schema with when executing baseline.
     *
     * @param baselineDescription The description to tag an existing schema with when executing baseline. (default: &lt;&lt; Flyway Baseline &gt;&gt;)
     */
    public void setBaselineDescription(String baselineDescription) {
        this.baselineDescription = baselineDescription;
    }

    /**
     * <p>
     * Whether to automatically call baseline when migrate is executed against a non-empty schema with no metadata table.
     * This schema will then be baselined with the {@code baselineVersion} before executing the migrations.
     * Only migrations above {@code baselineVersion} will then be applied.
     * </p>
     * <p>
     * This is useful for initial Flyway production deployments on projects with an existing DB.
     * </p>
     * <p>
     * Be careful when enabling this as it removes the safety net that ensures
     * Flyway does not migrate the wrong database in case of a configuration mistake!
     * </p>
     *
     * @param baselineOnMigrate {@code true} if baseline should be called on migrate for non-empty schemas, {@code false} if not. (default: {@code false})
     */
    public void setBaselineOnMigrate(boolean baselineOnMigrate) {
        this.baselineOnMigrate = baselineOnMigrate;
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
     * Gets the callbacks for lifecycle notifications.
     *
     * @return The callbacks for lifecycle notifications. An empty array if none. (default: none)
     */
    @Override
    public FlywayCallback[] getCallbacks() {
        return callbacks;
    }

    @Override
    public boolean isSkipDefaultCallbacks() {
        return skipDefaultCallbacks;
    }

    /**
     * Set the callbacks for lifecycle notifications.
     *
     * @param callbacks The callbacks for lifecycle notifications. (default: none)
     */
    public void setCallbacks(FlywayCallback... callbacks) {
        this.callbacks = callbacks;
    }

    /**
     * Set the callbacks for lifecycle notifications.
     *
     * @param callbacks The fully qualified class names of the callbacks for lifecycle notifications. (default: none)
     */
    public void setCallbacksAsClassNames(String... callbacks) {
        List<FlywayCallback> callbackList = ClassUtils.instantiateAll(callbacks, classLoader);
        setCallbacks(callbackList.toArray(new FlywayCallback[callbacks.length]));
    }

    /**
     * Whether Flyway should skip the default callbacks. If true, only custom callbacks are used.
     *
     * @param skipDefaultCallbacks Whether default built-in callbacks should be skipped. <p>(default: false)</p>
     */
    public void setSkipDefaultCallbacks(boolean skipDefaultCallbacks) {
        this.skipDefaultCallbacks = skipDefaultCallbacks;
    }

    /**
     * Sets custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
     *
     * @param resolvers The custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply. (default: empty list)
     */
    public void setResolvers(MigrationResolver... resolvers) {
        this.resolvers = resolvers;
    }

    /**
     * Sets custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
     *
     * @param resolvers The fully qualified class names of the custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply. (default: empty list)
     */
    public void setResolversAsClassNames(String... resolvers) {
        List<MigrationResolver> resolverList = ClassUtils.instantiateAll(resolvers, classLoader);
        setResolvers(resolverList.toArray(new MigrationResolver[resolvers.length]));
    }

    /**
     * Whether Flyway should skip the default resolvers. If true, only custom resolvers are used.
     *
     * @param skipDefaultResolvers Whether default built-in resolvers should be skipped. <p>(default: false)</p>
     */
    public void setSkipDefaultResolvers(boolean skipDefaultResolvers) {
        this.skipDefaultResolvers = skipDefaultResolvers;
    }

    /**
     * <p>Starts the database migration. All pending migrations will be applied in order.
     * Calling migrate on an up-to-date database has no effect.</p>
     * <img src="https://flywaydb.org/assets/balsamiq/command-migrate.png" alt="migrate">
     *
     * @return The number of successfully applied migrations.
     * @throws FlywayException when the migration failed.
     */
    public int migrate() throws FlywayException {
        return execute(new Command<Integer>() {
            public Integer execute(Connection connectionMetaDataTable,
                                   MigrationResolver migrationResolver, MetaDataTable metaDataTable, DbSupport dbSupport, Schema[] schemas, FlywayCallback[] flywayCallbacks) {
                if (validateOnMigrate) {
                    doValidate(connectionMetaDataTable, dbSupport, migrationResolver, metaDataTable, schemas, flywayCallbacks, true);
                }

                new DbSchemas(connectionMetaDataTable, schemas, metaDataTable).create();

                if (!metaDataTable.exists()) {
                    List<Schema> nonEmptySchemas = new ArrayList<Schema>();
                    for (Schema schema : schemas) {
                        if (!schema.empty()) {
                            nonEmptySchemas.add(schema);
                        }
                    }

                    if (!nonEmptySchemas.isEmpty()) {
                        if (baselineOnMigrate) {
                            new DbBaseline(connectionMetaDataTable, dbSupport, metaDataTable, schemas[0], baselineVersion, baselineDescription, flywayCallbacks).baseline();
                        } else {
                            // Second check for MySQL which is sometimes flaky otherwise
                            if (!metaDataTable.exists()) {
                                throw new FlywayException("Found non-empty schema(s) "
                                        + StringUtils.collectionToCommaDelimitedString(nonEmptySchemas)
                                        + " without metadata table! Use baseline()"
                                        + " or set baselineOnMigrate to true to initialize the metadata table.");
                            }
                        }
                    }
                }

                Connection connectionUserObjects = null;
                try {
                    connectionUserObjects =
                            dbSupport.useSingleConnection() ? connectionMetaDataTable : JdbcUtils.openConnection(dataSource);
                    DbMigrate dbMigrate =
                            new DbMigrate(connectionUserObjects, dbSupport, metaDataTable,
                                    schemas[0], migrationResolver, ignoreFailedFutureMigration, Flyway.this);
                    return dbMigrate.migrate();
                } finally {
                    if (!dbSupport.useSingleConnection()) {
                        JdbcUtils.closeConnection(connectionUserObjects);
                    }
                }
            }
        });
    }

    /**
     * <p>Validate applied migrations against resolved ones (on the filesystem or classpath)
     * to detect accidental changes that may prevent the schema(s) from being recreated exactly.</p>
     * <p>Validation fails if</p>
     * <ul>
     * <li>differences in migration names, types or checksums are found</li>
     * <li>versions have been applied that aren't resolved locally anymore</li>
     * <li>versions have been resolved that haven't been applied yet</li>
     * </ul>
     * <p>
     * <img src="https://flywaydb.org/assets/balsamiq/command-validate.png" alt="validate">
     *
     * @throws FlywayException when the validation failed.
     */
    public void validate() throws FlywayException {
        execute(new Command<Void>() {
            public Void execute(Connection connectionMetaDataTable,
                                MigrationResolver migrationResolver, MetaDataTable metaDataTable, DbSupport dbSupport, Schema[] schemas, FlywayCallback[] flywayCallbacks) {
                doValidate(connectionMetaDataTable, dbSupport, migrationResolver, metaDataTable, schemas, flywayCallbacks, false);
                return null;
            }
        });
    }

    /**
     * Performs the actual validation. All set up must have taken place beforehand.
     *
     * @param connectionMetaDataTable The database connection for the metadata table.
     * @param dbSupport               The database-specific support.
     * @param migrationResolver       The migration resolver;
     * @param metaDataTable           The metadata table.
     * @param schemas                 The schemas managed by Flyway.
     * @param pending                 Whether pending migrations are ok.
     */
    private void doValidate(Connection connectionMetaDataTable, DbSupport dbSupport, MigrationResolver migrationResolver,
                            MetaDataTable metaDataTable, Schema[] schemas, FlywayCallback[] flywayCallbacks, boolean pending) {
        String validationError =
                new DbValidate(connectionMetaDataTable, dbSupport, metaDataTable, schemas[0], migrationResolver,
                        target, outOfOrder, pending, ignoreMissingMigrations, ignoreFutureMigrations, flywayCallbacks).validate();

        if (validationError != null) {
            if (cleanOnValidationError) {
                new DbClean(connectionMetaDataTable, dbSupport, metaDataTable, schemas, flywayCallbacks, cleanDisabled).clean();
                metaDataTable.clearCache();
            } else {
                throw new FlywayException("Validate failed: " + validationError);
            }
        }
    }

    /**
     * <p>Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas.
     * The schemas are cleaned in the order specified by the {@code schemas} property.</p>
     * <img src="https://flywaydb.org/assets/balsamiq/command-clean.png" alt="clean">
     *
     * @throws FlywayException when the clean fails.
     */
    public void clean() {
        execute(new Command<Void>() {
            public Void execute(Connection connectionMetaDataTable,
                                MigrationResolver migrationResolver, MetaDataTable metaDataTable, DbSupport dbSupport, Schema[] schemas,
                                FlywayCallback[] flywayCallbacks) {
                new DbClean(connectionMetaDataTable, dbSupport, metaDataTable, schemas, flywayCallbacks, cleanDisabled).clean();
                return null;
            }
        });
    }

    /**
     * <p>Retrieves the complete information about all the migrations including applied, pending and current migrations with
     * details and status.</p>
     * <img src="https://flywaydb.org/assets/balsamiq/command-info.png" alt="info">
     *
     * @return All migrations sorted by version, oldest first.
     * @throws FlywayException when the info retrieval failed.
     */
    public MigrationInfoService info() {
        return execute(new Command<MigrationInfoService>() {
            public MigrationInfoService execute(final Connection connectionMetaDataTable,
                                                MigrationResolver migrationResolver, MetaDataTable metaDataTable, final DbSupport dbSupport, final Schema[] schemas, FlywayCallback[] flywayCallbacks) {
                try {
                    for (final FlywayCallback callback : flywayCallbacks) {
                        new TransactionTemplate(connectionMetaDataTable).execute(new Callable<Object>() {
                            @Override
                            public Object call() throws SQLException {
                                dbSupport.changeCurrentSchemaTo(schemas[0]);
                                callback.beforeInfo(connectionMetaDataTable);
                                return null;
                            }
                        });
                    }

                    MigrationInfoServiceImpl migrationInfoService =
                            new MigrationInfoServiceImpl(migrationResolver, metaDataTable, target, outOfOrder,
                                    true, true, true);
                    migrationInfoService.refresh();

                    for (final FlywayCallback callback : flywayCallbacks) {
                        new TransactionTemplate(connectionMetaDataTable).execute(new Callable<Object>() {
                            @Override
                            public Object call() throws SQLException {
                                dbSupport.changeCurrentSchemaTo(schemas[0]);
                                callback.afterInfo(connectionMetaDataTable);
                                return null;
                            }
                        });
                    }

                    return migrationInfoService;
                } finally {
                    dbSupport.restoreCurrentSchema();
                }
            }
        });
    }

    /**
     * <p>Baselines an existing database, excluding all migrations up to and including baselineVersion.</p>
     * <p/>
     * <img src="https://flywaydb.org/assets/balsamiq/command-baseline.png" alt="baseline">
     *
     * @throws FlywayException when the schema baselining failed.
     */
    public void baseline() throws FlywayException {
        execute(new Command<Void>() {
            public Void execute(Connection connectionMetaDataTable, MigrationResolver migrationResolver, MetaDataTable metaDataTable, DbSupport dbSupport, Schema[] schemas, FlywayCallback[] flywayCallbacks) {
                new DbSchemas(connectionMetaDataTable, schemas, metaDataTable).create();
                new DbBaseline(connectionMetaDataTable, dbSupport, metaDataTable, schemas[0], baselineVersion, baselineDescription, flywayCallbacks).baseline();
                return null;
            }
        });
    }

    /**
     * Repairs the Flyway metadata table. This will perform the following actions:
     * <ul>
     * <li>Remove any failed migrations on databases without DDL transactions (User objects left behind must still be cleaned up manually)</li>
     * <li>Correct wrong checksums</li>
     * </ul>
     * <img src="https://flywaydb.org/assets/balsamiq/command-repair.png" alt="repair">
     *
     * @throws FlywayException when the metadata table repair failed.
     */
    public void repair() throws FlywayException {
        execute(new Command<Void>() {
            public Void execute(Connection connectionMetaDataTable, MigrationResolver migrationResolver, MetaDataTable metaDataTable, DbSupport dbSupport, Schema[] schemas, FlywayCallback[] flywayCallbacks) {
                new DbRepair(dbSupport, connectionMetaDataTable, schemas[0], migrationResolver, metaDataTable, flywayCallbacks).repair();
                return null;
            }
        });
    }

    /**
     * Creates the MigrationResolver.
     *
     * @param dbSupport The database-specific support.
     * @param scanner   The Scanner for resolving migrations.
     * @return A new, fully configured, MigrationResolver instance.
     */
    private MigrationResolver createMigrationResolver(DbSupport dbSupport, Scanner scanner) {
        for (MigrationResolver resolver : resolvers) {
            ConfigurationInjectionUtils.injectFlywayConfiguration(resolver, this);
        }

        return new CompositeMigrationResolver(dbSupport, scanner, this, locations, createPlaceholderReplacer(), resolvers);
    }

    /**
     * @return A new, fully configured, PlaceholderReplacer.
     */
    private PlaceholderReplacer createPlaceholderReplacer() {
        if (placeholderReplacement) {
            return new PlaceholderReplacer(placeholders, placeholderPrefix, placeholderSuffix);
        }
        return PlaceholderReplacer.NO_PLACEHOLDERS;
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration. Property names are
     * documented in the flyway maven plugin.
     * <p/>
     * <p>To use a custom ClassLoader, setClassLoader() must be called prior to calling this method.</p>
     *
     * @param properties Properties used for configuration.
     * @throws FlywayException when the configuration failed.
     */
    @SuppressWarnings("ConstantConditions")
    public void configure(Properties properties) {
        Map<String, String> props = new HashMap<String, String>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            props.put(entry.getKey().toString(), entry.getValue().toString());
        }

        configure(props);
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration. Property names are
     * documented in the flyway maven plugin.
     * <p/>
     * <p>To use a custom ClassLoader, setClassLoader() must be called prior to calling this method.</p>
     *
     * @param props Properties used for configuration.
     * @throws FlywayException when the configuration failed.
     */
    public void configure(Map<String, String> props) {
        String driverProp = getValueAndRemoveEntry(props, "flyway.driver");
        String urlProp = getValueAndRemoveEntry(props, "flyway.url");
        String userProp = getValueAndRemoveEntry(props, "flyway.user");
        String passwordProp = getValueAndRemoveEntry(props, "flyway.password");

        if (StringUtils.hasText(urlProp)) {
            setDataSource(new DriverDataSource(classLoader, driverProp, urlProp, userProp, passwordProp, null));
        } else if (!StringUtils.hasText(urlProp) &&
                (StringUtils.hasText(driverProp) || StringUtils.hasText(userProp) || StringUtils.hasText(passwordProp))) {
            LOG.warn("Discarding INCOMPLETE dataSource configuration! flyway.url must be set.");
        }

        String locationsProp = getValueAndRemoveEntry(props, "flyway.locations");
        if (locationsProp != null) {
            setLocations(StringUtils.tokenizeToStringArray(locationsProp, ","));
        }
        String placeholderReplacementProp = getValueAndRemoveEntry(props, "flyway.placeholderReplacement");
        if (placeholderReplacementProp != null) {
            setPlaceholderReplacement(Boolean.parseBoolean(placeholderReplacementProp));
        }
        String placeholderPrefixProp = getValueAndRemoveEntry(props, "flyway.placeholderPrefix");
        if (placeholderPrefixProp != null) {
            setPlaceholderPrefix(placeholderPrefixProp);
        }
        String placeholderSuffixProp = getValueAndRemoveEntry(props, "flyway.placeholderSuffix");
        if (placeholderSuffixProp != null) {
            setPlaceholderSuffix(placeholderSuffixProp);
        }
        String sqlMigrationPrefixProp = getValueAndRemoveEntry(props, "flyway.sqlMigrationPrefix");
        if (sqlMigrationPrefixProp != null) {
            setSqlMigrationPrefix(sqlMigrationPrefixProp);
        }
        String repeatableSqlMigrationPrefixProp = getValueAndRemoveEntry(props, "flyway.repeatableSqlMigrationPrefix");
        if (repeatableSqlMigrationPrefixProp != null) {
            setRepeatableSqlMigrationPrefix(repeatableSqlMigrationPrefixProp);
        }
        String sqlMigrationSeparatorProp = getValueAndRemoveEntry(props, "flyway.sqlMigrationSeparator");
        if (sqlMigrationSeparatorProp != null) {
            setSqlMigrationSeparator(sqlMigrationSeparatorProp);
        }
        String sqlMigrationSuffixProp = getValueAndRemoveEntry(props, "flyway.sqlMigrationSuffix");
        if (sqlMigrationSuffixProp != null) {
            setSqlMigrationSuffix(sqlMigrationSuffixProp);
        }
        String encodingProp = getValueAndRemoveEntry(props, "flyway.encoding");
        if (encodingProp != null) {
            setEncoding(encodingProp);
        }
        String schemasProp = getValueAndRemoveEntry(props, "flyway.schemas");
        if (schemasProp != null) {
            setSchemas(StringUtils.tokenizeToStringArray(schemasProp, ","));
        }
        String tableProp = getValueAndRemoveEntry(props, "flyway.table");
        if (tableProp != null) {
            setTable(tableProp);
        }
        String cleanOnValidationErrorProp = getValueAndRemoveEntry(props, "flyway.cleanOnValidationError");
        if (cleanOnValidationErrorProp != null) {
            setCleanOnValidationError(Boolean.parseBoolean(cleanOnValidationErrorProp));
        }
        String cleanDisabledProp = getValueAndRemoveEntry(props, "flyway.cleanDisabled");
        if (cleanDisabledProp != null) {
            setCleanDisabled(Boolean.parseBoolean(cleanDisabledProp));
        }
        String validateOnMigrateProp = getValueAndRemoveEntry(props, "flyway.validateOnMigrate");
        if (validateOnMigrateProp != null) {
            setValidateOnMigrate(Boolean.parseBoolean(validateOnMigrateProp));
        }
        String baselineVersionProp = getValueAndRemoveEntry(props, "flyway.baselineVersion");
        if (baselineVersionProp != null) {
            setBaselineVersion(MigrationVersion.fromVersion(baselineVersionProp));
        }
        String baselineDescriptionProp = getValueAndRemoveEntry(props, "flyway.baselineDescription");
        if (baselineDescriptionProp != null) {
            setBaselineDescription(baselineDescriptionProp);
        }
        String baselineOnMigrateProp = getValueAndRemoveEntry(props, "flyway.baselineOnMigrate");
        if (baselineOnMigrateProp != null) {
            setBaselineOnMigrate(Boolean.parseBoolean(baselineOnMigrateProp));
        }
        String ignoreMissingMigrationsProp = getValueAndRemoveEntry(props, "flyway.ignoreMissingMigrations");
        if (ignoreMissingMigrationsProp != null) {
            setIgnoreMissingMigrations(Boolean.parseBoolean(ignoreMissingMigrationsProp));
        }
        String ignoreFutureMigrationsProp = getValueAndRemoveEntry(props, "flyway.ignoreFutureMigrations");
        if (ignoreFutureMigrationsProp != null) {
            setIgnoreFutureMigrations(Boolean.parseBoolean(ignoreFutureMigrationsProp));
        }
        String ignoreFailedFutureMigrationProp = getValueAndRemoveEntry(props, "flyway.ignoreFailedFutureMigration");
        if (ignoreFailedFutureMigrationProp != null) {
            setIgnoreFailedFutureMigration(Boolean.parseBoolean(ignoreFailedFutureMigrationProp));
        }
        String targetProp = getValueAndRemoveEntry(props, "flyway.target");
        if (targetProp != null) {
            setTarget(MigrationVersion.fromVersion(targetProp));
        }
        String outOfOrderProp = getValueAndRemoveEntry(props, "flyway.outOfOrder");
        if (outOfOrderProp != null) {
            setOutOfOrder(Boolean.parseBoolean(outOfOrderProp));
        }
        String resolversProp = getValueAndRemoveEntry(props, "flyway.resolvers");
        if (StringUtils.hasLength(resolversProp)) {
            setResolversAsClassNames(StringUtils.tokenizeToStringArray(resolversProp, ","));
        }
        String skipDefaultResolversProp = getValueAndRemoveEntry(props, "flyway.skipDefaultResolvers");
        if (skipDefaultResolversProp != null) {
            setSkipDefaultResolvers(Boolean.parseBoolean(skipDefaultResolversProp));
        }
        String callbacksProp = getValueAndRemoveEntry(props, "flyway.callbacks");
        if (StringUtils.hasLength(callbacksProp)) {
            setCallbacksAsClassNames(StringUtils.tokenizeToStringArray(callbacksProp, ","));
        }
        String skipDefaultCallbacksProp = getValueAndRemoveEntry(props, "flyway.skipDefaultCallbacks");
        if (skipDefaultCallbacksProp != null) {
            setSkipDefaultCallbacks(Boolean.parseBoolean(skipDefaultCallbacksProp));
        }

        Map<String, String> placeholdersFromProps = new HashMap<String, String>(placeholders);
        Iterator<Map.Entry<String, String>> iterator = props.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String propertyName = entry.getKey();

            if (propertyName.startsWith(PLACEHOLDERS_PROPERTY_PREFIX)
                    && propertyName.length() > PLACEHOLDERS_PROPERTY_PREFIX.length()) {
                String placeholderName = propertyName.substring(PLACEHOLDERS_PROPERTY_PREFIX.length());
                String placeholderValue = entry.getValue();
                placeholdersFromProps.put(placeholderName, placeholderValue);
                iterator.remove();
            }
        }
        setPlaceholders(placeholdersFromProps);

        String allowMixedMigrationsProp = getValueAndRemoveEntry(props, "flyway.allowMixedMigrations");
        if (allowMixedMigrationsProp != null) {
            setAllowMixedMigrations(Boolean.parseBoolean(allowMixedMigrationsProp));
        }

        String installedByProp = getValueAndRemoveEntry(props, "flyway.installedBy");
        if (installedByProp != null) {
            setInstalledBy(installedByProp);
        }

        for (String key : props.keySet()) {
            if (key.startsWith("flyway.")) {
                LOG.warn("Unknown configuration property: " + key);
            }
        }
    }

    /**
     * Retrieves the value for this key in this map and removes the corresponding entry from the map.
     *
     * @param map The map.
     * @param key The key.
     * @return The value. {@code null} if not found.
     */
    private String getValueAndRemoveEntry(Map<String, String> map, String key) {
        String value = map.get(key);
        map.remove(key);
        return value;
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

        VersionPrinter.printVersion();

        Connection connectionMetaDataTable = null;

        try {
            if (dataSource == null) {
                throw new FlywayException("Unable to connect to the database. Configure the url, user and password!");
            }

            connectionMetaDataTable = JdbcUtils.openConnection(dataSource);

            DbSupport dbSupport = DbSupportFactory.createDbSupport(connectionMetaDataTable, !dbConnectionInfoPrinted);
            dbConnectionInfoPrinted = true;
            LOG.debug("DDL Transactions Supported: " + dbSupport.supportsDdlTransactions());

            if (schemaNames.length == 0) {
                Schema currentSchema = dbSupport.getOriginalSchema();
                if (currentSchema == null) {
                    throw new FlywayException("Unable to determine schema for the metadata table." +
                            " Set a default schema for the connection or specify one using the schemas property!");
                }
                setSchemas(currentSchema.getName());
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

            Scanner scanner = new Scanner(classLoader);
            MigrationResolver migrationResolver = createMigrationResolver(dbSupport, scanner);

            if (!skipDefaultCallbacks) {
                Set<FlywayCallback> flywayCallbacks = new LinkedHashSet<FlywayCallback>(Arrays.asList(callbacks));
                flywayCallbacks.add(
                        new SqlScriptFlywayCallback(dbSupport, scanner, locations, createPlaceholderReplacer(), this));
                callbacks = flywayCallbacks.toArray(new FlywayCallback[flywayCallbacks.size()]);
            }

            for (FlywayCallback callback : callbacks) {
                ConfigurationInjectionUtils.injectFlywayConfiguration(callback, this);
            }

            MetaDataTable metaDataTable = new MetaDataTableImpl(dbSupport, schemas[0].getTable(table), installedBy);
            if (metaDataTable.upgradeIfNecessary()) {
                new DbRepair(dbSupport, connectionMetaDataTable, schemas[0], migrationResolver, metaDataTable, callbacks).repairChecksumsAndDescriptions();
                LOG.info("Metadata table " + table + " successfully upgraded to the Flyway 4.0 format.");
            }

            result = command.execute(connectionMetaDataTable, migrationResolver, metaDataTable, dbSupport, schemas, callbacks);
        } finally {
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
         * @param migrationResolver       The migration resolver to use.
         * @param metaDataTable           The metadata table.
         * @param dbSupport               The database-specific support for these connections.
         * @param schemas                 The schemas managed by Flyway.   @return The result of the operation.
         * @param flywayCallbacks         The callbacks to use.
         */
        T execute(Connection connectionMetaDataTable, MigrationResolver migrationResolver, MetaDataTable metaDataTable, DbSupport dbSupport, Schema[] schemas, FlywayCallback[] flywayCallbacks);
    }
}
