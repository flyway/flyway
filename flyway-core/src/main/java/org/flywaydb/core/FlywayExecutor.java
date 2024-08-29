/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.core;

import lombok.CustomLog;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.RootTelemetryModel;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.callback.*;

import org.flywaydb.core.internal.clazz.NoopClassProvider;
import org.flywaydb.core.internal.configuration.ConfigurationValidator;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.base.CommunityDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.resolver.script.ScriptMigrationResolver;
import org.flywaydb.core.internal.resource.NoopResourceProvider;
import org.flywaydb.core.internal.resource.ResourceNameValidator;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.scanner.LocationScannerCache;
import org.flywaydb.core.internal.scanner.ResourceNameCache;
import org.flywaydb.core.internal.scanner.Scanner;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.schemahistory.SchemaHistoryFactory;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;
import org.flywaydb.core.internal.strategy.RetryStrategy;
import org.flywaydb.core.internal.util.FileUtils;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.flywaydb.core.internal.database.DatabaseTypeRegister.redactJdbcUrl;
import static org.flywaydb.core.internal.util.DataUnits.MEGABYTE;

import org.flywaydb.core.internal.license.FlywayExpiredLicenseKeyException;

@CustomLog
public class FlywayExecutor {
    public interface Command<T> {
        T execute(CompositeMigrationResolver migrationResolver, SchemaHistory schemaHistory, Database database,
                  Schema defaultSchema, Schema[] schemas, CallbackExecutor callbackExecutor, StatementInterceptor statementInterceptor);
    }

    /**
     * Designed so we can fail fast if the configuration is invalid
     */
    private final ConfigurationValidator configurationValidator;
    /**
     * Designed so we can fail fast if the SQL file resources are invalid
     */
    private final ResourceNameValidator resourceNameValidator;
    /**
     * Used to cache resource names for classpath scanning between commands
     */
    private final ResourceNameCache resourceNameCache;
    /**
     * Used to cache LocationScanners between commands
     */
    private final LocationScannerCache locationScannerCache;
    /**
     * Whether the database connection info has already been printed in the logs
     */
    private boolean dbConnectionInfoPrinted;
    private final Configuration configuration;

    public FlywayExecutor(Configuration configuration) {
        this.configurationValidator = new ConfigurationValidator();
        this.resourceNameValidator = new ResourceNameValidator();
        this.resourceNameCache = new ResourceNameCache();
        this.locationScannerCache = new LocationScannerCache();
        this.configuration = configuration;
    }

