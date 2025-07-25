/*-
 * ========================LICENSE_START=================================
 * flyway-gradle-plugin
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
package org.flywaydb.gradle.task;

import static org.flywaydb.core.internal.configuration.ConfigUtils.FLYWAY_PLUGINS_PREFIX;
import static org.flywaydb.core.internal.configuration.ConfigUtils.putIfSet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.gradle.FlywayExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.TaskAction;

/**
 * A base class for all Flyway tasks.
 */
public abstract class AbstractFlywayTask extends DefaultTask {
    /**
     * The default Gradle configurations to use.
     */
    // #2272: Gradle 4.x introduced additional configuration names and Gradle 5.0 deprecated some old ones.
    // -> Rely on historic ones for Gradle 3.x
    private static final String[] DEFAULT_CONFIGURATIONS_GRADLE3 = {"compileClasspath", "runtime", "testCompileClasspath", "testRuntime"};
    // -> And use new ones with Gradle 4.x and newer
    private static final String[] DEFAULT_CONFIGURATIONS_GRADLE45 = {"compileClasspath", "runtimeClasspath", "testCompileClasspath", "testRuntimeClasspath"};

    /**
     * The flyway {} block in the build script.
     */
    protected FlywayExtension extension;

    /**
     * The fully qualified classname of the JDBC driver to use to connect to the database.
     */
    public String driver;

    /**
     * The JDBC url to use to connect to the database.
     */
    public String url;

    /**
     * The user to use to connect to the database.
     */
    public String user;

    /**
     * The password to use to connect to the database.
     */
    public String password;

    /**
     * The maximum number of retries when attempting to connect to the database. After each failed attempt, Flyway will
     * wait 1 second before attempting to connect again, up to the maximum number of times specified by connectRetries.
     * The interval between retries doubles with each subsequent attempt.
     * (default: 0)
     * <p>Also configurable with Gradle or System Property: ${flyway.connectRetries}</p>
     */
    public int connectRetries;

    /**
     * The maximum time between retries when attempting to connect to the database in seconds. This will cap the interval
     * between connect retry to the value provided.
     * (default: 120)
     * <p>Also configurable with Gradle or System Property: ${flyway.connectRetriesInterval}</p>
     */
    public int connectRetriesInterval;

    /**
     * The SQL statements to run to initialize a new database connection immediately after opening it.
     * (default: {@code null})
     * <p>Also configurable with Gradle or System Property: ${flyway.initSql}</p>
     */
    public String initSql;

    /**
     * The name of the schema history table that will be used by Flyway. (default: flyway_schema_history)
     * By default, (single-schema mode) the schema history table is placed in the default schema for the connection provided by the datasource.
     * When the {@code flyway.schemas} property is set (multi-schema mode), the schema history table is placed in the first schema of the list,
     * or in the schema specified to {@code flyway.defaultSchema}.
     * <p>Also configurable with Gradle or System Property: ${flyway.table}</p>
     */
    public String table;

    /**
     * The tablespace where to create the schema history table that will be used by Flyway.
     * If not specified, Flyway uses the default tablespace for the database connection.
     * This setting is only relevant for databases that do support the notion of tablespaces. Its value is simply
     * ignored for all others.
     * <p>Also configurable with Gradle or System Property: ${flyway.tablespace}</p>
     */
    public String tablespace;

    /**
     * The default schema managed by Flyway. This schema name is case-sensitive. If not specified, but <i>schemas</i>
     * is, Flyway uses the first schema in that list. If that is also not specified, Flyway uses the default schema for the
     * database connection.
     * <p>Consequences:</p>
     * <ul>
     * <li>This schema will be the one containing the schema history table.</li>
     * <li>This schema will be the default for the database connection (provided the database supports this concept).</li>
     * </ul>
     * <p>Also configurable with Gradle or System Property: ${flyway.defaultSchema}</p>
     */
    public String defaultSchema;

    /**
     * Whether Flyway should attempt to create the schemas specified in the <i>schemas</i> property.
     */
    public Boolean createSchemas;

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
     * <p>Also configurable with Gradle or System Property: ${flyway.schemas} (comma-separated list)</p>
     */
    public String[] schemas;

    /**
     * The version to tag an existing schema with when executing baseline. (default: 1)
     */
    public String baselineVersion;

    /**
     * The description to tag an existing schema with when executing baseline. (default: &lt;&lt; Flyway Baseline &gt;&gt;)
     */
    public String baselineDescription;

    /**
     * Locations to scan recursively for migrations.
     * The location type is determined by its prefix.
     * Unprefixed locations or locations starting with {@code classpath:} point to a package on the classpath and may
     * contain both SQL and Java-based migrations.
     * Locations starting with {@code filesystem:} point to a directory on the filesystem, may only
     * contain SQL migrations and are only scanned recursively down non-hidden directories.
     * (default: filesystem:src/main/resources/db/migration)
     */
    public String[] locations;

