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
package org.flywaydb.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.SettingsProblem;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.logging.EvolvingLog;
import org.flywaydb.core.internal.logging.buffered.BufferedLog;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.File;
import java.util.*;

import static org.flywaydb.core.internal.configuration.ConfigUtils.*;

/**
 * Common base class for all mojos with all common attributes.
 */
@SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
abstract class AbstractFlywayMojo extends AbstractMojo {
    private static final String CONFIG_WORKING_DIRECTORY = "flyway.workingDirectory";
    private static final String CONFIG_SERVER_ID = "flyway.serverId";
    private static final String CONFIG_VERSION = "flyway.version";
    private static final String CONFIG_SKIP = "flyway.skip";
    private static final String CONFIG_CURRENT = "flyway.current";

    Log log;

    /**
     * Whether to skip the execution of the Maven Plugin for this module.
     * <p>Also configurable with Maven or System Property: ${flyway.skip}</p>
     */
    @Parameter(property = CONFIG_SKIP)
    protected boolean skip;

    /**
     * The fully qualified classname of the JDBC driver to use to connect to the database.
     * By default, the driver is autodetected based on the url.
     * <p>Also configurable with Maven or System Property: ${flyway.driver}</p>
     */
    @Parameter(property = ConfigUtils.DRIVER)
    protected String driver;

    /**
     * The JDBC url to use to connect to the database.
     * <p>Also configurable with Maven or System Property: ${flyway.url}</p>
     */
    @Parameter(property = ConfigUtils.URL)
    protected String url;

    /**
     * The user to use to connect to the database. (default: <i>blank</i>)
     * The credentials can be specified by user/password or {@code serverId} from settings.xml
     * <p>Also configurable with Maven or System Property: ${flyway.user}</p>
     */
    @Parameter(property = ConfigUtils.USER)
    protected String user;

    /**
     * The password to use to connect to the database. (default: <i>blank</i>)
     * <p>Also configurable with Maven or System Property: ${flyway.password}</p>
     */
    @Parameter(property = ConfigUtils.PASSWORD)
    private String password;

    /**
     * The maximum number of retries when attempting to connect to the database. After each failed attempt, Flyway will
     * wait 1 second before attempting to connect again, up to the maximum number of times specified by connectRetries.
     * The interval between retries doubles with each subsequent attempt.
     * (default: 0)
     * <p>Also configurable with Maven or System Property: ${flyway.connectRetries}</p>
     */
    @Parameter(property = ConfigUtils.CONNECT_RETRIES)
    private int connectRetries;

    /**
     * The maximum time between retries when attempting to connect to the database in seconds. This will cap the interval
     * between connect retry to the value provided.
     * (default: 120)
     * <p>Also configurable with Maven or System Property: ${flyway.connectRetriesInterval}</p>
     */
    @Parameter(property = ConfigUtils.CONNECT_RETRIES_INTERVAL)
    private int connectRetriesInterval;

    /**
     * The SQL statements to run to initialize a new database connection immediately after opening it. (default: {@code null})
     * <p>Also configurable with Maven or System Property: ${flyway.initSql}</p>
     */
    @Parameter(property = ConfigUtils.INIT_SQL)
    private String initSql;

    /**
     * The default schema managed by Flyway. This schema name is case-sensitive. If not specified, but <i>schemas</i>
     * is, Flyway uses the first schema in that list. If that is also not specified, Flyway uses the default schema for the
     * database connection.
     * <p>Consequences:</p>
     * <ul>
     * <li>This schema will be the one containing the schema history table.</li>
     * <li>This schema will be the default for the database connection (provided the database supports this concept).</li>
     * </ul>
     * <p>Also configurable with Maven or System Property: ${flyway.defaultSchema}</p>
     */
    @Parameter
    private String defaultSchema;

    /**
     * The schemas managed by Flyway. These schema names are case-sensitive. If not specified, Flyway uses
     * the default schema for the database connection. If <i>defaultSchema</i> is not specified, then the first of
     * this list also acts as default schema.
     * <p>Consequences:</p>
     * <ul>
     * <li>Flyway will automatically attempt to create all these schemas, unless they already exist.</li>
     * <li>The schemas will be cleaned in the order of this list.</li>
     * <li>If Flyway created them, the schemas themselves will be dropped when cleaning.</li>
     * </ul>
     * <p>Also configurable with Maven or System Property: ${flyway.schemas} (comma-separated list)</p>
     */
    @Parameter
    private String[] schemas;

    /**
     * The name of the schema history table that will be used by Flyway. (default: flyway_schema_history)
     * By default, (single-schema mode) the schema history table is placed in the default schema for the connection provided by the datasource.
     * When the {@code flyway.schemas} property is set (multi-schema mode), the schema history table is placed in the first schema of the list,
     * or in the schema specified to {@code flyway.defaultSchema}.
     * <p>Also configurable with Maven or System Property: ${flyway.table}</p>
     */
    @Parameter(property = ConfigUtils.TABLE)
    private String table;

    /**
     * The tablespace where to create the schema history table that will be used by Flyway.
     * If not specified, Flyway uses the default tablespace for the database connection.
     * This setting is only relevant for databases that support the notion of tablespaces. Its value is simply
     * ignored for all others.
     * <p>Also configurable with Maven or System Property: ${flyway.tablespace}</p>
     */
    @Parameter(property = ConfigUtils.TABLESPACE)
    private String tablespace;

    /**
     * The version to tag an existing schema with when executing baseline. (default: 1)
     * <p>Also configurable with Maven or System Property: ${flyway.baselineVersion}</p>
     */
    @Parameter(property = ConfigUtils.BASELINE_VERSION)
    private String baselineVersion;

