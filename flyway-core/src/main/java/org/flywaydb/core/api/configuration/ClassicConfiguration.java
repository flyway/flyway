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

import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.api.*;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.extensibility.ConfigurationProvider;
import org.flywaydb.core.internal.configuration.ConfigUtils;

import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.configuration.models.FlywayModel;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.configuration.resolvers.EnvironmentResolver;
import org.flywaydb.core.internal.configuration.resolvers.PropertyResolver;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.scanner.ClasspathClassScanner;
import org.flywaydb.core.internal.util.*;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;






import static org.flywaydb.core.internal.configuration.ConfigUtils.removeBoolean;
import static org.flywaydb.core.internal.configuration.ConfigUtils.removeInteger;

/**
 * JavaBean-style configuration for Flyway. This is primarily meant for compatibility with scenarios where the
 * new FluentConfiguration isn't an easy fit, such as Spring XML bean configuration.
 * <p>This configuration can then be passed to Flyway using the <code>new Flyway(Configuration)</code> constructor.</p>
 */
@CustomLog
public class ClassicConfiguration implements Configuration {

    public static final String TEMP_ENVIRONMENT_NAME = "tempConfigEnvironment";
    @Getter
    @Setter
    private ConfigurationModel modernConfig = ConfigurationModel.defaults();

    private final Map<String, ResolvedEnvironment> resolvedEnvironments = new HashMap<>();

    @Getter
    @Setter
    private DataSource dataSource;

    @Getter
    @Setter
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public void setDefaultSchema(String defaultSchema) {
        getModernFlyway().setDefaultSchema(defaultSchema);
    }

    public String getDefaultSchema() {
        return getModernFlyway().getDefaultSchema();
    }

    private EnvironmentResolver environmentResolver;

    private EnvironmentModel getCurrentUnresolvedEnvironment() {
        String envName = modernConfig.getFlyway().getEnvironment();
        if (!StringUtils.hasText(envName)) {
            envName = "default";
        }

        return getModernConfig().getEnvironments().get(envName);
    }

    public ResolvedEnvironment getCurrentResolvedEnvironment() {
        String envName = modernConfig.getFlyway().getEnvironment();
        if (!StringUtils.hasText(envName)) {
            envName = "default";
        }

        return getResolvedEnvironment(envName);
    }

    public ResolvedEnvironment getResolvedEnvironment(String envName) {
        if (environmentResolver == null) {
            environmentResolver = new EnvironmentResolver(pluginRegister.getPlugins(PropertyResolver.class).stream().collect(Collectors.toMap(PropertyResolver::getName, x -> x)));
        }

        if (!resolvedEnvironments.containsKey(envName) && getModernConfig().getEnvironments().containsKey(envName)) {
            resolvedEnvironments.put(envName, environmentResolver.resolve(getModernConfig().getEnvironments().get(envName)));
        }

        return resolvedEnvironments.get(envName);
    }

    private FlywayModel getModernFlyway() {
        return modernConfig.getFlyway();
    }

    @Override
    public String[] getSchemas() {
        return getCurrentResolvedEnvironment().getSchemas().toArray(new String[0]);
    }

    @Override
    public Charset getEncoding() {
        return Charset.forName(getModernFlyway().getEncoding());
    }

    @Override
    public boolean isDetectEncoding() {
        return getModernFlyway().getDetectEncoding();
    }

    @Getter
    @Setter
    private ResourceProvider resourceProvider = null;
    @Getter
    @Setter
    private ClassProvider<JavaMigration> javaMigrationClassProvider = null;
    @Getter
    private JavaMigration[] javaMigrations = { };

    @Getter
    private MigrationResolver[] resolvers = { };

    private OutputStream dryRunOutput;

    private final List<Callback> callbacks = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    private final ClasspathClassScanner classScanner;
    @Getter
    @Setter
    private PluginRegister pluginRegister = new PluginRegister();

    public ClassicConfiguration(ConfigurationModel modernConfig) {
        this.classScanner = new ClasspathClassScanner(this.classLoader);
        this.modernConfig = modernConfig;
    }

    public ClassicConfiguration() {
        this.classScanner = new ClasspathClassScanner(this.classLoader);
    }

    /**
     * @param classLoader The ClassLoader to use for loading migrations, resolvers, etc. from the classpath. (default: Thread.currentThread().getContextClassLoader())
     */
    public ClassicConfiguration(ClassLoader classLoader) {
        if (classLoader != null) {
            this.classLoader = classLoader;
        }
        classScanner = new ClasspathClassScanner(this.classLoader);
    }

    /**
     * Creates a new configuration with the same values as this existing one.
     */
    public ClassicConfiguration(Configuration configuration) {
        this(configuration.getClassLoader());
        configure(configuration);
    }

    @Override
    public Callback[] getCallbacks() {
        return callbacks.toArray(new Callback[0]);
    }

    @Override
    public String getUrl() {
        return getCurrentResolvedEnvironment().getUrl();
    }

    @Override
    public String getUser() {
        return getCurrentResolvedEnvironment().getUser();
    }

    @Override
    public String getPassword() {
        return getCurrentResolvedEnvironment().getPassword();
    }

    @Override
    public Location[] getLocations() {
        Locations locations = new Locations(getModernFlyway().getLocations().toArray(new String[0]));
        return locations.getLocations().toArray(new Location[0]);
    }

    @Override
    public boolean isBaselineOnMigrate() {
        return getModernFlyway().getBaselineOnMigrate();
    }

    @Override
    public boolean isSkipExecutingMigrations() {
        return getModernFlyway().getSkipExecutingMigrations();
    }

    @Override
    public boolean isOutOfOrder() {
        return getModernFlyway().getOutOfOrder();
    }

    @Override
    public ValidatePattern[] getIgnoreMigrationPatterns() {
        String[] ignoreMigrationPatterns = getModernFlyway().getIgnoreMigrationPatterns().toArray(new String[0]);
        if (Arrays.equals(ignoreMigrationPatterns, new String[] { "" })) {
            return new ValidatePattern[0];
        } else {
            return Arrays.stream(ignoreMigrationPatterns)
                         .map(ValidatePattern::fromPattern)
                         .toArray(ValidatePattern[]::new);
        }
    }

    @Override
    public boolean isValidateMigrationNaming() {
        return getModernFlyway().getValidateMigrationNaming();
    }

    @Override
    public boolean isValidateOnMigrate() {
        return getModernFlyway().getValidateOnMigrate();
    }

    @Override
    public boolean isCleanOnValidationError() {
        return getModernFlyway().getCleanOnValidationError();
    }

    @Override
    public boolean isCleanDisabled() {
        return getModernFlyway().getCleanDisabled();
    }

    @Override
    public boolean isMixed() {
        return getModernFlyway().getMixed();
    }

    @Override
    public boolean isGroup() {
        return getModernFlyway().getGroup();
    }

