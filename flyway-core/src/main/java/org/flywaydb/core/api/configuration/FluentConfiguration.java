/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.api.configuration;

import lombok.experimental.Delegate;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.*;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.configuration.resolvers.ProvisionerMode;
import org.flywaydb.core.internal.util.ClassUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

/**
 * Fluent configuration for Flyway. This is the preferred means of configuring the Flyway API.
 * This configuration can be passed to Flyway using the <code>new Flyway(Configuration)</code> constructor.
 */
public class FluentConfiguration implements Configuration {
    @Delegate(types = Configuration.class)
    private final ClassicConfiguration config;

    public FluentConfiguration() {
        config = new ClassicConfiguration();
    }

    /**
     * @param classLoader The ClassLoader to use for loading migrations, resolvers, etc from the classpath. (default: Thread.currentThread().getContextClassLoader() )
     */
    public FluentConfiguration(ClassLoader classLoader) {
        config = new ClassicConfiguration(classLoader);
    }

    /**
     * @return The new fully-configured Flyway instance.
     */
    public Flyway load() {
        return new Flyway(this);
    }

    /**
     * Configure with the same values as this existing configuration.
     */
    public FluentConfiguration configuration(Configuration configuration) {
        config.configure(configuration);
        return this;
    }

    /**
     * Sets the stream where to output the SQL statements of a migration dry run. {@code null} to execute the SQL statements
     * directly against the database. The stream when be closing when Flyway finishes writing the output.
     * <i>Flyway Teams only</i>
     *
     * @param dryRunOutput The output file or {@code null} to execute the SQL statements directly against the database.
     */
    public FluentConfiguration dryRunOutput(OutputStream dryRunOutput) {
        config.setDryRunOutput(dryRunOutput);
        return this;
    }

    /**
     * Sets the file where to output the SQL statements of a migration dry run. {@code null} to execute the SQL statements
     * directly against the database. If the file specified is in a non-existent directory, Flyway will create all
     * directories and parent directories as needed.
     * <i>Flyway Teams only</i>
     *
     * @param dryRunOutput The output file or {@code null} to execute the SQL statements directly against the database.
     */
    public FluentConfiguration dryRunOutput(File dryRunOutput) {
        config.setDryRunOutputAsFile(dryRunOutput);
        return this;
    }

    /**
     * Sets the file where to output the SQL statements of a migration dry run. {@code null} to execute the SQL statements
     * directly against the database. If the file specified is in a non-existent directory, Flyway will create all
     * directories and parent directories as needed.
     * Paths starting with s3: point to a bucket in AWS S3, which must exist. They are in the format s3:<bucket>(/optionalfolder/subfolder)/filename.sql
     * Paths starting with gcs: point to a bucket in Google Cloud Storage, which must exist. They are in the format gcs:<bucket>(/optionalfolder/subfolder)/filename.sql
     * <i>Flyway Teams only</i>
     *
     * @param dryRunOutputFileName The name of the output file or {@code null} to execute the SQL statements directly against the database.
     */
    public FluentConfiguration dryRunOutput(String dryRunOutputFileName) {
        config.setDryRunOutputAsFileName(dryRunOutputFileName);
        return this;
    }

    public FluentConfiguration reportFilename(String reportFilename) {
        config.setReportFilename(reportFilename);
        return this;
    }

    public FluentConfiguration environment(String environment) {
        config.setEnvironment(environment);
        return this;
    }

    public FluentConfiguration allEnvironments(Map<String, EnvironmentModel> environments) {
        config.setAllEnvironments(environments);
        return this;
    }

    public FluentConfiguration provisionMode(ProvisionerMode mode) {
        config.setProvisionMode(mode);
        return this;
    }

    // Backwards compatible alias for provisionMode
    public FluentConfiguration environmentProvisionMode(ProvisionerMode mode) {
        return provisionMode(mode);
    }

    public FluentConfiguration workingDirectory(String workingDirectory) {
        config.setWorkingDirectory(workingDirectory);
        return this;
    }

    public FluentConfiguration clearCachedResolvedEnvironment(String environmentName) {
        config.requestResolvedEnvironmentRefresh(environmentName);
        return this;
    }

