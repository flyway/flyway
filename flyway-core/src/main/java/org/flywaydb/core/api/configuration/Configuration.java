/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.core.api.configuration;

import org.flywaydb.core.api.*;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.api.resolver.MigrationResolver;

import javax.sql.DataSource;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

public interface Configuration {
    /**
     * Retrieves the ClassLoader to use for loading migrations, resolvers, etc. from the classpath.
     *
     * @return The ClassLoader to use for loading migrations, resolvers, etc. from the classpath. (default: Thread.currentThread().getContextClassLoader() )
     */
    ClassLoader getClassLoader();

    /**
     * Retrieves the url used to construct the dataSource. May be null if the dataSource was passed in directly.
     *
     * @return The url used to construct the dataSource. May be null if the dataSource was passed in directly.
     */
    String getUrl();

    /**
     * Retrieves the user used to construct the dataSource. May be null if the dataSource was passed in directly, or if dataSource did not need a user.
     *
     * @return The user used to construct the dataSource. May be null if the dataSource was passed in directly, or if dataSource did not need a user.
     */
    String getUser();

    /**
     * Retrieves the password used to construct the dataSource. May be null if the dataSource was passed in directly, or if dataSource did not need a password.
     *
     * @return The password used to construct the dataSource. May be null if the dataSource was passed in directly, or if dataSource did not need a password.
     */
    String getPassword();

    /**
     * Retrieves the dataSource to use to access the database. Must have the necessary privileges to execute DDL.
     *
     * @return The dataSource to use to access the database. Must have the necessary privileges to execute DDL.
     */
    DataSource getDataSource();

    /**
     * The maximum number of retries when attempting to connect to the database. After each failed attempt, Flyway will
     * wait 1 second before attempting to connect again, up to the maximum number of times specified by connectRetries.
     * The interval between retries doubles with each subsequent attempt.
     *
     * @return The maximum number of retries when attempting to connect to the database. (default: 0)
     */
    int getConnectRetries();

    /**
     * The maximum time between retries when attempting to connect to the database in seconds. This will cap the interval
     * between connect retry to the value provided.
     *
     * @return The maximum time between retries in seconds (default: 120)
     */
    int getConnectRetriesInterval();

    /**
     * The SQL statements to run to initialize a new database connection immediately after opening it.
     *
     * @return The SQL statements. (default: {@code null})
     */
    String getInitSql();

    /**
     * Retrieves the version to tag an existing schema with when executing baseline.
     *
     * @return The version to tag an existing schema with when executing baseline. (default: 1)
     */
    MigrationVersion getBaselineVersion();

    /**
     * Retrieves the description to tag an existing schema with when executing baseline.
     *
     * @return The description to tag an existing schema with when executing baseline. (default: &lt;&lt; Flyway Baseline &gt;&gt;)
     */
    String getBaselineDescription();

    /**
     * Retrieves the custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
     *
     * @return The custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply. An empty array if none. (default: none)
     */
    MigrationResolver[] getResolvers();

    /**
     * Whether Flyway should skip the default resolvers. If true, only custom resolvers are used.
     *
     * @return Whether default built-in resolvers should be skipped. (default: false)
     */
    boolean isSkipDefaultResolvers();

    /**
     * Gets the callbacks for lifecycle notifications.
     *
     * @return The callbacks for lifecycle notifications. An empty array if none. (default: none)
     */
    Callback[] getCallbacks();

    /**
     * Whether Flyway should skip the default callbacks. If true, only custom callbacks are used.
     *
     * @return Whether default built-in callbacks should be skipped. (default: false)
     */
    boolean isSkipDefaultCallbacks();

    /**
     * The file name prefix for versioned SQL migrations.
     * Versioned SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to V1.1__My_description.sql
     *
     * @return The file name prefix for sql migrations. (default: V)
     */
    String getSqlMigrationPrefix();

    /**
     * The file name prefix for baseline migrations.
     * Baseline migrations represent all migrations with version <= current baseline migration version
     * while keeping older migrations if needed for upgrading older deployments
     * They have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to B1.1__My_description.sql
     * <i>Flyway Teams only</i>
     *
     * @return The file name prefix for baseline migration. (default: B)
     */
    String getBaselineMigrationPrefix();

    /**
     * The file name prefix for undo SQL migrations.
     * Undo SQL migrations are responsible for undoing the effects of the versioned migration with the same version.
     * They have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to U1.1__My_description.sql
     * <i>Flyway Teams only</i>
     *
     * @return The file name prefix for undo sql migrations. (default: U)
     */
    String getUndoSqlMigrationPrefix();