    /**
     * The fully qualified class names of the custom MigrationResolvers to be used in addition (default)
     * or as a replacement (using skipDefaultResolvers) to the built-in ones for resolving Migrations to
     * apply. (default: none)
     */
    public String[] resolvers;

    /**
     * If set to true, default built-in resolvers will be skipped, only custom migration resolvers will be used.
     * (default: false)
     */
    public Boolean skipDefaultResolvers;

    /**
     * The file name prefix for versioned SQL migrations. (default: V)
     * Versioned SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to V1_1__My_description.sql
     * <p>Also configurable with Gradle or System Property: ${flyway.sqlMigrationPrefix}</p>
     */
    public String sqlMigrationPrefix;

    /**
     * The file name prefix for undo SQL migrations. (default: U)
     * Undo SQL migrations are responsible for undoing the effects of the versioned migration with the same version.
     * They have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to U1.1__My_description.sql
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Gradle or System Property: ${flyway.undoSqlMigrationPrefix}</p>
     */
    public String undoSqlMigrationPrefix;

    /**
     * The file name prefix for repeatable SQL migrations (default: R).
     * Repeatable SQL migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix,
     * which using the defaults translates to R__My_description.sql
     * <p>Also configurable with Gradle or System Property: ${flyway.repeatableSqlMigrationPrefix}</p>
     */
    public String repeatableSqlMigrationPrefix;

    /**
     * The file name prefix for SQL migrations
     * SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to V1_1__My_description.sql
     */
    public String sqlMigrationSeparator;

    /**
     * The file name suffixes for SQL migrations. (default: .sql)
     * SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to V1_1__My_description.sql
     * Multiple suffixes (like .sql,.pkg,.pkb) can be specified for easier compatibility with other tools such as
     * editors with specific file associations.
     * <p>Also configurable with Gradle or System Property: ${flyway.sqlMigrationSuffixes}</p>
     */
    public String[] sqlMigrationSuffixes;

    /**
     * The encoding of SQL migrations.
     */
    public String encoding;

    /**
     * Whether Flyway should try to automatically detect SQL migration file encoding
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Gradle or System Property: ${flyway.detectEncoding}</p>
     */
    public Boolean detectEncoding;

    /**
     * Placeholders to replace in SQL migrations.
     */
    public Map<Object, Object> placeholders;

    /**
     * Properties to pass to the JDBC driver object.
     *
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Gradle or System Property: ${flyway.jdbcProperties}</p>
     */
    public Map<Object, Object> jdbcProperties;

    /**
     * Whether placeholders should be replaced.
     */
    public Boolean placeholderReplacement;

    /**
     * The prefix of every placeholder.
     */
    public String placeholderPrefix;

    /**
     * The suffix of every placeholder.
     */
    public String placeholderSuffix;

    /**
     * The separator of default placeholders.
     */
    public String placeholderSeparator;

    /**
     * The prefix of every script placeholder.
     */
    public String scriptPlaceholderPrefix;

    /**
     * The suffix of every script placeholder.
     */
    public String scriptPlaceholderSuffix;

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
     */
    public String target;

    /**
     * Gets the migrations that Flyway should consider when migrating or undoing. Leave empty to consider all available migrations.
     * Migrations not in this list will be ignored.
     * Values should be the version for versioned migrations (e.g. 1, 2.4, 6.5.3) or the description for repeatable migrations (e.g. Insert_Data, Create_Table)
     * <i>Flyway Teams only</i>
     */
    public String[] cherryPick;

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
    public String[] loggers;

    /**
     * An array of fully qualified FlywayCallback class implementations, or packages to scan for FlywayCallback implementations.
     */
    public String[] callbacks;

    /**
     * If set to true, default built-in callbacks will be skipped, only custom migration callbacks will be used.
     * <p>(default: false)</p>
     */
    public Boolean skipDefaultCallbacks;

    /**
     * Allows migrations to be run "out of order".
     */
    public Boolean outOfOrder;

    /**
     * Whether Flyway should skip actually executing the contents of the migrations and only update the schema history table.
     * This should be used when you have applied a migration manually (via executing the sql yourself, or via an ide), and
     * just want the schema history table to reflect this.
     * Use in conjunction with {@code cherryPick} to skip specific migrations instead of all pending ones.
     */
    public Boolean skipExecutingMigrations;

    /**
     * Whether Flyway should output a table with the results of queries when executing migrations (default: true).
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Gradle or System Property: ${flyway.outputQueryResults}</p>
     */
    public Boolean outputQueryResults;

