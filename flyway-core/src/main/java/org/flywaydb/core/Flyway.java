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
import org.flywaydb.core.internal.callback.LegacyCallback;
import org.flywaydb.core.internal.callback.SqlScriptFlywayCallbackFactory;
import org.flywaydb.core.internal.command.DbBaseline;
import org.flywaydb.core.internal.command.DbClean;
import org.flywaydb.core.internal.command.DbInfo;
import org.flywaydb.core.internal.command.DbMigrate;
import org.flywaydb.core.internal.command.DbRepair;
import org.flywaydb.core.internal.command.DbSchemas;
import org.flywaydb.core.internal.command.DbValidate;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.DatabaseFactory;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.schemahistory.SchemaHistoryFactory;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.VersionPrinter;
import org.flywaydb.core.internal.util.scanner.Scanner;

import javax.sql.DataSource;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is the centre point of Flyway, and for most users, the only class they will ever have to deal with.
 * <p>
 * It is THE public API from which all important Flyway functions such as clean, validate and migrate can be called.
 * </p>
 * <p>To get started all you need to do is <code>Flyway.config().dataSource(url, user, password).load().migrate();</code></p>
 * <p>Alternatively you can also use a <code>ClassicConfiguration</code> instance in combination with <code>new Flyway(configuration).migrate();</code></p>
 * <p>Note that starting with Flyway 6.0, this class will no longer implement <code>FlywayConfiguration</code> and you
 * should migrate to one of the configuration styles mentioned above instead.</p>
 */
public class Flyway implements FlywayConfiguration {
    private static final Log LOG = LogFactory.getLog(Flyway.class);

    /**
     * Configure a new Flyway instance. Call the <code>load()</code> method once you are ready to start using Flyway.
     *
     * @return A FluentConfiguration object.
     */
    public static FluentConfiguration config() {
        return new FluentConfiguration();
    }

    /**
     * Configure a new Flyway instance. Call the <code>load()</code> method once you are ready to start using Flyway.
     *
     * @param classLoader The class loader to use when instantiating classes.
     * @return A FluentConfiguration object.
     */
    public static FluentConfiguration config(ClassLoader classLoader) {
        return new FluentConfiguration(classLoader);
    }

    private final ClassicConfiguration configuration;

    /**
     * Whether the database connection info has already been printed in the logs.
     */
    private boolean dbConnectionInfoPrinted;

    /**
     * Creates a new instance of Flyway. This is your starting point.
     *
     * @deprecated Use {@link Flyway#config()} instead. Will be removed in Flyway 6.0.
     */
    @Deprecated
    public Flyway() {
        LOG.warn("The Flyway() constructor has been deprecated and will be removed in Flyway 6.0. Use Flyway.config() instead.");
        configuration = new ClassicConfiguration();
    }

    /**
     * Creates a new instance of Flyway. This is your starting point.
     *
     * @param classLoader The ClassLoader to use for loading migrations, resolvers, etc from the classpath. (default: Thread.currentThread().getContextClassLoader() )
     * @deprecated Use {@link Flyway#config(ClassLoader)} instead. Will be removed in Flyway 6.0.
     */
    @Deprecated
    public Flyway(ClassLoader classLoader) {
        LOG.warn("The Flyway(ClassLoader) constructor has been deprecated and will be removed in Flyway 6.0. Use Flyway.config(ClassLoader) instead.");
        configuration = new ClassicConfiguration(classLoader);
    }

    /**
     * Creates a new instance of Flyway with this configuration. This is primarily meant for {@link ClassicConfiguration}
     * cases. For all other usages, prefer {@link Flyway#config()}.
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
     * Creates a new instance of Flyway with this configuration. This is primarily meant for {@link ClassicConfiguration}
     * cases. For all other usages, prefer {@link Flyway#config()}.
     *
     * @param configuration The configuration to use.
     */
    public Flyway(Configuration configuration) {
        this.configuration = new ClassicConfiguration(configuration);
    }

