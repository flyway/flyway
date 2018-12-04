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
package org.flywaydb.core;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.errorhandler.ErrorHandler;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.callback.DefaultCallbackExecutor;
import org.flywaydb.core.internal.callback.LegacyCallback;
import org.flywaydb.core.internal.callback.NoopCallback;
import org.flywaydb.core.internal.callback.NoopCallbackExecutor;
import org.flywaydb.core.internal.callback.SqlScriptCallbackFactory;
import org.flywaydb.core.internal.clazz.ClassProvider;
import org.flywaydb.core.internal.clazz.NoopClassProvider;
import org.flywaydb.core.internal.command.DbBaseline;
import org.flywaydb.core.internal.command.DbClean;
import org.flywaydb.core.internal.command.DbInfo;
import org.flywaydb.core.internal.command.DbMigrate;
import org.flywaydb.core.internal.command.DbRepair;
import org.flywaydb.core.internal.command.DbSchemas;
import org.flywaydb.core.internal.command.DbValidate;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.database.DatabaseFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.resource.NoopResourceProvider;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.scanner.Scanner;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.schemahistory.SchemaHistoryFactory;
import org.flywaydb.core.internal.sqlscript.SqlStatementBuilderFactory;
import org.flywaydb.core.internal.util.StringUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is the centre point of Flyway, and for most users, the only class they will ever have to deal with.
 * <p>
 * It is THE public API from which all important Flyway functions such as clean, validate and migrate can be called.
 * </p>
 * <p>To get started all you need to do is</p>
 * <pre>
 * Flyway flyway = Flyway.configure().dataSource(url, user, password).load();
 * flyway.migrate();
 * </pre>
 * <p>
 * Deprecation warning: starting with Flyway 6.0 this class will no longer implement the Configuration interface.
 */
public class Flyway implements Configuration {
    private static final Log LOG = LogFactory.getLog(Flyway.class);

    private final ClassicConfiguration configuration;

    /**
     * Whether the database connection info has already been printed in the logs.
     */
    private boolean dbConnectionInfoPrinted;

    /**
     * This is your starting point. This creates a configuration which can be customized to your needs before being
     * loaded into a new Flyway instance using the load() method.
     * <p>In its simplest form, this is how you configure Flyway with all defaults to get started:</p>
     * <pre>Flyway flyway = Flyway.configure().dataSource(url, user, password).load();</pre>
     * <p>After that you have a fully-configured Flyway instance at your disposal which can be used to invoke Flyway
     * functionality such as migrate() or clean().</p>
     *
     * @return A new configuration from which Flyway can be loaded.
     */
    public static FluentConfiguration configure() {
        return new FluentConfiguration();
    }

    /**
     * This is your starting point. This creates a configuration which can be customized to your needs before being
     * loaded into a new Flyway instance using the load() method.
     * <p>In its simplest form, this is how you configure Flyway with all defaults to get started:</p>
     * <pre>Flyway flyway = Flyway.configure().dataSource(url, user, password).load();</pre>
     * <p>After that you have a fully-configured Flyway instance at your disposal which can be used to invoke Flyway
     * functionality such as migrate() or clean().</p>
     *
     * @param classLoader The class loader to use when loading classes and resources.
     * @return A new configuration from which Flyway can be loaded.
     */
    public static FluentConfiguration configure(ClassLoader classLoader) {
        return new FluentConfiguration(classLoader);
    }

    /**
     * Creates a new instance of Flyway. This is your starting point.
     *
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public Flyway() {
        configuration = new ClassicConfiguration();
    }

    /**
     * Creates a new instance of Flyway. This is your starting point.
     *
     * @param classLoader The ClassLoader to use for loading migrations, resolvers, etc from the classpath. (default: Thread.currentThread().getContextClassLoader() )
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public Flyway(ClassLoader classLoader) {
        configuration = new ClassicConfiguration(classLoader);
    }

    /**
     * Creates a new instance of Flyway with this configuration.
     *
     * @param configuration The configuration to use.
     * @deprecated Use {@link #Flyway(Configuration)} instead. Will be removed in Flyway 6.0.
     */
    @Deprecated
    public Flyway(FlywayConfiguration configuration) {
        LOG.warn("The Flyway(FlywayConfiguration) constructor has been deprecated and will be removed in Flyway 6.0. Use Flyway(Configuration) instead.");
        this.configuration = new ClassicConfiguration(configuration);
    }

    /**
     * Creates a new instance of Flyway with this configuration. In general the Flyway.configure() factory method should
     * be preferred over this constructor, unless you need to create or reuse separate Configuration objects.
     *
     * @param configuration The configuration to use.
     */
    public Flyway(Configuration configuration) {
        this.configuration = new ClassicConfiguration(configuration);
    }