    /**
     * Rules for the built-in error handler that let you override specific SQL states and errors codes in order to force
     * specific errors or warnings to be treated as debug messages, info messages, warnings or errors.
     * <p>Each error override has the following format: {@code STATE:12345:W}.
     * It is a 5 character SQL state (or * to match all SQL states), a colon,
     * the SQL error code (or * to match all SQL error codes), a colon and finally
     * the desired behavior that should override the initial one.</p>
     * <p>The following behaviors are accepted:</p>
     * <ul>
     * <li>{@code D} to force a debug message</li>
     * <li>{@code D-} to force a debug message, but do not show the original sql state and error code</li>
     * <li>{@code I} to force an info message</li>
     * <li>{@code I-} to force an info message, but do not show the original sql state and error code</li>
     * <li>{@code W} to force a warning</li>
     * <li>{@code W-} to force a warning, but do not show the original sql state and error code</li>
     * <li>{@code E} to force an error</li>
     * <li>{@code E-} to force an error, but do not show the original sql state and error code</li>
     * </ul>
     * <p>Example 1: to force Oracle stored procedure compilation issues to produce
     * errors instead of warnings, the following errorOverride can be used: {@code 99999:17110:E}</p>
     * <p>Example 2: to force SQL Server PRINT messages to be displayed as info messages (without SQL state and error
     * code details) instead of warnings, the following errorOverride can be used: {@code S0001:0:I-}</p>
     * <p>Example 3: to force all errors with SQL error code 123 to be treated as warnings instead,
     * the following errorOverride can be used: {@code *:123:W}</p>
     * <i>Flyway Teams only</i>
     *
     * @param errorOverrides The ErrorOverrides or an empty array if none are defined. (default: none)
     */
    public FluentConfiguration errorOverrides(String... errorOverrides) {
        config.setErrorOverrides(errorOverrides);
        return this;
    }

    /**
     * Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions).
     *
     * @param group {@code true} if migrations should be grouped. {@code false} if they should be applied individually instead. (default: {@code false})
     */
    public FluentConfiguration group(boolean group) {
        config.setGroup(group);
        return this;
    }

    /**
     * The username that will be recorded in the schema history table as having applied the migration.
     *
     * @param installedBy The username or {@code null} for the current database user of the connection. (default: {@code null}).
     */
    public FluentConfiguration installedBy(String installedBy) {
        config.setInstalledBy(installedBy);
        return this;
    }

    /**
     * The loggers Flyway should use. Valid options are:
     *
     * <ul>
     *     <li>auto: Auto detect the logger (default behavior)</li>
     *     <li>console: Use stdout/stderr (only available when using the CLI)</li>
     *     <li>slf4j: Use the slf4j logger</li>
     *     <li>log4j2: Use the log4j2 logger</li>
     *     <li>apache-commons: Use the Apache Commons logger</li>
     * </ul>
     *
     * Alternatively you can provide the fully qualified class name for any other logger to use that.
     */
    public FluentConfiguration loggers(String... loggers) {
        config.setLoggers(loggers);
        return this;
    }

    /**
     * Whether to allow mixing transactional and non-transactional statements within the same migration. Enabling this
     * automatically causes the entire affected migration to be run without a transaction.
     *
     * Note that this is only applicable for PostgreSQL, Aurora PostgreSQL, SQL Server and SQLite which all have
     * statements that do not run at all within a transaction.
     * This is not to be confused with implicit transaction, as they occur in MySQL or Oracle, where even though a
     * DDL statement was run within a transaction, the database will issue an implicit commit before and after
     * its execution.
     *
     * @param mixed {@code true} if mixed migrations should be allowed. {@code false} if an error should be thrown instead. (default: {@code false})
     */
    public FluentConfiguration mixed(boolean mixed) {
        config.setMixed(mixed);
        return this;
    }

    /**
     * Ignore migrations that match this comma-separated list of patterns when validating migrations.
     * Each pattern is of the form <migration_type>:<migration_state>
     * See https://documentation.red-gate.com/flyway/flyway-cli-and-api/configuration/parameters/flyway/ignore-migration-patterns for full details
     * Example: repeatable:missing,versioned:pending,*:failed
     */
    public FluentConfiguration ignoreMigrationPatterns(String... ignoreMigrationPatterns) {
        config.setIgnoreMigrationPatterns(ignoreMigrationPatterns);
        return this;
    }