    /**
     * Whether to automatically call validate or not when running migrate. (default: true)
     */
    public Boolean validateOnMigrate;

    /**
     * Deprecated, will be removed in a future release. <br>
     * Whether to automatically call clean or not when a validation error occurs. (default: {@code false})
     * This is exclusively intended as a convenience for development. even though we strongly recommend not to change
     * migration scripts once they have been checked into SCM and run, this provides a way of dealing with this case in
     * a smooth manner. The database will be wiped clean automatically, ensuring that the next migration will bring you
     * back to the state checked into SCM.
     * <b>Warning! Do not enable in production!</b>
     * <p>Also configurable with Gradle or System Property: ${flyway.cleanOnValidationError}</p>
     */
    public Boolean cleanOnValidationError;

    /**
     * Ignore migrations that match this comma-separated list of patterns when validating migrations.
     * Each pattern is of the form <migration_type>:<migration_state>
     * See https://documentation.red-gate.com/flyway/reference/configuration/flyway-namespace/flyway-ignore-migration-patterns-setting for full details
     * Example: repeatable:missing,versioned:pending,*:failed
     * (default: *:future)
     */
    public String[] ignoreMigrationPatterns;

    /**
     * Whether to validate migrations and callbacks whose scripts do not obey the correct naming convention. A failure can be
     * useful to check that errors such as case sensitivity in migration prefixes have been corrected.
     * {@code false} to continue normally, {@code true} to fail fast with an exception. (default: {@code false})
     */
    public Boolean validateMigrationNaming;

    /**
     * Whether to disable clean. (default: {@code true})
     * Set to false if you need to be able to clean your environment (can be a career limiting move)
     */
    public Boolean cleanDisabled;

    /**
     * Whether to automatically call baseline when migrate is executed against a non-empty schema with no schema history table.
     * This schema will then be baselined with the {@code baselineVersion} before executing the migrations.
     * Only migrations above {@code baselineVersion} will then be applied.
     * This is useful for initial Flyway production deployments on projects with an existing DB.
     * Be careful when enabling this as it removes the safety net that ensures
     * Flyway does not migrate the wrong database in case of a configuration mistake!
     * {@code true} if baseline should be called on migrate for non-empty schemas, {@code false} if not. (default: {@code false})
     */
    public Boolean baselineOnMigrate;

    /**
     * Whether to allow mixing transactional and non-transactional statements within the same migration. Enabling this
     * automatically causes the entire affected migration to be run without a transaction.
     *
     * Note that this is only applicable for PostgreSQL, Aurora PostgreSQL, SQL Server and SQLite which all have
     * statements that do not run at all within a transaction.
     * This is not to be confused with implicit transaction, as they occur in MySQL or Oracle, where even though a
     * DDL statement was run within a transaction, the database will issue an implicit commit before and after
     * its execution.
     * {@code true} if mixed migrations should be allowed. {@code false} if an error should be thrown instead. (default: {@code false})
     */
    public Boolean mixed;

    /**
     * Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions).
     * {@code true} if migrations should be grouped. {@code false} if they should be applied individually instead. (default: {@code false})
     */
    public Boolean group;

    /**
     * The username that will be recorded in the schema history table as having applied the migration.
     * {@code null} for the current database user of the connection. (default: {@code null}).
     */
    public String installedBy;

    /**
     * Gradle configurations that will be added to the classpath for running Flyway tasks.
     * (default: <code>compile</code>, <code>runtime</code>, <code>testCompile</code>, <code>testRuntime</code>)
     * <p>Also configurable with Gradle or System Property: ${flyway.configurations}</p>
     */
    public String[] configurations;

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
     * <p>Also configurable with Gradle or System Property: ${flyway.errorOverrides}</p>
     */
    public String[] errorOverrides;

    /**
     * The file where to output the SQL statements of a migration dry run. If the file specified is in a non-existent
     * directory, Flyway will create all directories and parent directories as needed.
     * Paths starting with s3: point to a bucket in AWS S3, which must exist. They are in the format s3:<bucket>(/optionalfolder/subfolder)/filename.sql
     * Paths starting with gcs: point to a bucket in Google Cloud Storage, which must exist. They are in the format gcs:<bucket>(/optionalfolder/subfolder)/filename.sql
     * <p>{@code null} to execute the SQL statements directly against the database. (default: {@code null})</p>
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Gradle or System Property: ${flyway.dryRunOutput}</p>
     */
    public String dryRunOutput;