    /**
     * The description to tag an existing schema with when executing baseline. (default: << Flyway Baseline >>)
     * <p>Also configurable with Maven or System Property: ${flyway.baselineDescription}</p>
     */
    @Parameter(property = ConfigUtils.BASELINE_DESCRIPTION)
    private String baselineDescription;

    /**
     * Locations to scan recursively for migrations.
     * The location type is determined by its prefix.
     * Unprefixed locations or locations starting with {@code classpath:} point to a package on the classpath and may
     * contain both SQL and Java-based migrations.
     * Locations starting with {@code filesystem:} point to a directory on the filesystem, may only
     * contain SQL migrations and are only scanned recursively down non-hidden directories.
     * (default: filesystem:src/main/resources/db/migration)
     * <p>Also configurable with Maven or System Property: ${flyway.locations} (Comma-separated list)</p>
     */
    @Parameter
    private String[] locations;

    /**
     * The fully qualified class names of the custom MigrationResolvers to be used in addition or as replacement
     * (if skipDefaultResolvers is true) to the built-in ones for resolving Migrations to apply. (default: none)
     * <p>Also configurable with Maven or System Property: ${flyway.resolvers} (Comma-separated list)</p>
     */
    @Parameter
    private String[] resolvers;

    /**
     * When set to true, default resolvers are skipped, i.e. only custom resolvers as defined by 'resolvers'
     * are used. (default: false)
     * <p>Also configurable with Maven or System Property: ${flyway.skipDefaultResolvers}</p>
     */
    @Parameter(property = ConfigUtils.SKIP_DEFAULT_RESOLVERS)
    private Boolean skipDefaultResolvers;

    /**
     * The encoding of SQL migrations. (default: UTF-8)
     * <p>Also configurable with Maven or System Property: ${flyway.encoding}</p>
     */
    @Parameter(property = ConfigUtils.ENCODING)
    private String encoding;

    /**
     * Whether Flyway should try to automatically detect SQL migration file encoding
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Maven or System Property: ${flyway.detectEncoding}</p>
     */
    @Parameter
    private Boolean detectEncoding;

    /**
     * The maximum number of retries when trying to obtain a lock. (default: 50)
     * <p>Also configurable with Maven or System Property: ${flyway.lockRetryCount}</p>
     */
    @Parameter(property = ConfigUtils.LOCK_RETRY_COUNT)
    private Integer lockRetryCount;

    /**
     * The file name prefix for versioned SQL migrations (default: V)
     * <p>Versioned SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to V1_1__My_description.sql</p>
     * <p>Also configurable with Maven or System Property: ${flyway.sqlMigrationPrefix}</p>
     */
    @Parameter(property = ConfigUtils.SQL_MIGRATION_PREFIX)
    private String sqlMigrationPrefix;

    /**
     * The file name prefix for baseline migrations. (default: B)
     * They have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to B1.1__My_description.sql
     * <p>Also configurable with Maven or System Property: ${flyway.baselineMigrationPrefix}</p>
     * <i>Flyway Teams only</i>
     */
    @Parameter(property = ConfigUtils.BASELINE_MIGRATION_PREFIX)
    private String baselineMigrationPrefix;

    /**
     * The file name prefix for undo SQL migrations. (default: U)
     * Undo SQL migrations are responsible for undoing the effects of the versioned migration with the same version.
     * <p>They have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to U1.1__My_description.sql</p>
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Maven or System Property: ${flyway.undoSqlMigrationPrefix}</p>
     */
    @Parameter(property = ConfigUtils.UNDO_SQL_MIGRATION_PREFIX)
    private String undoSqlMigrationPrefix;

    /**
     * The file name prefix for repeatable SQL migrations (default: R)
     * <p>Repeatable SQL migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix,
     * which using the defaults translates to R__My_description.sql</p>
     * <p>Also configurable with Maven or System Property:${flyway.repeatableSqlMigrationPrefix}</p>
     */
    @Parameter(property = ConfigUtils.REPEATABLE_SQL_MIGRATION_PREFIX)
    private String repeatableSqlMigrationPrefix;

    /**
     * The file name separator for SQL migrations (default: __)
     * <p>SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to V1_1__My_description.sql</p>
     * <p>Also configurable with Maven or System Property: ${flyway.sqlMigrationSeparator}</p>
     */
    @Parameter(property = ConfigUtils.SQL_MIGRATION_SEPARATOR)
    private String sqlMigrationSeparator;

    /**
     * The file name suffixes for SQL migrations. (default: .sql)
     * <p>SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to V1_1__My_description.sql</p>
     * <p>Multiple suffixes (like .sql,.pkg,.pkb) can be specified for easier compatibility with other tools such as
     * editors with specific file associations.</p>
     * <p>Also configurable with Maven or System Property: ${flyway.sqlMigrationSuffixes}</p>
     */
    @Parameter
    private String[] sqlMigrationSuffixes;

    /**
     * Whether to automatically call clean or not when a validation error occurs. (default: {@code false})
     * <p>This is exclusively intended as a convenience for development. even though we strongly recommend not to
     * change migration scripts once they have been checked into SCM and run, this provides a way of dealing with this
     * case in a smooth manner. The database will be wiped clean automatically, ensuring that the next migration will
     * bring you back to the state checked into SCM.</p>
     * <p><b>Warning! Do not enable in production!</b></p>
     * <p>Also configurable with Maven or System Property: ${flyway.cleanOnValidationError}</p>
     */
    @Parameter(property = ConfigUtils.CLEAN_ON_VALIDATION_ERROR)
    private Boolean cleanOnValidationError;