    /**
     * Ignore migrations that match this array of ValidatePatterns when validating migrations.
     * See https://documentation.red-gate.com/flyway/flyway-cli-and-api/configuration/parameters/flyway/ignore-migration-patterns for full details
     */
    public FluentConfiguration ignoreMigrationPatterns(ValidatePattern... ignoreMigrationPatterns) {
        config.setIgnoreMigrationPatterns(ignoreMigrationPatterns);
        return this;
    }

    /**
     * Whether to validate migrations and callbacks whose scripts do not obey the correct naming convention. A failure can be
     * useful to check that errors such as case sensitivity in migration prefixes have been corrected.
     *
     * @param validateMigrationNaming {@code false} to continue normally, {@code true} to fail fast with an exception. (default: {@code false})
     */
    public FluentConfiguration validateMigrationNaming(boolean validateMigrationNaming) {
        config.setValidateMigrationNaming(validateMigrationNaming);
        return this;
    }

    /**
     * Whether to automatically call validate or not when running migrate.
     *
     * @param validateOnMigrate {@code true} if validate should be called. {@code false} if not. (default: {@code true})
     */
    public FluentConfiguration validateOnMigrate(boolean validateOnMigrate) {
        config.setValidateOnMigrate(validateOnMigrate);
        return this;
    }

    /**
     * Whether to automatically call clean or not when a validation error occurs.
     * This is exclusively intended as a convenience for development. even though we strongly recommend not to change
     * migration scripts once they have been checked into SCM and run, this provides a way of dealing with this case in
     * a smooth manner. The database will be wiped clean automatically, ensuring that the next migration will bring you
     * back to the state checked into SCM.
     * <b>Warning! Do not enable in production!</b>
     *
     * @param cleanOnValidationError {@code true} if clean should be called. {@code false} if not. (default: {@code false})
     */
    public FluentConfiguration cleanOnValidationError(boolean cleanOnValidationError) {
        config.setCleanOnValidationError(cleanOnValidationError);
        return this;
    }

    /**
     * Whether to disable clean.
     * This is especially useful for production environments where running clean can be a career limiting move.
     *
     * @param cleanDisabled {@code true} to disable clean. {@code false} to be able to clean. (default: {@code true})
     */
    public FluentConfiguration cleanDisabled(boolean cleanDisabled) {
        config.setCleanDisabled(cleanDisabled);
        return this;
    }

    /**
     * Whether to disable community database support.
     * This is especially useful for production environments where using community databases is undesirable.
     *
     * @param communityDBSupportEnabled {@code true} to disable community database support. {@code false} to be able to use community database support. (default: {@code false})
     */
    public FluentConfiguration communityDBSupportEnabled(boolean communityDBSupportEnabled) {
        config.setCommunityDBSupportEnabled(communityDBSupportEnabled);
        return this;
    }

    /**
     * Sets the locations to scan recursively for migrations.
     * The location type is determined by its prefix.
     * Unprefixed locations or locations starting with {@code classpath:} point to a package on the classpath and may
     * contain both SQL and Java-based migrations.
     * Locations starting with {@code filesystem:} point to a directory on the filesystem, may only
     * contain SQL migrations and are only scanned recursively down non-hidden directories.
     *
     * @param locations Locations to scan recursively for migrations. (default: db/migration)
     */
    public FluentConfiguration locations(String... locations) {
        config.setLocationsAsStrings(locations);
        return this;
    }

    /**
     * Sets the locations to scan recursively for migrations.
     * The location type is determined by its prefix.
     * Unprefixed locations or locations starting with {@code classpath:} point to a package on the classpath and may
     * contain both SQL and Java-based migrations.
     * Locations starting with {@code filesystem:} point to a directory on the filesystem, may only
     * contain SQL migrations and are only scanned recursively down non-hidden directories.
     *
     * @param locations Locations to scan recursively for migrations. (default: db/migration)
     */
    public FluentConfiguration locations(Location... locations) {
        config.setLocations(locations);
        return this;
    }

    /**
     * Sets the encoding of SQL migrations.
     *
     * @param encoding The encoding of SQL migrations. (default: UTF-8)
     */
    public FluentConfiguration encoding(String encoding) {
        config.setEncodingAsString(encoding);
        return this;
    }

    /**
     * Sets the encoding of SQL migrations.
     *
     * @param encoding The encoding of SQL migrations. (default: UTF-8)
     */
    public FluentConfiguration encoding(Charset encoding) {
        config.setEncoding(encoding);
        return this;
    }