    /**
     * Whether to stream SQL migrations when executing them. Streaming doesn't load the entire migration in memory at
     * once. Instead each statement is loaded individually. This is particularly useful for very large SQL migrations
     * composed of multiple MB or even GB of reference data, as this dramatically reduces Flyway's memory consumption.
     * (default: {@code false}
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Gradle or System Property: ${flyway.stream}</p>
     */
    public Boolean stream;

    /**
     * Whether to batch SQL statements when executing them. Batching can save up to 99 percent of network roundtrips by
     * sending up to 100 statements at once over the network to the database, instead of sending each statement
     * individually. This is particularly useful for very large SQL migrations composed of multiple MB or even GB of
     * reference data, as this can dramatically reduce the network overhead. This is supported for INSERT, UPDATE,
     * DELETE, MERGE and UPSERT statements. All other statements are automatically executed without batching.
     * (default: {@code false})
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Gradle or System Property: ${flyway.batch}</p>
     */
    public Boolean batch;

    /**
     * Whether to Flyway's support for Oracle SQL*Plus commands should be activated.
     * (default: {@code false})
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Gradle or System Property: ${flyway.oracle.sqlplus}</p>
     */
    public Boolean oracleSqlplus;

    /**
     * Whether Flyway should issue a warning instead of an error whenever it encounters an Oracle SQL*Plus statement
     * it doesn't yet support. (default: {@code false})
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Gradle or System Property: ${flyway.oracle.sqlplusWarn}</p>
     */
    public Boolean oracleSqlplusWarn;

    /**
     * The location of your Oracle wallet, used to automatically sign in to your databases.
     *
     * <i>Flyway Teams only</i>
     * <p>Also configurable with Gradle or System Property: ${flyway.oracle.walletLocation}</p>
     */
    public String oracleWalletLocation;

    /**
     * When connecting to a Kerberos service to authenticate, the path to the Kerberos config file.
     * <i>Flyway Teams only</i>
     */
    public String kerberosConfigFile;

    /**
     * The maximum number of retries when trying to obtain a lock. (default: 50)
     */
    public Integer lockRetryCount;

    /**
     * The encoding of the external config files specified with the {@code flyway.configFiles} property. (default: UTF-8).
     * <p>Also configurable with Gradle or System Property: ${flyway.configFileEncoding}</p>
     */
    public String configFileEncoding;

    /**
     * Config files from which to load the Flyway configuration. The names of the individual properties match the ones you would
     * use as Gradle or System properties. The encoding of the files is defined by the
     * flyway.configFileEncoding property, which is UTF-8 by default. Relative paths are relative to the project root.
     * <p>Also configurable with Gradle or System Property: ${flyway.configFiles}</p>
     */
    public String[] configFiles;

    /**
     * The working directory to consider when dealing with relative paths for both config files and locations.
     * (default: basedir, the directory where the POM resides)
     * <p>Also configurable with Gradle or System Property: ${flyway.workingDirectory}</p>
     */
    public String workingDirectory;

    /**
     * Whether to fail if a location specified in the flyway.locations option doesn't exist
     *
     * @return @{code true} to fail (default: {@code false})
     */
    public boolean failOnMissingLocations;

    /**
     * The configuration for plugins
     * You will need to configure this with the key and value specific to your plugin
     */
    public Map<String, String> pluginConfiguration;

    public AbstractFlywayTask() {
        super();
        setGroup("Flyway");
        extension = (FlywayExtension) getProject().getExtensions().getByName("flyway");
    }

    @TaskAction
    public Object runTask() {
        try {
            Map<String, String> envVars = ConfigUtils.environmentVariablesToPropertyMap();

            Set<URL> extraURLs = new HashSet<>();
            if (isJavaProject()) {
                addClassesAndResourcesDirs(extraURLs);
            }

            addConfigurationArtifacts(determineConfigurations(envVars), extraURLs);

            ClassLoader classLoader = new URLClassLoader(
                    extraURLs.toArray(new URL[0]),
                    getProject().getBuildscript().getClassLoader());

            Map<String, String> config = createFlywayConfig(envVars);
            ConfigUtils.dumpConfigurationMap(config, "Using configuration:");

            Flyway flyway = Flyway.configure(classLoader).configuration(config).load();
            Object result = run(flyway);
            ((DriverDataSource) flyway.getConfiguration().getDataSource()).shutdownDatabase();
            return result;
        } catch (Exception e) {
            throw new FlywayException(collectMessages(e, "Error occurred while executing " + getName()), e);
        }
    }

    private void addClassesAndResourcesDirs(Set<URL> extraURLs) throws MalformedURLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final SourceSetContainer sourceSets = getProject().getExtensions().getByType(SourceSetContainer.class);