    /**
     * Retrieves the file name prefix for repeatable SQL migrations.
     * Repeatable SQL migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix,
     * which using the defaults translates to R__My_description.sql<
     *
     * @return The file name prefix for repeatable sql migrations. (default: R)
     */
    String getRepeatableSqlMigrationPrefix();

    /**
     * Retrieves the file name separator for sql migrations.
     * SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to V1_1__My_description.sql
     *
     * @return The file name separator for sql migrations. (default: __)
     */
    String getSqlMigrationSeparator();

    /**
     * The file name suffixes for SQL migrations. (default: .sql)
     * SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to V1_1__My_description.sql
     * Multiple suffixes (like .sql,.pkg,.pkb) can be specified for easier compatibility with other tools such as
     * editors with specific file associations.
     *
     * @return The file name suffixes for SQL migrations.
     */
    String[] getSqlMigrationSuffixes();

    /**
     * The manually added Java-based migrations. These are not Java-based migrations discovered through classpath
     * scanning and instantiated by Flyway. Instead these are manually added instances of JavaMigration.
     * This is particularly useful when working with a dependency injection container, where you may want the DI
     * container to instantiate the class and wire up its dependencies for you.
     *
     * @return The manually added Java-based migrations. An empty array if none. (default: none)
     */
    JavaMigration[] getJavaMigrations();

    /**
     * Checks whether placeholders should be replaced.
     *
     * @return Whether placeholders should be replaced. (default: true)
     */
    boolean isPlaceholderReplacement();

    /**
     * Retrieves the suffix of every placeholder.
     *
     * @return The suffix of every placeholder. (default: } )
     */
    String getPlaceholderSuffix();

    /**
     * Retrieves the prefix of every placeholder.
     *
     * @return The prefix of every placeholder. (default: ${ )
     */
    String getPlaceholderPrefix();

    /**
     * Retrieves the suffix of every script placeholder.
     *
     * @return The suffix of every script placeholder. (default: __ )
     */
    String getScriptPlaceholderSuffix();

    /**
     * Retrieves the prefix of every script placeholder.
     *
     * @return The prefix of every script placeholder. (default: FP__ )
     */
    String getScriptPlaceholderPrefix();

    /**
     * Retrieves the map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     *
     * @return The map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     */
    Map<String, String> getPlaceholders();

    /**
     * Gets the target version up to which Flyway should consider migrations.
     * Migrations with a higher version number will be ignored.
     * Special values:
     * <ul>
     * <li>{@code current}: Designates the current version of the schema</li>
     * <li>{@code latest}: The latest version of the schema, as defined by the migration with the highest version</li>
     * <li>{@code next}: The next version of the schema, as defined by the first pending migration</li>
     * <li>
     *     &lt;version&gt;? (end with a '?'): Instructs Flyway not to fail if the target version doesn't exist.
     *     In this case, Flyway will go up to but not beyond the specified target
     *     (default: fail if the target version doesn't exist) <i>Flyway Teams only</i>
     * </li>
     * </ul>
     *
     * @return The target version up to which Flyway should consider migrations. Defaults to {@code latest}
     */
    MigrationVersion getTarget();

    /**
     * Whether to fail if no migration with the configured target version exists (default: {@code true})
     */
    boolean isFailOnMissingTarget();

    /**
     * Gets the migrations that Flyway should consider when migrating or undoing. Leave empty to consider all available migrations.
     * Migrations not in this list will be ignored.
     * <i>Flyway Teams only</i>
     *
     * @return The migrations that Flyway should consider when migrating or undoing.
     */
    MigrationPattern[] getCherryPick();

    /**
     * Retrieves the name of the schema history table that will be used by Flyway.
     * By default, (single-schema mode) the schema history table is placed in the default schema for the connection provided by the datasource.
     * When the <i>flyway.schemas</i> property is set (multi-schema mode), the schema history table is placed in the first schema of the list,
     * or in the schema specified to <i>flyway.defaultSchema</i>.
     *
     * @return The name of the schema history table that will be used by Flyway. (default: flyway_schema_history)
     */
    String getTable();

    /**
     * The tablespace where to create the schema history table that will be used by Flyway.
     * If not specified, Flyway uses the default tablespace for the database connection.
     * This setting is only relevant for databases that do support the notion of tablespace. Its value is simply ignored for all others.
     *
     * @return The tablespace where to create the schema history table that will be used by Flyway.
     */
    String getTablespace();