    /**
     * Whether to disable clean. (default: {@code false})
     * This is especially useful for production environments where running clean can be quite a career limiting move.
     * <p>Also configurable with Maven or System Property: ${flyway.cleanDisabled}</p>
     */
    @Parameter(property = ConfigUtils.CLEAN_DISABLED)
    private Boolean cleanDisabled;

    /**
     * The target version up to which Flyway should consider migrations.
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
     * Defaults to {@code latest}.
     * <p>Also configurable with Maven or System Property: ${flyway.target}</p>
     */
    @Parameter(property = ConfigUtils.TARGET)
    private String target;

    /**
     * Gets the migrations that Flyway should consider when migrating or undoing. Leave empty to consider all available migrations.
     * Migrations not in this list will be ignored.
     * Values should be the version for versioned migrations (e.g. 1, 2.4, 6.5.3) or the description for repeatable migrations (e.g. Insert_Data, Create_Table)
     * <i>Flyway Teams only</i>
     */
    @Parameter
    private String[] cherryPick;

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
    @Parameter
    private String[] loggers;

    /**
     * Allows migrations to be run "out of order" (default: {@code false}).
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     * <p>Also configurable with Maven or System Property: ${flyway.outOfOrder}</p>
     */
    @Parameter(property = ConfigUtils.OUT_OF_ORDER)
    private Boolean outOfOrder;

    /**
     * Whether Flyway should skip actually executing the contents of the migrations and only update the schema history table.
     * This should be used when you have applied a migration manually (via executing the sql yourself, or via an IDE), and
     * just want the schema history table to reflect this.
     * Use in conjunction with {@code cherryPick} to skip specific migrations instead of all pending ones.
     * <i>Flyway Teams only</i>
     */
    @Parameter(property = ConfigUtils.SKIP_EXECUTING_MIGRATIONS)
    private Boolean skipExecutingMigrations;

    /**
     * Whether Flyway should output a table with the results of queries when executing migrations (default: true).
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Maven or System Property: ${flyway.outputQueryResults}</p>
     */
    @Parameter(property = ConfigUtils.OUTPUT_QUERY_RESULTS)
    private Boolean outputQueryResults;

    /**
     * @deprecated Will remove in Flyway V9. Use {@code ignoreMigrationPatterns} instead.
     *
     * Ignore missing migrations when reading the schema history table. These are migrations that were performed by an
     * older deployment of the application that are no longer available in this version. For example: we have migrations
     * available on the classpath with versions 1.0 and 3.0. The schema history table indicates that a migration with version 2.0
     * (unknown to us) has also been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to deploy
     * a newer version of the application even though it doesn't contain migrations included with an older one anymore.
     * Note that if the most recently applied migration is removed, Flyway has no way to know it is missing and will
     * mark it as future instead. (default: {@code false})
     * <p>Also configurable with Maven or System Property: ${flyway.ignoreMissingMigrations}</p>
     */
    @Parameter(property = ConfigUtils.IGNORE_MISSING_MIGRATIONS)
    private Boolean ignoreMissingMigrations;

    /**
     * @deprecated Will remove in Flyway V9. Use {@code ignoreMigrationPatterns} instead.
     *
     * Ignore ignored migrations when reading the schema history table. These are migrations that were added in between
     * already migrated migrations in this version. For example: we have migrations available on the classpath with
     * versions from 1.0 to 3.0. The schema history table indicates that version 1 was finished on 1.0.15, and the next
     * one was 2.0.0. But with the next release a new migration was added to version 1: 1.0.16. Such scenario is ignored
     * by migrate command, but by default is rejected by validate. When ignoreIgnoredMigrations is enabled, such case
     * will not be reported by validate command. This is useful for situations where one must be able to deliver
     * complete set of migrations in a delivery package for multiple versions of the product, and allows for further
     * development of older versions. (default: {@code false})
     * <p>Also configurable with Maven or System Property: ${flyway.ignoreIgnoredMigrations}</p>
     */
    @Parameter(property = ConfigUtils.IGNORE_IGNORED_MIGRATIONS)
    private Boolean ignoreIgnoredMigrations;

    /**
     * @deprecated Will remove in Flyway V9. Use {@code ignoreMigrationPatterns} instead.
     *
     * Ignore pending migrations when reading the schema history table. These are migrations that are available
     * but have not yet been applied. This can be useful for verifying that in-development migration changes
     * don't contain any validation-breaking changes of migrations that have already been applied to a production
     * environment, e.g. as part of a CI/CD process, without failing because of the existence of new migration versions.
     * (default: {@code false})
     * <p>Also configurable with Maven or System Property: ${flyway.ignorePendingMigrations}</p>
     */
    @Parameter(property = ConfigUtils.IGNORE_PENDING_MIGRATIONS)
    private Boolean ignorePendingMigrations;

    /**
     * @deprecated Will remove in Flyway V9. Use {@code ignoreMigrationPatterns} instead.
     *
     * Ignore future migrations when reading the schema history table. These are migrations that were performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The schema history table indicates that a migration to version 4.0
     * (unknown to us) has already been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to redeploy
     * an older version of the application after the database has been migrated by a newer one. (default: {@code true})
     * <p>Also configurable with Maven or System Property: ${flyway.ignoreFutureMigrations}</p>
     */
    @Parameter(property = ConfigUtils.IGNORE_FUTURE_MIGRATIONS)
    private Boolean ignoreFutureMigrations;

    /**
     * Ignore migrations that match this comma-separated list of patterns when validating migrations.
     * Each pattern is of the form <migration_type>:<migration_state>
     * See https://flywaydb.org/documentation/configuration/parameters/ignoreMigrationPatterns for full details
     * Example: repeatable:missing,versioned:pending,*:failed
     * <i>Flyway Teams only</i>
     */
    @Parameter
    private String[] ignoreMigrationPatterns;