        for (final SourceSet sourceSet : sourceSets) {
            try {
                FileCollection classesDirs = sourceSet.getOutput().getClassesDirs();
                for (File directory : classesDirs.getFiles()) {
                    URL classesUrl = directory.toURI().toURL();
                    getLogger().debug("Adding directory to Classpath: " + classesUrl);
                    extraURLs.add(classesUrl);
                }
            } catch (NoSuchMethodError ex) {
                getLogger().debug("Falling back to legacy getClassesDir method");

                // try legacy gradle 3.0 method instead
                @SuppressWarnings("JavaReflectionMemberAccess")
                Method getClassesDir = SourceSetOutput.class.getMethod("getClassesDir");

                File classesDir = (File) getClassesDir.invoke(sourceSet.getOutput());
                URL classesUrl = classesDir.toURI().toURL();

                getLogger().debug("Adding directory to Classpath: " + classesUrl);
                extraURLs.add(classesUrl);
            }

            URL resourcesUrl = sourceSet.getOutput().getResourcesDir().toURI().toURL();
            getLogger().debug("Adding directory to Classpath: " + resourcesUrl);
            extraURLs.add(resourcesUrl);
        }
    }

    private void addConfigurationArtifacts(String[] configurations, Set<URL> urls) throws IOException {
        for (String configuration : configurations) {
            getLogger().debug("Adding configuration to classpath: " + configuration);
            ResolvedConfiguration resolvedConfiguration =
                    getProject().getConfigurations().getByName(configuration).getResolvedConfiguration();
            for (ResolvedArtifact artifact : resolvedConfiguration.getResolvedArtifacts()) {
                URL artifactUrl = artifact.getFile().toURI().toURL();
                getLogger().debug("Adding artifact to classpath: " + artifactUrl);
                urls.add(artifactUrl);
            }
        }
    }

    private String[] determineConfigurations(Map<String, String> envVars) {
        if (envVars.containsKey(ConfigUtils.CONFIGURATIONS)) {
            return StringUtils.tokenizeToStringArray(envVars.get(ConfigUtils.CONFIGURATIONS), ",");
        }
        if (System.getProperties().containsKey(ConfigUtils.CONFIGURATIONS)) {
            return StringUtils.tokenizeToStringArray(System.getProperties().getProperty(ConfigUtils.CONFIGURATIONS), ",");
        }
        if (configurations != null) {
            return configurations;
        }
        if (extension.configurations != null) {
            return extension.configurations;
        }
        if (isJavaProject()) {
            if (getProject().getGradle().getGradleVersion().startsWith("3")) {
                return DEFAULT_CONFIGURATIONS_GRADLE3;
            }
            return DEFAULT_CONFIGURATIONS_GRADLE45;
        } else {
            return new String[0];
        }
    }

    /**
     * Executes the task's custom behavior.
     *
     * @param flyway The Flyway instance to use.
     * @return The result of the task.
     */
    protected abstract Object run(Flyway flyway);

    private Map<String, String> createFlywayConfig(Map<String, String> envVars) {
        Map<String, String> conf = new HashMap<>();

        addLocationsToConfig(conf);
        addConfigFromProperties(conf, loadConfigurationFromDefaultConfigFiles(envVars));

        putIfSet(conf, ConfigUtils.DRIVER, driver, extension.driver);
        putIfSet(conf, ConfigUtils.URL, url, extension.url);
        putIfSet(conf, ConfigUtils.USER, user, extension.user);
        putIfSet(conf, ConfigUtils.PASSWORD, password, extension.password);
        putIfSet(conf, ConfigUtils.CONNECT_RETRIES, connectRetries, extension.connectRetries);
        putIfSet(conf, ConfigUtils.CONNECT_RETRIES_INTERVAL, connectRetriesInterval, extension.connectRetriesInterval);
        putIfSet(conf, ConfigUtils.INIT_SQL, initSql, extension.initSql);
        putIfSet(conf, ConfigUtils.TABLE, table, extension.table);
        putIfSet(conf, ConfigUtils.TABLESPACE, tablespace, extension.tablespace);
        putIfSet(conf, ConfigUtils.BASELINE_VERSION, baselineVersion, extension.baselineVersion);
        putIfSet(conf, ConfigUtils.BASELINE_DESCRIPTION, baselineDescription, extension.baselineDescription);
        putIfSet(conf, ConfigUtils.SQL_MIGRATION_PREFIX, sqlMigrationPrefix, extension.sqlMigrationPrefix);
        putIfSet(conf, ConfigUtils.UNDO_SQL_MIGRATION_PREFIX, undoSqlMigrationPrefix, extension.undoSqlMigrationPrefix);
        putIfSet(conf, ConfigUtils.REPEATABLE_SQL_MIGRATION_PREFIX, repeatableSqlMigrationPrefix, extension.repeatableSqlMigrationPrefix);
        putIfSet(conf, ConfigUtils.SQL_MIGRATION_SEPARATOR, sqlMigrationSeparator, extension.sqlMigrationSeparator);
        putIfSet(conf, ConfigUtils.SQL_MIGRATION_SUFFIXES, StringUtils.arrayToCommaDelimitedString(sqlMigrationSuffixes), StringUtils.arrayToCommaDelimitedString(extension.sqlMigrationSuffixes));
        putIfSet(conf, ConfigUtils.MIXED, mixed, extension.mixed);
        putIfSet(conf, ConfigUtils.GROUP, group, extension.group);
        putIfSet(conf, ConfigUtils.INSTALLED_BY, installedBy, extension.installedBy);
        putIfSet(conf, ConfigUtils.ENCODING, encoding, extension.encoding);
        putIfSet(conf, ConfigUtils.DETECT_ENCODING, detectEncoding, extension.detectEncoding);
        putIfSet(conf, ConfigUtils.LOCK_RETRY_COUNT, lockRetryCount, extension.lockRetryCount);
        putIfSet(conf, ConfigUtils.PLACEHOLDER_REPLACEMENT, placeholderReplacement, extension.placeholderReplacement);
        putIfSet(conf, ConfigUtils.PLACEHOLDER_PREFIX, placeholderPrefix, extension.placeholderPrefix);
        putIfSet(conf, ConfigUtils.PLACEHOLDER_SUFFIX, placeholderSuffix, extension.placeholderSuffix);
        putIfSet(conf, ConfigUtils.PLACEHOLDER_SEPARATOR, placeholderSeparator, extension.placeholderSeparator);
        putIfSet(conf, ConfigUtils.SCRIPT_PLACEHOLDER_PREFIX, scriptPlaceholderPrefix, extension.scriptPlaceholderPrefix);
        putIfSet(conf, ConfigUtils.SCRIPT_PLACEHOLDER_SUFFIX, scriptPlaceholderSuffix, extension.scriptPlaceholderSuffix);
        putIfSet(conf, ConfigUtils.TARGET, target, extension.target);
        putIfSet(conf, ConfigUtils.LOGGERS, StringUtils.arrayToCommaDelimitedString(loggers), StringUtils.arrayToCommaDelimitedString(extension.loggers));
        putIfSet(conf, ConfigUtils.OUT_OF_ORDER, outOfOrder, extension.outOfOrder);
        putIfSet(conf, ConfigUtils.SKIP_EXECUTING_MIGRATIONS, skipExecutingMigrations, extension.skipExecutingMigrations);
        putIfSet(conf, ConfigUtils.OUTPUT_QUERY_RESULTS, outputQueryResults, extension.outputQueryResults);
        putIfSet(conf, ConfigUtils.VALIDATE_ON_MIGRATE, validateOnMigrate, extension.validateOnMigrate);
        putIfSet(conf, ConfigUtils.CLEAN_ON_VALIDATION_ERROR, cleanOnValidationError, extension.cleanOnValidationError);
        putIfSet(conf, ConfigUtils.IGNORE_MIGRATION_PATTERNS, StringUtils.arrayToCommaDelimitedString(ignoreMigrationPatterns), StringUtils.arrayToCommaDelimitedString(extension.ignoreMigrationPatterns));
        putIfSet(conf, ConfigUtils.VALIDATE_MIGRATION_NAMING, validateMigrationNaming, extension.validateMigrationNaming);
        putIfSet(conf, ConfigUtils.CLEAN_DISABLED, cleanDisabled, extension.cleanDisabled);
        putIfSet(conf, ConfigUtils.BASELINE_ON_MIGRATE, baselineOnMigrate, extension.baselineOnMigrate);
        putIfSet(conf, ConfigUtils.SKIP_DEFAULT_RESOLVERS, skipDefaultResolvers, extension.skipDefaultResolvers);
        putIfSet(conf, ConfigUtils.SKIP_DEFAULT_CALLBACKS, skipDefaultCallbacks, extension.skipDefaultCallbacks);
        putIfSet(conf, ConfigUtils.DEFAULT_SCHEMA, defaultSchema, extension.defaultSchema);
        putIfSet(conf, ConfigUtils.CREATE_SCHEMAS, createSchemas, extension.createSchemas);
        putIfSet(conf, ConfigUtils.FAIL_ON_MISSING_LOCATIONS, failOnMissingLocations, extension.failOnMissingLocations);

        putIfSet(conf, ConfigUtils.SCHEMAS, StringUtils.arrayToCommaDelimitedString(schemas), StringUtils.arrayToCommaDelimitedString(extension.schemas));
        putIfSet(conf, ConfigUtils.RESOLVERS, StringUtils.arrayToCommaDelimitedString(resolvers), StringUtils.arrayToCommaDelimitedString(extension.resolvers));
        putIfSet(conf, ConfigUtils.CALLBACKS, StringUtils.arrayToCommaDelimitedString(callbacks), StringUtils.arrayToCommaDelimitedString(extension.callbacks));
        putIfSet(conf, ConfigUtils.ERROR_OVERRIDES, StringUtils.arrayToCommaDelimitedString(errorOverrides), StringUtils.arrayToCommaDelimitedString(extension.errorOverrides));

        putIfSet(conf, ConfigUtils.DRYRUN_OUTPUT, dryRunOutput, extension.dryRunOutput);
        putIfSet(conf, ConfigUtils.STREAM, stream, extension.stream);
        putIfSet(conf, ConfigUtils.BATCH, batch, extension.batch);

        putIfSet(conf, ConfigUtils.KERBEROS_CONFIG_FILE, kerberosConfigFile, extension.kerberosConfigFile);

        if (extension.placeholders != null) {
            for (Map.Entry<Object, Object> entry : extension.placeholders.entrySet()) {
                conf.put(ConfigUtils.PLACEHOLDERS_PROPERTY_PREFIX + entry.getKey().toString(), entry.getValue().toString());
            }
        }
        if (placeholders != null) {
            for (Map.Entry<Object, Object> entry : placeholders.entrySet()) {
                conf.put(ConfigUtils.PLACEHOLDERS_PROPERTY_PREFIX + entry.getKey().toString(), entry.getValue().toString());
            }
        }

        if (extension.jdbcProperties != null) {
            for (Map.Entry<Object, Object> entry : extension.jdbcProperties.entrySet()) {
                conf.put(ConfigUtils.JDBC_PROPERTIES_PREFIX + entry.getKey().toString(), entry.getValue().toString());
            }
        }
        if (jdbcProperties != null) {
            for (Map.Entry<Object, Object> entry : jdbcProperties.entrySet()) {
                conf.put(ConfigUtils.JDBC_PROPERTIES_PREFIX + entry.getKey().toString(), entry.getValue().toString());
            }
        }

        conf.putAll(getPluginConfiguration(pluginConfiguration, extension.pluginConfiguration));

        addConfigFromProperties(conf, getProject().getProperties());
        addConfigFromProperties(conf, loadConfigurationFromConfigFiles(getWorkingDirectory(), envVars));
        addConfigFromProperties(conf, envVars);
        addConfigFromProperties(conf, System.getProperties());
        removeGradlePluginSpecificPropertiesToAvoidWarnings(conf);

        return conf;
    }

    public Map<String, String> getPluginConfiguration(Map<String, String> pluginConfiguration, Map<String, String> extensionPluginConfiguration) {
        Map<String, String> conf = new HashMap<>();

        if (pluginConfiguration == null && extensionPluginConfiguration == null) {
            return conf;
        }

        String camelCaseRegex = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";
        if (extensionPluginConfiguration != null) {
            for (String key : extensionPluginConfiguration.keySet()) {
                conf.put(FLYWAY_PLUGINS_PREFIX + String.join(".", key.split(camelCaseRegex)).toLowerCase(), extensionPluginConfiguration.get(key));
            }
        }
        if (pluginConfiguration != null) {
            for (String key : pluginConfiguration.keySet()) {
                conf.put(FLYWAY_PLUGINS_PREFIX + String.join(".", key.split(camelCaseRegex)).toLowerCase(), pluginConfiguration.get(key));
            }
        }

        return conf;
    }

    private void addLocationsToConfig(Map<String, String> conf) {
        File workingDirectory = getWorkingDirectory();

        conf.put(ConfigUtils.LOCATIONS, Location.FILESYSTEM_PREFIX + workingDirectory + "/src/main/resources/db/migration");

        String[] locationsToAdd = getLocations();

        if (locationsToAdd != null) {
            ConfigUtils.makeRelativeLocationsBasedOnWorkingDirectory(workingDirectory.getAbsolutePath(), locationsToAdd);
        }

        putIfSet(conf, ConfigUtils.LOCATIONS, StringUtils.arrayToCommaDelimitedString(locationsToAdd));
    }

    private String[] getLocations() {
        // To maintain override order, return configured values before extension values
        if (locations != null) {
            return locations;
        }

        if (extension.locations != null) {
            return extension.locations;
        }

        return null;
    }

    private File getWorkingDirectory() {
        // To maintain override order, return extension value first if present
        if (extension.workingDirectory != null) {
            return new File(extension.workingDirectory);
        }

        if (workingDirectory != null) {
            return new File(workingDirectory);
        }

        return new File(getProject().getProjectDir().getAbsolutePath());
    }

    /**
     * Load properties from the config files (if specified).
     *
     * @param workingDirectory The working directory to use.
     * @param envVars The environment variables converted to Flyway properties.
     * @return The properties.
     */
    private Map<String, String> loadConfigurationFromConfigFiles(File workingDirectory, Map<String, String> envVars) {
        String encoding = determineConfigurationFileEncoding(envVars);

        Map<String, String> conf = new HashMap<>();
        for (File configFile : determineConfigFiles(workingDirectory, envVars)) {
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

        Map<String, String> configMap = new HashMap<>();
        configMap.putAll(ConfigUtils.loadConfigurationFile(new File(System.getProperty("user.home") + "/" + ConfigUtils.CONFIG_FILE_NAME), encoding, false));
        configMap.putAll(ConfigUtils.loadConfigurationFile(new File(ConfigUtils.CONFIG_FILE_NAME), encoding, false));

        return configMap;
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
        if (extension.configFileEncoding != null) {
            return extension.configFileEncoding;
        }
        return "UTF-8";
    }

    /**
     * Determines the files to use for loading the configuration.
     *
     * @param workingDirectory The working directory to use.
     * @param envVars The environment variables converted to Flyway properties.
     * @return The configuration files.
     */
    private List<File> determineConfigFiles(File workingDirectory, Map<String, String> envVars) {
        List<File> configFiles = new ArrayList<>();

        if (envVars.containsKey(ConfigUtils.CONFIG_FILES)) {
            for (String file : StringUtils.tokenizeToStringArray(envVars.get(ConfigUtils.CONFIG_FILES), ",")) {
                configFiles.add(toFile(workingDirectory, file));
            }
            return configFiles;
        }

        if (System.getProperties().containsKey(ConfigUtils.CONFIG_FILES)) {
            for (String file : StringUtils.tokenizeToStringArray(System.getProperties().getProperty(ConfigUtils.CONFIG_FILES), ",")) {
                configFiles.add(toFile(workingDirectory, file));
            }
            return configFiles;
        }

        if (getProject().getProperties().containsKey(ConfigUtils.CONFIG_FILES)) {
            for (String file : StringUtils.tokenizeToStringArray(String.valueOf(getProject().getProperties().get(ConfigUtils.CONFIG_FILES)), ",")) {
                configFiles.add(toFile(workingDirectory, file));
            }
            return configFiles;
        }

        if (this.configFiles != null) {
            for (String file : this.configFiles) {
                configFiles.add(toFile(workingDirectory, file));
            }
            return configFiles;
        }

        if (extension.configFiles != null) {
            for (String file : extension.configFiles) {
                configFiles.add(toFile(workingDirectory, file));
            }
            return configFiles;
        }

        return configFiles;
    }

    /**
     * Converts this file name into a file, adjusting relative paths if necessary to make them relative to the pom.
     *
     * @param workingDirectory The working directory to use.
     * @param fileName The name of the file, relative or absolute.
     * @return The resulting file.
     */
    private File toFile(File workingDirectory, String fileName) {
        File file = new File(fileName);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(workingDirectory, fileName);
    }

    /**
     * Filters these properties to remove the Flyway Gradle Plugin-specific ones to avoid warnings.
     *
     * @param conf The properties to filter.
     */
    private static void removeGradlePluginSpecificPropertiesToAvoidWarnings(Map<String, String> conf) {
        conf.remove(ConfigUtils.CONFIG_FILES);
        conf.remove(ConfigUtils.CONFIG_FILE_ENCODING);
        conf.remove(ConfigUtils.CONFIGURATIONS);
        conf.remove("flyway.version");
        conf.remove("flyway.workingDirectory");
    }

    private static void addConfigFromProperties(Map<String, String> config, Properties properties) {
        for (String prop : properties.stringPropertyNames()) {
            if (prop.startsWith("flyway.")) {
                config.put(prop, properties.getProperty(prop));
            }
        }
    }

    private static void addConfigFromProperties(Map<String, String> config, Map<String, ?> properties) {
        for (String prop : properties.keySet()) {
            if (prop.startsWith("flyway.")) {
                config.put(prop, properties.get(prop).toString());
            }
        }
    }

    /**
     * Collect error messages from the stack trace.
     *
     * @param throwable Throwable instance from which the message should be built.
     * @param message The message to which the error message will be appended.
     * @return A String containing the composed messages.
     */
    private String collectMessages(Throwable throwable, String message) {
        if (throwable != null) {
            message += "\n" + throwable.getMessage();
            return collectMessages(throwable.getCause(), message);
        }
        return message;
    }

    private boolean isJavaProject() {
        return getProject().getPluginManager().hasPlugin("java");
    }
}
