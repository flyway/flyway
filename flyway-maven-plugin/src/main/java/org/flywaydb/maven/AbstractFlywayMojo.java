/*
 * Copyright 2010-2018 Boxfuse GmbH
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
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.flywaydb.core.internal.configuration.ConfigUtils.putArrayIfSet;
import static org.flywaydb.core.internal.configuration.ConfigUtils.putIfSet;

/**
 * Common base class for all mojos with all common attributes.
 */
@SuppressWarnings({"JavaDoc", "FieldCanBeLocal", "UnusedDeclaration"})
abstract class AbstractFlywayMojo extends AbstractMojo {
    private static final String CONFIG_WORKING_DIRECTORY = "flyway.workingDirectory";
    private static final String CONFIG_SERVER_ID = "flyway.serverId";
    private static final String CONFIG_VERSION = "flyway.version";
    private static final String CONFIG_SKIP = "flyway.skip";
    private static final String CONFIG_CURRENT = "flyway.current";

    Log log;

    /**
     * Whether to skip the execution of the Maven Plugin for this module.<br/>
     * <p>Also configurable with Maven or System Property: ${flyway.skip}</p>
     */
    @Parameter(property = CONFIG_SKIP)
    /* private -> for testing */ boolean skip;

    /**
     * The fully qualified classname of the jdbc driver to use to connect to the database.<br>
     * By default, the driver is autodetected based on the url.<br/>
     * <p>Also configurable with Maven or System Property: ${flyway.driver}</p>
     */
    @Parameter(property = ConfigUtils.DRIVER)
    /* private -> for testing */ String driver;

    /**
     * The jdbc url to use to connect to the database.<br>
     * <p>Also configurable with Maven or System Property: ${flyway.url}</p>
     */
    @Parameter(property = ConfigUtils.URL)
    /* private -> for testing */ String url;

    /**
     * The user to use to connect to the database. (default: <i>blank</i>)<br>
     * The credentials can be specified by user/password or {@code serverId} from settings.xml
     * <p>Also configurable with Maven or System Property: ${flyway.user}</p>
     */
    @Parameter(property = ConfigUtils.USER)
    /* private -> for testing */ String user;

    /**
     * The password to use to connect to the database. (default: <i>blank</i>)<br>
     * <p>Also configurable with Maven or System Property: ${flyway.password}</p>
     */
    @Parameter(property = ConfigUtils.PASSWORD)
    private String password;

    /**
     * The maximum number of retries when attempting to connect to the database. After each failed attempt, Flyway will
     * wait 1 second before attempting to connect again, up to the maximum number of times specified by connectRetries.
     * (default: 0)
     * <p>Also configurable with Maven or System Property: ${flyway.connectRetries}</p>
     */
    @Parameter(property = ConfigUtils.CONNECT_RETRIES)
    private int connectRetries;

    /**
     * The SQL statements to run to initialize a new database connection immediately after opening it.
     * (default: {@code null})
     * <p>Also configurable with Maven or System Property: ${flyway.initSql}</p>
     */
    @Parameter(property = ConfigUtils.INIT_SQL)
    private String initSql;

    /**
     * The schemas managed by Flyway. These schema names are case-sensitive. (default: The default schema for the database connection)
     * <p>Consequences:</p>
     * <ul>
     * <li>Flyway will automatically attempt to create all these schemas, unless the first one already exists.</li>
     * <li>The first schema in the list will be automatically set as the default one during the migration.</li>
     * <li>The first schema in the list will also be the one containing the schema history table.</li>
     * <li>The schemas will be cleaned in the order of this list.</li>
     * <li>If Flyway created them, the schemas themselves will as be dropped when cleaning.</li>
     * </ul>
     * <p>Also configurable with Maven or System Property: ${flyway.schemas} (comma-separated list)</p>
     */
    @Parameter
    private String[] schemas;

    /**
     * <p>The name of the schema history table that will be used by Flyway. (default: flyway_schema_history)</p>
     * <p> By default (single-schema mode) the
     * schema history table is placed in the default schema for the connection provided by the datasource. <br/> When the
     * {@code flyway.schemas} property is set (multi-schema mode), the schema history table is placed in the first schema of
     * the list. </p>
     * <p>Also configurable with Maven or System Property: ${flyway.table}</p>
     */
    @Parameter(property = ConfigUtils.TABLE)
    private String table;