    /**
     * Whether to validate migrations and callbacks whose scripts do not obey the correct naming convention. A failure
     * can be useful to check that errors such as case sensitivity in migration prefixes have been corrected.
     * <p>Also configurable with Maven or System Property: ${flyway.validateMigrationNaming}</p>
     */
    @Parameter(property = ConfigUtils.VALIDATE_MIGRATION_NAMING)
    private Boolean validateMigrationNaming;

    /**
     * Whether placeholders should be replaced. (default: true)
     * <p>Also configurable with Maven or System Property: ${flyway.placeholderReplacement}</p>
     */
    @Parameter(property = ConfigUtils.PLACEHOLDER_REPLACEMENT)
    private Boolean placeholderReplacement;

    /**
     * A map of &lt;placeholder, replacementValue&gt; to apply to SQL migration scripts.
     * <p>Also configurable with Maven or System Properties like ${flyway.placeholders.myplaceholder} or ${flyway.placeholders.otherone}</p>
     */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Parameter
    private Map<String, String> placeholders;

    /**
     * A map of &lt;propertyName, propertyValue&gt; to pass to the JDBC driver object
     * <p>Also configurable with Maven or System Properties like ${flyway.jdbcProperties.myProperty} or ${flyway.jdbcProperties.otherProperty}</p>
     */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Parameter
    private Map<String, String> jdbcProperties;

    /**
     * The prefix of every placeholder. (default: ${ )
     * <p>Also configurable with Maven or System Property: ${flyway.placeholderPrefix}</p>
     */
    @Parameter(property = ConfigUtils.PLACEHOLDER_PREFIX)
    private String placeholderPrefix;

    /**
     * The suffix of every placeholder. (default: } )
     * <p>Also configurable with Maven or System Property: ${flyway.placeholderSuffix}</p>
     */
    @Parameter(property = ConfigUtils.PLACEHOLDER_SUFFIX)
    private String placeholderSuffix;

    /**
     * The prefix of every script placeholder. (default: FP__ )
     */
    @Parameter(property = ConfigUtils.SCRIPT_PLACEHOLDER_PREFIX)
    private String scriptPlaceholderPrefix;

    /**
     * The suffix of every script placeholder. (default: __ )
     */
    @Parameter(property = ConfigUtils.SCRIPT_PLACEHOLDER_SUFFIX)
    private String scriptPlaceholderSuffix;

    /**
     * An array of FlywayCallback implementations. (default: empty )
     * <p>Also configurable with Maven or System Property: ${flyway.callbacks}</p>
     */
    @Parameter
    private String[] callbacks;

    /**
     * When set to true, default callbacks are skipped, i.e. only custom callbacks as defined by 'resolvers'
     * are used. (default: false)
     * <p>Also configurable with Maven or System Property: ${flyway.skipDefaultCallbacks}</p>
     */
    @Parameter(property = ConfigUtils.SKIP_DEFAULT_CALLBACKS)
    private Boolean skipDefaultCallbacks;

    /**
     * Whether to automatically call baseline when migrate is executed against a non-empty schema with no schema history table.
     * This schema will then be baselined with the {@code initialVersion} before executing the migrations.
     * Only migrations above {@code initialVersion} will then be applied.
     * This is useful for initial Flyway production deployments on projects with an existing DB.
     * Be careful when enabling this as it removes the safety net that ensures Flyway does not migrate the wrong
     * database in case of a configuration mistake! (default: {@code false})
     * <p>Also configurable with Maven or System Property: ${flyway.baselineOnMigrate}</p>
     */
    @Parameter(property = ConfigUtils.BASELINE_ON_MIGRATE)
    private Boolean baselineOnMigrate;

    /**
     * Whether to automatically call validate or not when running migrate. (default: {@code true})
     * <p>Also configurable with Maven or System Property: ${flyway.validateOnMigrate}</p>
     */
    @Parameter(property = ConfigUtils.VALIDATE_ON_MIGRATE)
    private Boolean validateOnMigrate;

    /**
     * Whether to allow mixing transactional and non-transactional statements within the same migration. Enabling this
     * automatically causes the entire affected migration to be run without a transaction.
     *
     * Note that this is only applicable for PostgreSQL, Aurora PostgreSQL, SQL Server and SQLite which all have
     * statements that do not run at all within a transaction.
     *
     * This is not to be confused with implicit transaction, as they occur in MySQL or Oracle, where even though a
     * DDL statement was run within a transaction, the database will issue an implicit commit before and after
     * its execution.
     * {@code true} if mixed migrations should be allowed. {@code false} if an error should be thrown instead. (default: {@code false})
     * <p>Also configurable with Maven or System Property: ${flyway.mixed}</p>
     */
    @Parameter(property = ConfigUtils.MIXED)
    private Boolean mixed;

    /**
     * Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions).
     * {@code true} if migrations should be grouped. {@code false} if they should be applied individually instead. (default: {@code false})
     * <p>Also configurable with Maven or System Property: ${flyway.group}</p>
     */
    @Parameter(property = ConfigUtils.GROUP)
    private Boolean group;

    /**
     * The username that will be recorded in the schema history table as having applied the migration.
     * {@code null} for the current database user of the connection. (default: {@code null}).
     * <p>Also configurable with Maven or System Property: ${flyway.installedBy}</p>
     */
    @Parameter(property = ConfigUtils.INSTALLED_BY)
    private String installedBy;

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
     * the following errorOverride can be used: {@code *:123:W}
     *
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Maven or System Property: ${flyway.errorOverrides}</p>
     */
    @Parameter
    private String[] errorOverrides;

