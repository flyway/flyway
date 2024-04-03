/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.ProgressLoggerJson;
import org.flywaydb.core.ProgressLoggerEmpty;
import org.flywaydb.core.api.*;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.extensibility.ConfigurationProvider;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.configuration.ConfigUtils;

import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.configuration.models.DataSourceModel;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;
import org.flywaydb.core.internal.configuration.models.FlywayModel;
import org.flywaydb.core.internal.configuration.models.ResolvedEnvironment;
import org.flywaydb.core.internal.configuration.resolvers.EnvironmentProvisioner;
import org.flywaydb.core.internal.configuration.resolvers.EnvironmentResolver;
import org.flywaydb.core.internal.configuration.resolvers.PropertyResolver;
import org.flywaydb.core.internal.configuration.resolvers.ProvisionerMode;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.DatabaseTypeRegister;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;
import org.flywaydb.core.internal.plugin.PluginRegister;
import org.flywaydb.core.internal.scanner.ClasspathClassScanner;
import org.flywaydb.core.internal.util.*;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;






import static org.flywaydb.core.internal.configuration.ConfigUtils.removeBoolean;
import static org.flywaydb.core.internal.configuration.ConfigUtils.removeInteger;

/**
 * JavaBean-style configuration for Flyway. This is primarily meant for compatibility with scenarios where the
 * new FluentConfiguration isn't an easy fit, such as Spring XML bean configuration.
 * <p>This configuration can then be passed to Flyway using the <code>new Flyway(Configuration)</code> constructor.</p>
 */
@CustomLog
@ExtensionMethod(Tier.class)
public class ClassicConfiguration implements Configuration {
    private static final Pattern ANY_WORD_BETWEEN_TWO_QUOTES_PATTERN = Pattern.compile("\"([^\"]*)\"");
    private static final Pattern ANY_WORD_BETWEEN_TWO_DOTS_PATTERN = Pattern.compile("\\.(.*?)\\.");

    public static final String TEMP_ENVIRONMENT_NAME = "tempConfigEnvironment";

    private final Map<String, DataSourceModel> dataSources = new HashMap<>();

    private EnvironmentResolver environmentResolver;
    private final Map<String, ResolvedEnvironment> resolvedEnvironments = new HashMap<>();

    @Getter
    @Setter
    private ConfigurationModel modernConfig = ConfigurationModel.defaults();

    @Getter
    @Setter
    private String workingDirectory;

    @Getter
    @Setter
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public DataSource getDataSource() {
        DataSourceModel model = dataSources.getOrDefault(getCurrentEnvironmentName(), null);
        if (model != null && model.getDataSource() != null) {
            return model.getDataSource();
        }

        if (StringUtils.hasText(getUrl())) {
            DataSource dataSource = new DriverDataSource(classLoader,
                getDriver(),
                getUrl(),
                getUser(),
                getPassword(),
                this,
                getJdbcProperties());

            dataSources.put(getCurrentEnvironmentName(), new DataSourceModel(dataSource, true));

            return dataSource;
        }

        return null;
    }

    public void setDefaultSchema(String defaultSchema) {
        getModernFlyway().setDefaultSchema(defaultSchema);
    }

    public String getDefaultSchema() {
        return getModernFlyway().getDefaultSchema();
    }

    public ResolvedEnvironment getCurrentResolvedEnvironment() {
        return getCurrentResolvedEnvironment(null);
    }

    public ResolvedEnvironment getCurrentResolvedEnvironment(ProgressLogger progress) {
        String envName = getCurrentEnvironmentName();

        String envProvisionMode = modernConfig.getFlyway().getEnvironmentProvisionMode();
        ProvisionerMode provisionerMode = StringUtils.hasText(envProvisionMode) ? ProvisionerMode.fromString(envProvisionMode) : ProvisionerMode.Provision;
        ResolvedEnvironment resolved = getResolvedEnvironment(envName, provisionerMode, progress);
        if (resolved == null) {
            throw new FlywayException("Environment '" + envName + "' not found. Check that this environment exists in your configuration.");
        }
        return resolved;
    }

    public ResolvedEnvironment getResolvedEnvironment(String envName) {
        return getResolvedEnvironment(envName, ProvisionerMode.Provision, null);
    }