    /**
     * The version to tag an existing schema with when executing baseline. (default: 1)<br/>
     * <p>Also configurable with Maven or System Property: ${flyway.baselineVersion}</p>
     */
    @Parameter(property = ConfigUtils.BASELINE_VERSION)
    private String baselineVersion;

    /**
     * The description to tag an existing schema with when executing baseline. (default: << Flyway Baseline >>)<br>
     * <p>Also configurable with Maven or System Property: ${flyway.baselineDescription}</p>
     */
    @Parameter(property = ConfigUtils.BASELINE_DESCRIPTION)
    private String baselineDescription;

    /**
     * Locations to scan recursively for migrations.
     * <p>The location type is determined by its prefix.
     * Unprefixed locations or locations starting with {@code classpath:} point to a package on the classpath and may
     * contain both SQL and Java-based migrations.
     * Locations starting with {@code filesystem:} point to a directory on the filesystem, may only
     * contain SQL migrations and are only scanned recursively down non-hidden directories.</p>
     * (default: filesystem:src/main/resources/db/migration)
     * <p>Also configurable with Maven or System Property: ${flyway.locations} (Comma-separated list)</p>
     */
    @Parameter
    private String[] locations;

    /**
     * The fully qualified class names of the custom MigrationResolvers to be used in addition or as replacement
     * (if skipDefaultResolvers is true) to the built-in ones for resolving Migrations to apply.
     * <p>(default: none)</p>
     * <p>Also configurable with Maven or System Property: ${flyway.resolvers} (Comma-separated list)</p>
     */
    @Parameter
    private String[] resolvers;

    /**
     * When set to true, default resolvers are skipped, i.e. only custom resolvers as defined by 'resolvers'
     * are used. (default: false)<br> <p>Also configurable with Maven or System Property:
     * ${flyway.skipDefaultResolvers}</p>
     */
    @Parameter(property = ConfigUtils.SKIP_DEFAULT_RESOLVERS)
    private Boolean skipDefaultResolvers;

    /**
     * The encoding of Sql migrations. (default: UTF-8)<br> <p>Also configurable with Maven or System Property:
     * ${flyway.encoding}</p>
     */
    @Parameter(property = ConfigUtils.ENCODING)
    private String encoding;

    /**
     * The file name prefix for versioned SQL migrations (default: V)
     * <p>
     * <p>Versioned SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     * <p>Also configurable with Maven or System Property: ${flyway.sqlMigrationPrefix}</p>
     */
    @Parameter(property = ConfigUtils.SQL_MIGRATION_PREFIX)
    private String sqlMigrationPrefix;

    /**
     * The file name prefix for undo SQL migrations. (default: U)
     * <p>Undo SQL migrations are responsible for undoing the effects of the versioned migration with the same version.</p>
     * <p>They have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to U1.1__My_description.sql</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     * <p>Also configurable with Maven or System Property: ${flyway.undoSqlMigrationPrefix}</p>
     */
    @Parameter(property = ConfigUtils.UNDO_SQL_MIGRATION_PREFIX)
    private String undoSqlMigrationPrefix;

    /**
     * The file name prefix for repeatable sql migrations (default: R) <p>Also configurable with Maven or System Property:
     * ${flyway.repeatableSqlMigrationPrefix}</p>
     * <p>
     * <p>Repeatable sql migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to R__My_description.sql</p>
     */
    @Parameter(property = ConfigUtils.REPEATABLE_SQL_MIGRATION_PREFIX)
    private String repeatableSqlMigrationPrefix;

    /**
     * The file name separator for Sql migrations (default: __) <p>Also configurable with Maven or System Property:
     * ${flyway.sqlMigrationSeparator}</p>
     * <p>
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     */
    @Parameter(property = ConfigUtils.SQL_MIGRATION_SEPARATOR)
    private String sqlMigrationSeparator;

    /**
     * The file name suffix for Sql migrations (default: .sql) <p>Also configurable with Maven or System Property:
     * ${flyway.sqlMigrationSuffix}</p>
     * <p>
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     *
     * @deprecated Use {@link AbstractFlywayMojo#sqlMigrationSuffixes} instead. Will be removed in Flyway 6.0.0.
     */
    @Parameter(property = ConfigUtils.SQL_MIGRATION_SUFFIX)
    @Deprecated
    private String sqlMigrationSuffix;