    /**
     * Sets whether SQL should be executed within a transaction.
     *
     * @param executeInTransaction {@code true} to enable execution of SQL in a transaction, {@code false} otherwise
     */
    public FluentConfiguration executeInTransaction(boolean executeInTransaction) {
        config.setExecuteInTransaction(executeInTransaction);
        return this;
    }


    /**
     * Whether Flyway should try to automatically detect SQL migration file encoding
     *
     * @param detectEncoding {@code true} to enable auto detection, {@code false} otherwise
     * <i>Flyway Teams only</i>
     */
    public FluentConfiguration detectEncoding(boolean detectEncoding) {
        config.setDetectEncoding(detectEncoding);
        return this;
    }

    /**
     * Sets the default schema managed by Flyway. This schema name is case-sensitive. If not specified, but <i>schemas</i>
     * is, Flyway uses the first schema in that list. If that is also not specified, Flyway uses the default schema for the
     * database connection.
     * <p>Consequences:</p>
     * <ul>
     * <li>This schema will be the one containing the schema history table.</li>
     * <li>This schema will be the default for the database connection (provided the database supports this concept).</li>
     * </ul>
     *
     * @param schema The default schema managed by Flyway, which is where the schema history table will reside.
     */
    public FluentConfiguration defaultSchema(String schema) {
        config.setDefaultSchema(schema);
        return this;
    }

    /**
     * Sets the schemas managed by Flyway. These schema names are case-sensitive. If not specified, Flyway uses
     * the default schema for the database connection. If <i>defaultSchema</i> is not specified, then the first of
     * this list also acts as the default schema.
     * <p>Consequences:</p>
     * <ul>
     * <li>Flyway will automatically attempt to create all these schemas, unless they already exist.</li>
     * <li>The schemas will be cleaned in the order of this list.</li>
     * <li>If Flyway created them, the schemas themselves will be dropped when cleaning.</li>
     * </ul>
     *
     * @param schemas The schemas managed by Flyway. May not be {@code null}. Must contain at least one element.
     */
    public FluentConfiguration schemas(String... schemas) {
        config.setSchemas(schemas);
        return this;
    }

    /**
     * Sets the name of the schema history table that will be used by Flyway.
     * By default, (single-schema mode) the schema history table is placed in the default schema for the connection provided by the datasource.
     * When the <i>flyway.schemas</i> property is set (multi-schema mode), the schema history table is placed in the first schema of the list,
     * or in the schema specified to <i>flyway.defaultSchema</i>.
     *
     * @param table The name of the schema history table that will be used by Flyway. (default: flyway_schema_history)
     */
    public FluentConfiguration table(String table) {
        config.setTable(table);
        return this;
    }

    /**
     * Sets the tablespace where to create the schema history table that will be used by Flyway.
     * If not specified, Flyway uses the default tablespace for the database connection.
     * This setting is only relevant for databases that do support the notion of tablespaces. Its value is simply ignored for all others.
     *
     * @param tablespace The tablespace where to create the schema history table that will be used by Flyway.
     */
    public FluentConfiguration tablespace(String tablespace) {
        config.setTablespace(tablespace);
        return this;
    }

    /**
     * Sets the target version up to which Flyway should consider migrations.
     * Migrations with a higher version number will be ignored.
     * Special values:
     * <ul>
     * <li>{@code current}: Designates the current version of the schema</li>
     * <li>{@code latest}: The latest version of the schema, as defined by the migration with the highest version</li>
     * <li>{@code next}: The next version of the schema, as defined by the first pending migration</li>
     * </ul>
     * Defaults to {@code latest}.
     */
    public FluentConfiguration target(MigrationVersion target) {
        config.setTarget(target);
        return this;
    }

    /**
     * Sets the target version up to which Flyway should consider migrations.
     * Migrations with a higher version number will be ignored.
     * Special values:
     * <ul>
     * <li>{@code current}: Designates the current version of the schema</li>
     * <li>{@code latest}: The latest version of the schema, as defined by the migration with the highest version</li>
     * <li>
     *     &lt;version&gt;? (end with a '?'): Instructs Flyway not to fail if the target version doesn't exist.
     *     In this case, Flyway will go up to but not beyond the specified target
     *     (default: fail if the target version doesn't exist) <i>Flyway Teams only</i>
     * </li>
     * </ul>
     * Defaults to {@code latest}.
     */
    public FluentConfiguration target(String target) {
        config.setTargetAsString(target);
        return this;
    }

