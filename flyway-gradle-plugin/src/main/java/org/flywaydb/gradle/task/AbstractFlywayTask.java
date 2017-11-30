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
package org.flywaydb.gradle.task;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.gradle.FlywayExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.flywaydb.core.internal.configuration.ConfigUtils.putIfSet;

/**
 * A base class for all flyway tasks.
 */
public abstract class AbstractFlywayTask extends DefaultTask {
    /**
     * The flyway {} block in the build script.
     */
    protected FlywayExtension extension;
    /**
     * The fully qualified classname of the jdbc driver to use to connect to the database
     */
    public String driver;

    /**
     * The jdbc url to use to connect to the database
     */
    public String url;

    /**
     * The user to use to connect to the database
     */
    public String user;

    /**
     * The password to use to connect to the database
     */
    public String password;

    /**
     * The name of Flyway's schema history table
     */
    public String table;

    /**
     * The case-sensitive list of schemas managed by Flyway
     */
    public String[] schemas;

    /**
     * The version to tag an existing schema with when executing baseline. (default: 1)
     */
    public String baselineVersion;

    /**
     * The description to tag an existing schema with when executing baseline. (default: << Flyway Baseline >>)
     */
    public String baselineDescription;

    /**
     * Locations to scan recursively for migrations. The location type is determined by its prefix.
     * (default: filesystem:src/main/resources/db/migration)
     * <p>
     * <tt>Unprefixed locations or locations starting with classpath:</tt>
     * point to a package on the classpath and may contain both sql and java-based migrations.
     * <p>
     * <tt>Locations starting with filesystem:</tt>
     * point to a directory on the filesystem and may only contain sql migrations.
     */
    public String[] locations;

    /**
     * The fully qualified class names of the custom MigrationResolvers to be used in addition (default)
     * or as a replacement (using skipDefaultResolvers) to the built-in ones for resolving Migrations to
     * apply.
     * <p>(default: none)</p>
     */
    public String[] resolvers;

    /**
     * If set to true, default built-in resolvers will be skipped, only custom migration resolvers will be used.
     * <p>(default: false)</p>
     */
    public Boolean skipDefaultResolvers;

    /**
     * The file name prefix for Sql migrations
     * <p>
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     */
    public String sqlMigrationPrefix;

    /**
     * The file name prefix for repeatable sql migrations (default: R).
     * <p>
     * <p>Repeatable sql migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to R__My_description.sql</p>
     */
    public String repeatableSqlMigrationPrefix;

    /**
     * The file name prefix for Sql migrations
     * <p>
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     */
    public String sqlMigrationSeparator;

    /**
     * The file name suffix for Sql migrations
     * <p>
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     */
    public String sqlMigrationSuffix;

    /**
     * The encoding of Sql migrations
     */
    public String encoding;

    /**
     * Placeholders to replace in Sql migrations
     */
    public Map<Object, Object> placeholders;

    /**
     * Whether placeholders should be replaced.
     */
    public Boolean placeholderReplacement;

    /**
     * The prefix of every placeholder
     */
    public String placeholderPrefix;

    /**
     * The suffix of every placeholder
     */
    public String placeholderSuffix;

    /**
     * The target version up to which Flyway should consider migrations.
     * Migrations with a higher version number will be ignored.
     * The special value {@code current} designates the current version of the schema.
     */
    public String target;

    /**
     * An array of fully qualified FlywayCallback class implementations
     */
    public String[] callbacks;

    /**
     * If set to true, default built-in callbacks will be skipped, only custom migration callbacks will be used.
     * <p>(default: false)</p>
     */
    public Boolean skipDefaultCallbacks;

    /**
     * Allows migrations to be run "out of order"
     */
    public Boolean outOfOrder;

    /**
     * Whether to automatically call validate or not when running migrate. (default: true)
     */
    public Boolean validateOnMigrate;

    /**
     * Whether to automatically call clean or not when a validation error occurs
     */
    public Boolean cleanOnValidationError;

    /**
     * Ignore missing migrations when reading the schema history table. These are migrations that were performed by an
     * older deployment of the application that are no longer available in this version. For example: we have migrations
     * available on the classpath with versions 1.0 and 3.0. The schema history table indicates that a migration with version 2.0
     * (unknown to us) has also been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to deploy
     * a newer version of the application even though it doesn't contain migrations included with an older one anymore.
     * Note that if the most recently applied migration is removed, Flyway has no way to know it is missing and will
     * mark it as future instead.
     * <p>
     * {@code true} to continue normally and log a warning, {@code false} to fail fast with an exception.
     * (default: {@code false})
     */
    public Boolean ignoreMissingMigrations;