    /**
     * The file where to output the SQL statements of a migration dry run. If the file specified is in a non-existent
     * directory, Flyway will create all directories and parent directories as needed.
     * Paths starting with s3: point to a bucket in AWS S3, which must exist. They are in the format s3:<bucket>(/optionalfolder/subfolder)/filename.sql
     * Paths starting with gcs: point to a bucket in Google Cloud Storage, which must exist. They are in the format gcs:<bucket>(/optionalfolder/subfolder)/filename.sql
     * {@code null} to execute the SQL statements directly against the database. (default: {@code null})
     *
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Maven or System Property: ${flyway.dryRunOutput}</p>
     */
    @Parameter(property = ConfigUtils.DRYRUN_OUTPUT)
    private String dryRunOutput;

    /**
     * Whether to stream SQL migrations when executing them. Streaming doesn't load the entire migration in memory at
     * once. Instead each statement is loaded individually. This is particularly useful for very large SQL migrations
     * composed of multiple MB or even GB of reference data, as this dramatically reduces Flyway's memory consumption.
     * (default: {@code false}
     *
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Maven or System Property: ${flyway.stream}</p>
     */
    @Parameter(property = ConfigUtils.STREAM)
    private Boolean stream;

    /**
     * Whether to batch SQL statements when executing them. Batching can save up to 99 percent of network roundtrips by
     * sending up to 100 statements at once over the network to the database, instead of sending each statement
     * individually. This is particularly useful for very large SQL migrations composed of multiple MB or even GB of
     * reference data, as this can dramatically reduce the network overhead. This is supported for INSERT, UPDATE,
     * DELETE, MERGE and UPSERT statements. All other statements are automatically executed without batching.
     * (default: {@code false})
     *
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Maven or System Property: ${flyway.batch}</p>
     */
    @Parameter(property = ConfigUtils.BATCH)
    private Boolean batch;

    /**
     * Whether to Flyway's support for Oracle SQL*Plus commands should be activated. (default: {@code false})
     *
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Maven or System Property: ${flyway.oracle.sqlplus}</p>
     */
    @Parameter(property = ConfigUtils.ORACLE_SQLPLUS)
    private Boolean oracleSqlplus;

    /**
     * Whether Flyway should issue a warning instead of an error whenever it encounters an Oracle SQL*Plus statement
     * it doesn't yet support. (default: {@code false})
     *
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Maven or System Property: ${flyway.oracle.sqlplusWarn}</p>
     */
    @Parameter(property = ConfigUtils.ORACLE_SQLPLUS_WARN)
    private Boolean oracleSqlplusWarn;

    /**
     * The location of your Oracle wallet, used to automatically sign in to your databases.
     *
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Maven or System Property: ${flyway.oracle.walletLocation}</p>
     */
    @Parameter(property = ConfigUtils.ORACLE_WALLET_LOCATION)
    private String oracleWalletLocation;

    /**
     * When connecting to a Kerberos service to authenticate, the path to the Kerberos config file.
     * <i>Flyway Teams only</i>
     */
    @Parameter(property = ConfigUtils.KERBEROS_CONFIG_FILE)
    private String kerberosConfigFile;

    /**
     * Your Flyway license key (FL01...). Not yet a Flyway Teams Edition customer?
     * Request your <a href="https://flywaydb.org/download">Flyway trial license key</a>
     * to try out Flyway Teams Edition features free for 30 days.
     *
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Maven or System Property: ${flyway.licenseKey}</p>
     */
    @Parameter(property = ConfigUtils.LICENSE_KEY)
    private String licenseKey;

    /**
     * The encoding of the external config files specified with the {@code flyway.configFiles} property. (default: UTF-8).
     * <p>Also configurable with Maven or System Property: ${flyway.configFileEncoding}</p>
     */
    @Parameter(property = ConfigUtils.CONFIG_FILE_ENCODING)
    private String configFileEncoding;

    /**
     * Config files from which to load the Flyway configuration. The names of the individual properties match the ones
     * you would use as Maven or System properties. The encoding of the files is defined by the
     * flyway.configFileEncoding property, which is UTF-8 by default. Relative paths are relative to the POM or
     * <code>workingDirectory</code> if set.
     * <p>Also configurable with Maven or System Property: ${flyway.configFiles}</p>
     */
    @Parameter(property = ConfigUtils.CONFIG_FILES)
    private String[] configFiles;

    /**
     * Whether Flyway should attempt to create the schemas specified in the schemas property.
     * <p>Also configurable with Maven or System Property: ${flyway.createSchemas}</p>
     */
    @Parameter(property = ConfigUtils.CREATE_SCHEMAS)
    private Boolean createSchemas;

    /**
     * The working directory to consider when dealing with relative paths for both config files and locations.
     * (default: basedir, the directory where the POM resides)
     * <p>Also configurable with Maven or System Property: ${flyway.workingDirectory}</p>
     */
    @Parameter(property = CONFIG_WORKING_DIRECTORY)
    private String workingDirectory;

    /**
     * Whether to fail if a location specified in the flyway.locations option doesn't exist
     *
     * @return @{code true} to fail (default: {@code false})
     */
    @Parameter(property = ConfigUtils.FAIL_ON_MISSING_LOCATIONS)
    public Boolean failOnMissingLocations;

    /**
     * The id of the server tag in settings.xml (default: flyway-db)
     * The credentials can be specified by user/password or {@code serverId} from settings.xml
     * <p>Also configurable with Maven or System Property: ${flyway.serverId}</p>
     */
    @Parameter(property = CONFIG_SERVER_ID)
    private String serverId = "flyway-db";

    /**
     * The link to the settings.xml
     */
    @Parameter(defaultValue = "${settings}", readonly = true)
    protected Settings settings;