    /**
     * The file name suffixes for SQL migrations. (default: .sql)
     * <p>SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     * <p>Multiple suffixes (like .sql,.pkg,.pkb) can be specified for easier compatibility with other tools such as
     * editors with specific file associations.</p>
     * <p>Also configurable with Maven or System Property: ${flyway.sqlMigrationSuffixes}</p>
     */
    @Parameter
    private String[] sqlMigrationSuffixes;

    /**
     * Whether to automatically call clean or not when a validation error occurs. (default: {@code false})<br/>
     * <p> This is exclusively intended as a convenience for development. Even tough we
     * strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a
     * way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that
     * the next migration will bring you back to the state checked into SCM.</p>
     * <p><b>Warning ! Do not enable in production !</b></p><br/>
     * <p>Also configurable with Maven or System Property: ${flyway.cleanOnValidationError}</p>
     */
    @Parameter(property = ConfigUtils.CLEAN_ON_VALIDATION_ERROR)
    private Boolean cleanOnValidationError;

    /**
     * Whether to disable clean. (default: {@code false})
     * <p>This is especially useful for production environments where running clean can be quite a career limiting move.</p>
     * <p>Also configurable with Maven or System Property: ${flyway.cleanDisabled}</p>
     */
    @Parameter(property = ConfigUtils.CLEAN_DISABLED)
    private Boolean cleanDisabled;

    /**
     * The target version up to which Flyway should consider migrations.
     * Migrations with a higher version number will be ignored.
     * The special value {@code current} designates the current version of the schema. (default: the latest version)
     * <p>Also configurable with Maven or System Property: ${flyway.target}</p>
     */
    @Parameter(property = ConfigUtils.TARGET)
    private String target;

    /**
     * Allows migrations to be run "out of order" (default: {@code false}).
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     * <p>Also configurable with Maven or System Property: ${flyway.outOfOrder}</p>
     */
    @Parameter(property = ConfigUtils.OUT_OF_ORDER)
    private Boolean outOfOrder;

    /**
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
     * Whether placeholders should be replaced. (default: true)<br>
     * <p>Also configurable with Maven or System Property: ${flyway.placeholderReplacement}</p>
     */
    @Parameter(property = ConfigUtils.PLACEHOLDER_REPLACEMENT)
    private Boolean placeholderReplacement;

    /**
     * A map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     * <p/>
     * <p>Also configurable with Maven or System Properties like ${flyway.placeholders.myplaceholder} or ${flyway.placeholders.otherone}</p>
     */
    @Parameter
    private Map<String, String> placeholders;

    /**
     * The prefix of every placeholder. (default: ${ )<br>
     * <p>Also configurable with Maven or System Property: ${flyway.placeholderPrefix}</p>
     */
    @Parameter(property = ConfigUtils.PLACEHOLDER_PREFIX)
    private String placeholderPrefix;

    /**
     * The suffix of every placeholder. (default: } )<br>
     * <p>Also configurable with Maven or System Property: ${flyway.placeholderSuffix}</p>
     */
    @Parameter(property = ConfigUtils.PLACEHOLDER_SUFFIX)
    private String placeholderSuffix;

    /**
     * An array of FlywayCallback implementations. (default: empty )<br>
     * <p>Also configurable with Maven or System Property: ${flyway.callbacks}</p>
     */
    @Parameter
    private String[] callbacks;

    /**
     * When set to true, default callbacks are skipped, i.e. only custom callbacks as defined by 'resolvers'
     * are used. (default: false)<br> <p>Also configurable with Maven or System Property:
     * ${flyway.skipDefaultCallbacks}</p>
     */
    @Parameter(property = ConfigUtils.SKIP_DEFAULT_CALLBACKS)
    private Boolean skipDefaultCallbacks;