    @Override
    public String getInstalledBy() {
        return getModernFlyway().getInstalledBy();
    }

    @Override
    public String[] getErrorOverrides() {
        return getModernFlyway().getErrorOverrides().toArray(new String[0]);
    }

    @Override
    public OutputStream getDryRunOutput() {

        if (this.dryRunOutput == null) {

            String dryRunOutputFileName = modernConfig.getFlyway().getDryRunOutput();
            if (!StringUtils.hasText(dryRunOutputFileName)) {
                return null;
            }









        }

        return this.dryRunOutput;
    }

    @Override
    public boolean isStream() {
        return getModernFlyway().getStream();
    }

    @Override
    public boolean isBatch() {
        return getModernFlyway().getBatch();
    }

    @Override
    public boolean isOracleSqlplus() {
        return getModernFlyway().getOracleSqlplus();
    }

    @Override
    public boolean isOracleSqlplusWarn() {
        return getModernFlyway().getOracleSqlplusWarn();
    }

    @Override
    public String getKerberosConfigFile() {
        return getModernFlyway().getKerberosConfigFile();
    }

    @Override
    public String getOracleKerberosCacheFile() {
        return getModernFlyway().getOracleKerberosCacheFile();
    }

    @Override
    public String getLicenseKey() {
        return getModernFlyway().getLicenseKey();
    }

    @Override
    public boolean isOutputQueryResults() {
        return getModernFlyway().getOutputQueryResults();
    }

    @Override
    public boolean isCreateSchemas() {
        return getModernFlyway().getCreateSchemas();
    }

    @Override
    public int getLockRetryCount() {
        return getModernFlyway().getLockRetryCount();
    }

    @Override
    public Map<String, String> getJdbcProperties() {
        return getCurrentResolvedEnvironment().getJdbcProperties();
    }

    @Override
    public boolean isFailOnMissingLocations() {
        return getModernFlyway().getFailOnMissingLocations();
    }

    @Override
    public String getOracleWalletLocation() {
        return getModernFlyway().getOracleWalletLocation();
    }

    @Override
    public String[] getLoggers() {
        return getModernFlyway().getLoggers().toArray(new String[0]);
    }

    @Override
    public int getConnectRetries() {
        return getCurrentResolvedEnvironment().getConnectRetries();
    }

    @Override
    public int getConnectRetriesInterval() {
        return getCurrentResolvedEnvironment().getConnectRetriesInterval();
    }

    @Override
    public String getInitSql() {
        return getCurrentResolvedEnvironment().getInitSql();
    }

    @Override
    public MigrationVersion getBaselineVersion() {

        return MigrationVersion.fromVersion(getModernFlyway().getBaselineVersion()); // todo - should this be in env
    }

    @Override
    public String getBaselineDescription() {
        return getModernFlyway().getBaselineDescription();
    }

    @Override
    public boolean isSkipDefaultResolvers() {
        return getModernFlyway().getSkipDefaultResolvers();
    }

    @Override
    public boolean isSkipDefaultCallbacks() {
        return getModernFlyway().getSkipDefaultCallbacks();
    }

    @Override
    public String getSqlMigrationPrefix() {
        return getModernFlyway().getSqlMigrationPrefix();
    }

    @Override
    public String getUndoSqlMigrationPrefix() {
        return getModernFlyway().getUndoSqlMigrationPrefix();
    }

    @Override
    public boolean isExecuteInTransaction() {
        return getModernFlyway().getExecuteInTransaction();
    }

    @Override
    public String getRepeatableSqlMigrationPrefix() {
        return getModernFlyway().getRepeatableSqlMigrationPrefix();
    }

    @Override
    public String getSqlMigrationSeparator() {
        return getModernFlyway().getSqlMigrationSeparator();
    }

    @Override
    public String[] getSqlMigrationSuffixes() {
        return getModernFlyway().getSqlMigrationSuffixes().toArray(new String[0]);
    }

    @Override
    public boolean isPlaceholderReplacement() {
        return getModernFlyway().getPlaceholderReplacement();
    }

    @Override
    public String getPlaceholderSuffix() {
        return getModernFlyway().getPlaceholderSuffix();
    }

    @Override
    public String getPlaceholderPrefix() {
        return getModernFlyway().getPlaceholderPrefix();
    }

    @Override
    public String getPlaceholderSeparator() {
        return getModernFlyway().getPlaceholderSeparator();
    }

    @Override
    public String getScriptPlaceholderSuffix() {
        return getModernFlyway().getScriptPlaceholderSuffix();
    }

    @Override
    public String getScriptPlaceholderPrefix() {
        return getModernFlyway().getScriptPlaceholderPrefix();
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return getModernFlyway().getPlaceholders();
    }

    @Override
    public MigrationVersion getTarget() {
        String target = getModernFlyway().getTarget();
        if (target.endsWith("?")) {

            throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("failOnMissingTarget");





        } else {
            getModernFlyway().setFailOnMissingTarget(true);
            return MigrationVersion.fromVersion(target);
        }
    }

    @Override
    public boolean isFailOnMissingTarget() {
        return getModernFlyway().getFailOnMissingTarget();
    }

    @Override
    public MigrationPattern[] getCherryPick() {
        MigrationPattern[] cherryPick = null;
        if (getModernFlyway().getCherryPick() != null) {
            cherryPick = getModernFlyway().getCherryPick().stream().map(MigrationPattern::new).toArray(MigrationPattern[]::new);
        }

        cherryPick = (cherryPick != null && cherryPick.length == 0) ? null : cherryPick;

        if (cherryPick == null) {
            return null;
        }

        Set<String> cherryPickValues = new HashSet<>();
        List<String> duplicateValues = new ArrayList<>();

        for (MigrationPattern migrationPattern : cherryPick) {
            String migrationPatternString = migrationPattern.toString();

            if (cherryPickValues.contains(migrationPatternString)) {
                duplicateValues.add(migrationPatternString);
            }

            cherryPickValues.add(migrationPatternString);
        }

        if (!duplicateValues.isEmpty()) {
            StringBuilder listString = new StringBuilder();

            for (String s : duplicateValues) {
                listString.append(s).append(" ");
            }

            throw new FlywayException("Duplicate values not allowed in cherryPick. Detected duplicates: \n" + listString);
        }

        return cherryPick;
    }

    @Override
    public String getTable() {
        return getModernFlyway().getTable();
    }

    @Override
    public String getTablespace() {
        return getModernFlyway().getTablespace();
    }