    @Override
    @Deprecated
    public Location[] getLocations() {
        LOG.warn("Flyway.getLocations() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getLocations();
    }

    @Override
    @Deprecated
    public String getEncoding() {
        LOG.warn("Flyway.getEncoding() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getEncoding();
    }

    @Override
    @Deprecated
    public String[] getSchemas() {
        LOG.warn("Flyway.getSchemas() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getSchemas();
    }

    @Override
    @Deprecated
    public String getTable() {
        LOG.warn("Flyway.getTable() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getTable();
    }

    @Override
    @Deprecated
    public MigrationVersion getTarget() {
        LOG.warn("Flyway.getTarget() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getTarget();
    }

    @Override
    @Deprecated
    public boolean isPlaceholderReplacement() {
        LOG.warn("Flyway.isPlaceholderReplacement() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.isPlaceholderReplacement();
    }

    @Override
    @Deprecated
    public Map<String, String> getPlaceholders() {
        LOG.warn("Flyway.getPlaceholders() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getPlaceholders();
    }

    @Override
    @Deprecated
    public String getPlaceholderPrefix() {
        LOG.warn("Flyway.getPlaceholderPrefix() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getPlaceholderPrefix();
    }

    @Override
    @Deprecated
    public String getPlaceholderSuffix() {
        LOG.warn("Flyway.getPlaceholderSuffix() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getPlaceholderSuffix();
    }

    @Override
    @Deprecated
    public String getSqlMigrationPrefix() {
        LOG.warn("Flyway.getSqlMigrationPrefix() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getSqlMigrationPrefix();
    }

    @Override
    @Deprecated
    public String getRepeatableSqlMigrationPrefix() {
        LOG.warn("Flyway.getRepeatableSqlMigrationPrefix() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getRepeatableSqlMigrationPrefix();
    }

    @Override
    @Deprecated
    public String getSqlMigrationSeparator() {
        LOG.warn("Flyway.getSqlMigrationSeparator() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getSqlMigrationSeparator();
    }

    @Override
    @Deprecated
    public String getSqlMigrationSuffix() {
        LOG.warn("sqlMigrationSuffix has been deprecated and will be removed in Flyway 6.0.0. Use sqlMigrationSuffixes instead.");
        return configuration.getSqlMigrationSuffixes()[0];
    }

    @Override
    @Deprecated
    public String[] getSqlMigrationSuffixes() {
        LOG.warn("Flyway.getSqlMigrationSuffixes() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getSqlMigrationSuffixes();
    }

    @Override
    @Deprecated
    public boolean isIgnoreMissingMigrations() {
        LOG.warn("Flyway.isIgnoreMissingMigrations() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.isIgnoreMissingMigrations();
    }

    @Override
    @Deprecated
    public boolean isIgnoreIgnoredMigrations() {
        LOG.warn("Flyway.isIgnoreIgnoredMigrations() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.isIgnoreIgnoredMigrations();
    }

    @Override
    @Deprecated
    public boolean isIgnoreFutureMigrations() {
        LOG.warn("Flyway.isIgnoreFutureMigrations() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.isIgnoreFutureMigrations();
    }

    @Override
    @Deprecated
    public boolean isValidateOnMigrate() {
        LOG.warn("Flyway.isValidateOnMigrate() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.isValidateOnMigrate();
    }

    @Override
    @Deprecated
    public boolean isCleanOnValidationError() {
        LOG.warn("Flyway.isCleanOnValidationError() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.isCleanOnValidationError();
    }

    @Override
    @Deprecated
    public boolean isCleanDisabled() {
        LOG.warn("Flyway.isCleanDisabled() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.isCleanDisabled();
    }

    @Override
    @Deprecated
    public MigrationVersion getBaselineVersion() {
        LOG.warn("Flyway.getBaselineVersion() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getBaselineVersion();
    }

    @Override
    @Deprecated
    public String getBaselineDescription() {
        LOG.warn("Flyway.getBaselineDescription() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getBaselineDescription();
    }

    @Override
    @Deprecated
    public boolean isBaselineOnMigrate() {
        LOG.warn("Flyway.isBaselineOnMigrate() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.isBaselineOnMigrate();
    }

    @Override
    @Deprecated
    public boolean isOutOfOrder() {
        LOG.warn("Flyway.isOutOfOrder() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.isOutOfOrder();
    }

    @Override
    @Deprecated
    public MigrationResolver[] getResolvers() {
        LOG.warn("Flyway.getResolvers() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getResolvers();
    }

    @Override
    @Deprecated
    public boolean isSkipDefaultResolvers() {
        LOG.warn("Flyway.isSkipDefaultResolvers() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.isSkipDefaultResolvers();
    }

    @Override
    @Deprecated
    public DataSource getDataSource() {
        LOG.warn("Flyway.getDataSource() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getDataSource();
    }

    @Override
    @Deprecated
    public ClassLoader getClassLoader() {
        LOG.warn("Flyway.getClassLoader() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getClassLoader();
    }

    @Override
    @Deprecated
    public boolean isMixed() {
        LOG.warn("Flyway.isMixed() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.isMixed();
    }

    @Override
    @Deprecated
    public String getInstalledBy() {
        LOG.warn("Flyway.getInstalledBy() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getInstalledBy();
    }

    @Override
    @Deprecated
    public boolean isGroup() {
        LOG.warn("Flyway.isGroup() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.isGroup();
    }

    @Override
    @Deprecated
    public ErrorHandler[] getErrorHandlers() {
        LOG.warn("Flyway.getErrorHandlers() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getErrorHandlers();
    }

    @Override
    @Deprecated
    public OutputStream getDryRunOutput() {
        LOG.warn("Flyway.getDryRunOutput() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getDryRunOutput();
    }

    /**
     * Sets the stream where to output the SQL statements of a migration dry run. {@code null} to execute the SQL statements
     * directly against the database. The stream when be closing when Flyway finishes writing the output.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param dryRunOutput The output file or {@code null} to execute the SQL statements directly against the database.
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setDryRunOutput(OutputStream dryRunOutput) {
        LOG.warn("Flyway.setDryRunOutput() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setDryRunOutput(dryRunOutput);
    }

    /**
     * Sets the file where to output the SQL statements of a migration dry run. {@code null} to execute the SQL statements
     * directly against the database. If the file specified is in a non-existent directory, Flyway will create all
     * directories and parent directories as needed.
     * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
     *
     * @param dryRunOutput The output file or {@code null} to execute the SQL statements directly against the database.
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setDryRunOutputAsFile(File dryRunOutput) {
        LOG.warn("Flyway.setDryRunOutputAsFile() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
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
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setDryRunOutputAsFileName(String dryRunOutputFileName) {
        LOG.warn("Flyway.setDryRunOutputAsFileName() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
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
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setErrorHandlers(ErrorHandler... errorHandlers) {
        LOG.warn("Flyway.setErrorHandlers() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
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
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setErrorHandlersAsClassNames(String... errorHandlerClassNames) {
        LOG.warn("Flyway.setErrorHandlersAsClassNames() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setErrorHandlersAsClassNames(errorHandlerClassNames);
    }

    /**
     * Whether to group all pending migrations together in the same transaction when applying them (only recommended for databases with support for DDL transactions).
     *
     * @param group {@code true} if migrations should be grouped. {@code false} if they should be applied individually instead. (default: {@code false})
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setGroup(boolean group) {
        LOG.warn("Flyway.setGroup() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setGroup(group);
    }

    /**
     * The username that will be recorded in the schema history table as having applied the migration.
     *
     * @param installedBy The username or {@code null} for the current database user of the connection. (default: {@code null}).
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setInstalledBy(String installedBy) {
        LOG.warn("Flyway.setInstalledBy() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setInstalledBy(installedBy);
    }

    /**
     * Whether to allow mixing transactional and non-transactional statements within the same migration.
     *
     * @param mixed {@code true} if mixed migrations should be allowed. {@code false} if an error should be thrown instead. (default: {@code false})
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setMixed(boolean mixed) {
        LOG.warn("Flyway.setMixed() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
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
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setIgnoreMissingMigrations(boolean ignoreMissingMigrations) {
        LOG.warn("Flyway.setIgnoreMissingMigrations() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
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
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setIgnoreIgnoredMigrations(boolean ignoreIgnoredMigrations) {
        LOG.warn("Flyway.setIgnoreIgnoredMigrations() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
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
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setIgnoreFutureMigrations(boolean ignoreFutureMigrations) {
        LOG.warn("Flyway.setIgnoreFutureMigrations() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setIgnoreFutureMigrations(ignoreFutureMigrations);
    }

    /**
     * Whether to automatically call validate or not when running migrate.
     *
     * @param validateOnMigrate {@code true} if validate should be called. {@code false} if not. (default: {@code true})
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setValidateOnMigrate(boolean validateOnMigrate) {
        LOG.warn("Flyway.setValidateOnMigrate() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
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
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setCleanOnValidationError(boolean cleanOnValidationError) {
        LOG.warn("Flyway.setCleanOnValidationError() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setCleanOnValidationError(cleanOnValidationError);
    }

    /**
     * Whether to disable clean.
     * <p>This is especially useful for production environments where running clean can be quite a career limiting move.</p>
     *
     * @param cleanDisabled {@code true} to disabled clean. {@code false} to leave it enabled.  (default: {@code false})
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setCleanDisabled(boolean cleanDisabled) {
        LOG.warn("Flyway.setCleanDisabled() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
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
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setLocations(String... locations) {
        LOG.warn("Flyway.setLocations() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setLocationsAsStrings(locations);
    }

    /**
     * Sets the encoding of Sql migrations.
     *
     * @param encoding The encoding of Sql migrations. (default: UTF-8)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setEncoding(String encoding) {
        LOG.warn("Flyway.setEncoding() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setEncoding(encoding);
    }

    /**
     * Sets the schemas managed by Flyway. These schema names are case-sensitive. (default: The default schema for the datasource connection)
     * <p>Consequences:</p>
     * <ul>
     * <li>The first schema in the list will be automatically set as the default one during the migration.</li>
     * <li>The first schema in the list will also be the one containing the schema history table.</li>
     * <li>The schemas will be cleaned in the order of this list.</li>
     * </ul>
     *
     * @param schemas The schemas managed by Flyway. May not be {@code null}. Must contain at least one element.
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setSchemas(String... schemas) {
        LOG.warn("Flyway.setSchemas() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setSchemas(schemas);
    }

    /**
     * <p>Sets the name of the schema schema history table that will be used by Flyway.</p><p> By default (single-schema mode)
     * the schema history table is placed in the default schema for the connection provided by the datasource. </p> <p> When
     * the <i>flyway.schemas</i> property is set (multi-schema mode), the schema history table is placed in the first schema
     * of the list. </p>
     *
     * @param table The name of the schema schema history table that will be used by flyway. (default: flyway_schema_history)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setTable(String table) {
        LOG.warn("Flyway.setTable() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setTable(table);
    }

    /**
     * Sets the target version up to which Flyway should consider migrations. Migrations with a higher version number will
     * be ignored.
     *
     * @param target The target version up to which Flyway should consider migrations. (default: the latest version)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setTarget(MigrationVersion target) {
        LOG.warn("Flyway.setTarget() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setTarget(target);
    }

    /**
     * Sets the target version up to which Flyway should consider migrations.
     * Migrations with a higher version number will be ignored.
     *
     * @param target The target version up to which Flyway should consider migrations.
     *               The special value {@code current} designates the current version of the schema. (default: the latest
     *               version)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setTargetAsString(String target) {
        LOG.warn("Flyway.setTargetAsString() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setTargetAsString(target);
    }

    /**
     * Sets whether placeholders should be replaced.
     *
     * @param placeholderReplacement Whether placeholders should be replaced. (default: true)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setPlaceholderReplacement(boolean placeholderReplacement) {
        LOG.warn("Flyway.setPlaceholderReplacement() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setPlaceholderReplacement(placeholderReplacement);
    }

    /**
     * Sets the placeholders to replace in sql migration scripts.
     *
     * @param placeholders The map of &lt;placeholder, replacementValue&gt; to apply to sql migration scripts.
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setPlaceholders(Map<String, String> placeholders) {
        LOG.warn("Flyway.setPlaceholders() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setPlaceholders(placeholders);
    }

    /**
     * Sets the prefix of every placeholder.
     *
     * @param placeholderPrefix The prefix of every placeholder. (default: ${ )
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setPlaceholderPrefix(String placeholderPrefix) {
        LOG.warn("Flyway.setPlaceholderPrefix() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setPlaceholderPrefix(placeholderPrefix);
    }

    /**
     * Sets the suffix of every placeholder.
     *
     * @param placeholderSuffix The suffix of every placeholder. (default: } )
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setPlaceholderSuffix(String placeholderSuffix) {
        LOG.warn("Flyway.setPlaceholderSuffix() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setPlaceholderSuffix(placeholderSuffix);
    }

    /**
     * Sets the file name prefix for sql migrations.
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     *
     * @param sqlMigrationPrefix The file name prefix for sql migrations (default: V)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
        LOG.warn("Flyway.setSqlMigrationPrefix() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setSqlMigrationPrefix(sqlMigrationPrefix);
    }

    @Override
    @Deprecated
    public String getUndoSqlMigrationPrefix() {
        LOG.warn("Flyway.getUndoSqlMigrationPrefix() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
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
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setUndoSqlMigrationPrefix(String undoSqlMigrationPrefix) {
        LOG.warn("Flyway.setUndoSqlMigrationPrefix() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setUndoSqlMigrationPrefix(undoSqlMigrationPrefix);
    }

    /**
     * Sets the file name prefix for repeatable sql migrations.
     * <p>Repeatable sql migrations have the following file name structure: prefixSeparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to R__My_description.sql</p>
     *
     * @param repeatableSqlMigrationPrefix The file name prefix for repeatable sql migrations (default: R)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setRepeatableSqlMigrationPrefix(String repeatableSqlMigrationPrefix) {
        LOG.warn("Flyway.setRepeatableSqlMigrationPrefix() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setRepeatableSqlMigrationPrefix(repeatableSqlMigrationPrefix);
    }

    /**
     * Sets the file name separator for sql migrations.
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     *
     * @param sqlMigrationSeparator The file name separator for sql migrations (default: __)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setSqlMigrationSeparator(String sqlMigrationSeparator) {
        LOG.warn("Flyway.setSqlMigrationSeparator() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setSqlMigrationSeparator(sqlMigrationSeparator);
    }

    /**
     * Sets the file name suffix for sql migrations.
     * <p>Sql migrations have the following file name structure: prefixVERSIONseparatorDESCRIPTIONsuffix ,
     * which using the defaults translates to V1_1__My_description.sql</p>
     *
     * @param sqlMigrationSuffix The file name suffix for sql migrations (default: .sql)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setSqlMigrationSuffix(String sqlMigrationSuffix) {
        LOG.warn("sqlMigrationSuffix has been deprecated and will be removed in Flyway 6.0.0. Use sqlMigrationSuffixes instead.");
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
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setSqlMigrationSuffixes(String... sqlMigrationSuffixes) {
        LOG.warn("Flyway.setSqlMigrationSuffixes() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setSqlMigrationSuffixes(sqlMigrationSuffixes);
    }

    /**
     * Sets the datasource to use. Must have the necessary privileges to execute ddl.
     *
     * @param dataSource The datasource to use. Must have the necessary privileges to execute ddl.
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setDataSource(DataSource dataSource) {
        LOG.warn("Flyway.setDataSource() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
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
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setDataSource(String url, String user, String password, String... initSqls) {
        LOG.warn("Flyway.setDataSource() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setDataSource(url, user, password, initSqls);
    }

    /**
     * Sets the ClassLoader to use for resolving migrations on the classpath.
     *
     * @param classLoader The ClassLoader to use for loading migrations, resolvers, etc from the classpath. (default: Thread.currentThread().getContextClassLoader() )
     * @deprecated Will be removed in Flyway 6.0. Use {@link Flyway#config(ClassLoader)} instead.
     */
    @Deprecated
    public void setClassLoader(ClassLoader classLoader) {
        LOG.warn("Flyway.setClassLoader() has been deprecated and will be removed in Flyway 6.0. Use {@link Flyway#config(ClassLoader)} instead.");
        configuration.setClassLoader(classLoader);
    }

    /**
     * Sets the version to tag an existing schema with when executing baseline.
     *
     * @param baselineVersion The version to tag an existing schema with when executing baseline. (default: 1)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setBaselineVersion(MigrationVersion baselineVersion) {
        LOG.warn("Flyway.setBaselineVersion() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setBaselineVersion(baselineVersion);
    }

    /**
     * Sets the version to tag an existing schema with when executing baseline.
     *
     * @param baselineVersion The version to tag an existing schema with when executing baseline. (default: 1)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setBaselineVersionAsString(String baselineVersion) {
        LOG.warn("Flyway.setBaselineVersionAsString() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setBaselineVersionAsString(baselineVersion);
    }

    /**
     * Sets the description to tag an existing schema with when executing baseline.
     *
     * @param baselineDescription The description to tag an existing schema with when executing baseline. (default: &lt;&lt; Flyway Baseline &gt;&gt;)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setBaselineDescription(String baselineDescription) {
        LOG.warn("Flyway.setBaselineDescription() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
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
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setBaselineOnMigrate(boolean baselineOnMigrate) {
        LOG.warn("Flyway.setBaselineOnMigrate() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setBaselineOnMigrate(baselineOnMigrate);
    }

    /**
     * Allows migrations to be run "out of order".
     * <p>If you already have versions 1 and 3 applied, and now a version 2 is found,
     * it will be applied too instead of being ignored.</p>
     *
     * @param outOfOrder {@code true} if outOfOrder migrations should be applied, {@code false} if not. (default: {@code false})
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setOutOfOrder(boolean outOfOrder) {
        LOG.warn("Flyway.setOutOfOrder() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setOutOfOrder(outOfOrder);
    }

    @Override
    @Deprecated
    public Callback[] getCallbacks() {
        LOG.warn("Flyway.getCallbacks() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.getCallbacks();
    }

    @Override
    @Deprecated
    public boolean isSkipDefaultCallbacks() {
        LOG.warn("Flyway.isSkipDefaultCallbacks() has been deprecated and will be removed in Flyway 6.0. Use the same method on Flyway.getConfiguration() instead.");
        return configuration.isSkipDefaultCallbacks();
    }

    /**
     * Set the callbacks for lifecycle notifications.
     *
     * @param callbacks The callbacks for lifecycle notifications. (default: none)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setCallbacks(FlywayCallback... callbacks) {
        LOG.warn("Flyway.setCallbacks() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        List<Callback> l = new ArrayList<>();
        for (FlywayCallback callback : callbacks) {
            l.add(new LegacyCallback(callback));
        }
        configuration.setCallbacks(l.toArray(new Callback[l.size()]));
    }

    /**
     * Set the callbacks for lifecycle notifications.
     *
     * @param callbacks The fully qualified class names of the callbacks for lifecycle notifications. (default: none)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setCallbacksAsClassNames(String... callbacks) {
        LOG.warn("Flyway.setCallbacksAsClassNames() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setCallbacksAsClassNames(callbacks);
    }

    /**
     * Whether Flyway should skip the default callbacks. If true, only custom callbacks are used.
     *
     * @param skipDefaultCallbacks Whether default built-in callbacks should be skipped. <p>(default: false)</p>
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setSkipDefaultCallbacks(boolean skipDefaultCallbacks) {
        LOG.warn("Flyway.setSkipDefaultCallbacks() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setSkipDefaultCallbacks(skipDefaultCallbacks);
    }

    /**
     * Sets custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
     *
     * @param resolvers The custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply. (default: empty list)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setResolvers(MigrationResolver... resolvers) {
        LOG.warn("Flyway.setResolvers() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setResolvers(resolvers);
    }

    /**
     * Sets custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.
     *
     * @param resolvers The fully qualified class names of the custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply. (default: empty list)
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setResolversAsClassNames(String... resolvers) {
        LOG.warn("Flyway.setResolversAsClassNames() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setResolversAsClassNames(resolvers);
    }

    /**
     * Whether Flyway should skip the default resolvers. If true, only custom resolvers are used.
     *
     * @param skipDefaultResolvers Whether default built-in resolvers should be skipped. <p>(default: false)</p>
     * @deprecated Setters on the Flyway class have been deprecated and will be removed in Flyway 6.0. Use the matching method on {@link Flyway#config()} instead.
     */
    @Deprecated
    public void setSkipDefaultResolvers(boolean skipDefaultResolvers) {
        LOG.warn("Flyway.setSkipDefaultResolvers() has been deprecated and will be removed in Flyway 6.0. Use the matching method on Flyway.config() instead.");
        configuration.setSkipDefaultResolvers(skipDefaultResolvers);
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
                    doValidate(database, migrationResolver, schemaHistory, schemas, callbackExecutor, true);
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
        });
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

        throw new org.flywaydb.core.internal.exception.FlywayProUpgradeRequiredException("undo");












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
            public Void execute(MigrationResolver migrationResolver, SchemaHistory schemaHistory, Database database,
                                Schema[] schemas, CallbackExecutor callbackExecutor



            ) {
                doValidate(database, migrationResolver, schemaHistory, schemas, callbackExecutor, false);
                return null;
            }
        });
    }

    /**
     * Performs the actual validation. All set up must have taken place beforehand.
     *
     * @param database          The database-specific support.
     * @param migrationResolver The migration resolver;
     * @param schemaHistory     The schema history table.
     * @param schemas           The schemas managed by Flyway.
     * @param callbackExecutor  The callback executor.
     * @param pending           Whether pending migrations are ok.
     */
    private void doValidate(Database database, MigrationResolver migrationResolver,
                            SchemaHistory schemaHistory, Schema[] schemas, CallbackExecutor callbackExecutor, boolean pending) {
        String validationError =
                new DbValidate(database, schemaHistory, schemas[0], migrationResolver,
                        configuration.getTarget(), configuration.isOutOfOrder(), pending,
                        configuration.isIgnoreMissingMigrations(),
                        configuration.isIgnoreIgnoredMigrations(),
                        configuration.isIgnoreFutureMigrations(),
                        callbackExecutor).validate();

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
            public MigrationInfoService execute(MigrationResolver migrationResolver, SchemaHistory schemaHistory,
                                                final Database database, final Schema[] schemas, CallbackExecutor callbackExecutor



            ) {
                return new DbInfo(migrationResolver, schemaHistory, configuration, callbackExecutor).info();
            }
        });
    }

    /**
     * <p>Baselines an existing database, excluding all migrations up to and including baselineVersion.</p>
     * <p>
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
        });
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
                new DbRepair(database, migrationResolver, schemaHistory, callbackExecutor).repair();
                return null;
            }
        });
    }

    /**
     * Creates the MigrationResolver.
     *
     * @param database            The database-specific support.
     * @param scanner             The Scanner for resolving migrations.
     * @param placeholderReplacer The placeholder replacer.
     * @return A new, fully configured, MigrationResolver instance.
     */
    private MigrationResolver createMigrationResolver(Database database, Scanner scanner,
                                                      PlaceholderReplacer placeholderReplacer) {
        for (MigrationResolver resolver : configuration.getResolvers()) {
            ConfigUtils.injectFlywayConfiguration(resolver, configuration);
        }

        return new CompositeMigrationResolver(database, scanner, configuration,
                Arrays.asList(configuration.getLocations()),
                placeholderReplacer, configuration.getResolvers());
    }

    /**
     * @return A new, fully configured, PlaceholderReplacer.
     */
    private PlaceholderReplacer createPlaceholderReplacer() {
        if (configuration.isPlaceholderReplacement()) {
            return new PlaceholderReplacer(configuration.getPlaceholders(), configuration.getPlaceholderPrefix(),
                    configuration.getPlaceholderSuffix());
        }
        return PlaceholderReplacer.NO_PLACEHOLDERS;
    }

    /**
     * Configures Flyway with these properties. This overwrites any existing configuration. Property names are
     * documented in the flyway maven plugin.
     * <p>To use a custom ClassLoader, setClassLoader() must be called prior to calling this method.</p>
     *
     * @param properties Properties used for configuration.
     * @throws FlywayException when the configuration failed.
     * @deprecated {@link Flyway#configure(Properties)} has been deprecated and will be removed in Flyway 6.0. Use Flyway.config().configure().load() instead.
     */
    @SuppressWarnings("ConstantConditions")
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
     * @deprecated {@link Flyway#configure(Map)} has been deprecated and will be removed in Flyway 6.0. Use Flyway.config().configure().load() instead.
     */
    @Deprecated
    public void configure(Map<String, String> props) {
        LOG.warn("Flyway.configure() has been deprecated and will be removed in Flyway 6.0. Use Flyway.config().configure().load() instead.");
        configuration.configure(props);
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
            Scanner scanner = new Scanner(configuration.getClassLoader());
            PlaceholderReplacer placeholderReplacer = createPlaceholderReplacer();
            result = command.execute(
                    createMigrationResolver(database, scanner, placeholderReplacer),
                    SchemaHistoryFactory.getSchemaHistory(configuration, database, schemas[0]



                    ),
                    database,
                    schemas,
                    new CallbackExecutor(configuration, database, schemas[0],
                            prepareCallbacks(database, scanner, placeholderReplacer



                            ))



            );
        } finally {
            if (database != null) {
                database.close();
            }





        }
        return result;
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

    private List<Callback> prepareCallbacks(Database database, Scanner scanner, PlaceholderReplacer placeholderReplacer



    ) {
        List<Callback> effectiveCallbacks = new ArrayList<>();






        effectiveCallbacks.addAll(Arrays.asList(configuration.getCallbacks()));

        if (!configuration.isSkipDefaultCallbacks()) {
            effectiveCallbacks.addAll(new SqlScriptFlywayCallbackFactory(database, scanner,
                    Arrays.asList(configuration.getLocations()), placeholderReplacer, configuration).getCallbacks());
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