    /**
     * <p>
     * Whether to automatically call baseline when migrate is executed against a non-empty schema with no schema history table.
     * This schema will then be baselined with the {@code initialVersion} before executing the migrations.
     * Only migrations above {@code initialVersion} will then be applied.
     * </p>
     * <p>
     * This is useful for initial Flyway production deployments on projects with an existing DB.
     * </p>
     * <p>
     * Be careful when enabling this as it removes the safety net that ensures
     * Flyway does not migrate the wrong database in case of a configuration mistake! (default: {@code false})
     * </p>
     * <p>Also configurable with Maven or System Property: ${flyway.baselineOnMigrate}</p>
     */
    @Parameter(property = ConfigUtils.BASELINE_ON_MIGRATE)
    private Boolean baselineOnMigrate;

    /**
     * Whether to automatically call validate or not when running migrate. (default: {@code true})<br/>
     * <p>Also configurable with Maven or System Property: ${flyway.validateOnMigrate}</p>
     */
    @Parameter(property = ConfigUtils.VALIDATE_ON_MIGRATE)
    private Boolean validateOnMigrate;

    /**
     * Whether to allow mixing transactional and non-transactional statements within the same migration.
     * <p>
     * {@code true} if mixed migrations should be allowed. {@code false} if an error should be thrown instead. (default: {@code false})
     * <p>Also configurable with Maven or System Property: ${flyway.mixed}</p>
     */
    @Parameter(property = ConfigUtils.MIXED)
    private Boolean mixed;

    /**
     * Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions).
     * <p>{@code true} if migrations should be grouped. {@code false} if they should be applied individually instead. (default: {@code false})</p>
     * <p>Also configurable with Maven or System Property: ${flyway.group}</p>
     */
    @Parameter(property = ConfigUtils.GROUP)
    private Boolean group;

    /**
     * The username that will be recorded in the schema history table as having applied the migration.
     * <p>{@code null} for the current database user of the connection. (default: {@code null}).</p>
     * <p>Also configurable with Maven or System Property: ${flyway.installedBy}</p>
     */
    @Parameter(property = ConfigUtils.INSTALLED_BY)
    private String installedBy;

    /**
     * The fully qualified class names of handlers for errors and warnings that occur during a migration. This can be
     * used to customize Flyway's behavior by for example
     * throwing another runtime exception, outputting a warning or suppressing the error instead of throwing a FlywayException.
     * ErrorHandlers are invoked in order until one reports to have successfully handled the errors or warnings.
     * If none do, or if none are present, Flyway falls back to its default handling of errors and warnings.
     * (default: none)
     * <p>Also configurable with Maven or System Property: ${flyway.errorHandlers}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @deprecated ErrorHandlers have been deprecated and will be removed in Flyway 6.0 use statement-level callbacks instead.
     */
    @Deprecated
    @Parameter
    private String[] errorHandlers;

    /**
     * Rules for the built-in error handler that let you override specific SQL states and errors codes in order to force
     * specific errors or warnings to be treated as debug messages, info messages, warnings or errors.
     * <p>Each error override has the following format: {@code STATE:12345:W}.
     * It is a 5 character SQL state, a colon, the SQL error code, a colon and finally the desired
     * behavior that should override the initial one.</p>
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
     * <p>Also configurable with Maven or System Property: ${flyway.errorOverrides}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     */
    @Parameter
    private String[] errorOverrides;

    /**
     * The file where to output the SQL statements of a migration dry run. If the file specified is in a non-existent
     * directory, Flyway will create all directories and parent directories as needed.
     * <p>{@code null} to execute the SQL statements directly against the database. (default: {@code null})</p>
     * <p>Also configurable with Maven or System Property: ${flyway.dryRunOutput}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     */
    @Parameter(property = ConfigUtils.DRYRUN_OUTPUT)
    private String dryRunOutput;

    /**
     * Whether to stream SQL migrations when executing them. Streaming doesn't load the entire migration in memory at
     * once. Instead each statement is loaded individually. This is particularly useful for very large SQL migrations
     * composed of multiple MB or even GB of reference data, as this dramatically reduces Flyway's memory consumption.
     * (default: {@code false}
     * <p>Also configurable with Maven or System Property: ${flyway.stream}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
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
     * <p>Also configurable with Maven or System Property: ${flyway.batch}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     */
    @Parameter(property = ConfigUtils.BATCH)
    private Boolean batch;