    /**
     * @return The configuration that Flyway is using.
     */
    public Configuration getConfiguration() {
        return new ClassicConfiguration(configuration);
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public Location[] getLocations() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getLocations();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public Charset getEncoding() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getEncoding();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public String[] getSchemas() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getSchemas();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public String getTable() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getTable();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public MigrationVersion getTarget() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getTarget();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isPlaceholderReplacement() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isPlaceholderReplacement();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public Map<String, String> getPlaceholders() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getPlaceholders();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public String getPlaceholderPrefix() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getPlaceholderPrefix();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public String getPlaceholderSuffix() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getPlaceholderSuffix();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public String getSqlMigrationPrefix() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getSqlMigrationPrefix();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public String getRepeatableSqlMigrationPrefix() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getRepeatableSqlMigrationPrefix();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public String getSqlMigrationSeparator() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getSqlMigrationSeparator();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    public String getSqlMigrationSuffix() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getSqlMigrationSuffixes()[0];
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public String[] getSqlMigrationSuffixes() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getSqlMigrationSuffixes();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isIgnoreMissingMigrations() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isIgnoreMissingMigrations();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isIgnoreIgnoredMigrations() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isIgnoreIgnoredMigrations();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isIgnorePendingMigrations() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isIgnorePendingMigrations();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isIgnoreFutureMigrations() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isIgnoreFutureMigrations();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isValidateOnMigrate() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isValidateOnMigrate();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isCleanOnValidationError() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isCleanOnValidationError();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isCleanDisabled() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isCleanDisabled();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public MigrationVersion getBaselineVersion() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getBaselineVersion();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public String getBaselineDescription() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getBaselineDescription();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isBaselineOnMigrate() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isBaselineOnMigrate();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isOutOfOrder() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isOutOfOrder();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public MigrationResolver[] getResolvers() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getResolvers();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isSkipDefaultResolvers() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isSkipDefaultResolvers();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public DataSource getDataSource() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getDataSource();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public int getConnectRetries() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getConnectRetries();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public String getInitSql() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getInitSql();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public ClassLoader getClassLoader() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getClassLoader();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isMixed() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isMixed();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public String getInstalledBy() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getInstalledBy();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isGroup() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isGroup();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public ErrorHandler[] getErrorHandlers() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getErrorHandlers();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public String[] getErrorOverrides() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getErrorOverrides();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public OutputStream getDryRunOutput() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getDryRunOutput();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isStream() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isStream();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isBatch() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isBatch();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isOracleSqlplus() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isOracleSqlplus();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public String getLicenseKey() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getLicenseKey();
    }