    /**
     * Sets the stream where to output the SQL statements of a migration dry run. {@code null} to execute the SQL statements
     * directly against the database. The stream will be closed when Flyway finishes writing the output.
     * <i>Flyway Teams only</i>
     *
     * @param dryRunOutput The output file or {@code null} to execute the SQL statements directly against the database.
     */
    public void setDryRunOutput(OutputStream dryRunOutput) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("dryRunOutput");




    }

    /**
     * Sets the file where to output the SQL statements of a migration dry run. {@code null} to execute the SQL statements
     * directly against the database. If the file specified is in a non-existent directory, Flyway will create all
     * directories and parent directories as needed.
     * <i>Flyway Teams only</i>
     *
     * @param dryRunOutput The output file or {@code null} to execute the SQL statements directly against the database.
     */
    public void setDryRunOutputAsFile(File dryRunOutput) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("dryRunOutput");











































    }

    private OutputStream getDryRunOutputAsFile(File dryRunOutput) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("dryRunOutput");










































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
    public void setDryRunOutputAsFileName(String dryRunOutputFileName) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("dryRunOutput");




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
    public void setErrorOverrides(String... errorOverrides) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("errorOverrides");




    }

    /**
     * The username that will be recorded in the schema history table as having applied the migration.
     *
     * @param installedBy The username or {@code null} for the current database user of the connection. (default: {@code null}).
     */
    public void setInstalledBy(String installedBy) {
        if ("".equals(installedBy)) {
            installedBy = null;
        }
        getModernFlyway().setInstalledBy(installedBy);
    }

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
    public void setLoggers(String... loggers) {
        getModernFlyway().setLoggers(Arrays.stream(loggers).collect(Collectors.toList()));
    }

    /**
     * Ignore migrations that match this comma-separated list of patterns when validating migrations.
     * Each pattern is of the form <migration_type>:<migration_state>
     * See https://flywaydb.org/documentation/configuration/parameters/ignoreMigrationPatterns for full details
     * Example: repeatable:missing,versioned:pending,*:failed
     * <i>Flyway Teams only</i>
     */
    public void setIgnoreMigrationPatterns(String... ignoreMigrationPatterns) {
        getModernFlyway().setIgnoreMigrationPatterns(Arrays.stream(ignoreMigrationPatterns).collect(Collectors.toList()));
    }

    /**
     * Ignore migrations that match this array of ValidatePatterns when validating migrations.
     * See https://flywaydb.org/documentation/configuration/parameters/ignoreMigrationPatterns for full details
     * <i>Flyway Teams only</i>
     */
    public void setIgnoreMigrationPatterns(ValidatePattern... ignoreMigrationPatterns) {
        getModernFlyway().setIgnoreMigrationPatterns(Arrays.stream(ignoreMigrationPatterns).map(ValidatePattern::toString).collect(Collectors.toList()));
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
    public void setLocationsAsStrings(String... locations) {
        getModernFlyway().setLocations(Arrays.stream(locations).collect(Collectors.toList()));
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
    public void setLocations(Location... locations) {
        getModernFlyway().setLocations(Arrays.stream(locations).map(Location::getDescriptor).collect(Collectors.toList()));
    }

    /**
     * Whether Flyway should try to automatically detect SQL migration file encoding
     *
     * @param detectEncoding {@code true} to enable auto detection, {@code false} otherwise
     *                       <i>Flyway Teams only</i>
     */
    public void setDetectEncoding(boolean detectEncoding) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("detectEncoding");




    }

    /**
     * Sets whether SQL should be executed within a transaction.
     *
     * @param executeInTransaction {@code true} to enable execution of SQL in a transaction, {@code false} otherwise
     */
    public void setExecuteInTransaction(boolean executeInTransaction) {
        getModernFlyway().setExecuteInTransaction(executeInTransaction);
    }

    /**
     * Sets the encoding of SQL migrations.
     *
     * @param encoding The encoding of SQL migrations. (default: UTF-8)
     */
    public void setEncodingAsString(String encoding) {
        getModernFlyway().setEncoding(encoding);
    }

    /**
     * Sets the target version up to which Flyway should consider migrations.
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
    public void setTargetAsString(String target) {
        getModernFlyway().setTarget(target);
    }

    /**
     * Gets the migrations that Flyway should consider when migrating or undoing. Leave empty to consider all available migrations.
     * Migrations not in this list will be ignored.
     * <i>Flyway Teams only</i>
     */
    public void setCherryPick(MigrationPattern... cherryPick) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("cherryPick");









    }

    /**
     * Gets the migrations that Flyway should consider when migrating or undoing. Leave empty to consider all available migrations.
     * Migrations not in this list will be ignored.
     * Values should be the version for versioned migrations (e.g. 1, 2.4, 6.5.3) or the description for repeatable migrations (e.g. Insert_Data, Create_Table)
     * <i>Flyway Teams only</i>
     */
    public void setCherryPick(String... cherryPickAsString) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("cherryPick");




    }

    /**
     * Sets the prefix of every placeholder.
     *
     * @param placeholderPrefix The prefix of every placeholder. (default: ${ )
     */
    public void setPlaceholderPrefix(String placeholderPrefix) {
        if (!StringUtils.hasLength(placeholderPrefix)) {
            throw new FlywayException("placeholderPrefix cannot be empty!", ErrorCode.CONFIGURATION);
        }
        getModernFlyway().setPlaceholderPrefix(placeholderPrefix);
    }

    /**
     * Sets the prefix of every script placeholder.
     *
     * @param scriptPlaceholderPrefix The prefix of every placeholder. (default: FP__ )
     */
    public void setScriptPlaceholderPrefix(String scriptPlaceholderPrefix) {
        if (!StringUtils.hasLength(scriptPlaceholderPrefix)) {
            throw new FlywayException("scriptPlaceholderPrefix cannot be empty!", ErrorCode.CONFIGURATION);
        }
        getModernFlyway().setScriptPlaceholderPrefix(scriptPlaceholderPrefix);
    }

    /**
     * Sets the suffix of every placeholder.
     *
     * @param placeholderSuffix The suffix of every placeholder. (default: } )
     */
    public void setPlaceholderSuffix(String placeholderSuffix) {
        if (!StringUtils.hasLength(placeholderSuffix)) {
            throw new FlywayException("placeholderSuffix cannot be empty!", ErrorCode.CONFIGURATION);
        }
        getModernFlyway().setPlaceholderSuffix(placeholderSuffix);
    }

    /**
     * Sets the separator of default placeholders.
     *
     * @param placeholderSeparator The separator of default placeholders. (default: : )
     */
    public void setPlaceholderSeparator(String placeholderSeparator) {
        if (!StringUtils.hasLength(placeholderSeparator)) {
            throw new FlywayException("placeholderSeparator cannot be empty!", ErrorCode.CONFIGURATION);
        }
        getModernFlyway().setPlaceholderSeparator(placeholderSeparator);
    }

    /**
     * Sets the suffix of every placeholder.
     *
     * @param scriptPlaceholderSuffix The suffix of every placeholder. (default: __ )
     */
    public void setScriptPlaceholderSuffix(String scriptPlaceholderSuffix) {
        if (!StringUtils.hasLength(scriptPlaceholderSuffix)) {
            throw new FlywayException("scriptPlaceholderSuffix cannot be empty!", ErrorCode.CONFIGURATION);
        }
        getModernFlyway().setScriptPlaceholderSuffix(scriptPlaceholderSuffix);
    }

    /**
     * Sets the file name prefix for sql migrations.
     * SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to V1_1__My_description.sql
     *
     * @param sqlMigrationPrefix The file name prefix for sql migrations (default: V)
     */
    public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
        getModernFlyway().setSqlMigrationPrefix(sqlMigrationPrefix);
    }

    /**
     * Sets the file name prefix for undo SQL migrations. (default: U)
     * Undo SQL migrations are responsible for undoing the effects of the versioned migration with the same version.</p>
     * They have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to U1.1__My_description.sql
     * <<i>Flyway Teams only</i>
     *
     * @param undoSqlMigrationPrefix The file name prefix for undo SQL migrations. (default: U)
     */
    public void setUndoSqlMigrationPrefix(String undoSqlMigrationPrefix) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("undoSqlMigrationPrefix");




    }

    /**
     * The manually added Java-based migrations. These are not Java-based migrations discovered through classpath
     * scanning and instantiated by Flyway. Instead these are manually added instances of JavaMigration.
     * This is particularly useful when working with a dependency injection container, where you may want the DI
     * container to instantiate the class and wire up its dependencies for you.
     *
     * @param javaMigrations The manually added Java-based migrations. An empty array if none. (default: none)
     */
    public void setJavaMigrations(JavaMigration... javaMigrations) {
        if (javaMigrations == null) {
            throw new FlywayException("javaMigrations cannot be null", ErrorCode.CONFIGURATION);
        }
        this.javaMigrations = javaMigrations;
    }

    /**
     * Whether to stream SQL migrations when executing them. Streaming doesn't load the entire migration in memory at
     * once. Instead each statement is loaded individually. This is particularly useful for very large SQL migrations
     * composed of multiple MB or even GB of reference data, as this dramatically reduces Flyway's memory consumption.
     * <i>Flyway Teams only</i>
     *
     * @param stream {@code true} to stream SQL migrations. {@code false} to fully loaded them in memory instead. (default: {@code false})
     */
    public void setStream(boolean stream) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("stream");




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
    public void setBatch(boolean batch) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("batch");




    }

    /**
     * Sets the file name separator for sql migrations.
     * SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix,
     * which using the defaults translates to V1_1__My_description.sql
     *
     * @param sqlMigrationSeparator The file name separator for sql migrations (default: __)
     */
    public void setSqlMigrationSeparator(String sqlMigrationSeparator) {
        if (!StringUtils.hasLength(sqlMigrationSeparator)) {
            throw new FlywayException("sqlMigrationSeparator cannot be empty!", ErrorCode.CONFIGURATION);
        }

        getModernFlyway().setSqlMigrationSeparator(sqlMigrationSeparator);
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
    public void setSqlMigrationSuffixes(String... sqlMigrationSuffixes) {
        getModernFlyway().setSqlMigrationSuffixes(Arrays.stream(sqlMigrationSuffixes).collect(Collectors.toList()));
    }

    /**
     * Sets the datasource to use. Must have the necessary privileges to execute DDL.
     * To use a custom ClassLoader, setClassLoader() must be called prior to calling this method.
     *
     * @param url      The JDBC URL of the database.
     * @param user     The user of the database.
     * @param password The password of the database.
     */
    public void setDataSource(String url, String user, String password) {
        getCurrentUnresolvedEnvironment().setUrl(url);
        getCurrentUnresolvedEnvironment().setUser(user);
        getCurrentUnresolvedEnvironment().setPassword(password);

        this.dataSource = new DriverDataSource(classLoader, null, url, user, password, this);
    }

    /**
     * The maximum number of retries when attempting to connect to the database. After each failed attempt, Flyway will
     * wait 1 second before attempting to connect again, up to the maximum number of times specified by connectRetries.
     * The interval between retries doubles with each subsequent attempt.
     *
     * @param connectRetries The maximum number of retries (default: 0).
     */
    public void setConnectRetries(int connectRetries) {
        if (connectRetries < 0) {
            throw new FlywayException("Invalid number of connectRetries (must be 0 or greater): " + connectRetries, ErrorCode.CONFIGURATION);
        }
        getCurrentUnresolvedEnvironment().setConnectRetries(connectRetries);
    }

    /**
     * The maximum time between retries when attempting to connect to the database in seconds. This will cap the interval
     * between connect retry to the value provided.
     *
     * @param connectRetriesInterval The maximum time between retries in seconds (default: 120).
     */
    public void setConnectRetriesInterval(int connectRetriesInterval) {
        if (connectRetriesInterval < 0) {
            throw new FlywayException("Invalid number for connectRetriesInterval (must be 0 or greater): " + connectRetriesInterval, ErrorCode.CONFIGURATION);
        }
        getCurrentUnresolvedEnvironment().setConnectRetriesInterval(connectRetriesInterval);
    }

    /**
     * Sets the version to tag an existing schema with when executing baseline.
     *
     * @param baselineVersion The version to tag an existing schema with when executing baseline. (default: 1)
     */
    public void setBaselineVersionAsString(String baselineVersion) {
        setBaselineVersion(baselineVersion);
    }

    /**
     * Whether Flyway should skip actually executing the contents of the migrations and only update the schema history table.
     * This should be used when you have applied a migration manually (via executing the sql yourself, or via an IDE), and
     * just want the schema history table to reflect this.
     *
     * Use in conjunction with {@code cherryPick} to skip specific migrations instead of all pending ones.
     *
     * <i>Flyway Teams only</i>
     */
    public void setSkipExecutingMigrations(boolean skipExecutingMigrations) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("skipExecutingMigrations");




    }

    public void setCallbacks(Callback... callbacks) {
        this.callbacks.clear();
        this.callbacks.addAll(Arrays.asList(callbacks));
    }

    /**
     * Set the callbacks for lifecycle notifications.
     *
     * @param callbacks The fully qualified class names, or full qualified package to scan, of the callbacks for lifecycle notifications. (default: none)
     */
    public void setCallbacksAsClassNames(String... callbacks) {
        getModernFlyway().setCallbacks(Arrays.stream(callbacks).collect(Collectors.toList()));
        this.callbacks.clear();
        for (String callback : getModernFlyway().getCallbacks()) {
            this.callbacks.addAll(loadCallbackPath(callback));
        }
    }

    /**
     * Load this callback path as a class if it exists, else scan this location for classes that implement Callback.
     *
     * @param callbackPath The path to load or scan.
     */
    private List<Callback> loadCallbackPath(String callbackPath) {
        List<Callback> callbacks = new ArrayList<>();
        // try to load it as a classname
        Object o = null;
        try {
            o = ClassUtils.instantiate(callbackPath, classLoader);
        } catch (FlywayException ex) {
            // If the path failed to load, assume it points to a package instead.
        }

        if (o != null) {
            // If we have a non-null o, check that it inherits from the right interface
            if (o instanceof Callback) {
                callbacks.add((Callback) o);
            } else {
                throw new FlywayException("Invalid callback: " + callbackPath + " (must implement org.flywaydb.core.api.callback.Callback)", ErrorCode.CONFIGURATION);
            }
        } else {
            // else try to scan this location and load all callbacks found within
            callbacks.addAll(loadCallbackLocation(callbackPath, true));
        }

        return callbacks;
    }

    /**
     * Scan this location for classes that implement Callback.
     *
     * @param path            The path to scan.
     * @param errorOnNotFound Whether to show an error if the location is not found.
     */
    public List<Callback> loadCallbackLocation(String path, boolean errorOnNotFound) {
        List<Callback> callbacks = new ArrayList<>();
        List<String> callbackClasses = classScanner.scanForType(path, Callback.class, errorOnNotFound);
        for (String callback : callbackClasses) {
            Class<? extends Callback> callbackClass;
            try {
                callbackClass = ClassUtils.loadClass(Callback.class, callback, classLoader);
            } catch (Throwable e) {
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                LOG.warn("Skipping " + Callback.class + ": " + ClassUtils.formatThrowable(e) + (
                        rootCause == e
                                ? ""
                                : " caused by " + ClassUtils.formatThrowable(rootCause)
                                + " at " + ExceptionUtils.getThrowLocation(rootCause)
                ));
                callbackClass = null;
            }
            if (callbackClass != null) { // Filter out abstract classes
                Callback callbackObj = ClassUtils.instantiate(callback, classLoader);
                callbacks.add(callbackObj);
            }
        }

        return callbacks;
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
        getModernFlyway().setMigrationResolvers(Arrays.stream(resolvers).collect(Collectors.toList()));
    }

    /**
     * Whether Flyway's support for Oracle SQL*Plus commands should be activated.
     * <i>Flyway Teams only</i>
     *
     * @param oracleSqlplus {@code true} to active SQL*Plus support. {@code false} to fail fast instead. (default: {@code false})
     */
    public void setOracleSqlplus(boolean oracleSqlplus) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("oracle.sqlplus");




    }

    /**
     * Whether Flyway should issue a warning instead of an error whenever it encounters an Oracle SQL*Plus statementit doesn't yet support.
     * <i>Flyway Teams only</i>
     *
     * @param oracleSqlplusWarn {@code true} to issue a warning. {@code false} to fail fast instead. (default: {@code false})
     */
    public void setOracleSqlplusWarn(boolean oracleSqlplusWarn) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("oracle.sqlplusWarn");




    }

    /**
     * When Oracle needs to connect to a Kerberos service to authenticate, the location of the Kerberos cache.
     * <i>Flyway Teams only</i>
     */
    public void setOracleKerberosCacheFile(String oracleKerberosCacheFile) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("oracle.kerberosCacheFile");




    }

    /**
     * When connecting to a Kerberos service to authenticate, the path to the Kerberos config file.
     * <i>Flyway Teams only</i>
     */
    public void setKerberosConfigFile(String kerberosConfigFile) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("kerberosConfigFile");




    }

    /**
     * The location of your Oracle wallet, used to automatically sign in to your databases.
     *
     * <i>Flyway Teams only</i>
     *
     * @param oracleWalletLocation The path to your Oracle Wallet
     */
    public void setOracleWalletLocation(String oracleWalletLocation) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("oracle.walletLocation");




    }

    /**
     * Whether Flyway should attempt to create the schemas specified in the schemas property.
     *
     * @param createSchemas @{code true} to attempt to create the schemas (default: {@code true})
     */
    public void setShouldCreateSchemas(boolean createSchemas) {
        getModernFlyway().setCreateSchemas(createSchemas);
    }

    /**
     * Your Flyway license key (FL01...). Not yet a Flyway Teams Edition customer?
     * Request your <a href="https://flywaydb.org/download">Flyway trial license key</a>
     * to try out Flyway Teams Edition features free for 30 days.
     *
     * <i>Flyway Teams only</i>
     */
    public void setLicenseKey(String licenseKey) {

         LOG.warn("License key detected - in order to use Teams or Enterprise features, download " + org.flywaydb.core.internal.license.Edition.ENTERPRISE + " & " + org.flywaydb.core.internal.license.Edition.TIER3 + " here: " +  org.flywaydb.core.internal.util.FlywayDbWebsiteLinks.TEAMS_ENTERPRISE_DOWNLOAD);




    }

    /**
     * Whether Flyway should output a table with the results of queries when executing migrations.
     * <i>Flyway Teams only</i>
     *
     * @return {@code true} to output the results table (default: {@code true})
     */
    public void setOutputQueryResults(boolean outputQueryResults) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("outputQueryResults");




    }

    /**
     * Properties to pass to the JDBC driver object.
     * <i>Flyway Teams only</i>
     */
    public void setJdbcProperties(Map<String, String> jdbcProperties) {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("jdbcProperties");




    }

    /**
     * Configure with the same values as this existing configuration.
     */
    public void configure(Configuration configuration) {
        setModernConfig(ConfigurationModel.clone(configuration.getModernConfig()));

        setJavaMigrations(configuration.getJavaMigrations());
        setResourceProvider(configuration.getResourceProvider());
        setJavaMigrationClassProvider(configuration.getJavaMigrationClassProvider());
        setCallbacks(configuration.getCallbacks());

        List<MigrationResolver> migrationResolvers = new ArrayList<>(Arrays.asList(configuration.getResolvers()));
        if (getModernFlyway().getMigrationResolvers() != null) {
            String[] resolversFromConfig = getModernFlyway().getMigrationResolvers().toArray(new String[0]);
            List<MigrationResolver> resolverList = ClassUtils.instantiateAll(resolversFromConfig, classLoader);
            migrationResolvers.addAll(resolverList);
        }

        setResolvers(migrationResolvers.toArray(new MigrationResolver[0]));

        getModernFlyway().setMigrationResolvers(null);

        dataSource = configuration.getDataSource();
        pluginRegister = configuration.getPluginRegister();




        configureFromConfigurationProviders(this);
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration. Properties are documented
     * here: https://flywaydb.org/documentation/configuration/parameters/
     * <p>To use a custom ClassLoader, setClassLoader() must be called prior to calling this method.</p>
     *
     * @param properties Properties used for configuration.
     *
     * @throws FlywayException when the configuration failed.
     */
    public void configure(Properties properties) {
        configure(ConfigUtils.propertiesToMap(properties));
    }

    public void setUrl(String url) {
        this.dataSource = null;
        getCurrentUnresolvedEnvironment().setUrl(url);
        this.resolvedEnvironments.clear();
    }

    public void setUser(String user) {
        this.dataSource = null;
        getCurrentUnresolvedEnvironment().setUser(user);
        this.resolvedEnvironments.clear();
    }

    public void setPassword(String password) {
        this.dataSource = null;
        getCurrentUnresolvedEnvironment().setPassword(password);
        this.resolvedEnvironments.clear();
    }

    public void setDriver(String driver) {
        this.dataSource = null;
        getCurrentUnresolvedEnvironment().setDriver(driver);
        this.resolvedEnvironments.clear();
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration. Properties are documented
     * here: https://flywaydb.org/documentation/configuration/parameters/
     * <p>To use a custom ClassLoader, it must be passed to the Flyway constructor prior to calling this method.</p>
     *
     * @param props Properties used for configuration.
     *
     * @throws FlywayException when the configuration failed.
     */

    public void configure(Map<String, String> props) {
        // Make copy to prevent removing elements from the original.
        Map<String, String> tempProps = new HashMap<>(props);

        for (String key : props.keySet()) {
            if (key.startsWith("environments." + TEMP_ENVIRONMENT_NAME)) {
                tempProps.put(key.replace("environments." + TEMP_ENVIRONMENT_NAME, "flyway"), props.get(key));
            }
        }

        props = new HashMap<>(tempProps);

        for (ConfigurationExtension configurationExtension : pluginRegister.getPlugins(ConfigurationExtension.class)) {
            configurationExtension.extractParametersFromConfiguration(props);
        }

        String driverProp = props.remove(ConfigUtils.DRIVER);
        if (driverProp != null) {
            setDriver(driverProp);
        }
        String urlProp = props.remove(ConfigUtils.URL);
        if (urlProp != null) {
            setUrl(urlProp);
        }
        String userProp = props.remove(ConfigUtils.USER);
        if (userProp != null) {
            setUser(userProp);
        }
        String passwordProp = props.remove(ConfigUtils.PASSWORD);
        if (passwordProp != null) {
            setPassword(passwordProp);
        }
        Integer connectRetriesProp = removeInteger(props, ConfigUtils.CONNECT_RETRIES);
        if (connectRetriesProp != null) {
            setConnectRetries(connectRetriesProp);
        }
        Integer connectRetriesIntervalProp = removeInteger(props, ConfigUtils.CONNECT_RETRIES_INTERVAL);
        if (connectRetriesIntervalProp != null) {
            setConnectRetriesInterval(connectRetriesIntervalProp);
        }
        String initSqlProp = props.remove(ConfigUtils.INIT_SQL);
        if (initSqlProp != null) {
            setInitSql(initSqlProp);
        }
        String locationsProp = props.remove(ConfigUtils.LOCATIONS);
        if (locationsProp != null) {
            setLocationsAsStrings(StringUtils.tokenizeToStringArray(locationsProp, ","));
        }
        Boolean placeholderReplacementProp = removeBoolean(props, ConfigUtils.PLACEHOLDER_REPLACEMENT);
        if (placeholderReplacementProp != null) {
            setPlaceholderReplacement(placeholderReplacementProp);
        }
        String placeholderPrefixProp = props.remove(ConfigUtils.PLACEHOLDER_PREFIX);
        if (placeholderPrefixProp != null) {
            setPlaceholderPrefix(placeholderPrefixProp);
        }
        String placeholderSuffixProp = props.remove(ConfigUtils.PLACEHOLDER_SUFFIX);
        if (placeholderSuffixProp != null) {
            setPlaceholderSuffix(placeholderSuffixProp);
        }
        String placeholderSeparatorProp = props.remove(ConfigUtils.PLACEHOLDER_SEPARATOR);
        if (placeholderSeparatorProp != null) {
            setPlaceholderSeparator(placeholderSeparatorProp);
        }
        String scriptPlaceholderPrefixProp = props.remove(ConfigUtils.SCRIPT_PLACEHOLDER_PREFIX);
        if (scriptPlaceholderPrefixProp != null) {
            setScriptPlaceholderPrefix(scriptPlaceholderPrefixProp);
        }
        String scriptPlaceholderSuffixProp = props.remove(ConfigUtils.SCRIPT_PLACEHOLDER_SUFFIX);
        if (scriptPlaceholderSuffixProp != null) {
            setScriptPlaceholderSuffix(scriptPlaceholderSuffixProp);
        }
        String sqlMigrationPrefixProp = props.remove(ConfigUtils.SQL_MIGRATION_PREFIX);
        if (sqlMigrationPrefixProp != null) {
            setSqlMigrationPrefix(sqlMigrationPrefixProp);
        }
        String undoSqlMigrationPrefixProp = props.remove(ConfigUtils.UNDO_SQL_MIGRATION_PREFIX);
        if (undoSqlMigrationPrefixProp != null) {
            setUndoSqlMigrationPrefix(undoSqlMigrationPrefixProp);
        }
        String repeatableSqlMigrationPrefixProp = props.remove(ConfigUtils.REPEATABLE_SQL_MIGRATION_PREFIX);
        if (repeatableSqlMigrationPrefixProp != null) {
            setRepeatableSqlMigrationPrefix(repeatableSqlMigrationPrefixProp);
        }
        String sqlMigrationSeparatorProp = props.remove(ConfigUtils.SQL_MIGRATION_SEPARATOR);
        if (sqlMigrationSeparatorProp != null) {
            setSqlMigrationSeparator(sqlMigrationSeparatorProp);
        }
        String sqlMigrationSuffixesProp = props.remove(ConfigUtils.SQL_MIGRATION_SUFFIXES);
        if (sqlMigrationSuffixesProp != null) {
            setSqlMigrationSuffixes(StringUtils.tokenizeToStringArray(sqlMigrationSuffixesProp, ","));
        }
        String encodingProp = props.remove(ConfigUtils.ENCODING);
        if (encodingProp != null) {
            setEncodingAsString(encodingProp);
        }
        Boolean detectEncoding = removeBoolean(props, ConfigUtils.DETECT_ENCODING);
        if (detectEncoding != null) {
            setDetectEncoding(detectEncoding);
        }
        Boolean executeInTransaction = removeBoolean(props, ConfigUtils.EXECUTE_IN_TRANSACTION);
        if (executeInTransaction != null) {
            setExecuteInTransaction(executeInTransaction);
        }
        String defaultSchemaProp = props.remove(ConfigUtils.DEFAULT_SCHEMA);
        if (defaultSchemaProp != null) {
            setDefaultSchema(defaultSchemaProp);
        }
        String schemasProp = props.remove(ConfigUtils.SCHEMAS);
        if (schemasProp != null) {
            setSchemas(StringUtils.tokenizeToStringArray(schemasProp, ","));
        }
        String tableProp = props.remove(ConfigUtils.TABLE);
        if (tableProp != null) {
            setTable(tableProp);
        }
        String tablespaceProp = props.remove(ConfigUtils.TABLESPACE);
        if (tablespaceProp != null) {
            setTablespace(tablespaceProp);
        }
        Boolean cleanOnValidationErrorProp = removeBoolean(props, ConfigUtils.CLEAN_ON_VALIDATION_ERROR);
        if (cleanOnValidationErrorProp != null) {
            setCleanOnValidationError(cleanOnValidationErrorProp);
        }
        Boolean cleanDisabledProp = removeBoolean(props, ConfigUtils.CLEAN_DISABLED);
        if (cleanDisabledProp != null) {
            setCleanDisabled(cleanDisabledProp);
        }
        Boolean validateOnMigrateProp = removeBoolean(props, ConfigUtils.VALIDATE_ON_MIGRATE);
        if (validateOnMigrateProp != null) {
            setValidateOnMigrate(validateOnMigrateProp);
        }
        String baselineVersionProp = props.remove(ConfigUtils.BASELINE_VERSION);
        if (baselineVersionProp != null) {
            setBaselineVersion(baselineVersionProp);
        }
        String baselineDescriptionProp = props.remove(ConfigUtils.BASELINE_DESCRIPTION);
        if (baselineDescriptionProp != null) {
            setBaselineDescription(baselineDescriptionProp);
        }
        Boolean baselineOnMigrateProp = removeBoolean(props, ConfigUtils.BASELINE_ON_MIGRATE);
        if (baselineOnMigrateProp != null) {
            setBaselineOnMigrate(baselineOnMigrateProp);
        }
        Boolean validateMigrationNamingProp = removeBoolean(props, ConfigUtils.VALIDATE_MIGRATION_NAMING);
        if (validateMigrationNamingProp != null) {
            setValidateMigrationNaming(validateMigrationNamingProp);
        }
        String targetProp = props.remove(ConfigUtils.TARGET);
        if (targetProp != null) {
            setTargetAsString(targetProp);
        }
        String cherryPickProp = props.remove(ConfigUtils.CHERRY_PICK);
        if (cherryPickProp != null) {
            setCherryPick(StringUtils.tokenizeToStringArray(cherryPickProp, ","));
        }
        String loggersProp = props.remove(ConfigUtils.LOGGERS);
        if (loggersProp != null) {
            setLoggers(StringUtils.tokenizeToStringArray(loggersProp, ","));
        }
        Integer lockRetryCount = removeInteger(props, ConfigUtils.LOCK_RETRY_COUNT);
        if (lockRetryCount != null) {
            setLockRetryCount(lockRetryCount);
        }
        Boolean outOfOrderProp = removeBoolean(props, ConfigUtils.OUT_OF_ORDER);
        if (outOfOrderProp != null) {
            setOutOfOrder(outOfOrderProp);
        }
        Boolean skipExecutingMigrationsProp = removeBoolean(props, ConfigUtils.SKIP_EXECUTING_MIGRATIONS);
        if (skipExecutingMigrationsProp != null) {
            setSkipExecutingMigrations(skipExecutingMigrationsProp);
        }
        Boolean outputQueryResultsProp = removeBoolean(props, ConfigUtils.OUTPUT_QUERY_RESULTS);
        if (outputQueryResultsProp != null) {
            setOutputQueryResults(outputQueryResultsProp);
        }
        String resolversProp = props.remove(ConfigUtils.RESOLVERS);
        if (StringUtils.hasLength(resolversProp)) {
            setResolversAsClassNames(StringUtils.tokenizeToStringArray(resolversProp, ","));
        }
        Boolean skipDefaultResolversProp = removeBoolean(props, ConfigUtils.SKIP_DEFAULT_RESOLVERS);
        if (skipDefaultResolversProp != null) {
            setSkipDefaultResolvers(skipDefaultResolversProp);
        }
        String callbacksProp = props.remove(ConfigUtils.CALLBACKS);
        if (StringUtils.hasLength(callbacksProp)) {
            setCallbacksAsClassNames(StringUtils.tokenizeToStringArray(callbacksProp, ","));
        }
        Boolean skipDefaultCallbacksProp = removeBoolean(props, ConfigUtils.SKIP_DEFAULT_CALLBACKS);
        if (skipDefaultCallbacksProp != null) {
            setSkipDefaultCallbacks(skipDefaultCallbacksProp);
        }
        Map<String, String> placeholdersFromProps = getPropertiesUnderNamespace(props, getPlaceholders(), ConfigUtils.PLACEHOLDERS_PROPERTY_PREFIX);
        setPlaceholders(placeholdersFromProps);
        Boolean mixedProp = removeBoolean(props, ConfigUtils.MIXED);
        if (mixedProp != null) {
            setMixed(mixedProp);
        }
        Boolean groupProp = removeBoolean(props, ConfigUtils.GROUP);
        if (groupProp != null) {
            setGroup(groupProp);
        }
        String installedByProp = props.remove(ConfigUtils.INSTALLED_BY);
        if (installedByProp != null) {
            setInstalledBy(installedByProp);
        }
        String dryRunOutputProp = props.remove(ConfigUtils.DRYRUN_OUTPUT);
        if (dryRunOutputProp != null) {
            setDryRunOutputAsFileName(dryRunOutputProp);
        }
        String errorOverridesProp = props.remove(ConfigUtils.ERROR_OVERRIDES);
        if (errorOverridesProp != null) {
            setErrorOverrides(StringUtils.tokenizeToStringArray(errorOverridesProp, ","));
        }
        Boolean streamProp = removeBoolean(props, ConfigUtils.STREAM);
        if (streamProp != null) {
            setStream(streamProp);
        }
        Boolean batchProp = removeBoolean(props, ConfigUtils.BATCH);
        if (batchProp != null) {
            setBatch(batchProp);
        }
        Boolean oracleSqlplusProp = removeBoolean(props, ConfigUtils.ORACLE_SQLPLUS);
        if (oracleSqlplusProp != null) {
            setOracleSqlplus(oracleSqlplusProp);
        }
        Boolean oracleSqlplusWarnProp = removeBoolean(props, ConfigUtils.ORACLE_SQLPLUS_WARN);
        if (oracleSqlplusWarnProp != null) {
            setOracleSqlplusWarn(oracleSqlplusWarnProp);
        }
        Boolean createSchemasProp = removeBoolean(props, ConfigUtils.CREATE_SCHEMAS);
        if (createSchemasProp != null) {
            setShouldCreateSchemas(createSchemasProp);
        }
        String kerberosConfigFile = props.remove(ConfigUtils.KERBEROS_CONFIG_FILE);
        if (kerberosConfigFile != null) {
            setKerberosConfigFile(kerberosConfigFile);
        }
        String oracleKerberosCacheFile = props.remove(ConfigUtils.ORACLE_KERBEROS_CACHE_FILE);
        if (oracleKerberosCacheFile != null) {
            setOracleKerberosCacheFile(oracleKerberosCacheFile);
        }

        String oracleWalletLocationProp = props.remove(ConfigUtils.ORACLE_WALLET_LOCATION);
        if (oracleWalletLocationProp != null) {
            setOracleWalletLocation(oracleWalletLocationProp);
        }
        String ignoreMigrationPatternsProp = props.remove(ConfigUtils.IGNORE_MIGRATION_PATTERNS);
        if (ignoreMigrationPatternsProp != null) {
            setIgnoreMigrationPatterns(StringUtils.tokenizeToStringArray(ignoreMigrationPatternsProp, ","));
        }
        String licenseKeyProp = props.remove(ConfigUtils.LICENSE_KEY);
        if (licenseKeyProp != null) {
            setLicenseKey(licenseKeyProp);
        }
        Boolean failOnMissingLocationsProp = removeBoolean(props, ConfigUtils.FAIL_ON_MISSING_LOCATIONS);
        if (failOnMissingLocationsProp != null) {
            setFailOnMissingLocations(failOnMissingLocationsProp);
        }

        if (StringUtils.hasText(getCurrentResolvedEnvironment().getUrl()) && (dataSource == null || StringUtils.hasText(urlProp) || StringUtils.hasText(driverProp) || StringUtils.hasText(userProp) || StringUtils.hasText(passwordProp))) {
            Map<String, String> jdbcPropertiesFromProps = getPropertiesUnderNamespace(props, getPlaceholders(), ConfigUtils.JDBC_PROPERTIES_PREFIX);
            setDataSource(new DriverDataSource(classLoader, getCurrentResolvedEnvironment().getDriver(), getCurrentResolvedEnvironment().getUrl(), getCurrentResolvedEnvironment().getUser(), getCurrentResolvedEnvironment().getPassword(), this, jdbcPropertiesFromProps));
        }

        ConfigUtils.checkConfigurationForUnrecognisedProperties(props, "flyway.");
    }

    public void setFailOnMissingLocations(Boolean failOnMissingLocationsProp) {
        getModernFlyway().setFailOnMissingLocations(failOnMissingLocationsProp);
    }

    public String getDriver() {
        return getCurrentResolvedEnvironment().getDriver();
    }

    public void setGroup(Boolean groupProp) {
        getModernFlyway().setGroup(groupProp);
    }

    public void setMixed(Boolean mixedProp) {
        getModernFlyway().setMixed(mixedProp);
    }

    public void setEncoding(Charset encoding) {
        getModernFlyway().setEncoding(encoding.name());
    }

    public void setPlaceholders(Map<String, String> placeholdersFromProps) {
        getModernFlyway().setPlaceholders(placeholdersFromProps);
    }

    public void setSkipDefaultCallbacks(Boolean skipDefaultCallbacksProp) {
        getModernFlyway().setSkipDefaultCallbacks(skipDefaultCallbacksProp);
    }

    public void setSkipDefaultResolvers(Boolean skipDefaultResolversProp) {
        getModernFlyway().setSkipDefaultResolvers(skipDefaultResolversProp);
    }

    public void setOutOfOrder(Boolean outOfOrderProp) {
        getModernFlyway().setOutOfOrder(outOfOrderProp);
    }

    public void setLockRetryCount(Integer lockRetryCount) {
        getModernFlyway().setLockRetryCount(lockRetryCount);
    }

    public void setValidateMigrationNaming(Boolean validateMigrationNamingProp) {
        getModernFlyway().setValidateMigrationNaming(validateMigrationNamingProp);
    }

    public void setBaselineOnMigrate(Boolean baselineOnMigrateProp) {
        getModernFlyway().setBaselineOnMigrate(baselineOnMigrateProp);
    }

    public void setBaselineDescription(String baselineDescriptionProp) {
        getModernFlyway().setBaselineDescription(baselineDescriptionProp);
    }

    public void setBaselineVersion(String baselineVersionProp) {
        getModernFlyway().setBaselineVersion(baselineVersionProp);
    }

    public void setBaselineVersion(MigrationVersion baselineVersion) {
        getModernFlyway().setBaselineVersion(baselineVersion.getVersion());
    }

    public void setValidateOnMigrate(Boolean validateOnMigrateProp) {
        getModernFlyway().setValidateOnMigrate(validateOnMigrateProp);
    }

    public void setCleanDisabled(Boolean cleanDisabledProp) {
        getModernFlyway().setCleanDisabled(cleanDisabledProp);
    }

    public void setCleanOnValidationError(Boolean cleanOnValidationErrorProp) {
        getModernFlyway().setCleanOnValidationError(cleanOnValidationErrorProp);
    }

    public void setTablespace(String tablespaceProp) {
        getModernFlyway().setTablespace(tablespaceProp);
    }

    public void setTable(String tableProp) {
        getModernFlyway().setTable(tableProp);
    }

    public void setSchemas(String[] tokenizeToStringArray) {
        List<String> schemas = Arrays.stream(tokenizeToStringArray).collect(Collectors.toList());
        getCurrentUnresolvedEnvironment().setSchemas(schemas);
        resolvedEnvironments.clear();
    }

    public void setRepeatableSqlMigrationPrefix(String repeatableSqlMigrationPrefixProp) {
        getModernFlyway().setRepeatableSqlMigrationPrefix(repeatableSqlMigrationPrefixProp);
    }

    public void setPlaceholderReplacement(Boolean placeholderReplacementProp) {
        getModernFlyway().setPlaceholderReplacement(placeholderReplacementProp);
    }

    public void setInitSql(String initSqlProp) {
        getCurrentUnresolvedEnvironment().setInitSql(initSqlProp);
        resolvedEnvironments.clear();
    }

    private void configureFromConfigurationProviders(ClassicConfiguration configuration) {
        Map<String, String> config = new HashMap<>();
        for (ConfigurationProvider configurationProvider : pluginRegister.getPlugins(ConfigurationProvider.class)) {
            ConfigurationExtension configurationExtension = (ConfigurationExtension) pluginRegister.getPlugin(configurationProvider.getConfigurationExtensionClass());
            try {
                config.putAll(configurationProvider.getConfiguration(configurationExtension, configuration));
            } catch (Exception e) {
                throw new FlywayException("Unable to read configuration from " + configurationProvider.getClass().getName() + ": " + e.getMessage());
            }
        }
        configure(config);
    }

    private Map<String, String> getPropertiesUnderNamespace(Map<String, String> properties, Map<String, String> current, String namespace) {
        Iterator<Map.Entry<String, String>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String propertyName = entry.getKey();

            if (propertyName.startsWith(namespace) && propertyName.length() > namespace.length()) {
                String placeholderName = propertyName.substring(namespace.length());
                String placeholderValue = entry.getValue();
                current.put(placeholderName, placeholderValue);
                iterator.remove();
            }
        }
        return current;
    }

    /**
     * Configures Flyway using FLYWAY_* environment variables.
     */
    public void configureUsingEnvVars() {
        configure(ConfigUtils.environmentVariablesToPropertyMap());
    }

    public void setTarget(MigrationVersion target) {
        if (target != null) {
            getModernFlyway().setTarget(target.getName());
        } else {
            getModernFlyway().setTarget("latest");
        }
    }
}