    /**
     * The default schema managed by Flyway. This schema name is case-sensitive. If not specified, but <i>schemas</i>
     * is, Flyway uses the first schema in that list. If that is also not specified, Flyway uses the default schema for the
     * database connection.
     * <p>Consequences:</p>
     * <ul>
     * <li>This schema will be the one containing the schema history table.</li>
     * <li>This schema will be the default for the database connection (provided the database supports this concept).</li>
     * </ul>
     *
     * @return The default schema managed by Flyway, which is where the schema history table will reside. (default: The first schema specified in getSchemas(), and failing that the default schema for the database connection)
     */
    String getDefaultSchema();

    /**
     * The schemas managed by Flyway. These schema names are case-sensitive. If not specified, Flyway uses
     * the default schema for the database connection. If <i>defaultSchema</i> is not specified, then the first of
     * this list also acts as the default schema.
     * <p>Consequences:</p>
     * <ul>
     * <li>Flyway will automatically attempt to create all these schemas, unless they already exist.</li>
     * <li>The schemas will be cleaned in the order of this list.</li>
     * <li>If Flyway created them, the schemas themselves will be dropped when cleaning.</li>
     * </ul>
     *
     * @return The schemas managed by Flyway. (default: The default schema for the database connection)
     */
    String[] getSchemas();

    /**
     * Retrieves the encoding of Sql migrations.
     *
     * @return The encoding of Sql migrations. (default: UTF-8)
     */
    Charset getEncoding();

    /**
     * Whether Flyway should try to automatically detect SQL migration file encoding
     *
     * @return {@code true} to enable auto detection, {@code false} otherwise
     * <i>Flyway Teams only</i>
     */
    boolean isDetectEncoding();

    /**
     * Retrieves the locations to scan recursively for migrations.
     * The location type is determined by its prefix.
     * Unprefixed locations or locations starting with {@code classpath:} point to a package on the classpath and may
     * contain both SQL and Java-based migrations.
     * Locations starting with {@code filesystem:} point to a directory on the filesystem, may only
     * contain SQL migrations and are only scanned recursively down non-hidden directories.
     *
     * @return Locations to scan recursively for migrations. (default: classpath:db/migration)
     */
    Location[] getLocations();

    /**
     * Whether to automatically call baseline when migrate is executed against a non-empty schema with no schema history table.
     * This schema will then be initialized with the {@code baselineVersion} before executing the migrations.
     * Only migrations above {@code baselineVersion} will then be applied.
     *
     * This is useful for initial Flyway production deployments on projects with an existing DB.
     *
     * Be careful when enabling this as it removes the safety net that ensures Flyway does not migrate the wrong database in case of a configuration mistake!
     *
     * @return {@code true} if baseline should be called on migrate for non-empty schemas, {@code false} if not. (default: {@code false})
     */
    boolean isBaselineOnMigrate();

    /**
     * Whether Flyway should skip actually executing the contents of the migrations and only update the schema history table.
     * This should be used when you have applied a migration manually (via executing the sql yourself, or via an ide), and
     * just want the schema history table to reflect this.
     *
     * Use in conjunction with {@code cherryPick} to skip specific migrations instead of all pending ones.
     *
     * <i>Flyway Teams only</i>
     *
     * @return {@code true} if executing the migrations should be skipped on migrate, {@code false} if not. (default: {@code false})
     */
    boolean isSkipExecutingMigrations();

    /**
     * Allows migrations to be run "out of order".
     * If you already have versions 1 and 3 applied, and now a version 2 is found, it will be applied too instead of being ignored.
     *
     * @return {@code true} if outOfOrder migrations should be applied, {@code false} if not. (default: {@code false})
     */
    boolean isOutOfOrder();

    /**
     * @return {@code true} to continue normally and log a warning, {@code false} to fail fast with an exception. (default: {@code false})
     * @deprecated Will remove in Flyway V9. Use {@code getIgnoreMigrationPatterns} instead.
     *
     * Ignore missing migrations when reading the schema history table. These are migrations that were performed by an
     * older deployment of the application that are no longer available in this version. For example: we have migrations
     * available on the classpath with versions 1.0 and 3.0. The schema history table indicates that a migration with version 2.0
     * (unknown to us) has also been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to deploy
     * a newer version of the application even though it doesn't contain migrations included with an older one anymore.
     * Note that if the most recently applied migration is removed, Flyway has no way to know it is missing and will
     * mark it as future instead.
     */
    @Deprecated
    boolean isIgnoreMissingMigrations();