    /**
     * The configuration for plugins
     * You will need to configure this with the key and value specific to your plugin
     */
    @Parameter
    private Map<String, String> pluginConfiguration;

    /**
     * Reference to the current project that includes the Flyway Maven plugin.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject mavenProject;

    @Component
    private SettingsDecrypter settingsDecrypter;

    /**
     * Load username and password from settings.
     *
     * @throws FlywayException when the credentials could not be loaded.
     */
    private void loadCredentialsFromSettings() throws FlywayException {
        final Server server = settings.getServer(serverId);
        if (user == null) {
            if (server != null) {
                user = server.getUsername();
                SettingsDecryptionResult result =
                        settingsDecrypter.decrypt(new DefaultSettingsDecryptionRequest(server));
                for (SettingsProblem problem : result.getProblems()) {
                    if (problem.getSeverity() == SettingsProblem.Severity.ERROR
                            || problem.getSeverity() == SettingsProblem.Severity.FATAL) {
                        throw new FlywayException("Unable to decrypt password: " + problem, problem.getException());
                    }
                }
                password = result.getServer().getPassword();
            }
        } else if (server != null) {
            throw new FlywayException("You specified credentials both in the Flyway config and settings.xml. Use either one or the other");
        }
    }

    /**
     * Retrieves the value of this boolean property, based on the matching System property on the Maven property.
     *
     * @param systemPropertyName The name of the System property.
     * @param mavenPropertyValue The value of the Maven property.
     */
    protected boolean getBooleanProperty(String systemPropertyName, boolean mavenPropertyValue) {
        String systemPropertyValue = System.getProperty(systemPropertyName);
        if (systemPropertyValue != null) {
            return Boolean.parseBoolean(systemPropertyValue);
        }
        return mavenPropertyValue;
    }