    /**
     * Sets whether placeholders should be replaced.
     *
     * @param placeholderReplacement Whether placeholders should be replaced. (default: true)
     */
    public FluentConfiguration placeholderReplacement(boolean placeholderReplacement) {
        config.setPlaceholderReplacement(placeholderReplacement);
        return this;
    }

    /**
     * Sets the placeholders to replace in sql migration scripts.
     *
     * @param placeholders The map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     */
    public FluentConfiguration placeholders(Map<String, String> placeholders) {
        config.setPlaceholders(placeholders);
        return this;
    }

    /**
     * Sets the prefix of every placeholder.
     *
     * @param placeholderPrefix The prefix of every placeholder. (default: ${ )
     */
    public FluentConfiguration placeholderPrefix(String placeholderPrefix) {
        config.setPlaceholderPrefix(placeholderPrefix);
        return this;
    }

    /**
     * Sets the suffix of every placeholder.
     *
     * @param placeholderSuffix The suffix of every placeholder. (default: } )
     */
    public FluentConfiguration placeholderSuffix(String placeholderSuffix) {
        config.setPlaceholderSuffix(placeholderSuffix);
        return this;
    }

    /**
     * Sets the separator of default placeholders.
     *
     * @param placeholderSeparator The separator of default placeholders. (default: : )
     */
    public FluentConfiguration placeholderSeparator(String placeholderSeparator) {
        config.setPlaceholderSeparator(placeholderSeparator);
        return this;
    }

    /**
     * Sets the prefix of every script placeholder.
     *
     * @param scriptPlaceholderPrefix The prefix of every placeholder. (default: FP__ )
     */
    public FluentConfiguration scriptPlaceholderPrefix(String scriptPlaceholderPrefix) {
        config.setScriptPlaceholderPrefix(scriptPlaceholderPrefix);
        return this;
    }

    /**
     * Sets the suffix of every script placeholder.
     *
     * @param scriptPlaceholderSuffix The suffix of every script placeholder. (default: __ )
     */
    public FluentConfiguration scriptPlaceholderSuffix(String scriptPlaceholderSuffix) {
        config.setScriptPlaceholderSuffix(scriptPlaceholderSuffix);
        return this;
    }

    /**
     * Sets the file name prefix for sql migrations.
     * SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to V1_1__My_description.sql
     *
     * @param sqlMigrationPrefix The file name prefix for sql migrations (default: V)
     */
    public FluentConfiguration sqlMigrationPrefix(String sqlMigrationPrefix) {
        config.setSqlMigrationPrefix(sqlMigrationPrefix);
        return this;
    }

    /**
     * Sets the file name prefix for repeatable sql migrations.
     * Repeatable SQL migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix,
     * which using the defaults translates to R__My_description.sql
     *
     * @param repeatableSqlMigrationPrefix The file name prefix for repeatable sql migrations (default: R)
     */
    public FluentConfiguration repeatableSqlMigrationPrefix(String repeatableSqlMigrationPrefix) {
        config.setRepeatableSqlMigrationPrefix(repeatableSqlMigrationPrefix);
        return this;
    }

    /**
     * Sets the file name separator for sql migrations.
     * SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to V1_1__My_description.sql
     *
     * @param sqlMigrationSeparator The file name separator for sql migrations (default: __)
     */
    public FluentConfiguration sqlMigrationSeparator(String sqlMigrationSeparator) {
        config.setSqlMigrationSeparator(sqlMigrationSeparator);
        return this;
    }

    /**
     * The file name suffixes for SQL migrations. (default: .sql)
     * SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to V1_1__My_description.sql
     * Multiple suffixes (like .sql,.pkg,.pkb) can be specified for easier compatibility with other tools such as
     * editors with specific file associations.
     *
     * @param sqlMigrationSuffixes The file name suffixes for SQL migrations.
     */
    public FluentConfiguration sqlMigrationSuffixes(String... sqlMigrationSuffixes) {
        config.setSqlMigrationSuffixes(sqlMigrationSuffixes);
        return this;
    }