    /**
     * @return {@code true} to continue normally, {@code false} to fail fast with an exception. (default: {@code false})
     * @deprecated Will remove in Flyway V9. Use {@code getIgnoreMigrationPatterns} instead.
     *
     * Ignore ignored migrations when reading the schema history table. These are migrations that were added in between
     * already migrated migrations in this version. For example: we have migrations available on the classpath with
     * versions from 1.0 to 3.0. The schema history table indicates that version 1 was finished on 1.0.15, and the next
     * one was 2.0.0. But with the next release a new migration was added to version 1: 1.0.16. Such scenario is ignored
     * by migrate command, but by default is rejected by validate. When ignoreIgnoredMigrations is enabled, such case
     * will not be reported by validate command. This is useful for situations where one must be able to deliver
     * complete set of migrations in a delivery package for multiple versions of the product, and allows for further
     * development of older versions.
     */
    @Deprecated
    boolean isIgnoreIgnoredMigrations();

    /**
     * @return {@code true} to continue normally, {@code false} to fail fast with an exception. (default: {@code false})
     * @deprecated Will remove in Flyway V9. Use {@code getIgnoreMigrationPatterns} instead.
     *
     * Ignore pending migrations when reading the schema history table. These are migrations that are available
     * but have not yet been applied. This can be useful for verifying that in-development migration changes
     * don't contain any validation-breaking changes of migrations that have already been applied to a production
     * environment, e.g. as part of a CI/CD process, without failing because of the existence of new migration versions.
     */
    @Deprecated
    boolean isIgnorePendingMigrations();

    /**
     * @return {@code true} to continue normally and log a warning, {@code false} to fail fast with an exception. (default: {@code true})
     * @deprecated Will remove in Flyway V9. Use {@code getIgnoreMigrationPatterns} instead.
     *
     * Ignore future migrations when reading the schema history table. These are migrations that were performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The schema history table indicates that a migration to version 4.0
     * (unknown to us) has already been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to redeploy
     * an older version of the application after the database has been migrated by a newer one.
     */
    @Deprecated
    boolean isIgnoreFutureMigrations();

    /**
     * Ignore migrations that match this comma-separated list of patterns when validating migrations.
     * Each pattern is of the form <migration_type>:<migration_state>
     * See https://flywaydb.org/documentation/configuration/parameters/ignoreMigrationPatterns for full details
     * Example: repeatable:missing,versioned:pending,*:failed
     * <i>Flyway Teams only</i>
     */
    ValidatePattern[] getIgnoreMigrationPatterns();

    /**
     * Whether to validate migrations and callbacks whose scripts do not obey the correct naming convention. A failure can be
     * useful to check that errors such as case sensitivity in migration prefixes have been corrected.
     *
     * @return {@code false} to continue normally, {@code true} to fail fast with an exception. (default: {@code false})
     */
    boolean isValidateMigrationNaming();

    /**
     * Whether to automatically call validate or not when running migrate.
     *
     * @return {@code true} if validate should be called. {@code false} if not. (default: {@code true})
     */
    boolean isValidateOnMigrate();

    /**
     * Whether to automatically call clean or not when a validation error occurs.
     * This is exclusively intended as a convenience for development. even though we
     * strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a
     * way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that
     * the next migration will bring you back to the state checked into SCM.
     * <b>Warning! Do not enable in production!</b>
     *
     * @return {@code true} if clean should be called. {@code false} if not. (default: {@code false})
     */
    boolean isCleanOnValidationError();

    /**
     * Whether to disable clean.
     * This is especially useful for production environments where running clean can be quite a career limiting move.
     *
     * @return {@code true} to disable clean. {@code false} to leave it enabled. (default: {@code false})
     */
    boolean isCleanDisabled();

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
     * @return {@code true} if mixed migrations should be allowed. {@code false} if an error should be thrown instead. (default: {@code false})
     */
    boolean isMixed();

    /**
     * Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions).
     *
     * @return {@code true} if migrations should be grouped. {@code false} if they should be applied individually instead. (default: {@code false})
     */
    boolean isGroup();

    /**
     * The username that will be recorded in the schema history table as having applied the migration.
     *
     * @return The username or {@code null} for the current database user of the connection. (default: {@code null}).
     */
    String getInstalledBy();

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
     * @return The ErrorOverrides or an empty array if none are defined. (default: none)
     */
    String[] getErrorOverrides();

    /**
     * The stream where to output the SQL statements of a migration dry run. {@code null} if the SQL statements
     * are executed against the database directly.
     * <i>Flyway Teams only</i>
     *
     * @return The stream or {@code null} if the SQL statements are executed against the database directly.
     */
    OutputStream getDryRunOutput();