    public final void execute() throws MojoExecutionException {
        LogFactory.setFallbackLogCreator(new MavenLogCreator(this));
        log = LogFactory.getLog(getClass());

        try {
            if (getBooleanProperty(CONFIG_SKIP, skip)) {
                log.info("Skipping Flyway execution");
                return;
            }

            Set<String> classpathElements = new HashSet<>();
            classpathElements.addAll(mavenProject.getCompileClasspathElements());
            classpathElements.addAll(mavenProject.getRuntimeClasspathElements());

            ClassRealm classLoader = (ClassRealm) Thread.currentThread().getContextClassLoader();
            for (String classpathElement : classpathElements) {
                classLoader.addURL(new File(classpathElement).toURI().toURL());
            }

            File workDir = StringUtils.hasText(workingDirectory) ?
                    toFile(mavenProject.getBasedir(), workingDirectory) :
                    mavenProject.getBasedir();

            if (locations != null) {
                for (int i = 0; i < locations.length; i++) {
                    if (locations[i].startsWith(Location.FILESYSTEM_PREFIX)) {
                        String newLocation = locations[i].substring(Location.FILESYSTEM_PREFIX.length());
                        File file = toFile(workDir, newLocation);
                        locations[i] = Location.FILESYSTEM_PREFIX + file.getAbsolutePath();
                    }
                }
            } else {
                locations = new String[] {
                        Location.FILESYSTEM_PREFIX + workDir.getAbsolutePath() + "/src/main/resources/db/migration"
                };
            }

            Map<String, String> envVars = ConfigUtils.environmentVariablesToPropertyMap();

            Map<String, String> conf = new HashMap<>(loadConfigurationFromDefaultConfigFiles(envVars));

            loadCredentialsFromSettings();

            putIfSet(conf, ConfigUtils.DRIVER, driver);
            putIfSet(conf, ConfigUtils.URL, url);
            putIfSet(conf, ConfigUtils.USER, user);
            putIfSet(conf, ConfigUtils.PASSWORD, password);
            putIfSet(conf, ConfigUtils.CONNECT_RETRIES, connectRetries);
            putIfSet(conf, ConfigUtils.CONNECT_RETRIES_INTERVAL, connectRetriesInterval);
            putIfSet(conf, ConfigUtils.INIT_SQL, initSql);
            putIfSet(conf, ConfigUtils.DEFAULT_SCHEMA, defaultSchema);
            putArrayIfSet(conf, ConfigUtils.SCHEMAS, schemas);
            putIfSet(conf, ConfigUtils.TABLE, table);
            putIfSet(conf, ConfigUtils.TABLESPACE, tablespace);
            putIfSet(conf, ConfigUtils.BASELINE_VERSION, baselineVersion);
            putIfSet(conf, ConfigUtils.BASELINE_DESCRIPTION, baselineDescription);
            putArrayIfSet(conf, ConfigUtils.LOCATIONS, locations);
            putArrayIfSet(conf, ConfigUtils.RESOLVERS, resolvers);
            putIfSet(conf, ConfigUtils.SKIP_DEFAULT_RESOLVERS, skipDefaultResolvers);
            putArrayIfSet(conf, ConfigUtils.CALLBACKS, callbacks);
            putIfSet(conf, ConfigUtils.SKIP_DEFAULT_CALLBACKS, skipDefaultCallbacks);
            putIfSet(conf, ConfigUtils.ENCODING, encoding);
            putIfSet(conf, ConfigUtils.DETECT_ENCODING, detectEncoding);
            putIfSet(conf, ConfigUtils.LOCK_RETRY_COUNT, lockRetryCount);
            putIfSet(conf, ConfigUtils.SQL_MIGRATION_PREFIX, sqlMigrationPrefix);
            putIfSet(conf, ConfigUtils.BASELINE_MIGRATION_PREFIX, baselineMigrationPrefix);
            putIfSet(conf, ConfigUtils.UNDO_SQL_MIGRATION_PREFIX, undoSqlMigrationPrefix);
            putIfSet(conf, ConfigUtils.REPEATABLE_SQL_MIGRATION_PREFIX, repeatableSqlMigrationPrefix);
            putIfSet(conf, ConfigUtils.SQL_MIGRATION_SEPARATOR, sqlMigrationSeparator);
            putArrayIfSet(conf, ConfigUtils.SQL_MIGRATION_SUFFIXES, sqlMigrationSuffixes);
            putIfSet(conf, ConfigUtils.MIXED, mixed);
            putIfSet(conf, ConfigUtils.GROUP, group);
            putIfSet(conf, ConfigUtils.INSTALLED_BY, installedBy);
            putIfSet(conf, ConfigUtils.CLEAN_ON_VALIDATION_ERROR, cleanOnValidationError);
            putIfSet(conf, ConfigUtils.CLEAN_DISABLED, cleanDisabled);
            putIfSet(conf, ConfigUtils.OUT_OF_ORDER, outOfOrder);
            putIfSet(conf, ConfigUtils.SKIP_EXECUTING_MIGRATIONS, skipExecutingMigrations);
            putIfSet(conf, ConfigUtils.OUTPUT_QUERY_RESULTS, outputQueryResults);
            putIfSet(conf, ConfigUtils.TARGET, target);
            putArrayIfSet(conf, ConfigUtils.CHERRY_PICK, cherryPick);
            putArrayIfSet(conf, ConfigUtils.LOGGERS, loggers);
            putIfSet(conf, ConfigUtils.IGNORE_MISSING_MIGRATIONS, ignoreMissingMigrations);
            putIfSet(conf, ConfigUtils.IGNORE_IGNORED_MIGRATIONS, ignoreIgnoredMigrations);
            putIfSet(conf, ConfigUtils.IGNORE_PENDING_MIGRATIONS, ignorePendingMigrations);
            putIfSet(conf, ConfigUtils.IGNORE_FUTURE_MIGRATIONS, ignoreFutureMigrations);
            putArrayIfSet(conf, ConfigUtils.IGNORE_MIGRATION_PATTERNS, ignoreMigrationPatterns);
            putIfSet(conf, ConfigUtils.VALIDATE_MIGRATION_NAMING, validateMigrationNaming);
            putIfSet(conf, ConfigUtils.PLACEHOLDER_REPLACEMENT, placeholderReplacement);
            putIfSet(conf, ConfigUtils.PLACEHOLDER_PREFIX, placeholderPrefix);
            putIfSet(conf, ConfigUtils.PLACEHOLDER_SUFFIX, placeholderSuffix);
            putIfSet(conf, ConfigUtils.SCRIPT_PLACEHOLDER_PREFIX, scriptPlaceholderPrefix);
            putIfSet(conf, ConfigUtils.SCRIPT_PLACEHOLDER_SUFFIX, scriptPlaceholderSuffix);
            putIfSet(conf, ConfigUtils.BASELINE_ON_MIGRATE, baselineOnMigrate);
            putIfSet(conf, ConfigUtils.VALIDATE_ON_MIGRATE, validateOnMigrate);
            putIfSet(conf, ConfigUtils.DRIVER, driver);
            putIfSet(conf, ConfigUtils.CREATE_SCHEMAS, createSchemas);
            putIfSet(conf, ConfigUtils.FAIL_ON_MISSING_LOCATIONS, failOnMissingLocations);

            putArrayIfSet(conf, ConfigUtils.ERROR_OVERRIDES, errorOverrides);
            putIfSet(conf, ConfigUtils.DRYRUN_OUTPUT, dryRunOutput);
            putIfSet(conf, ConfigUtils.STREAM, stream);
            putIfSet(conf, ConfigUtils.BATCH, batch);

            putIfSet(conf, ConfigUtils.ORACLE_SQLPLUS, oracleSqlplus);
            putIfSet(conf, ConfigUtils.ORACLE_SQLPLUS_WARN, oracleSqlplusWarn);
            putIfSet(conf, ConfigUtils.ORACLE_WALLET_LOCATION, oracleWalletLocation);

            putIfSet(conf, ConfigUtils.KERBEROS_CONFIG_FILE, kerberosConfigFile);

            putIfSet(conf, ConfigUtils.LICENSE_KEY, licenseKey);

            if (placeholders != null) {
                for (String placeholder : placeholders.keySet()) {
                    String value = placeholders.get(placeholder);
                    conf.put(ConfigUtils.PLACEHOLDERS_PROPERTY_PREFIX + placeholder, value == null ? "" : value);
                }
            }

            if (jdbcProperties != null) {
                for (String property : jdbcProperties.keySet()) {
                    String value = jdbcProperties.get(property);
                    conf.put(ConfigUtils.JDBC_PROPERTIES_PREFIX + property, value == null ? "" : value);
                }
            }

            conf.putAll(getPluginConfiguration(pluginConfiguration));

            conf.putAll(ConfigUtils.propertiesToMap(mavenProject.getProperties()));
            conf.putAll(loadConfigurationFromConfigFiles(workDir, envVars));
            conf.putAll(envVars);
            conf.putAll(ConfigUtils.propertiesToMap(System.getProperties()));
            removeMavenPluginSpecificPropertiesToAvoidWarnings(conf);

            if (conf.getOrDefault(ConfigUtils.LOGGERS, "auto").equalsIgnoreCase("auto")) {
                conf.put(ConfigUtils.LOGGERS, "maven");
            }

            Flyway flyway = Flyway.configure(classLoader).configuration(conf).load();
            doExecute(flyway);
        } catch (Exception e) {
            throw new MojoExecutionException(e.toString(), ExceptionUtils.getRootCause(e));
        } finally {
            Log currentLog = ((EvolvingLog) log).getLog();
            if (currentLog instanceof BufferedLog) {
                ((BufferedLog) currentLog).flush(new MavenLog(this.getLog()));
            }
        }
    }