    /**
     * Executes this command with proper resource handling and cleanup.
     *
     * @param command The command to execute.
     * @param <T> The type of the result.
     * @return The result of the command.
     */
    public <T> T execute(Command<T> command, boolean scannerRequired, FlywayTelemetryManager flywayTelemetryManager) {
        T result;

        configurationValidator.validate(configuration);

        StatementInterceptor statementInterceptor = configuration.getPluginRegister().getPlugins(StatementInterceptor.class).stream()
                                                                 .filter(i -> i.isConfigured(configuration))
                                                                 .findFirst()
                                                                 .orElse(null);

        final Pair<ResourceProvider, ClassProvider<JavaMigration>> resourceProviderClassProviderPair = createResourceAndClassProviders(scannerRequired);
        final ResourceProvider resourceProvider = resourceProviderClassProviderPair.getLeft();
        final ClassProvider<JavaMigration> classProvider = resourceProviderClassProviderPair.getRight();
        final ParsingContext parsingContext = new ParsingContext();






        resourceNameValidator.validateSQLMigrationNaming(resourceProvider, configuration);

        JdbcConnectionFactory jdbcConnectionFactory = new JdbcConnectionFactory(configuration.getDataSource(), configuration, statementInterceptor);

        final DatabaseType databaseType = jdbcConnectionFactory.getDatabaseType();
        final SqlScriptFactory sqlScriptFactory = databaseType.createSqlScriptFactory(configuration, parsingContext);
        RetryStrategy.setNumberOfRetries(configuration.getLockRetryCount());

        final SqlScriptExecutorFactory noCallbackSqlScriptExecutorFactory = databaseType.createSqlScriptExecutorFactory(
                jdbcConnectionFactory, NoopCallbackExecutor.INSTANCE, null);

        jdbcConnectionFactory.setConnectionInitializer((jdbcConnectionFactory1, connection) -> {
            if (configuration.getInitSql() == null) {
                return;
            }
            StringResource resource = new StringResource(configuration.getInitSql());

            SqlScript sqlScript = sqlScriptFactory.createSqlScript(resource, true, resourceProvider);

            boolean outputQueryResults = configuration.isOutputQueryResults();

            noCallbackSqlScriptExecutorFactory.createSqlScriptExecutor(connection, false, false, outputQueryResults).execute(sqlScript, configuration);
        });

        Database database = null;
        try {
            database = databaseType.createDatabase(configuration, jdbcConnectionFactory, statementInterceptor);

            if (!dbConnectionInfoPrinted) {
                dbConnectionInfoPrinted = true;

                if (database.getDatabaseType() instanceof CommunityDatabaseType) {
                    LOG.info(((CommunityDatabaseType) database.getDatabaseType()).announcementForCommunitySupport());
                }

                LOG.info("Database: " + redactJdbcUrl(jdbcConnectionFactory.getJdbcUrl()) + " (" + jdbcConnectionFactory.getProductName() + ")");
                LOG.debug("Database Type: " + database.getDatabaseType().getName());
                LOG.debug("Driver: " + jdbcConnectionFactory.getDriverInfo());

                if (flywayTelemetryManager != null) {
                    RootTelemetryModel rootTelemetryModel = flywayTelemetryManager.getRootTelemetryModel();
                    if (rootTelemetryModel != null) {
                        rootTelemetryModel.setDatabaseEngine(database.getDatabaseType().getName());
                        rootTelemetryModel.setDatabaseVersion(database.getVersion().toString());
                        rootTelemetryModel.setDatabaseHosting(database.getDatabaseHosting());
                    }
                }
            }

            LOG.debug("DDL Transactions Supported: " + database.supportsDdlTransactions());

            Pair<Schema, List<Schema>> schemas = SchemaHistoryFactory.prepareSchemas(configuration, database);
            Schema defaultSchema = schemas.getLeft();

            if (statementInterceptor != null) {
                statementInterceptor.init(configuration, database, defaultSchema.getTable(configuration.getTable()));
            }

            parsingContext.populate(database, configuration);

            database.ensureSupported(configuration);

            DefaultCallbackExecutor callbackExecutor = new DefaultCallbackExecutor(configuration, database, defaultSchema, flywayTelemetryManager, prepareCallbacks(
                    database, resourceProvider, jdbcConnectionFactory, sqlScriptFactory, statementInterceptor, defaultSchema, parsingContext, flywayTelemetryManager));

            SqlScriptExecutorFactory sqlScriptExecutorFactory = databaseType.createSqlScriptExecutorFactory(jdbcConnectionFactory, callbackExecutor, statementInterceptor);

            SchemaHistory schemaHistory = SchemaHistoryFactory.getSchemaHistory(
                    configuration,
                    noCallbackSqlScriptExecutorFactory,
                    sqlScriptFactory,
                    database,
                    defaultSchema,
                    statementInterceptor);

            result = command.execute(
                    createMigrationResolver(resourceProvider, classProvider, sqlScriptExecutorFactory, sqlScriptFactory, parsingContext, statementInterceptor),
                    schemaHistory,
                    database,
                    defaultSchema,
                    schemas.getRight().toArray(new Schema[0]),
                    callbackExecutor,
                    statementInterceptor);
        } finally {
            IOUtils.close(database);
            if (statementInterceptor instanceof AutoCloseable) {
                IOUtils.close((AutoCloseable) statementInterceptor);
            }
            showMemoryUsage();
        }

        File permit_file = new File(FileUtils.getAppDataFlywayCLILocation(), "permit");
        if (LicenseGuard.getTier(configuration) == Tier.COMMUNITY && !permit_file.exists()) {
            LOG.info("");
            LOG.info("You are not signed in to Flyway, to sign in please run auth");
        }

        return result;
    }