    /**
     * Whether to stream SQL migrations when executing them. Streaming doesn't load the entire migration in memory at
     * once. Instead each statement is loaded individually. This is particularly useful for very large SQL migrations
     * composed of multiple MB or even GB of reference data, as this dramatically reduces Flyway's memory consumption.
     * <i>Flyway Teams only</i>
     *
     * @return {@code true} to stream SQL migrations. {@code false} to fully loaded them in memory instead. (default: {@code false})
     */
    boolean isStream();

    /**
     * Whether to batch SQL statements when executing them. Batching can save up to 99 percent of network roundtrips by
     * sending up to 100 statements at once over the network to the database, instead of sending each statement
     * individually. This is particularly useful for very large SQL migrations composed of multiple MB or even GB of
     * reference data, as this can dramatically reduce the network overhead. This is supported for INSERT, UPDATE,
     * DELETE, MERGE and UPSERT statements. All other statements are automatically executed without batching.
     * <i>Flyway Teams only</i>
     *
     * @return {@code true} to batch SQL statements. {@code false} to execute them individually instead. (default: {@code false})
     */
    boolean isBatch();

    /**
     * Whether to Flyway's support for Oracle SQL*Plus commands should be activated.
     *
     * <i>Flyway Teams only</i>
     *
     * @return {@code true} to active SQL*Plus support. {@code false} to fail fast instead. (default: {@code false})
     */
    boolean isOracleSqlplus();

    /**
     * Whether Flyway should issue a warning instead of an error whenever it encounters an Oracle SQL*Plus statement
     * it doesn't yet support.
     *
     * <i>Flyway Teams only</i>
     *
     * @return {@code true} to issue a warning. {@code false} to fail fast instead. (default: {@code false})
     */
    boolean isOracleSqlplusWarn();

    /**
     * The path to the Kerberos config file.
     * <i>Flyway Teams only</i>
     */
    String getKerberosConfigFile();

    String getOracleKerberosConfigFile();

    String getOracleKerberosCacheFile();

    /**
     * Your Flyway license key (FL01...). Not yet a Flyway Teams Edition customer?
     * Request your <a href="https://flywaydb.org/download">Flyway trial license key</a>
     * to try out Flyway Teams Edition features free for 30 days.
     *
     * <i>Flyway Teams only</i>
     *
     * @return Your Flyway license key.
     */
    String getLicenseKey();

    /**
     * Whether Flyway should output a table with the results of queries when executing migrations.
     *
     * <i>Flyway Teams only</i>
     *
     * @return {@code true} to output the results table (default: {@code true})
     */
    boolean isOutputQueryResults();

    /**
     * Retrieves the custom ResourceProvider to be used to look up resources. If not set, the default strategy will be used.
     *
     * @return The custom ResourceProvider to be used to look up resources (default: null)
     */
    ResourceProvider getResourceProvider();

    /**
     * Retrieves the custom ClassProvider to be used to look up {@link JavaMigration} classes. If not set, the default strategy will be used.
     *
     * @return The custom ClassProvider to be used to look up {@link JavaMigration} classes (default: null)
     */
    ClassProvider<JavaMigration> getJavaMigrationClassProvider();

    /**
     * Whether Flyway should attempt to create the schemas specified in the schemas property.
     *
     * @return @{code true} to attempt to create the schemas (default: {@code true})
     */
    boolean isCreateSchemas();

    /**
     * The maximum number of retries when trying to obtain a lock. -1 indicates attempting to repeat indefinitely.
     */
    int getLockRetryCount();

    /**
     * Properties to pass to the JDBC driver object
     *
     * <i>Flyway Teams only</i>
     *
     * @return Properties that will be passed to the JDBC driver object
     */
    Map<String, String> getJdbcProperties();

    /**
     * Whether to fail if a location specified in the flyway.locations option doesn't exist
     *
     * @return @{code true} to fail (default: {@code false})
     */
    boolean isFailOnMissingLocations();

    /**
     * The location of your Oracle wallet, used to automatically sign in to your databases.
     *
     * <i>Flyway Teams only</i>
     */
    String getOracleWalletLocation();

    /**
     * The loggers Flyway should use. Valid options are:
     *
     * <ul>
     *     <li>auto: Auto detect the logger (default behavior)</li>
     *     <li>console: Use stdout/stderr (only available when using the CLI)</li>
     *     <li>slf4j2: Use the slf4j2 logger</li>
     *     <li>log4j2: Use the log4j2 logger</li>
     *     <li>apache-commons: Use the Apache Commons logger</li>
     * </ul>
     *
     * Alternatively you can provide the fully qualified class name for any other logger to use that.
     */
    String[] getLoggers();
}