    /**
     * The manually added Java-based migrations. These are not Java-based migrations discovered through classpath
     * scanning and instantiated by Flyway. Instead these are manually added instances of JavaMigration.
     * This is particularly useful when working with a dependency injection container, where you may want the DI
     * container to instantiate the class and wire up its dependencies for you.
     *
     * @param javaMigrations The manually added Java-based migrations. An empty array if none. (default: none)
     */
    public FluentConfiguration javaMigrations(JavaMigration... javaMigrations) {
        config.setJavaMigrations(javaMigrations);
        return this;
    }

    /**
     * Sets the datasource to use. Must have the necessary privileges to execute DDL.
     *
     * @param dataSource The datasource to use. Must have the necessary privileges to execute DDL.
     */
    public FluentConfiguration dataSource(DataSource dataSource) {
        config.setDataSource(dataSource);
        return this;
    }

    /**
     * Sets the datasource to use. Must have the necessary privileges to execute DDL.
     *
     * @param url The JDBC URL of the database.
     * @param user The user of the database.
     * @param password The password of the database.
     */
    public FluentConfiguration dataSource(String url, String user, String password) {
        config.setDataSource(url, user, password);
        return this;
    }

    /**
     * The maximum number of retries when attempting to connect to the database. After each failed attempt, Flyway will
     * wait 1 second before attempting to connect again, up to the maximum number of times specified by connectRetries.
     * The interval between retries doubles with each subsequent attempt.
     *
     * @param connectRetries The maximum number of retries (default: 0).
     */
    public FluentConfiguration connectRetries(int connectRetries) {
        config.setConnectRetries(connectRetries);
        return this;
    }

    /**
     * The maximum time between retries when attempting to connect to the database in seconds. This will cap the interval
     * between connect retry to the value provided.
     *
     * @param connectRetriesInterval The maximum time between retries in seconds (default: 120).
     */
    public FluentConfiguration connectRetriesInterval(int connectRetriesInterval) {
        config.setConnectRetriesInterval(connectRetriesInterval);
        return this;
    }

    /**
     * The SQL statements to run to initialize a new database connection immediately after opening it.
     *
     * @param initSql The SQL statements. (default: {@code null})
     */
    public FluentConfiguration initSql(String initSql) {
        config.setInitSql(initSql);
        return this;
    }

    /**
     * Sets the version to tag an existing schema with when executing baseline.
     *
     * @param baselineVersion The version to tag an existing schema with when executing baseline. (default: 1)
     */
    public FluentConfiguration baselineVersion(MigrationVersion baselineVersion) {
        config.setBaselineVersion(baselineVersion);
        return this;
    }

    /**
     * Sets the version to tag an existing schema with when executing baseline.
     *
     * @param baselineVersion The version to tag an existing schema with when executing baseline. (default: 1)
     */
    public FluentConfiguration baselineVersion(String baselineVersion) {
        config.setBaselineVersion(baselineVersion);
        return this;
    }

    /**
     * Sets the description to tag an existing schema with when executing baseline.
     *
     * @param baselineDescription The description to tag an existing schema with when executing baseline. (default: &lt;&lt; Flyway Baseline &gt;&gt;)
     */
    public FluentConfiguration baselineDescription(String baselineDescription) {
        config.setBaselineDescription(baselineDescription);
        return this;
    }

    /**
     * Whether to automatically call baseline when migrate is executed against a non-empty schema with no schema history table.
     * This schema will then be baselined with the {@code baselineVersion} before executing the migrations.
     * Only migrations above {@code baselineVersion} will then be applied.
     *
     * This is useful for initial Flyway production deployments on projects with an existing DB.
     *
     * Be careful when enabling this as it removes the safety net that ensures
     * Flyway does not migrate the wrong database in case of a configuration mistake!
     *
     * @param baselineOnMigrate {@code true} if baseline should be called on migrate for non-empty schemas, {@code false} if not. (default: {@code false})
     */
    public FluentConfiguration baselineOnMigrate(boolean baselineOnMigrate) {
        config.setBaselineOnMigrate(baselineOnMigrate);
        return this;
    }

    /**
     * Allows migrations to be run "out of order".
     * If you already have versions 1 and 3 applied, and now a version 2 is found, it will be applied too instead of being ignored.
     *
     * @param outOfOrder {@code true} if outOfOrder migrations should be applied, {@code false} if not. (default: {@code false})
     */
    public FluentConfiguration outOfOrder(boolean outOfOrder) {
        config.setOutOfOrder(outOfOrder);
        return this;
    }