    private Pair<ResourceProvider, ClassProvider<JavaMigration>> createResourceAndClassProviders(boolean scannerRequired) {
        ResourceProvider resourceProvider;
        ClassProvider<JavaMigration> classProvider;
        if (!scannerRequired && configuration.isSkipDefaultResolvers() && configuration.isSkipDefaultCallbacks()) {
            resourceProvider = NoopResourceProvider.INSTANCE;
            //noinspection unchecked
            classProvider = NoopClassProvider.INSTANCE;
        } else {
            if (configuration.getResourceProvider() != null && configuration.getJavaMigrationClassProvider() != null) {
                // don't create the scanner at all in this case
                resourceProvider = configuration.getResourceProvider();
                classProvider = configuration.getJavaMigrationClassProvider();
            } else {
                boolean stream = false;




                Scanner<JavaMigration> scanner = new Scanner<>(
                        JavaMigration.class,
                        stream,
                        resourceNameCache,
                        locationScannerCache,
                        configuration);
                // set the defaults
                resourceProvider = scanner;
                classProvider = scanner;
                if (configuration.getResourceProvider() != null) {
                    resourceProvider = configuration.getResourceProvider();
                }
                if (configuration.getJavaMigrationClassProvider() != null) {
                    classProvider = configuration.getJavaMigrationClassProvider();
                }
            }
        }

        return Pair.of(resourceProvider, classProvider);
    }

    private List<Callback> prepareCallbacks(Database database, ResourceProvider resourceProvider,
                                            JdbcConnectionFactory jdbcConnectionFactory,
                                            SqlScriptFactory sqlScriptFactory, StatementInterceptor statementInterceptor,
                                            Schema schema, ParsingContext parsingContext, FlywayTelemetryManager flywayTelemetryManager) {
        List<Callback> effectiveCallbacks = new ArrayList<>();
        CallbackExecutor callbackExecutor = NoopCallbackExecutor.INSTANCE;

        if (statementInterceptor != null) {
            effectiveCallbacks.addAll(statementInterceptor.getCallbacks());
        }

        effectiveCallbacks.addAll(Arrays.asList(configuration.getCallbacks()));











        LOG.debug("Scanning for script callbacks ...");
        ScriptMigrationResolver scriptMigrationResolver = new ScriptMigrationResolver(resourceProvider, configuration, parsingContext, statementInterceptor);
        scriptMigrationResolver.resolveCallbacks();
        effectiveCallbacks.addAll(scriptMigrationResolver.scriptCallbacks);

        if (!configuration.isSkipDefaultCallbacks()) {
            SqlScriptExecutorFactory sqlScriptExecutorFactory = jdbcConnectionFactory.getDatabaseType().createSqlScriptExecutorFactory(
                    jdbcConnectionFactory, callbackExecutor, statementInterceptor);

            effectiveCallbacks.addAll(new SqlScriptCallbackFactory(resourceProvider, sqlScriptExecutorFactory, sqlScriptFactory, configuration).getCallbacks());
        }







        return effectiveCallbacks;
    }

    private CompositeMigrationResolver createMigrationResolver(ResourceProvider resourceProvider,
                                                      ClassProvider<JavaMigration> classProvider,
                                                      SqlScriptExecutorFactory sqlScriptExecutorFactory,
                                                      SqlScriptFactory sqlScriptFactory,
                                                      ParsingContext parsingContext,
                                                      StatementInterceptor statementInterceptor) {
        return new CompositeMigrationResolver(resourceProvider, classProvider, configuration, sqlScriptExecutorFactory, sqlScriptFactory, parsingContext, statementInterceptor, configuration.getResolvers());
    }

    private void showMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long free = runtime.freeMemory();
        long total = runtime.totalMemory();
        long used = total - free;

        long totalMB = MEGABYTE.fromBytes(total);
        long usedMB = MEGABYTE.fromBytes(used);
        LOG.debug("Memory usage: " + usedMB + " of " + totalMB + "M");
    }

}