    /**
     * Ignore future migrations when reading the schema history table. These are migrations that were performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The schema history table indicates that a migration to version 4.0
     * (unknown to us) has already been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to redeploy
     * an older version of the application after the database has been migrated by a newer one. (default: {@code true})
     */
    public Boolean ignoreFutureMigrations;

    /**
     * Whether to disable clean. (default: {@code false})
     * <p>This is especially useful for production environments where running clean can be quite a career limiting move.</p>
     */
    public Boolean cleanDisabled;

    /**
     * <p>
     * Whether to automatically call baseline when migrate is executed against a non-empty schema with no schema history table.
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
     * <p>
     * <p>{@code true} if baseline should be called on migrate for non-empty schemas, {@code false} if not. (default: {@code false})</p>
     */
    public Boolean baselineOnMigrate;

    /**
     * Whether to allow mixing transactional and non-transactional statements within the same migration.
     * <p>{@code true} if mixed migrations should be allowed. {@code false} if an error should be thrown instead. (default: {@code false})</p>
     */
    public Boolean mixed;

    /**
     * Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions).
     * <p>{@code true} if migrations should be grouped. {@code false} if they should be applied individually instead. (default: {@code false})</p>
     */
    public Boolean group;

    /**
     * The username that will be recorded in the schema history table as having applied the migration.
     * <p>
     * {@code null} for the current database user of the connection. (default: {@code null}).
     */
    public String installedBy;

    /**
     * The fully qualified class names of handlers for errors and warnings that occur during a migration. This can be
     * used to customize Flyway's behavior by for example
     * throwing another runtime exception, outputting a warning or suppressing the error instead of throwing a FlywayException.
     * ErrorHandlers are invoked in order until one reports to have successfully handled the errors or warnings.
     * If none do, or if none are present, Flyway falls back to its default handling of errors and warnings.
     * (default: none)
     * <p>Also configurable with Gradle or System Property: ${flyway.errorHandler}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     */
    public String[] errorHandlers;

    /**
     * The file where to output the SQL statements of a migration dry run. If the file specified is in a non-existent
     * directory, Flyway will create all directories and parent directories as needed.
     * <p>{@code null} to execute the SQL statements directly against the database. (default: {@code null})</p>
     * <p>Also configurable with Gradle or System Property: ${flyway.dryRunOutput}</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     */
    public String dryRunOutput;

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

    public AbstractFlywayTask() {
        super();
        setGroup("Flyway");
        extension = (FlywayExtension) getProject().getExtensions().getByName("flyway");
    }