    public ResolvedEnvironment getResolvedEnvironment(String envName, ProvisionerMode provisionerMode, ProgressLogger progress) {
        if (environmentResolver == null) {
            environmentResolver = new EnvironmentResolver(
                pluginRegister.getLicensedPlugins(PropertyResolver.class, this).stream().collect(Collectors.toMap(PropertyResolver::getName, x -> x)),
                pluginRegister.getLicensedPlugins(EnvironmentProvisioner.class, this).stream().collect(Collectors.toMap(EnvironmentProvisioner::getName, x -> x))
            );
        }

        if (!resolvedEnvironments.containsKey(envName) && getModernConfig().getEnvironments().containsKey(envName)) {
            EnvironmentModel unresolved = getModernConfig().getEnvironments().get(envName);
            ResolvedEnvironment resolved = environmentResolver.resolve(envName, unresolved, provisionerMode, this, progress == null ? createProgress("environment") : progress);
            resolvedEnvironments.put(envName, resolved);
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
    public boolean isReportEnabled() {
        return modernConfig.getFlyway().getReportEnabled() != null && modernConfig.getFlyway().getReportEnabled();
    }

    @Override
    public Charset getEncoding() {
        return Charset.forName(getModernFlyway().getEncoding());
    }

    @Override
    public boolean isDetectEncoding() {
        return getModernFlyway().getDetectEncoding();
    }

    @Override
    public String getReportFilename() {
        return getModernFlyway().getReportFilename();
    }

    @Override
    public Map<String, ResolvedEnvironment> getCachedResolvedEnvironments() {
        return Map.copyOf(resolvedEnvironments);
    }

    @Override
    public Map<String, DataSourceModel> getCachedDataSources() {
        return Map.copyOf(dataSources);
    }

    @Getter
    @Setter
    private ResourceProvider resourceProvider = null;
    @Getter
    @Setter
    private ClassProvider<JavaMigration> javaMigrationClassProvider = null;
    @Getter
    private JavaMigration[] javaMigrations = {};

    private MigrationResolver[] resolvers = {};

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
        loadUnloadedCallbacks();
        return callbacks.toArray(new Callback[0]);
    }

    @Override
    public MigrationResolver[] getResolvers() {
        if (getModernFlyway().getMigrationResolvers() != null) {
            List<MigrationResolver> migrationResolvers = new ArrayList<>(Arrays.asList(resolvers));
            migrationResolvers.addAll(
                getModernFlyway().getMigrationResolvers()
                    .stream()
                    .filter(p -> Arrays.stream(resolvers).noneMatch(r -> r.getClass().getCanonicalName().equals(p)))
                    .map(p -> ClassUtils.<MigrationResolver>instantiate(p, classLoader)).toList()
            );
            setResolvers(migrationResolvers.toArray(new MigrationResolver[0]));
        }
        return resolvers;
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
        if (Arrays.equals(ignoreMigrationPatterns, new String[]{""})) {
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
    public boolean isCommunityDBSupportEnabled() {
        return getModernFlyway().getCommunityDBSupportEnabled();
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
    public String getKerberosConfigFile() {
        return getModernFlyway().getKerberosConfigFile();
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
    public String[] getLoggers() {
        return getModernFlyway().getLoggers().toArray(new String[0]);
    }

    @Override
    public int getConnectRetries() {
        final var connectionRetries = getCurrentResolvedEnvironment().getConnectRetries();
        return connectionRetries != null ? connectionRetries : 0;
    }

    @Override
    public int getConnectRetriesInterval() {
        final Integer connectionRetriesInterval = getCurrentResolvedEnvironment().getConnectRetriesInterval();
        return connectionRetriesInterval != null ? connectionRetriesInterval : 120;
    }

    @Override
    public String getInitSql() {
        return getCurrentResolvedEnvironment().getInitSql();
    }

    @Override
    public MigrationVersion getBaselineVersion() {

        return MigrationVersion.fromVersion(getModernFlyway().getBaselineVersion() != null ? getModernFlyway().getBaselineVersion() : "1");
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
            getModernFlyway().setFailOnMissingTarget(false);
            return MigrationVersion.fromVersion(target.substring(0, target.length() - 1));
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
        ConfigurationExtension cherryPickConfig = pluginRegister.getLicensedPlugin("CherryPickConfigurationExtension", this);

        if (cherryPickConfig == null) {
            LOG.debug("CherryPickConfigurationExtension not found");
            return null;
        }
        List<String> cherryPickList = (List<String>) ClassUtils.getFieldValue(cherryPickConfig, "cherryPick");

        if (cherryPickList != null) {
            cherryPick = cherryPickList.stream().map(MigrationPattern::new).toArray(MigrationPattern[]::new);
        }

        cherryPick = (cherryPick != null && cherryPick.length == 0) ? null : cherryPick;

        if (cherryPick == null) {
            return null;
        }

        Set<String> cherryPickValues = new HashSet<>();
        List<String> duplicateValues = new ArrayList<>();
        StringBuilder migrationPatternsString = new StringBuilder();

        for (MigrationPattern migrationPattern : cherryPick) {
            String migrationPatternString = migrationPattern.toString();
            migrationPatternsString.append(migrationPatternString).append(" ");

            if (cherryPickValues.contains(migrationPatternString)) {
                duplicateValues.add(migrationPatternString);
            }

            cherryPickValues.add(migrationPatternString);
        }

        if (!duplicateValues.isEmpty()) {
            throw new FlywayException("Duplicate values not allowed in migration patterns. Duplication detected in: \n" + migrationPatternsString);
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

        throw new org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException(Tier.TEAMS, LicenseGuard.getTier(this), "dryRunOutput");




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

        throw new org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException(Tier.TEAMS, LicenseGuard.getTier(this), "dryRunOutput");











































    }

    private OutputStream getDryRunOutputAsFile(File dryRunOutput) {

        throw new org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException(Tier.TEAMS, LicenseGuard.getTier(this), "dryRunOutput");










































    }

    /**
     * Sets the file where to output the SQL statements of a migration dry run. {@code null} to execute the SQL
     * statements directly against the database. If the file specified is in a non-existent directory, Flyway will
     * create all directories and parent directories as needed. Paths starting with s3: point to a bucket in AWS S3,
     * which must exist. They are in the format s3:<bucket>(/optionalfolder/subfolder)/filename.sql Paths starting with
     * gcs: point to a bucket in Google Cloud Storage, which must exist. They are in the format
     * gcs:<bucket>(/optionalfolder/subfolder)/filename.sql
     * <i>Flyway Teams only</i>
     *
     * @param dryRunOutputFileName The name of the output file or {@code null} to execute the SQL statements directly
     *                             against the database.
     */
    public void setDryRunOutputAsFileName(String dryRunOutputFileName) {

        throw new org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException(Tier.TEAMS, LicenseGuard.getTier(this), "dryRunOutput");




    }

    /**
     * Rules for the built-in error handler that let you override specific SQL states and errors codes in order to force
     * specific errors or warnings to be treated as debug messages, info messages, warnings or errors.
     * <p>Each error override has the following format: {@code STATE:12345:W}.
     * It is a 5 character SQL state (or * to match all SQL states), a colon, the SQL error code (or * to match all SQL
     * error codes), a colon and finally the desired behavior that should override the initial one.</p>
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

        throw new org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException(Tier.TEAMS, LicenseGuard.getTier(this), "errorOverrides");




    }

    /**
     * The username that will be recorded in the schema history table as having applied the migration.
     *
     * @param installedBy The username or {@code null} for the current database user of the connection. (default:
     *                    {@code null}).
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
     *     <li>slf4j: Use the slf4j logger</li>
     *     <li>log4j2: Use the log4j2 logger</li>
     *     <li>apache-commons: Use the Apache Commons logger</li>
     * </ul>
     * Alternatively you can provide the fully qualified class name for any other logger to use that.
     */
    public void setLoggers(String... loggers) {
        getModernFlyway().setLoggers(Arrays.stream(loggers).collect(Collectors.toList()));
    }

    /**
     * Ignore migrations that match this comma-separated list of patterns when validating migrations.
     * Each pattern is of the form <migration_type>:<migration_state>
     * See https://documentation.red-gate.com/fd/ignore-migration-patterns-184127507.html for full details
     * Example: repeatable:missing,versioned:pending,*:failed
     * <i>Flyway Teams only</i>
     */
    public void setIgnoreMigrationPatterns(String... ignoreMigrationPatterns) {
        getModernFlyway().setIgnoreMigrationPatterns(Arrays.stream(ignoreMigrationPatterns).collect(Collectors.toList()));
    }

    /**
     * Ignore migrations that match this array of ValidatePatterns when validating migrations.
     * See https://documentation.red-gate.com/fd/ignore-migration-patterns-184127507.html for full details
     * <i>Flyway Teams only</i>
     */
    public void setIgnoreMigrationPatterns(ValidatePattern... ignoreMigrationPatterns) {
        getModernFlyway().setIgnoreMigrationPatterns(Arrays.stream(ignoreMigrationPatterns).map(ValidatePattern::toString).collect(Collectors.toList()));
    }

    /**
     * Sets the locations to scan recursively for migrations. The location type is determined by its prefix. Unprefixed
     * locations or locations starting with {@code classpath:} point to a package on the classpath and may contain both
     * SQL and Java-based migrations. Locations starting with {@code filesystem:} point to a directory on the
     * filesystem, may only contain SQL migrations and are only scanned recursively down non-hidden directories.
     *
     * @param locations Locations to scan recursively for migrations. (default: db/migration)
     */
    public void setLocationsAsStrings(String... locations) {
        getModernFlyway().setLocations(Arrays.stream(locations).collect(Collectors.toList()));
    }

    /**
     * Sets the locations to scan recursively for migrations. The location type is determined by its prefix. Unprefixed
     * locations or locations starting with {@code classpath:} point to a package on the classpath and may contain both
     * SQL and Java-based migrations. Locations starting with {@code filesystem:} point to a directory on the
     * filesystem, may only contain SQL migrations and are only scanned recursively down non-hidden directories.
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
        getModernFlyway().setDetectEncoding(detectEncoding);
    }

    public void setReportFilename(String reportFilename) {
        getModernFlyway().setReportFilename(reportFilename);
    }

    public void setEnvironment(String environment) {
        if (modernConfig.getEnvironments().containsKey(environment)) {
            getModernFlyway().setEnvironment(environment);
        } else {
            throw new FlywayException("Environment '" + environment + "' not found");
        }
    }

    public void setEnvironmentProvisionMode(ProvisionerMode provisionerMode) {
        getModernFlyway().setEnvironmentProvisionMode(provisionerMode.toString());
    }

    public void setAllEnvironments(Map<String, EnvironmentModel> environments) {
        getModernConfig().setEnvironments(environments);
        resolvedEnvironments.clear();
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
     * Sets the target version up to which Flyway should consider migrations. Migrations with a higher version number
     * will be ignored. Special values:
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
     * Sets the prefix of every placeholder.
     *
     * @param placeholderPrefix The prefix of every placeholder. (default: ${ )
     */
    public void setPlaceholderPrefix(String placeholderPrefix) {
        if (!StringUtils.hasLength(placeholderPrefix)) {
            throw new FlywayException("placeholderPrefix cannot be empty!", CoreErrorCode.CONFIGURATION);
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
            throw new FlywayException("scriptPlaceholderPrefix cannot be empty!", CoreErrorCode.CONFIGURATION);
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
            throw new FlywayException("placeholderSuffix cannot be empty!", CoreErrorCode.CONFIGURATION);
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
            throw new FlywayException("placeholderSeparator cannot be empty!", CoreErrorCode.CONFIGURATION);
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
            throw new FlywayException("scriptPlaceholderSuffix cannot be empty!", CoreErrorCode.CONFIGURATION);
        }
        getModernFlyway().setScriptPlaceholderSuffix(scriptPlaceholderSuffix);
    }

    /**
     * Sets the file name prefix for sql migrations. SQL migrations have the following file name structure:
     * prefixVERSIONseparatorDESCRIPTIONsuffix, which using the defaults translates to V1_1__My_description.sql
     *
     * @param sqlMigrationPrefix The file name prefix for sql migrations (default: V)
     */
    public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
        getModernFlyway().setSqlMigrationPrefix(sqlMigrationPrefix);
    }

    /**
     * The manually added Java-based migrations. These are not Java-based migrations discovered through classpath
     * scanning and instantiated by Flyway. Instead these are manually added instances of JavaMigration. This is
     * particularly useful when working with a dependency injection container, where you may want the DI container to
     * instantiate the class and wire up its dependencies for you.
     *
     * @param javaMigrations The manually added Java-based migrations. An empty array if none. (default: none)
     */
    public void setJavaMigrations(JavaMigration... javaMigrations) {
        if (javaMigrations == null) {
            throw new FlywayException("javaMigrations cannot be null", CoreErrorCode.CONFIGURATION);
        }
        this.javaMigrations = javaMigrations;
    }

    /**
     * Whether to stream SQL migrations when executing them. Streaming doesn't load the entire migration in memory at
     * once. Instead each statement is loaded individually. This is particularly useful for very large SQL migrations
     * composed of multiple MB or even GB of reference data, as this dramatically reduces Flyway's memory consumption.
     * <i>Flyway Teams only</i>
     *
     * @param stream {@code true} to stream SQL migrations. {@code false} to fully loaded them in memory instead.
     *               (default: {@code false})
     */
    public void setStream(boolean stream) {
        getModernFlyway().setStream(stream);
    }

    /**
     * Whether to batch SQL statements when executing them. Batching can save up to 99 percent of network roundtrips by
     * sending up to 100 statements at once over the network to the database, instead of sending each statement
     * individually. This is particularly useful for very large SQL migrations composed of multiple MB or even GB of
     * reference data, as this can dramatically reduce the network overhead. This is supported for INSERT, UPDATE,
     * DELETE, MERGE and UPSERT statements. All other statements are automatically executed without batching.
     * <i>Flyway Teams only</i>
     *
     * @param batch {@code true} to batch SQL statements. {@code false} to execute them individually instead. (default:
     *              {@code false})
     */
    public void setBatch(boolean batch) {
        getModernFlyway().setBatch(batch);
    }

    /**
     * Sets the file name separator for sql migrations. SQL migrations have the following file name structure:
     * prefixVERSIONseparatorDESCRIPTIONsuffix, which using the defaults translates to V1_1__My_description.sql
     *
     * @param sqlMigrationSeparator The file name separator for sql migrations (default: __)
     */
    public void setSqlMigrationSeparator(String sqlMigrationSeparator) {
        if (!StringUtils.hasLength(sqlMigrationSeparator)) {
            throw new FlywayException("sqlMigrationSeparator cannot be empty!", CoreErrorCode.CONFIGURATION);
        }

        getModernFlyway().setSqlMigrationSeparator(sqlMigrationSeparator);
    }

    /**
     * The file name suffixes for SQL migrations. (default: .sql) SQL migrations have the following file name structure:
     * prefixVERSIONseparatorDESCRIPTIONsuffix, which using the defaults translates to V1_1__My_description.sql Multiple
     * suffixes (like .sql,.pkg,.pkb) can be specified for easier compatibility with other tools such as editors with
     * specific file associations.
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

        requestResolvedEnvironmentRefresh(getCurrentEnvironmentName());
        dataSources.put(getCurrentEnvironmentName(), new DataSourceModel(
            new DriverDataSource(classLoader,
                null,
                url,
                user,
                password,
                this,
                getCurrentUnresolvedEnvironment().getJdbcProperties()), true)
        );

        licenseGuardJdbcUrl(url);
    }

    public void setDataSource(DataSource dataSource) {
        dataSources.put(getCurrentEnvironmentName(), new DataSourceModel(dataSource, false));
    }

    public DatabaseType getDatabaseType() {
        String url = getUrl();
        DataSourceModel model = dataSources.getOrDefault(getCurrentEnvironmentName(), null);

        if (StringUtils.hasText(url)) {
            return DatabaseTypeRegister.getDatabaseTypeForUrl(url, this);
        } else if (model != null) {
            if (model.getDatabaseType() != null) {
                return model.getDatabaseType();
            }
            if (model.getDataSource() != null) {
                try (Connection connection = model.getDataSource().getConnection()) {
                    DatabaseType databaseType = DatabaseTypeRegister.getDatabaseTypeForConnection(connection, this);
                    model.setDatabaseType(databaseType);
                    return databaseType;
                } catch (SQLException ignored) {
                }
            }
        }

        return null;
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
            throw new FlywayException("Invalid number of connectRetries (must be 0 or greater): " + connectRetries, CoreErrorCode.CONFIGURATION);
        }
        getCurrentUnresolvedEnvironment().setConnectRetries(connectRetries);
        requestResolvedEnvironmentRefresh(getCurrentEnvironmentName());
    }

    /**
     * The maximum time between retries when attempting to connect to the database in seconds. This will cap the
     * interval between connect retry to the value provided.
     *
     * @param connectRetriesInterval The maximum time between retries in seconds (default: 120).
     */
    public void setConnectRetriesInterval(int connectRetriesInterval) {
        if (connectRetriesInterval < 0) {
            throw new FlywayException("Invalid number for connectRetriesInterval (must be 0 or greater): " + connectRetriesInterval, CoreErrorCode.CONFIGURATION);
        }
        getCurrentUnresolvedEnvironment().setConnectRetriesInterval(connectRetriesInterval);
        requestResolvedEnvironmentRefresh(getCurrentEnvironmentName());
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
     * Use in conjunction with {@code cherryPick} to skip specific migrations instead of all pending ones.
     */
    public void setSkipExecutingMigrations(boolean skipExecutingMigrations) {
        getModernFlyway().setSkipExecutingMigrations(skipExecutingMigrations);
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
    }

    private void loadUnloadedCallbacks() {
        for (String callback : getModernFlyway().getCallbacks()) {
            List<Callback> newCallbacks = loadCallbackPath(callback);
            this.callbacks.addAll(
                newCallbacks.stream()
                    .filter(this::callbackNotLoadedYet)
                    .toList()
            );
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
                throw new FlywayException("Invalid callback: " + callbackPath + " (must implement org.flywaydb.core.api.callback.Callback)", CoreErrorCode.CONFIGURATION);
            }
        } else {
            // else try to scan this location and load all callbacks found within
            callbacks.addAll(loadCallbackLocation(callbackPath, true));
        }

        return callbacks;
    }

    private boolean callbackNotLoadedYet(Callback callback) {
        return this.callbacks.stream()
            .noneMatch(c -> c.getClass().getCanonicalName().equals(callback.getClass().getCanonicalName()));
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
     * @param resolvers The custom MigrationResolvers to be used in addition to the built-in ones for resolving
     *                  Migrations to apply. (default: empty list)
     */
    public void setResolvers(MigrationResolver... resolvers) {
        this.resolvers = resolvers;
    }

    /**
     * Sets custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
     *
     * @param resolvers The fully qualified class names of the custom MigrationResolvers to be used in addition to the
     *                  built-in ones for resolving Migrations to apply. (default: empty list)
     */
    public void setResolversAsClassNames(String... resolvers) {
        getModernFlyway().setMigrationResolvers(Arrays.stream(resolvers).collect(Collectors.toList()));
    }

    /**
     * When connecting to a Kerberos service to authenticate, the path to the Kerberos config file.
     * <i>Flyway Teams only</i>
     */
    public void setKerberosConfigFile(String kerberosConfigFile) {

        throw new org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException(Tier.TEAMS, LicenseGuard.getTier(this), "kerberosConfigFile");




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
     * Whether Flyway should output a table with the results of queries when executing migrations.
     * <i>Flyway Teams only</i>
     *
     * @return {@code true} to output the results table (default: {@code true})
     */
    public void setOutputQueryResults(boolean outputQueryResults) {
        getModernFlyway().setOutputQueryResults(outputQueryResults);
    }

    /**
     * Properties to pass to the JDBC driver object.
     */
    public void setJdbcProperties(Map<String, String> jdbcProperties) {
        getCurrentUnresolvedEnvironment().setJdbcProperties(jdbcProperties);
        requestResolvedEnvironmentRefresh(getCurrentEnvironmentName());
    }

    /**
     * Configure with the same values as this existing configuration.
     */
    public void configure(Configuration configuration) {
        setModernConfig(ConfigurationModel.clone(configuration.getModernConfig()));
        setWorkingDirectory(configuration.getWorkingDirectory());

        setJavaMigrations(configuration.getJavaMigrations());
        setResourceProvider(configuration.getResourceProvider());
        setJavaMigrationClassProvider(configuration.getJavaMigrationClassProvider());
        setCallbacks(configuration.getCallbacks());

        setResolvers(configuration.getResolvers().clone());

        getModernFlyway().setMigrationResolvers(null);

        resolvedEnvironments.clear();
        resolvedEnvironments.putAll(configuration.getCachedResolvedEnvironments());
        dataSources.clear();
        dataSources.putAll(configuration.getCachedDataSources());
        pluginRegister = configuration.getPluginRegister().getCopy();




        configureFromConfigurationProviders(this);
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration. Properties are documented
     * here: https://documentation.red-gate.com/fd/parameters-184127474.html
     * <p>To use a custom ClassLoader, setClassLoader() must be called prior to calling this method.</p>
     *
     * @param properties Properties used for configuration.
     * @throws FlywayException when the configuration failed.
     */
    public void configure(Properties properties) {
        configure(ConfigUtils.propertiesToMap(properties));
    }

    public void setUrl(String url) {
        getCurrentUnresolvedEnvironment().setUrl(url);
        requestResolvedEnvironmentRefresh(getCurrentEnvironmentName());

        licenseGuardJdbcUrl(url);
    }

    public void setUser(String user) {
        getCurrentUnresolvedEnvironment().setUser(user);
        requestResolvedEnvironmentRefresh(getCurrentEnvironmentName());
    }

    public void setPassword(String password) {
        getCurrentUnresolvedEnvironment().setPassword(password);
        requestResolvedEnvironmentRefresh(getCurrentEnvironmentName());
    }

    public void setDriver(String driver) {
        getCurrentUnresolvedEnvironment().setDriver(driver);
        requestResolvedEnvironmentRefresh(getCurrentEnvironmentName());
    }

    private void setJarDirsAsStrings(String... jarDirs) {
        getCurrentUnresolvedEnvironment().setJarDirs(Arrays.stream(jarDirs).collect(Collectors.toList()));
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration. Properties are documented
     * here: https://documentation.red-gate.com/fd/parameters-184127474.html
     * <p>To use a custom ClassLoader, it must be passed to the Flyway constructor prior to calling this method.</p>
     *
     * @param props Properties used for configuration.
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

        props.computeIfAbsent(ConfigUtils.REPORT_FILENAME, k -> getModernConfig().getFlyway().getReportFilename());

        HashMap<String, Map<String, Object>> configExtensionsPropertyMap = new HashMap<>();

        List<String> keysToRemove = new ArrayList<>();

        final String deprecatedNameSpace = "plugins";
        Set<Map.Entry<String, String>> sortedEntrySet = new LinkedHashSet<>();
        sortedEntrySet.addAll(props.entrySet().stream().filter(r -> r.getKey().contains(deprecatedNameSpace)).collect(Collectors.toSet()));
        sortedEntrySet.addAll(props.entrySet().stream().filter(r -> !sortedEntrySet.contains(r)).collect(Collectors.toSet()));

        for (Map.Entry<String, String> params : sortedEntrySet) {

            String text = params.getKey();
            Matcher matcher = ANY_WORD_BETWEEN_TWO_DOTS_PATTERN.matcher(text);
            final String rootNamespace = matcher.find() ? matcher.group(1) : "";

            List<ConfigurationExtension> configExtensions = pluginRegister.getPlugins(ConfigurationExtension.class)
                .stream()
                .filter(c -> c.getNamespace().isEmpty() || rootNamespace.equals(c.getNamespace()) || rootNamespace.equals(deprecatedNameSpace))
                .collect(Collectors.toList());

            configExtensions.forEach(c -> {
                if (c.getNamespace().isEmpty()) {
                    String replaceNamespace = "flyway." + deprecatedNameSpace + ".";
                    String fixedKey = params.getKey().replace(replaceNamespace, "");
                    parsePropertiesFromConfigExtension(configExtensionsPropertyMap, keysToRemove, params, fixedKey, c);
                }
            });

            configExtensions.forEach(c -> {
                String replaceNamespace = "flyway.";
                if (StringUtils.hasText(rootNamespace) && !c.getNamespace().isEmpty()) {
                    replaceNamespace = "flyway." + rootNamespace + ".";
                }
                String fixedKey = params.getKey().replace(replaceNamespace, "");
                parsePropertiesFromConfigExtension(configExtensionsPropertyMap, keysToRemove, params, fixedKey, c);
            });
        }

        determineKeysToRemoveAndRemoveFromProps(configExtensionsPropertyMap, keysToRemove, props);

        String reportFilenameProp = props.remove(ConfigUtils.REPORT_FILENAME);
        if (reportFilenameProp != null) {
            setReportFilename(reportFilenameProp);
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
        String outputType = props.remove(ConfigUtils.OUTPUT_TYPE);
        if (outputType != null) {
            getModernConfig().getFlyway().setOutputType(outputType);
        }
        String locationsProp = props.remove(ConfigUtils.LOCATIONS);
        if (locationsProp != null) {
            setLocationsAsStrings(StringUtils.tokenizeToStringArray(locationsProp, ","));
        }
        String jarDirsProp = props.remove(ConfigUtils.JAR_DIRS);
        if (jarDirsProp != null) {
            setJarDirsAsStrings(StringUtils.tokenizeToStringArray(jarDirsProp, ","));
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
        Boolean communityDBSupportEnabledProd = removeBoolean(props, ConfigUtils.COMMUNITY_DB_SUPPORT_ENABLED);
        if (communityDBSupportEnabledProd != null) {
            setCommunityDBSupportEnabled(communityDBSupportEnabledProd);
        }
        Boolean reportEnabledProp = removeBoolean(props, ConfigUtils.REPORT_ENABLED);
        if (reportEnabledProp != null) {
            setReportEnabled(reportEnabledProp);
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
        Boolean createSchemasProp = removeBoolean(props, ConfigUtils.CREATE_SCHEMAS);
        if (createSchemasProp != null) {
            setShouldCreateSchemas(createSchemasProp);
        }
        String kerberosConfigFile = props.remove(ConfigUtils.KERBEROS_CONFIG_FILE);
        if (kerberosConfigFile != null) {
            setKerberosConfigFile(kerberosConfigFile);
        }
        String ignoreMigrationPatternsProp = props.remove(ConfigUtils.IGNORE_MIGRATION_PATTERNS);
        if (ignoreMigrationPatternsProp != null) {
            setIgnoreMigrationPatterns(StringUtils.tokenizeToStringArray(ignoreMigrationPatternsProp, ","));
        }
        Boolean failOnMissingLocationsProp = removeBoolean(props, ConfigUtils.FAIL_ON_MISSING_LOCATIONS);
        if (failOnMissingLocationsProp != null) {
            setFailOnMissingLocations(failOnMissingLocationsProp);
        }

        Map<String, String> jdbcPropertiesFromProps = getPropertiesUnderNamespace(props, getPlaceholders(),
            ConfigUtils.JDBC_PROPERTIES_PREFIX);
        if (!jdbcPropertiesFromProps.isEmpty()) {
            setJdbcProperties(jdbcPropertiesFromProps);
        }

        if (getDataSource() == null || Stream.of(urlProp, driverProp, userProp, passwordProp).anyMatch(StringUtils::hasText)) {
            String environmentName = getCurrentEnvironmentName();
            if (resolvedEnvironments.containsKey(environmentName) &&
                !resolvedEnvironmentMatchesClassicConfig(resolvedEnvironments.get(environmentName))) {
                    requestResolvedEnvironmentRefresh(environmentName);
            }

            if (Stream.of(urlProp, driverProp, userProp, passwordProp).anyMatch(StringUtils::hasText)) {
                DriverDataSource driverDataSource = null;
                boolean isGenerated = false;
                if (StringUtils.hasText(urlProp)) {
                    driverDataSource = new DriverDataSource(classLoader,
                        getDriver(),
                        getUrl(),
                        getUser(),
                        getPassword(),
                        this,
                        getJdbcProperties());
                    isGenerated = true;
                }
                dataSources.put(environmentName, new DataSourceModel(driverDataSource, isGenerated));
            }
        }

        ConfigUtils.checkConfigurationForUnrecognisedProperties(props, "flyway.");
    }

    private static void parsePropertiesFromConfigExtension(HashMap<String, Map<String, Object>> configExtensionsPropertyMap, List<String> keysToRemove, Map.Entry<String, String> params, String fixedKey, ConfigurationExtension configExtension) {
        List<String> fields = Arrays.stream(configExtension.getClass().getDeclaredFields()).map(Field::getName).collect(Collectors.toList());

        if (keysToRemove.contains(params.getKey()) && configExtension.isStub()) {
            return;
        }

        String rootKey = fixedKey.contains(".") ? fixedKey.substring(0, fixedKey.indexOf(".")) : fixedKey;
        if (fields.contains(rootKey)) {
            Object value = params.getValue();

            if (!configExtensionsPropertyMap.containsKey(configExtension.getClass().toString())) {
                configExtensionsPropertyMap.put(configExtension.getClass().toString(), new HashMap<>());
            }

            if (fixedKey.contains(".")) {
                String[] path = fixedKey.split("\\.");
                Map<String, Object> currentConfigExtensionProperties = new HashMap<>();
                if (!configExtensionsPropertyMap.get(configExtension.getClass().toString()).containsKey(path[0])) {
                    configExtensionsPropertyMap.get(configExtension.getClass().toString()).put(path[0], currentConfigExtensionProperties);
                } else {
                    currentConfigExtensionProperties = (Map<String, Object>) configExtensionsPropertyMap.get(configExtension.getClass().toString()).get(path[0]);
                }
                Object currentConfigExtension = configExtension;
                Field[] declaredFields = configExtension.getClass().getDeclaredFields();
                Field field = Arrays.stream(declaredFields).filter(f -> f.getName().equals(path[0])).findFirst().orElse(null);
                try {
                    currentConfigExtension = field.getType().getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    LOG.error("Failed to get configuration extension", e);
                }

                for (int i = 1; i < path.length; i++) {
                    final String currentPath = path[i];
                    try {
                        Field[] subFields = currentConfigExtension.getClass().getDeclaredFields();
                        field = Arrays.stream(subFields).filter(f -> f.getName().equals(currentPath)).findFirst().orElse(null);
                        if (field.getType() == List.class || field.getType() == String[].class) {
                            value = ((String) value).split(",");
                        } else if (field.getType() == Boolean.class) {
                            currentConfigExtension = null;
                        } else {
                            currentConfigExtension = field.getType().getDeclaredConstructor().newInstance();
                        }
                    } catch (Exception ignored) {
                    }
                    if (i < path.length - 1) {
                        Map<String, Object> newValue = new HashMap<>();
                        currentConfigExtensionProperties.put(path[i], newValue);
                        currentConfigExtensionProperties = newValue;
                    } else {
                        currentConfigExtensionProperties.put(path[i], value);
                    }
                }
            } else {
                configExtensionsPropertyMap.get(configExtension.getClass().toString()).put(fixedKey, value);
            }

            keysToRemove.add(params.getKey());
        }
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

    public void setCommunityDBSupportEnabled(Boolean communityDBSupportEnabled){
        getModernFlyway().setCommunityDBSupportEnabled(communityDBSupportEnabled);
    }

    public void setReportEnabled(Boolean reportEnabled) {
        getModernFlyway().setReportEnabled(reportEnabled);
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
        requestResolvedEnvironmentRefresh(getCurrentEnvironmentName());
    }

    public void setRepeatableSqlMigrationPrefix(String repeatableSqlMigrationPrefixProp) {
        getModernFlyway().setRepeatableSqlMigrationPrefix(repeatableSqlMigrationPrefixProp);
    }

    public void setPlaceholderReplacement(Boolean placeholderReplacementProp) {
        getModernFlyway().setPlaceholderReplacement(placeholderReplacementProp);
    }

    public void setInitSql(String initSqlProp) {
        getCurrentUnresolvedEnvironment().setInitSql(initSqlProp);
        requestResolvedEnvironmentRefresh(getCurrentEnvironmentName());
    }

    private void requestResolvedEnvironmentRefresh(String environmentName) {
        resolvedEnvironments.remove(environmentName);

        DataSourceModel model = dataSources.get(environmentName);
        if (model != null && model.isDataSourceGenerated()) {
            dataSources.remove(environmentName);
        }
    }

    //todo - this seems a little simple, but it's a start
    private boolean resolvedEnvironmentMatchesClassicConfig(ResolvedEnvironment environment) {
        return Objects.equals(this.getUrl(), environment.getUrl()) &&
            Objects.equals(this.getUser(), environment.getUser()) &&
            Objects.equals(this.getPassword(), environment.getPassword());
    }

    public String getCurrentEnvironmentName() {
        String envName = modernConfig.getFlyway().getEnvironment();
        if (!StringUtils.hasText(envName)) {
            envName = "default";
        }
        if (!getModernConfig().getEnvironments().containsKey(envName)) {
            throw new FlywayException(
                "Environment '" + envName + "' not found. Check that this environment exists in your configuration.");
        }

        return envName;
    }

    private EnvironmentModel getCurrentUnresolvedEnvironment() {
        return getModernConfig().getEnvironments().get(getCurrentEnvironmentName());
    }

    private void licenseGuardJdbcUrl(String url) {
        if (!url.toLowerCase().startsWith("jdbc-secretsmanager:")) {
            return;
        }












    }

    private void determineKeysToRemoveAndRemoveFromProps(HashMap<String, Map<String, Object>> configExtensionsPropertyMap, List<String> keysToRemove, Map<String, String> props) {
        for (Map.Entry<String, Map<String, Object>> property : configExtensionsPropertyMap.entrySet()) {
            ConfigurationExtension cfg = pluginRegister.getPlugins(ConfigurationExtension.class).stream().filter(c -> c.getClass().toString().equals(property.getKey())).findFirst().orElse(null);
            if (cfg != null) {
                Map<String, Object> mpTmp = property.getValue();

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> mp = new HashMap<>();
                    for (Map.Entry<String, Object> entry : mpTmp.entrySet()) {
                        Field[] subFields = cfg.getClass().getDeclaredFields();
                        Field field = Arrays.stream(subFields).filter(f -> f.getName().equals(entry.getKey())).findFirst().orElse(null);
                        Object value = (field.getType() == List.class || field.getType() == String[].class) ?
                            ((String) entry.getValue()).split(",") :
                            entry.getValue();

                        mp.put(entry.getKey(), value);
                    }
                    ConfigurationExtension newConfigurationExtension = objectMapper.convertValue(mp, cfg.getClass());
                    MergeUtils.mergeModel(newConfigurationExtension, cfg);
                } catch (Exception e) {
                    Matcher matcher = ANY_WORD_BETWEEN_TWO_QUOTES_PATTERN.matcher(e.getMessage());
                    if (matcher.find()) {
                        String errorProperty = matcher.group(1);
                        List<String> propsToRemove = keysToRemove.stream().filter(k -> k.endsWith(errorProperty)).collect(Collectors.toList());
                        keysToRemove.removeAll(propsToRemove);
                    }
                }
            }
        }

        props.keySet().removeAll(keysToRemove);
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

    @Override
    public ProgressLogger createProgress(String operationName) {
        if (getModernFlyway().getOutputProgress() && "json".equalsIgnoreCase(getModernFlyway().getOutputType())) {
            return new ProgressLoggerJson(operationName);
        } else {
            return new ProgressLoggerEmpty();
        }
    }
}