    public Map<String, String> getPluginConfiguration(Map<String, String> pluginConfiguration) {
        Map<String, String> conf = new HashMap<>();

        if (pluginConfiguration == null) {
            return conf;
        }

        String camelCaseRegex = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";
        for (String key : pluginConfiguration.keySet()) {
            conf.put(FLYWAY_PLUGINS_PREFIX + String.join(".", key.split(camelCaseRegex)).toLowerCase(), pluginConfiguration.get(key));
        }

        return conf;
    }

    /**
     * Determines the files to use for loading the configuration.
     *
     * @param workDir The working directory to use.
     * @param envVars The environment variables converted to Flyway properties.
     * @return The configuration files.
     */
    private List<File> determineConfigFiles(File workDir, Map<String, String> envVars) {
        List<File> configFiles = new ArrayList<>();

        if (envVars.containsKey(ConfigUtils.CONFIG_FILES)) {
            for (String file : StringUtils.tokenizeToStringArray(envVars.get(ConfigUtils.CONFIG_FILES), ",")) {
                configFiles.add(toFile(workDir, file));
            }
            return configFiles;
        }

        if (System.getProperties().containsKey(ConfigUtils.CONFIG_FILES)) {
            for (String file : StringUtils.tokenizeToStringArray(System.getProperties().getProperty(ConfigUtils.CONFIG_FILES), ",")) {
                configFiles.add(toFile(workDir, file));
            }
            return configFiles;
        }

        if (mavenProject.getProperties().containsKey(ConfigUtils.CONFIG_FILES)) {
            for (String file : StringUtils.tokenizeToStringArray(mavenProject.getProperties().getProperty(ConfigUtils.CONFIG_FILES), ",")) {
                configFiles.add(toFile(workDir, file));
            }
        } else if (this.configFiles != null) {
            for (String configFile : this.configFiles) {
                configFiles.add(toFile(workDir, configFile));
            }
        }
        return configFiles;
    }

    /**
     * Converts this file name into a file, adjusting relative paths if necessary to make them relative to the pom.
     *
     * @param workDir The working directory to use.
     * @param fileName The name of the file, relative or absolute.
     * @return The resulting file.
     */
    private File toFile(File workDir, String fileName) {
        File file = new File(fileName);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(workDir, fileName);
    }

    /**
     * Determines the encoding to use for loading the configuration files.
     *
     * @param envVars The environment variables converted to Flyway properties.
     * @return The encoding. (default: UTF-8)
     */
    private String determineConfigurationFileEncoding(Map<String, String> envVars) {
        if (envVars.containsKey(ConfigUtils.CONFIG_FILE_ENCODING)) {
            return envVars.get(ConfigUtils.CONFIG_FILE_ENCODING);
        }
        if (System.getProperties().containsKey(ConfigUtils.CONFIG_FILE_ENCODING)) {
            return System.getProperties().getProperty(ConfigUtils.CONFIG_FILE_ENCODING);
        }
        if (configFileEncoding != null) {
            return configFileEncoding;
        }
        return "UTF-8";
    }

    /**
     * Filters these properties to remove the Flyway Maven Plugin-specific ones to avoid warnings.
     *
     * @param conf The properties to filter.
     */
    private static void removeMavenPluginSpecificPropertiesToAvoidWarnings(Map<String, String> conf) {
        conf.remove(ConfigUtils.CONFIG_FILES);
        conf.remove(ConfigUtils.CONFIG_FILE_ENCODING);
        conf.remove(CONFIG_CURRENT);
        conf.remove(CONFIG_SKIP);
        conf.remove(CONFIG_VERSION);
        conf.remove(CONFIG_SERVER_ID);
        conf.remove(CONFIG_WORKING_DIRECTORY);
    }

    /**
     * Load properties from the config files (if specified).
     *
     * @param workDir The working directory to use.
     * @param envVars The environment variables converted to Flyway properties.
     * @return The properties.
     */
    private Map<String, String> loadConfigurationFromConfigFiles(File workDir, Map<String, String> envVars) {
        String encoding = determineConfigurationFileEncoding(envVars);

        Map<String, String> conf = new HashMap<>();
        for (File configFile : determineConfigFiles(workDir, envVars)) {
            conf.putAll(ConfigUtils.loadConfigurationFile(configFile, encoding, true));
        }
        return conf;
    }

    /**
     * Load properties from the default config files (if available).
     *
     * @param envVars The environment variables converted to Flyway properties.
     * @return The properties.
     */
    private Map<String, String> loadConfigurationFromDefaultConfigFiles(Map<String, String> envVars) {
        String encoding = determineConfigurationFileEncoding(envVars);
        File configFile = new File(System.getProperty("user.home") + "/" + ConfigUtils.CONFIG_FILE_NAME);

        return new HashMap<>(ConfigUtils.loadConfigurationFile(configFile, encoding, false));
    }

    /**
     * Retrieves this property from either the system or the maven properties.
     *
     * @param name The name of the property to retrieve.
     * @return The property value. {@code null} if not found.
     */
    protected String getProperty(String name) {
        String systemProperty = System.getProperty(name);

        if (systemProperty != null) {
            return systemProperty;
        }

        return mavenProject.getProperties().getProperty(name);
    }

    /**
     * Executes this mojo.
     *
     * @param flyway The flyway instance to operate on.
     * @throws Exception Any exception.
     */
    protected abstract void doExecute(Flyway flyway) throws Exception;
}