    @TaskAction
    public Object runTask() {
        try {
            List<URL> extraURLs = new ArrayList<>();
            if (isJavaProject()) {
                JavaPluginConvention plugin = getProject().getConvention().getPlugin(JavaPluginConvention.class);

                for (SourceSet sourceSet : plugin.getSourceSets()) {
                    URL classesUrl = sourceSet.getOutput().getClassesDir().toURI().toURL();
                    getLogger().debug("Adding directory to Classpath: " + classesUrl);
                    extraURLs.add(classesUrl);

                    URL resourcesUrl = sourceSet.getOutput().getResourcesDir().toURI().toURL();
                    getLogger().debug("Adding directory to Classpath: " + resourcesUrl);
                    extraURLs.add(resourcesUrl);
                }

                addDependenciesWithScope(extraURLs, "compile");
                addDependenciesWithScope(extraURLs, "runtime");
                addDependenciesWithScope(extraURLs, "testCompile");
                addDependenciesWithScope(extraURLs, "testRuntime");
            }

            ClassLoader classLoader = new URLClassLoader(
                    extraURLs.toArray(new URL[extraURLs.size()]),
                    getProject().getBuildscript().getClassLoader());

            Flyway flyway = new Flyway(classLoader);
            flyway.configure(createFlywayConfig());
            return run(flyway);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    private void addDependenciesWithScope(List<URL> urls, String scope) throws IOException {
        for (ResolvedArtifact artifact : getProject().getConfigurations().getByName(scope).getResolvedConfiguration().getResolvedArtifacts()) {
            URL artifactUrl = artifact.getFile().toURI().toURL();
            getLogger().debug("Adding Dependency to Classpath: " + artifactUrl);
            urls.add(artifactUrl);
        }
    }

    /**
     * Executes the task's custom behavior.
     */
    protected abstract Object run(Flyway flyway);

    /**
     * Creates the Flyway config to use.
     */
    private Map<String, String> createFlywayConfig() {
        Map<String, String> conf = new HashMap<>();
        conf.put(ConfigUtils.LOCATIONS, Location.FILESYSTEM_PREFIX + getProject().getProjectDir().getAbsolutePath() + "/src/main/resources/db/migration");

        Map<String, String> envVars = ConfigUtils.environmentVariablesToPropertyMap();
        addConfigFromProperties(conf, loadConfigurationFromDefaultConfigFiles(envVars));

        putIfSet(conf, ConfigUtils.DRIVER, driver, extension.driver);
        putIfSet(conf, ConfigUtils.URL, url, extension.url);
        putIfSet(conf, ConfigUtils.USER, user, extension.user);
        putIfSet(conf, ConfigUtils.PASSWORD, password, extension.password);
        putIfSet(conf, ConfigUtils.TABLE, table, extension.table);
        putIfSet(conf, ConfigUtils.BASELINE_VERSION, baselineVersion, extension.baselineVersion);
        putIfSet(conf, ConfigUtils.BASELINE_DESCRIPTION, baselineDescription, extension.baselineDescription);
        putIfSet(conf, ConfigUtils.SQL_MIGRATION_PREFIX, sqlMigrationPrefix, extension.sqlMigrationPrefix);
        putIfSet(conf, ConfigUtils.REPEATABLE_SQL_MIGRATION_PREFIX, repeatableSqlMigrationPrefix, extension.repeatableSqlMigrationPrefix);
        putIfSet(conf, ConfigUtils.SQL_MIGRATION_SEPARATOR, sqlMigrationSeparator, extension.sqlMigrationSeparator);
        putIfSet(conf, ConfigUtils.SQL_MIGRATION_SUFFIX, sqlMigrationSuffix, extension.sqlMigrationSuffix);
        putIfSet(conf, ConfigUtils.MIXED, mixed, extension.mixed);
        putIfSet(conf, ConfigUtils.GROUP, group, extension.group);
        putIfSet(conf, ConfigUtils.INSTALLED_BY, installedBy, extension.installedBy);
        putIfSet(conf, ConfigUtils.ENCODING, encoding, extension.encoding);
        putIfSet(conf, ConfigUtils.PLACEHOLDER_REPLACEMENT, placeholderReplacement, extension.placeholderReplacement);
        putIfSet(conf, ConfigUtils.PLACEHOLDER_PREFIX, placeholderPrefix, extension.placeholderPrefix);
        putIfSet(conf, ConfigUtils.PLACEHOLDER_SUFFIX, placeholderSuffix, extension.placeholderSuffix);
        putIfSet(conf, ConfigUtils.TARGET, target, extension.target);
        putIfSet(conf, ConfigUtils.OUT_OF_ORDER, outOfOrder, extension.outOfOrder);
        putIfSet(conf, ConfigUtils.VALIDATE_ON_MIGRATE, validateOnMigrate, extension.validateOnMigrate);
        putIfSet(conf, ConfigUtils.CLEAN_ON_VALIDATION_ERROR, cleanOnValidationError, extension.cleanOnValidationError);
        putIfSet(conf, ConfigUtils.IGNORE_MISSING_MIGRATIONS, ignoreMissingMigrations, extension.ignoreMissingMigrations);
        putIfSet(conf, ConfigUtils.IGNORE_FUTURE_MIGRATIONS, ignoreFutureMigrations, extension.ignoreFutureMigrations);
        putIfSet(conf, ConfigUtils.CLEAN_DISABLED, cleanDisabled, extension.cleanDisabled);
        putIfSet(conf, ConfigUtils.BASELINE_ON_MIGRATE, baselineOnMigrate, extension.baselineOnMigrate);
        putIfSet(conf, ConfigUtils.SKIP_DEFAULT_RESOLVERS, skipDefaultResolvers, extension.skipDefaultResolvers);
        putIfSet(conf, ConfigUtils.SKIP_DEFAULT_CALLBACKS, skipDefaultCallbacks, extension.skipDefaultCallbacks);

        putIfSet(conf, ConfigUtils.SCHEMAS, StringUtils.arrayToCommaDelimitedString(schemas), StringUtils.arrayToCommaDelimitedString(extension.schemas));
        putIfSet(conf, ConfigUtils.LOCATIONS, StringUtils.arrayToCommaDelimitedString(locations), StringUtils.arrayToCommaDelimitedString(extension.locations));
        putIfSet(conf, ConfigUtils.RESOLVERS, StringUtils.arrayToCommaDelimitedString(resolvers), StringUtils.arrayToCommaDelimitedString(extension.resolvers));
        putIfSet(conf, ConfigUtils.CALLBACKS, StringUtils.arrayToCommaDelimitedString(callbacks), StringUtils.arrayToCommaDelimitedString(extension.callbacks));
        putIfSet(conf, ConfigUtils.ERROR_HANDLERS, StringUtils.arrayToCommaDelimitedString(errorHandlers), StringUtils.arrayToCommaDelimitedString(extension.errorHandlers));

        putIfSet(conf, ConfigUtils.DRYRUN_OUTPUT, dryRunOutput, extension.dryRunOutput);

        if (placeholders != null) {
            for (Map.Entry<Object, Object> entry : placeholders.entrySet()) {
                conf.put(ConfigUtils.PLACEHOLDERS_PROPERTY_PREFIX + entry.getKey().toString(), entry.getValue().toString());
            }
        }
        if (extension.placeholders != null) {
            for (Map.Entry<Object, Object> entry : extension.placeholders.entrySet()) {
                conf.put(ConfigUtils.PLACEHOLDERS_PROPERTY_PREFIX + entry.getKey().toString(), entry.getValue().toString());
            }
        }

        addConfigFromProperties(conf, getProject().getProperties());
        addConfigFromProperties(conf, loadConfigurationFromConfigFiles(envVars));
        addConfigFromProperties(conf, envVars);
        addConfigFromProperties(conf, System.getProperties());
        removeGradlePluginSpecificPropertiesToAvoidWarnings(conf);

        return conf;
    }

    /**
     * Retrieve the properties from the config files (if specified).
     *
     * @param envVars The environment variables converted to Flyway properties.
     * @return The properties.
     */
    private Map<String, String> loadConfigurationFromConfigFiles(Map<String, String> envVars) {
        String encoding = determineConfigurationFileEncoding(envVars);

        Map<String, String> conf = new HashMap<>();
        for (File configFile : determineConfigFiles(envVars)) {
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

        Map<String, String> conf = new HashMap<>();
        conf.putAll(ConfigUtils.loadConfigurationFile(
                new File(System.getProperty("user.home") + "/" + ConfigUtils.CONFIG_FILE_NAME), encoding, false));
        return conf;
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
     * @param envVars The environment variables converted to Flyway properties.
     * @return The configuration files.
     */
    private List<File> determineConfigFiles(Map<String, String> envVars) {
        List<File> configFiles = new ArrayList<>();

        if (envVars.containsKey(ConfigUtils.CONFIG_FILES)) {
            for (String file : StringUtils.tokenizeToStringArray(envVars.get(ConfigUtils.CONFIG_FILES), ",")) {
                configFiles.add(toFile(file));
            }
            return configFiles;
        }

        if (System.getProperties().containsKey(ConfigUtils.CONFIG_FILES)) {
            for (String file : StringUtils.tokenizeToStringArray(System.getProperties().getProperty(ConfigUtils.CONFIG_FILES), ",")) {
                configFiles.add(toFile(file));
            }
            return configFiles;
        }

        if (getProject().getProperties().containsKey(ConfigUtils.CONFIG_FILES)) {
            for (String file : StringUtils.tokenizeToStringArray(System.getProperties().getProperty(ConfigUtils.CONFIG_FILES), ",")) {
                configFiles.add(toFile(file));
            }
            return configFiles;
        }

        if (this.configFiles != null) {
            for (String file : this.configFiles) {
                configFiles.add(toFile(file));
            }
            return configFiles;
        }

        if (extension.configFiles != null) {
            for (String file : extension.configFiles) {
                configFiles.add(toFile(file));
            }
            return configFiles;
        }

        return configFiles;
    }

    /**
     * Converts this fileName into a file, adjusting relative paths if necessary to make them relative to the pom.
     *
     * @param fileName The name of the file, relative or absolute.
     * @return The resulting file.
     */
    private File toFile(String fileName) {
        File file = new File(fileName);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(getProject().getProjectDir(), fileName);
    }

    /**
     * Filters there properties to remove the Flyway Gradle Plugin-specific ones to avoid warnings.
     *
     * @param conf The properties to filter.
     */
    private static void removeGradlePluginSpecificPropertiesToAvoidWarnings(Map<String, String> conf) {
        conf.remove(ConfigUtils.CONFIG_FILES);
        conf.remove(ConfigUtils.CONFIG_FILE_ENCODING);
        conf.remove("flyway.version");
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
     * @param throwable Throwable instance to be handled
     */
    private void handleException(Throwable throwable) {
        String message = "Error occurred while executing " + getName();
        throw new FlywayException(collectMessages(throwable, message), throwable);
    }

    /**
     * Collect error messages from the stack trace
     *
     * @param throwable Throwable instance from which the message should be build
     * @param message   the message to which the error message will be appended
     * @return a String containing the composed messages
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