    /**
     * Whether Flyway should skip actually executing the contents of the migrations and only update the schema history table.
     * This should be used when you have applied a migration manually (via executing the sql yourself, or via an ide), and
     * just want the schema history table to reflect this.
     *
     * Use in conjunction with {@code cherryPick} to skip specific migrations instead of all pending ones.
     */
    public FluentConfiguration skipExecutingMigrations(boolean skipExecutingMigrations) {
        config.setSkipExecutingMigrations(skipExecutingMigrations);
        return this;
    }

    /**
     * Set the callbacks for lifecycle notifications.
     *
     * @param callbacks The callbacks for lifecycle notifications. (default: none)
     */
    public FluentConfiguration callbacks(Callback... callbacks) {
        config.setCallbacks(callbacks);
        return this;
    }

    /**
     * Set the callbacks for lifecycle notifications.
     *
     * @param callbacks The fully qualified class names, or full qualified package to scan, of the callbacks for lifecycle notifications. (default: none)
     */
    public FluentConfiguration callbacks(String... callbacks) {
        config.setCallbacksAsClassNames(callbacks);
        return this;
    }

    /**
     * Whether Flyway should skip the default callbacks. If true, only custom callbacks are used.
     *
     * @param skipDefaultCallbacks Whether default built-in callbacks should be skipped. (default: false)
     */
    public FluentConfiguration skipDefaultCallbacks(boolean skipDefaultCallbacks) {
        config.setSkipDefaultCallbacks(skipDefaultCallbacks);
        return this;
    }

    /**
     * Sets custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
     *
     * @param resolvers The custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply. (default: empty list)
     */
    public FluentConfiguration resolvers(MigrationResolver... resolvers) {
        config.setResolvers(resolvers);
        return this;
    }

    /**
     * Sets custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
     *
     * @param resolvers The fully qualified class names of the custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply. (default: empty list)
     */
    public FluentConfiguration resolvers(String... resolvers) {
        config.setResolversAsClassNames(resolvers);
        return this;
    }

    /**
     * Whether Flyway should skip the default resolvers. If true, only custom resolvers are used.
     *
     * @param skipDefaultResolvers Whether default built-in resolvers should be skipped. (default: false)
     */
    public FluentConfiguration skipDefaultResolvers(boolean skipDefaultResolvers) {
        config.setSkipDefaultResolvers(skipDefaultResolvers);
        return this;
    }

    /**
     * Whether to stream SQL migrations when executing them. Streaming doesn't load the entire migration in memory at
     * once. Instead each statement is loaded individually. This is particularly useful for very large SQL migrations
     * composed of multiple MB or even GB of reference data, as this dramatically reduces Flyway's memory consumption.
     * <i>Flyway Teams only</i>
     *
     * @param stream {@code true} to stream SQL migrations. {@code false} to fully loaded them in memory instead. (default: {@code false})
     */
    public FluentConfiguration stream(boolean stream) {
        config.setStream(stream);
        return this;
    }

    /**
     * Whether to batch SQL statements when executing them. Batching can save up to 99 percent of network roundtrips by
     * sending up to 100 statements at once over the network to the database, instead of sending each statement
     * individually. This is particularly useful for very large SQL migrations composed of multiple MB or even GB of
     * reference data, as this can dramatically reduce the network overhead. This is supported for INSERT, UPDATE,
     * DELETE, MERGE and UPSERT statements. All other statements are automatically executed without batching.
     * <i>Flyway Teams only</i>
     *
     * @param batch {@code true} to batch SQL statements. {@code false} to execute them individually instead. (default: {@code false})
     */
    public FluentConfiguration batch(boolean batch) {
        config.setBatch(batch);
        return this;
    }

    public FluentConfiguration lockRetryCount(int lockRetryCount) {
        config.setLockRetryCount(lockRetryCount);
        return this;
    }

    /**
     * Properties to pass to the JDBC driver object
     *
     * @param jdbcProperties The properties to pass to the JDBC driver object
     */
    public FluentConfiguration jdbcProperties(Map<String, String> jdbcProperties) {
        config.setJdbcProperties(jdbcProperties);
        return this;
    }

    /**
     * When connecting to a Kerberos service to authenticate, the path to the Kerberos config file.
     * <i>Flyway Teams only</i>
     *
     * @param kerberosConfigFile The path to the Kerberos config file
     */
    public FluentConfiguration kerberosConfigFile(String kerberosConfigFile) {
        config.setKerberosConfigFile(kerberosConfigFile);
        return this;
    }