    /**
     * Whether to Flyway's support for Oracle SQL*Plus commands should be activated.
     * (default: {@code false})
     * <p>Also configurable with Maven or System Property: ${flyway.oracle.sqlplus}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     */
    @Parameter(property = ConfigUtils.ORACLE_SQLPLUS)
    private Boolean oracleSqlplus;

    /**
     * Flyway's license key.
     * <p>Also configurable with Maven or System Property: ${flyway.licenseKey}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     */
    @Parameter(property = ConfigUtils.LICENSE_KEY)
    private String licenseKey;

    /**
     * Properties file from which to load the Flyway configuration. The names of the individual properties match the ones you would
     * use as Maven or System properties. The encoding of the file must be the same as the encoding defined with the
     * {@code flyway.encoding) property, which is UTF-8 by default. Relative paths are relative to the POM. (default: flyway.properties)
     * <p/>
     * <p>Also configurable with Maven or System Property: ${flyway.configFile}</p>
     */
    @Deprecated
    @Parameter(property = ConfigUtils.CONFIG_FILE)
    private File configFile;

    /**
     * The encoding of the external config files specified with the {@code flyway.configFiles} property. (default: UTF-8).
     * <p/>
     * <p>Also configurable with Maven or System Property: ${flyway.configFileEncoding}</p>
     */
    @Parameter(property = ConfigUtils.CONFIG_FILE_ENCODING)
    private String configFileEncoding;

    /**
     * Config files from which to load the Flyway configuration. The names of the individual properties match the ones you would
     * use as Maven or System properties. The encoding of the files is defined by the
     * flyway.configFileEncoding property, which is UTF-8 by default. Relative paths are relative to the POM or
     * <code>workingDirectory</code> if set.
     * <p/>
     * <p>Also configurable with Maven or System Property: ${flyway.configFiles}</p>
     */
    private File[] configFiles;

    /**
     * The working directory to consider when dealing with relative paths for both config files and locations.
     * (default: basedir, the directory where the POM resides)
     * <p/>
     * <p>Also configurable with Maven or System Property: ${flyway.workingDirectory}</p>
     */
    @Parameter(property = CONFIG_WORKING_DIRECTORY)
    private File workingDirectory;

    /**
     * The id of the server tag in settings.xml (default: flyway-db)<br/>
     * The credentials can be specified by user/password or {@code serverId} from settings.xml<br>
     * <p>Also configurable with Maven or System Property: ${flyway.serverId}</p>
     */
    @Parameter(property = CONFIG_SERVER_ID)
    private String serverId = "flyway-db";

    /**
     * The link to the settings.xml
     */
    @Parameter(defaultValue = "${settings}", readonly = true)
    /* private -> for testing */ Settings settings;

    /**
     * Reference to the current project that includes the Flyway Maven plugin.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    /* private -> for testing */ MavenProject mavenProject;

    @Component
    private SettingsDecrypter settingsDecrypter;

    /**
     * Load username password from settings
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
     * Retrieves the value of this boolean property, based on the matching System on the Maven property.
     *
     * @param systemPropertyName The name of the System property.
     * @param mavenPropertyValue The value of the Maven property.
     * @return The value to use.
     */
    /* private -> for testing */ boolean getBooleanProperty(String systemPropertyName, boolean mavenPropertyValue) {
        String systemPropertyValue = System.getProperty(systemPropertyName);
        if (systemPropertyValue != null) {
            return Boolean.getBoolean(systemPropertyName);
        }
        return mavenPropertyValue;
    }