    /**
     * Sets the stream where to output the SQL statements of a migration dry run. {@code null} to execute the SQL statements
     * directly against the database. The stream when be closing when Flyway finishes writing the output.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param dryRunOutput The output file or {@code null} to execute the SQL statements directly against the database.
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setDryRunOutput(OutputStream dryRunOutput) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setDryRunOutput(dryRunOutput);
    }

    /**
     * Sets the file where to output the SQL statements of a migration dry run. {@code null} to execute the SQL statements
     * directly against the database. If the file specified is in a non-existent directory, Flyway will create all
     * directories and parent directories as needed.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param dryRunOutput The output file or {@code null} to execute the SQL statements directly against the database.
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setDryRunOutputAsFile(File dryRunOutput) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setDryRunOutputAsFile(dryRunOutput);
    }

    /**
     * Sets the file where to output the SQL statements of a migration dry run. {@code null} to execute the SQL statements
     * directly against the database. If the file specified is in a non-existent directory, Flyway will create all
     * directories and parent directories as needed.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param dryRunOutputFileName The name of the output file or {@code null} to execute the SQL statements directly
     *                             against the database.
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setDryRunOutputAsFileName(String dryRunOutputFileName) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setDryRunOutputAsFileName(dryRunOutputFileName);
    }

    /**
     * Handlers for errors and warnings that occur during a migration. This can be used to customize Flyway's behavior by for example
     * throwing another runtime exception, outputting a warning or suppressing the error instead of throwing a FlywayException.
     * ErrorHandlers are invoked in order until one reports to have successfully handled the errors or warnings.
     * If none do, or if none are present, Flyway falls back to its default handling of errors and warnings.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param errorHandlers The ErrorHandlers or an empty array if the default internal handler should be used instead. (default: none)
     * @deprecated ErrorHandlers have been deprecated and will be removed in Flyway 6.0 use statement-level callbacks instead.
     */
    @Deprecated
    public void setErrorHandlers(ErrorHandler... errorHandlers) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setErrorHandlers(errorHandlers);
    }

    /**
     * Handlers for errors and warnings that occur during a migration. This can be used to customize Flyway's behavior by for example
     * throwing another runtime exception, outputting a warning or suppressing the error instead of throwing a FlywayException.
     * ErrorHandlers are invoked in order until one reports to have successfully handled the errors or warnings.
     * If none do, or if none are present, Flyway falls back to its default handling of errors and warnings.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param errorHandlerClassNames The fully qualified class names of ErrorHandlers or an empty array if the default
     *                               internal handler should be used instead. (default: none)
     * @deprecated ErrorHandlers have been deprecated and will be removed in Flyway 6.0 use statement-level callbacks instead.
     */
    @Deprecated
    public void setErrorHandlersAsClassNames(String... errorHandlerClassNames) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setErrorHandlersAsClassNames(errorHandlerClassNames);
    }

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
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param errorOverrides The ErrorOverrides or an empty array if none are defined. (default: none)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setErrorOverrides(String... errorOverrides) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setErrorOverrides(errorOverrides);
    }

    /**
     * Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions).
     *
     * @param group {@code true} if migrations should be grouped. {@code false} if they should be applied individually instead. (default: {@code false})
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setGroup(boolean group) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setGroup(group);
    }

    /**
     * The username that will be recorded in the schema history table as having applied the migration.
     *
     * @param installedBy The username or {@code null} for the current database user of the connection. (default: {@code null}).
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setInstalledBy(String installedBy) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setInstalledBy(installedBy);
    }

    /**
     * Whether to allow mixing transactional and non-transactional statements within the same migration.
     *
     * @param mixed {@code true} if mixed migrations should be allowed. {@code false} if an error should be thrown instead. (default: {@code false})
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setMixed(boolean mixed) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setMixed(mixed);
    }

    /**
     * Ignore missing migrations when reading the schema history table. These are migrations that were performed by an
     * older deployment of the application that are no longer available in this version. For example: we have migrations
     * available on the classpath with versions 1.0 and 3.0. The schema history table indicates that a migration with version 2.0
     * (unknown to us) has also been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to deploy
     * a newer version of the application even though it doesn't contain migrations included with an older one anymore.
     * Note that if the most recently applied migration is removed, Flyway has no way to know it is missing and will
     * mark it as future instead.
     *
     * @param ignoreMissingMigrations {@code true} to continue normally and log a warning, {@code false} to fail fast with an exception.
     *                                (default: {@code false})
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setIgnoreMissingMigrations(boolean ignoreMissingMigrations) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setIgnoreMissingMigrations(ignoreMissingMigrations);
    }

    /**
     * Ignore ignored migrations when reading the schema history table. These are migrations that were added in between
     * already migrated migrations in this version. For example: we have migrations available on the classpath with
     * versions from 1.0 to 3.0. The schema history table indicates that version 1 was finished on 1.0.15, and the next
     * one was 2.0.0. But with the next release a new migration was added to version 1: 1.0.16. Such scenario is ignored
     * by migrate command, but by default is rejected by validate. When ignoreIgnoredMigrations is enabled, such case
     * will not be reported by validate command. This is useful for situations where one must be able to deliver
     * complete set of migrations in a delivery package for multiple versions of the product, and allows for further
     * development of older versions.
     *
     * @param ignoreIgnoredMigrations {@code true} to continue normally, {@code false} to fail fast with an exception.
     *                                (default: {@code false})
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setIgnoreIgnoredMigrations(boolean ignoreIgnoredMigrations) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setIgnoreIgnoredMigrations(ignoreIgnoredMigrations);
    }

    /**
     * Whether to ignore future migrations when reading the schema history table. These are migrations that were performed by a
     * newer deployment of the application that are not yet available in this version. For example: we have migrations
     * available on the classpath up to version 3.0. The schema history table indicates that a migration to version 4.0
     * (unknown to us) has already been applied. Instead of bombing out (fail fast) with an exception, a
     * warning is logged and Flyway continues normally. This is useful for situations where one must be able to redeploy
     * an older version of the application after the database has been migrated by a newer one.
     *
     * @param ignoreFutureMigrations {@code true} to continue normally and log a warning, {@code false} to fail
     *                               fast with an exception. (default: {@code true})
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setIgnoreFutureMigrations(boolean ignoreFutureMigrations) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setIgnoreFutureMigrations(ignoreFutureMigrations);
    }

    /**
     * Whether to automatically call validate or not when running migrate.
     *
     * @param validateOnMigrate {@code true} if validate should be called. {@code false} if not. (default: {@code true})
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setValidateOnMigrate(boolean validateOnMigrate) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setValidateOnMigrate(validateOnMigrate);
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
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setCleanOnValidationError(boolean cleanOnValidationError) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setCleanOnValidationError(cleanOnValidationError);
    }

    /**
     * Whether to disable clean.
     * <p>This is especially useful for production environments where running clean can be quite a career limiting move.</p>
     *
     * @param cleanDisabled {@code true} to disabled clean. {@code false} to leave it enabled.  (default: {@code false})
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setCleanDisabled(boolean cleanDisabled) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setCleanDisabled(cleanDisabled);
    }

    /**
     * Sets the locations to scan recursively for migrations.
     * <p>The location type is determined by its prefix.
     * Unprefixed locations or locations starting with {@code classpath:} point to a package on the classpath and may
     * contain both sql and java-based migrations.
     * Locations starting with {@code filesystem:} point to a directory on the filesystem and may only contain sql
     * migrations.</p>
     *
     * @param locations Locations to scan recursively for migrations. (default: db/migration)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setLocations(String... locations) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setLocationsAsStrings(locations);
    }

    /**
     * Sets the encoding of Sql migrations.
     *
     * @param encoding The encoding of Sql migrations. (default: UTF-8)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setEncoding(String encoding) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setEncodingAsString(encoding);
    }

    /**
     * Sets the schemas managed by Flyway. These schema names are case-sensitive. (default: The default schema for the database connection)
     * <p>Consequences:</p>
     * <ul>
     * <li>Flyway will automatically attempt to create all these schemas, unless the first one already exists.</li>
     * <li>The first schema in the list will be automatically set as the default one during the migration.</li>
     * <li>The first schema in the list will also be the one containing the schema history table.</li>
     * <li>The schemas will be cleaned in the order of this list.</li>
     * <li>If Flyway created them, the schemas themselves will as be dropped when cleaning.</li>
     * </ul>
     *
     * @param schemas The schemas managed by Flyway. May not be {@code null}. Must contain at least one element.
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setSchemas(String... schemas) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setSchemas(schemas);
    }

    /**
     * <p>Sets the name of the schema schema history table that will be used by Flyway.</p><p> By default (single-schema mode)
     * the schema history table is placed in the default schema for the connection provided by the datasource. </p> <p> When
     * the <i>flyway.schemas</i> property is set (multi-schema mode), the schema history table is placed in the first schema
     * of the list. </p>
     *
     * @param table The name of the schema schema history table that will be used by flyway. (default: flyway_schema_history)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setTable(String table) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setTable(table);
    }

    /**
     * Sets the target version up to which Flyway should consider migrations. Migrations with a higher version number will
     * be ignored.
     *
     * @param target The target version up to which Flyway should consider migrations. (default: the latest version)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setTarget(MigrationVersion target) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setTarget(target);
    }

    /**
     * Sets the target version up to which Flyway should consider migrations.
     * Migrations with a higher version number will be ignored.
     *
     * @param target The target version up to which Flyway should consider migrations.
     *               The special value {@code current} designates the current version of the schema. (default: the latest
     *               version)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setTargetAsString(String target) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setTargetAsString(target);
    }

    /**
     * Sets whether placeholders should be replaced.
     *
     * @param placeholderReplacement Whether placeholders should be replaced. (default: true)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setPlaceholderReplacement(boolean placeholderReplacement) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setPlaceholderReplacement(placeholderReplacement);
    }

    /**
     * Sets the placeholders to replace in sql migration scripts.
     *
     * @param placeholders The map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setPlaceholders(Map<String, String> placeholders) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setPlaceholders(placeholders);
    }

    /**
     * Sets the prefix of every placeholder.
     *
     * @param placeholderPrefix The prefix of every placeholder. (default: ${ )
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setPlaceholderPrefix(String placeholderPrefix) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setPlaceholderPrefix(placeholderPrefix);
    }

    /**
     * Sets the suffix of every placeholder.
     *
     * @param placeholderSuffix The suffix of every placeholder. (default: } )
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setPlaceholderSuffix(String placeholderSuffix) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setPlaceholderSuffix(placeholderSuffix);
    }

    /**
     * Sets the file name prefix for sql migrations.
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     *
     * @param sqlMigrationPrefix The file name prefix for sql migrations (default: V)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setSqlMigrationPrefix(sqlMigrationPrefix);
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public String getUndoSqlMigrationPrefix() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getUndoSqlMigrationPrefix();
    }

    /**
     * Sets the file name prefix for undo SQL migrations. (default: U)
     * <p>Undo SQL migrations are responsible for undoing the effects of the versioned migration with the same version.</p>
     * <p>They have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to U1.1__My_description.sql</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param undoSqlMigrationPrefix The file name prefix for undo SQL migrations. (default: U)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setUndoSqlMigrationPrefix(String undoSqlMigrationPrefix) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setUndoSqlMigrationPrefix(undoSqlMigrationPrefix);
    }

    /**
     * Sets the file name prefix for repeatable sql migrations.
     * <p>Repeatable sql migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to R__My_description.sql</p>
     *
     * @param repeatableSqlMigrationPrefix The file name prefix for repeatable sql migrations (default: R)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setRepeatableSqlMigrationPrefix(String repeatableSqlMigrationPrefix) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setRepeatableSqlMigrationPrefix(repeatableSqlMigrationPrefix);
    }

    /**
     * Sets the file name separator for sql migrations.
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     *
     * @param sqlMigrationSeparator The file name separator for sql migrations (default: __)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setSqlMigrationSeparator(String sqlMigrationSeparator) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setSqlMigrationSeparator(sqlMigrationSeparator);
    }

    /**
     * Sets the file name suffix for sql migrations.
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     *
     * @param sqlMigrationSuffix The file name suffix for sql migrations (default: .sql)
     * @deprecated sqlMigrationSuffix has been deprecated and will be removed in Flyway 6.0.0. Use sqlMigrationSuffixes instead.
     */
    @Deprecated
    public void setSqlMigrationSuffix(String sqlMigrationSuffix) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setSqlMigrationSuffixes(sqlMigrationSuffix);
    }

    /**
     * The file name suffixes for SQL migrations. (default: .sql)
     * <p>SQL migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     * <p>Multiple suffixes (like .sql,.pkg,.pkb) can be specified for easier compatibility with other tools such as
     * editors with specific file associations.</p>
     *
     * @param sqlMigrationSuffixes The file name suffixes for SQL migrations.
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setSqlMigrationSuffixes(String... sqlMigrationSuffixes) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setSqlMigrationSuffixes(sqlMigrationSuffixes);
    }

    /**
     * Sets the datasource to use. Must have the necessary privileges to execute ddl.
     *
     * @param dataSource The datasource to use. Must have the necessary privileges to execute ddl.
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setDataSource(DataSource dataSource) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setDataSource(dataSource);
    }

    /**
     * Sets the datasource to use. Must have the necessary privileges to execute ddl.
     * <p>To use a custom ClassLoader, setClassLoader() must be called prior to calling this method.</p>
     *
     * @param url      The JDBC URL of the database.
     * @param user     The user of the database.
     * @param password The password of the database.
     * @param initSqls The (optional) sql statements to execute to initialize a connection immediately after obtaining it.
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setDataSource(String url, String user, String password, String... initSqls) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setDataSource(url, user, password, initSqls);
    }

    /**
     * Sets the ClassLoader to use for resolving migrations on the classpath.
     *
     * @param classLoader The ClassLoader to use for loading migrations, resolvers, etc from the classpath. (default: Thread.currentThread().getContextClassLoader() )
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure(ClassLoader) instead.
     */
    @Deprecated
    public void setClassLoader(ClassLoader classLoader) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure(ClassLoader) instead.");
        configuration.setClassLoader(classLoader);
    }

    /**
     * Sets the version to tag an existing schema with when executing baseline.
     *
     * @param baselineVersion The version to tag an existing schema with when executing baseline. (default: 1)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setBaselineVersion(MigrationVersion baselineVersion) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setBaselineVersion(baselineVersion);
    }

    /**
     * Sets the version to tag an existing schema with when executing baseline.
     *
     * @param baselineVersion The version to tag an existing schema with when executing baseline. (default: 1)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setBaselineVersionAsString(String baselineVersion) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setBaselineVersionAsString(baselineVersion);
    }

    /**
     * Sets the description to tag an existing schema with when executing baseline.
     *
     * @param baselineDescription The description to tag an existing schema with when executing baseline. (default: &lt;&lt; Flyway Baseline &gt;&gt;)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setBaselineDescription(String baselineDescription) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setBaselineDescription(baselineDescription);
    }

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
     *
     * @param baselineOnMigrate {@code true} if baseline should be called on migrate for non-empty schemas, {@code false} if not. (default: {@code false})
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setBaselineOnMigrate(boolean baselineOnMigrate) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setBaselineOnMigrate(baselineOnMigrate);
    }

    /**
     * Allows migrations to be run "out of order".
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     *
     * @param outOfOrder {@code true} if outOfOrder migrations should be applied, {@code false} if not. (default: {@code false})
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setOutOfOrder(boolean outOfOrder) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setOutOfOrder(outOfOrder);
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public Callback[] getCallbacks() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.getCallbacks();
    }

    /**
     * @deprecated Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.
     */
    @Deprecated
    @Override
    public boolean isSkipDefaultCallbacks() {
        LOG.warn("Direct configuration retrieval from the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.getConfiguration() instead.");
        return configuration.isSkipDefaultCallbacks();
    }

    /**
     * Set the callbacks for lifecycle notifications.
     *
     * @param callbacks The callbacks for lifecycle notifications. (default: none)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setCallbacks(Callback... callbacks) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setCallbacks(callbacks);
    }

    /**
     * Set the callbacks for lifecycle notifications.
     *
     * @param callbacks The callbacks for lifecycle notifications. (default: none)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setCallbacks(FlywayCallback... callbacks) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        List<Callback> l = new ArrayList<>();
        for (FlywayCallback callback : callbacks) {
            l.add(new LegacyCallback(callback));
        }
        configuration.setCallbacks(l.toArray(new Callback[0]));
    }

    /**
     * Set the callbacks for lifecycle notifications.
     *
     * @param callbacks The fully qualified class names of the callbacks for lifecycle notifications. (default: none)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setCallbacksAsClassNames(String... callbacks) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setCallbacksAsClassNames(callbacks);
    }

    /**
     * Whether Flyway should skip the default callbacks. If true, only custom callbacks are used.
     *
     * @param skipDefaultCallbacks Whether default built-in callbacks should be skipped. <p>(default: false)</p>
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setSkipDefaultCallbacks(boolean skipDefaultCallbacks) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setSkipDefaultCallbacks(skipDefaultCallbacks);
    }

    /**
     * Sets custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
     *
     * @param resolvers The custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply. (default: empty list)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setResolvers(MigrationResolver... resolvers) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setResolvers(resolvers);
    }

    /**
     * Sets custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
     *
     * @param resolvers The fully qualified class names of the custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply. (default: empty list)
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setResolversAsClassNames(String... resolvers) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setResolversAsClassNames(resolvers);
    }

    /**
     * Whether Flyway should skip the default resolvers. If true, only custom resolvers are used.
     *
     * @param skipDefaultResolvers Whether default built-in resolvers should be skipped. <p>(default: false)</p>
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setSkipDefaultResolvers(boolean skipDefaultResolvers) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setSkipDefaultResolvers(skipDefaultResolvers);
    }

    /**
     * Whether to stream SQL migrations when executing them. Streaming doesn't load the entire migration in memory at
     * once. Instead each statement is loaded individually. This is particularly useful for very large SQL migrations
     * composed of multiple MB or even GB of reference data, as this dramatically reduces Flyway's memory consumption.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param stream {@code true} to stream SQL migrations. {@code false} to fully loaded them in memory instead. (default: {@code false})
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setStream(boolean stream) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setStream(stream);
    }

    /**
     * Whether to batch SQL statements when executing them. Batching can save up to 99 percent of network roundtrips by
     * sending up to 100 statements at once over the network to the database, instead of sending each statement
     * individually. This is particularly useful for very large SQL migrations composed of multiple MB or even GB of
     * reference data, as this can dramatically reduce the network overhead. This is supported for INSERT, UPDATE,
     * DELETE, MERGE and UPSERT statements. All other statements are automatically executed without batching.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param batch {@code true} to batch SQL statements. {@code false} to execute them individually instead. (default: {@code false})
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setBatch(boolean batch) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setBatch(batch);
    }

    /**
     * Whether to Flyway's support for Oracle SQL*Plus commands should be activated.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param oracleSqlplus {@code true} to active SQL*Plus support. {@code false} to fail fast instead. (default: {@code false})
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setOracleSqlplus(boolean oracleSqlplus) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setOracleSqlplus(oracleSqlplus);
    }

    /**
     * Flyway's license key.
     *
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param licenseKey The license key.
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void setLicenseKey(String licenseKey) {
        LOG.warn("Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.");
        configuration.setLicenseKey(licenseKey);
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
            public Integer execute(MigrationResolver migrationResolver,
                                   SchemaHistory schemaHistory, Database database, Schema[] schemas, CallbackExecutor callbackExecutor



            ) {
                if (configuration.isValidateOnMigrate()) {
                    doValidate(database, migrationResolver, schemaHistory, schemas, callbackExecutor,
                            true // Always ignore pending migrations when validating before migrating
                    );
                }

                new DbSchemas(database, schemas, schemaHistory).create();

                if (!schemaHistory.exists()) {
                    List<Schema> nonEmptySchemas = new ArrayList<>();
                    for (Schema schema : schemas) {
                        if (!schema.empty()) {
                            nonEmptySchemas.add(schema);
                        }
                    }

                    if (!nonEmptySchemas.isEmpty()) {
                        if (configuration.isBaselineOnMigrate()) {
                            doBaseline(schemaHistory, database, schemas, callbackExecutor);
                        } else {
                            // Second check for MySQL which is sometimes flaky otherwise
                            if (!schemaHistory.exists()) {
                                throw new FlywayException("Found non-empty schema(s) "
                                        + StringUtils.collectionToCommaDelimitedString(nonEmptySchemas)
                                        + " without schema history table! Use baseline()"
                                        + " or set baselineOnMigrate to true to initialize the schema history table.");
                            }
                        }
                    }
                }

                return new DbMigrate(database, schemaHistory, schemas[0], migrationResolver, configuration,
                        callbackExecutor).migrate();
            }
        }, true);
    }

    private void doBaseline(SchemaHistory schemaHistory, Database database, Schema[] schemas, CallbackExecutor callbackExecutor) {
        new DbBaseline(database, schemaHistory, schemas[0],
                configuration.getBaselineVersion(), configuration.getBaselineDescription(),
                callbackExecutor).baseline();
    }

    /**
     * <p>Undoes the most recently applied versioned migration. If target is specified, Flyway will attempt to undo
     * versioned migrations in the order they were applied until it hits one with a version below the target. If there
     * is no versioned migration to undo, calling undo has no effect.</p>
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     * <img src="https://flywaydb.org/assets/balsamiq/command-undo.png" alt="undo">
     *
     * @return The number of successfully undone migrations.
     * @throws FlywayException when the undo failed.
     */
    public int undo() throws FlywayException {

        throw new org.flywaydb.core.internal.license.FlywayProUpgradeRequiredException("undo");












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
     *
     * <img src="https://flywaydb.org/assets/balsamiq/command-validate.png" alt="validate">
     *
     * @throws FlywayException when the validation failed.
     */
    public void validate() throws FlywayException {
        execute(new Command<Void>() {
            public Void execute(MigrationResolver migrationResolver, SchemaHistory schemaHistory, Database database,
                                Schema[] schemas, CallbackExecutor callbackExecutor



            ) {
                doValidate(database, migrationResolver, schemaHistory, schemas, callbackExecutor,
                        configuration.isIgnorePendingMigrations());
                return null;
            }
        }, true);
    }

    /**
     * Performs the actual validation. All set up must have taken place beforehand.
     *
     * @param database          The database-specific support.
     * @param migrationResolver The migration resolver;
     * @param schemaHistory     The schema history table.
     * @param schemas           The schemas managed by Flyway.
     * @param callbackExecutor  The callback executor.
     * @param ignorePending     Whether to ignore pending migrations.
     */
    private void doValidate(Database database, MigrationResolver migrationResolver, SchemaHistory schemaHistory,
                            Schema[] schemas, CallbackExecutor callbackExecutor, boolean ignorePending) {
        String validationError =
                new DbValidate(database, schemaHistory, schemas[0], migrationResolver,
                        configuration, ignorePending, callbackExecutor).validate();

        if (validationError != null) {
            if (configuration.isCleanOnValidationError()) {
                doClean(database, schemaHistory, schemas, callbackExecutor);
            } else {
                throw new FlywayException("Validate failed: " + validationError);
            }
        }
    }

    private void doClean(Database database, SchemaHistory schemaHistory, Schema[] schemas, CallbackExecutor callbackExecutor) {
        new DbClean(database, schemaHistory, schemas, callbackExecutor, configuration.isCleanDisabled()).clean();
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
            public Void execute(MigrationResolver migrationResolver, SchemaHistory schemaHistory, Database database,
                                Schema[] schemas, CallbackExecutor callbackExecutor



            ) {
                doClean(database, schemaHistory, schemas, callbackExecutor);
                return null;
            }
        }, false);
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
            public MigrationInfoService execute(MigrationResolver migrationResolver, SchemaHistory schemaHistory,
                                                final Database database, final Schema[] schemas, CallbackExecutor callbackExecutor



            ) {
                return new DbInfo(migrationResolver, schemaHistory, configuration, callbackExecutor).info();
            }
        }, true);
    }

    /**
     * <p>Baselines an existing database, excluding all migrations up to and including baselineVersion.</p>
     *
     * <img src="https://flywaydb.org/assets/balsamiq/command-baseline.png" alt="baseline">
     *
     * @throws FlywayException when the schema baselining failed.
     */
    public void baseline() throws FlywayException {
        execute(new Command<Void>() {
            public Void execute(MigrationResolver migrationResolver,
                                SchemaHistory schemaHistory, Database database, Schema[] schemas, CallbackExecutor callbackExecutor



            ) {
                new DbSchemas(database, schemas, schemaHistory).create();
                doBaseline(schemaHistory, database, schemas, callbackExecutor);
                return null;
            }
        }, false);
    }

    /**
     * Repairs the Flyway schema history table. This will perform the following actions:
     * <ul>
     * <li>Remove any failed migrations on databases without DDL transactions (User objects left behind must still be cleaned up manually)</li>
     * <li>Realign the checksums, descriptions and types of the applied migrations with the ones of the available migrations</li>
     * </ul>
     * <img src="https://flywaydb.org/assets/balsamiq/command-repair.png" alt="repair">
     *
     * @throws FlywayException when the schema history table repair failed.
     */
    public void repair() throws FlywayException {
        execute(new Command<Void>() {
            public Void execute(MigrationResolver migrationResolver,
                                SchemaHistory schemaHistory, Database database, Schema[] schemas, CallbackExecutor callbackExecutor



            ) {
                new DbRepair(database, migrationResolver, schemaHistory, callbackExecutor, configuration).repair();
                return null;
            }
        }, true);
    }

    /**
     * Creates the MigrationResolver.
     *
     * @param database                   The database-specific support.
     * @param resourceProvider           The resource provider.
     * @param classProvider              The class provider.
     * @param sqlStatementBuilderFactory The SQL statement builder factory.
     * @return A new, fully configured, MigrationResolver instance.
     */
    private MigrationResolver createMigrationResolver(Database database,
                                                      ResourceProvider resourceProvider,
                                                      ClassProvider classProvider,
                                                      SqlStatementBuilderFactory sqlStatementBuilderFactory



    ) {
        for (MigrationResolver resolver : configuration.getResolvers()) {
            ConfigUtils.injectFlywayConfiguration(resolver, configuration);
        }

        return new CompositeMigrationResolver(database,
                resourceProvider, classProvider, configuration,
                sqlStatementBuilderFactory



                , configuration.getResolvers());
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration. Property names are
     * documented in the flyway maven plugin.
     * <p>To use a custom ClassLoader, the Flyway(ClassLoader) constructor must be called prior to calling this method.</p>
     *
     * @param properties Properties used for configuration.
     * @throws FlywayException when the configuration failed.
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void configure(Properties properties) {
        configure(ConfigUtils.propertiesToMap(properties));
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration. Property names are
     * documented in the flyway maven plugin.
     * <p>To use a custom ClassLoader, it must be passed to the Flyway constructor prior to calling this method.</p>
     *
     * @param props Properties used for configuration.
     * @throws FlywayException when the configuration failed.
     * @deprecated Direct configuration of the Flyway object has been deprecated and will be removed in Flyway 6.0. Use Flyway.configure() instead.
     */
    @Deprecated
    public void configure(Map<String, String> props) {
        configuration.configure(props);
    }

    /**
     * Executes this command with proper resource handling and cleanup.
     *
     * @param command The command to execute.
     * @param <T>     The type of the result.
     * @return The result of the command.
     */
    /*private -> testing*/ <T> T execute(Command<T> command, boolean scannerRequired) {
        T result;

        VersionPrinter.printVersion(



        );

        if (configuration.getDataSource() == null) {
            throw new FlywayException("Unable to connect to the database. Configure the url, user and password!");
        }












        Database database = null;
        try {
            database = DatabaseFactory.createDatabase(configuration, !dbConnectionInfoPrinted



            );
            dbConnectionInfoPrinted = true;
            LOG.debug("DDL Transactions Supported: " + database.supportsDdlTransactions());

            Schema[] schemas = prepareSchemas(database);

            ResourceProvider resourceProvider;
            ClassProvider classProvider;
            if (!scannerRequired && configuration.isSkipDefaultResolvers() && configuration.isSkipDefaultCallbacks()) {
                resourceProvider = NoopResourceProvider.INSTANCE;
                classProvider = NoopClassProvider.INSTANCE;
            } else {
                Scanner scanner = new Scanner(
                        Arrays.asList(configuration.getLocations()),
                        configuration.getClassLoader(),
                        configuration.getEncoding()



                );
                resourceProvider = scanner;
                classProvider = scanner;
            }

            SqlStatementBuilderFactory sqlStatementBuilderFactory = database.createSqlStatementBuilderFactory(



            );

            CallbackExecutor callbackExecutor = new DefaultCallbackExecutor(configuration, database, schemas[0],
                    prepareCallbacks(database, resourceProvider, sqlStatementBuilderFactory



                    ));

            result = command.execute(
                    createMigrationResolver(database, resourceProvider, classProvider, sqlStatementBuilderFactory



                    ),
                    SchemaHistoryFactory.getSchemaHistory(configuration, database, schemas[0]



                    ),
                    database,
                    schemas,
                    callbackExecutor



            );
        } finally {
            if (database != null) {
                database.close();
            }





            showMemoryUsage();
        }
        return result;
    }

    private void showMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long free = runtime.freeMemory();
        long total = runtime.totalMemory();
        long used = total - free;

        long totalMB = total / (1024 * 1024);
        long usedMB = used / (1024 * 1024);
        LOG.debug("Memory usage: " + usedMB + " of " + totalMB + "M");
    }

    private Schema[] prepareSchemas(Database database) {
        String[] schemaNames = configuration.getSchemas();
        if (schemaNames.length == 0) {
            Schema currentSchema = database.getMainConnection().getCurrentSchema();
            if (currentSchema == null) {
                throw new FlywayException("Unable to determine schema for the schema history table." +
                        " Set a default schema for the connection or specify one using the schemas property!");
            }
            schemaNames = new String[]{currentSchema.getName()};
        }

        if (schemaNames.length == 1) {
            LOG.debug("Schema: " + schemaNames[0]);
        } else {
            LOG.debug("Schemas: " + StringUtils.arrayToCommaDelimitedString(schemaNames));
        }

        Schema[] schemas = new Schema[schemaNames.length];
        for (int i = 0; i < schemaNames.length; i++) {
            schemas[i] = database.getMainConnection().getSchema(schemaNames[i]);
        }
        return schemas;
    }

    private List<Callback> prepareCallbacks(Database database, ResourceProvider resourceProvider,
                                            SqlStatementBuilderFactory sqlStatementBuilderFactory





    ) {
        List<Callback> effectiveCallbacks = new ArrayList<>();


















        effectiveCallbacks.addAll(Arrays.asList(configuration.getCallbacks()));

        if (!configuration.isSkipDefaultCallbacks()) {
            effectiveCallbacks.addAll(
                    new SqlScriptCallbackFactory(
                            database,
                            resourceProvider,
                            sqlStatementBuilderFactory,
                            configuration



                    ).getCallbacks());
        }








        for (Callback callback : effectiveCallbacks) {
            if (callback instanceof LegacyCallback) {
                ConfigUtils.injectFlywayConfiguration(callback, configuration);
            }
        }
        return effectiveCallbacks;
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
         * @param migrationResolver The migration resolver to use.
         * @param schemaHistory     The schema history table.
         * @param database          The database-specific support for these connections.
         * @param schemas           The schemas managed by Flyway.
         * @param callbackExecutor  The callback executor.
         * @return The result of the operation.
         */
        T execute(MigrationResolver migrationResolver, SchemaHistory schemaHistory,
                  Database database, Schema[] schemas, CallbackExecutor callbackExecutor



        );
    }
}