    /**
     * Custom ResourceProvider to be used to look up resources. If not set, the default strategy will be used.
     *
     * @param resourceProvider Custom ResourceProvider to be used to look up resources
     */
    public FluentConfiguration resourceProvider(ResourceProvider resourceProvider) {
        config.setResourceProvider(resourceProvider);
        return this;
    }

    /**
     * Custom ClassProvider to be used to look up {@link JavaMigration} classes. If not set, the default strategy will be used.
     *
     * @param javaMigrationClassProvider Custom ClassProvider to be used to look up {@link JavaMigration} classes.
     */
    public FluentConfiguration javaMigrationClassProvider(ClassProvider<JavaMigration> javaMigrationClassProvider) {
        config.setJavaMigrationClassProvider(javaMigrationClassProvider);
        return this;
    }

    /**
     * Whether Flyway should output a table with the results of queries when executing migrations.
     * <i>Flyway Teams only</i>
     *
     * @param outputQueryResults {@code true} to output a table with the results of queries when executing migrations. (default: {@code true})
     */
    public FluentConfiguration outputQueryResults(boolean outputQueryResults) {
        config.setOutputQueryResults(outputQueryResults);
        return this;
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration. Property names are documented in the flyway maven plugin.
     * To use a custom ClassLoader, setClassLoader() must be called prior to calling this method.
     *
     * @param properties Properties used for configuration.
     * @throws FlywayException when the configuration failed.
     */
    public FluentConfiguration configuration(Properties properties) {
        config.configure(properties);
        return this;
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration. Property names are documented in the flyway maven plugin.
     * To use a custom ClassLoader, it must be passed to the Flyway constructor prior to calling this method.
     *
     * @param props Properties used for configuration.
     * @throws FlywayException when the configuration failed.
     */
    public FluentConfiguration configuration(Map<String, String> props) {
        config.configure(props);
        return this;
    }

    /**
     * Load configuration files from the default locations:
     * $installationDir$/conf/flyway.conf
     * $user.home$/flyway.conf
     * $workingDirectory$/flyway.conf
     *
     * The configuration files must be encoded with UTF-8.
     *
     * @throws FlywayException When the configuration failed.
     */
    public FluentConfiguration loadDefaultConfigurationFiles() {
        return loadDefaultConfigurationFiles("UTF-8");
    }

    /**
     * Load configuration files from the default locations:
     * $installationDir$/conf/flyway.conf
     * $user.home$/flyway.conf
     * $workingDirectory$/flyway.conf
     *
     * @param encoding The conf file encoding.
     * @throws FlywayException When the configuration failed.
     */
    public FluentConfiguration loadDefaultConfigurationFiles(String encoding) {
        String installationPath = ClassUtils.getLocationOnDisk(FluentConfiguration.class);
        File installationDir = new File(installationPath).getParentFile();
        String workingDirectory = getWorkingDirectory() != null ? getWorkingDirectory() : null;

        Map<String, String> configMap = ConfigUtils.loadDefaultConfigurationFiles(installationDir, workingDirectory, encoding);

        config.configure(configMap);
        return this;
    }

    /**
     * Whether Flyway should attempt to create the schemas specified in the schemas property
     *
     * @param createSchemas @{code true} to attempt to create the schemas (default: {@code true})
     */
    public FluentConfiguration createSchemas(boolean createSchemas) {
        config.setShouldCreateSchemas(createSchemas);
        return this;
    }

    /**
     * Configures Flyway using FLYWAY_* environment variables.
     *
     * @throws FlywayException When the configuration failed.
     */
    public FluentConfiguration envVars() {
        config.configureUsingEnvVars();
        return this;
    }

    /**
     * Whether to fail if a location specified in the flyway.locations option doesn't exist
     *
     * @return @{code true} to fail (default: {@code false})
     */
    public FluentConfiguration failOnMissingLocations(boolean failOnMissingLocations) {
        config.setFailOnMissingLocations(failOnMissingLocations);
        return this;
    }

    /**
     * Sets the JDBC driver to use. Must match the driver for the database type in the url.
     *
     * @param driver The name of the JDBC driver of the database.
     */
    public FluentConfiguration driver(String driver) {
        config.setDriver(driver);
        return this;
    }
}