    public final void execute() throws MojoExecutionException {
        LogFactory.setLogCreator(new MavenLogCreator(this));
        log = LogFactory.getLog(getClass());

        if (getBooleanProperty(CONFIG_SKIP, skip)) {
            log.info("Skipping Flyway execution");
            return;
        }

        try {
            Set<String> classpathElements = new HashSet<>();
            classpathElements.addAll(mavenProject.getCompileClasspathElements());
            classpathElements.addAll(mavenProject.getRuntimeClasspathElements());

            ClassRealm classLoader = (ClassRealm) Thread.currentThread().getContextClassLoader();
            for (String classpathElement : classpathElements) {
                classLoader.addURL(new File(classpathElement).toURI().toURL());
            }

            File workDir = workingDirectory == null ? mavenProject.getBasedir() : workingDirectory;

            if (locations != null) {
                for (int i = 0; i < locations.length; i++) {
                    if (locations[i].startsWith(Location.FILESYSTEM_PREFIX)) {
                        String newLocation = locations[i].substring(Location.FILESYSTEM_PREFIX.length());
                        File file = new File(newLocation);
                        if (!file.isAbsolute()) {
                            file = new File(workDir, newLocation);
                        }
                        locations[i] = Location.FILESYSTEM_PREFIX + file.getAbsolutePath();
                    }
                }
            } else {
                locations = new String[]{
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
            putIfSet(conf, ConfigUtils.INIT_SQL, initSql);
            putArrayIfSet(conf, ConfigUtils.SCHEMAS, schemas);
            putIfSet(conf, ConfigUtils.TABLE, table);
            putIfSet(conf, ConfigUtils.BASELINE_VERSION, baselineVersion);
            putIfSet(conf, ConfigUtils.BASELINE_DESCRIPTION, baselineDescription);
            putArrayIfSet(conf, ConfigUtils.LOCATIONS, locations);
            putArrayIfSet(conf, ConfigUtils.RESOLVERS, resolvers);
            putIfSet(conf, ConfigUtils.SKIP_DEFAULT_RESOLVERS, skipDefaultResolvers);
            putArrayIfSet(conf, ConfigUtils.CALLBACKS, callbacks);
            putIfSet(conf, ConfigUtils.SKIP_DEFAULT_CALLBACKS, skipDefaultCallbacks);
            putIfSet(conf, ConfigUtils.ENCODING, encoding);
            putIfSet(conf, ConfigUtils.SQL_MIGRATION_PREFIX, sqlMigrationPrefix);
            putIfSet(conf, ConfigUtils.UNDO_SQL_MIGRATION_PREFIX, undoSqlMigrationPrefix);
            putIfSet(conf, ConfigUtils.REPEATABLE_SQL_MIGRATION_PREFIX, repeatableSqlMigrationPrefix);
            putIfSet(conf, ConfigUtils.SQL_MIGRATION_SEPARATOR, sqlMigrationSeparator);
            putIfSet(conf, ConfigUtils.SQL_MIGRATION_SUFFIX, sqlMigrationSuffix);
            putArrayIfSet(conf, ConfigUtils.SQL_MIGRATION_SUFFIXES, sqlMigrationSuffixes);
            putIfSet(conf, ConfigUtils.MIXED, mixed);
            putIfSet(conf, ConfigUtils.GROUP, group);
            putIfSet(conf, ConfigUtils.INSTALLED_BY, installedBy);
            putIfSet(conf, ConfigUtils.CLEAN_ON_VALIDATION_ERROR, cleanOnValidationError);
            putIfSet(conf, ConfigUtils.CLEAN_DISABLED, cleanDisabled);
            putIfSet(conf, ConfigUtils.OUT_OF_ORDER, outOfOrder);
            putIfSet(conf, ConfigUtils.TARGET, target);
            putIfSet(conf, ConfigUtils.IGNORE_MISSING_MIGRATIONS, ignoreMissingMigrations);
            putIfSet(conf, ConfigUtils.IGNORE_IGNORED_MIGRATIONS, ignoreIgnoredMigrations);
            putIfSet(conf, ConfigUtils.IGNORE_PENDING_MIGRATIONS, ignorePendingMigrations);
            putIfSet(conf, ConfigUtils.IGNORE_FUTURE_MIGRATIONS, ignoreFutureMigrations);
            putIfSet(conf, ConfigUtils.PLACEHOLDER_REPLACEMENT, placeholderReplacement);
            putIfSet(conf, ConfigUtils.PLACEHOLDER_PREFIX, placeholderPrefix);
            putIfSet(conf, ConfigUtils.PLACEHOLDER_SUFFIX, placeholderSuffix);
            putIfSet(conf, ConfigUtils.BASELINE_ON_MIGRATE, baselineOnMigrate);
            putIfSet(conf, ConfigUtils.VALIDATE_ON_MIGRATE, validateOnMigrate);
            putIfSet(conf, ConfigUtils.DRIVER, driver);

            putArrayIfSet(conf, ConfigUtils.ERROR_HANDLERS, errorHandlers);
            putArrayIfSet(conf, ConfigUtils.ERROR_OVERRIDES, errorOverrides);
            putIfSet(conf, ConfigUtils.DRYRUN_OUTPUT, dryRunOutput);
            putIfSet(conf, ConfigUtils.STREAM, stream);
            putIfSet(conf, ConfigUtils.BATCH, batch);

            putIfSet(conf, ConfigUtils.ORACLE_SQLPLUS, oracleSqlplus);

            putIfSet(conf, ConfigUtils.LICENSE_KEY, licenseKey);

            if (placeholders != null) {
                for (String placeholder : placeholders.keySet()) {
                    String value = placeholders.get(placeholder);
                    conf.put(ConfigUtils.PLACEHOLDERS_PROPERTY_PREFIX + placeholder, value == null ? "" : value);
                }
            }

            conf.putAll(ConfigUtils.propertiesToMap(mavenProject.getProperties()));
            conf.putAll(loadConfigurationFromConfigFiles(workDir, envVars));
            conf.putAll(envVars);
            conf.putAll(ConfigUtils.propertiesToMap(System.getProperties()));
            removeMavenPluginSpecificPropertiesToAvoidWarnings(conf);

            Flyway flyway = Flyway.configure(classLoader).configure(conf).load();
            doExecute(flyway);
        } catch (Exception e) {
            throw new MojoExecutionException(e.toString(), ExceptionUtils.getRootCause(e));
        }
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

        if (System.getProperties().containsKey(ConfigUtils.CONFIG_FILE)) {
            log.warn(ConfigUtils.CONFIG_FILE + " is deprecated and will be removed in Flyway 6.0. Use " + ConfigUtils.CONFIG_FILES + " instead.");
            configFiles.add(toFile(workDir, System.getProperties().getProperty(ConfigUtils.CONFIG_FILE)));
            return configFiles;
        }
        if (System.getProperties().containsKey(ConfigUtils.CONFIG_FILES)) {
            for (String file : StringUtils.tokenizeToStringArray(System.getProperties().getProperty(ConfigUtils.CONFIG_FILES), ",")) {
                configFiles.add(toFile(workDir, file));
            }
            return configFiles;
        }

        if (mavenProject.getProperties().containsKey(ConfigUtils.CONFIG_FILE)) {
            log.warn(ConfigUtils.CONFIG_FILE + " is deprecated and will be removed in Flyway 6.0. Use " + ConfigUtils.CONFIG_FILES + " instead.");
            configFiles.add(toFile(workDir, mavenProject.getProperties().getProperty(ConfigUtils.CONFIG_FILE)));
        } else if (configFile != null) {
            configFiles.add(configFile);
        }
        if (mavenProject.getProperties().containsKey(ConfigUtils.CONFIG_FILES)) {
            for (String file : StringUtils.tokenizeToStringArray(mavenProject.getProperties().getProperty(ConfigUtils.CONFIG_FILES), ",")) {
                configFiles.add(toFile(workDir, file));
            }
        } else if (this.configFiles != null) {
            configFiles.addAll(Arrays.asList(this.configFiles));
        }
        return configFiles;
    }

    /**
     * Converts this fileName into a file, adjusting relative paths if necessary to make them relative to the pom.
     *
     * @param workDir  The working directory to use.
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
     * Filters there properties to remove the Flyway Maven Plugin-specific ones to avoid warnings.
     *
     * @param conf The properties to filter.
     */
    private static void removeMavenPluginSpecificPropertiesToAvoidWarnings(Map<String, String> conf) {
        conf.remove(ConfigUtils.CONFIG_FILE);
        conf.remove(ConfigUtils.CONFIG_FILES);
        conf.remove(ConfigUtils.CONFIG_FILE_ENCODING);
        conf.remove(CONFIG_CURRENT);
        conf.remove(CONFIG_SKIP);
        conf.remove(CONFIG_VERSION);
        conf.remove(CONFIG_SERVER_ID);
        conf.remove(CONFIG_WORKING_DIRECTORY);
    }

    /**
     * Retrieve the properties from the config files (if specified).
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
     * Retrieve the properties from the config files (if specified).
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
     * @throws Exception any exception
     */
    protected abstract void doExecute(Flyway flyway) throws Exception;
}