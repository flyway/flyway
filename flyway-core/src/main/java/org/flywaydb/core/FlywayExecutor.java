/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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

import lombok.CustomLog;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.*;

import org.flywaydb.core.internal.clazz.NoopClassProvider;
import org.flywaydb.core.internal.configuration.ConfigurationValidator;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;

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
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.flywaydb.core.internal.util.DataUnits.MEGABYTE;






@CustomLog
public class FlywayExecutor {
    public interface Command<T> {
        T execute(MigrationResolver migrationResolver, SchemaHistory schemaHistory,
                  Database database, Schema[] schemas, CallbackExecutor callbackExecutor,
                  StatementInterceptor statementInterceptor
                 );
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
    public <T> T execute(Command<T> command, boolean scannerRequired) {
        T result;

        configurationValidator.validate(configuration);

        StatementInterceptor statementInterceptor = null;











        final Pair<ResourceProvider, ClassProvider<JavaMigration>> resourceProviderClassProviderPair = createResourceAndClassProviders(scannerRequired);
        final ResourceProvider resourceProvider = resourceProviderClassProviderPair.getLeft();
        final ClassProvider<JavaMigration> classProvider = resourceProviderClassProviderPair.getRight();
        final ParsingContext parsingContext = new ParsingContext();






        if (configuration.isValidateMigrationNaming()) {
            resourceNameValidator.validateSQLMigrationNaming(resourceProvider, configuration);
        }

        JdbcConnectionFactory jdbcConnectionFactory = new JdbcConnectionFactory(
                configuration.getDataSource(), configuration, statementInterceptor);

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

            boolean outputQueryResults = false;




            noCallbackSqlScriptExecutorFactory.createSqlScriptExecutor(connection, false, false, outputQueryResults).execute(sqlScript);
        });

        Database database = null;
        try {
            VersionPrinter.printVersion();

            database = databaseType.createDatabase(configuration, !dbConnectionInfoPrinted, jdbcConnectionFactory, statementInterceptor);
            databaseType.printMessages();
            dbConnectionInfoPrinted = true;
            LOG.debug("DDL Transactions Supported: " + database.supportsDdlTransactions());

            Pair<Schema, List<Schema>> schemas = SchemaHistoryFactory.prepareSchemas(configuration, database);
            Schema defaultSchema = schemas.getLeft();







            parsingContext.populate(database, configuration);

            database.ensureSupported();

            DefaultCallbackExecutor callbackExecutor = new DefaultCallbackExecutor(configuration, database, defaultSchema,
                                                                                   prepareCallbacks(
                                                                                           database, resourceProvider, jdbcConnectionFactory, sqlScriptFactory, statementInterceptor, defaultSchema, parsingContext
                                                                                                   ));

            SqlScriptExecutorFactory sqlScriptExecutorFactory =
                    databaseType.createSqlScriptExecutorFactory(jdbcConnectionFactory, callbackExecutor, statementInterceptor);

            SchemaHistory schemaHistory = SchemaHistoryFactory.getSchemaHistory(
                    configuration,
                    noCallbackSqlScriptExecutorFactory,
                    sqlScriptFactory,
                    database,
                    defaultSchema,
                    statementInterceptor);









            result = command.execute(
                    createMigrationResolver(resourceProvider, classProvider, sqlScriptExecutorFactory, sqlScriptFactory, parsingContext),
                    schemaHistory,
                    database,
                    schemas.getRight().toArray(new Schema[0]),
                    callbackExecutor,
                    statementInterceptor);
        } finally {
            IOUtils.close(database);





            showMemoryUsage();
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
                        Arrays.asList(configuration.getLocations()),
                        configuration.getClassLoader(),
                        configuration.getEncoding(),
                        configuration.isDetectEncoding(),
                        stream,
                        resourceNameCache,
                        locationScannerCache,
                        configuration.isFailOnMissingLocations()
                );
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
                                            Schema schema, ParsingContext parsingContext) {
        List<Callback> effectiveCallbacks = new ArrayList<>();
        CallbackExecutor callbackExecutor = NoopCallbackExecutor.INSTANCE;















        effectiveCallbacks.addAll(Arrays.asList(configuration.getCallbacks()));








        if (!configuration.isSkipDefaultCallbacks()) {
            SqlScriptExecutorFactory sqlScriptExecutorFactory =
                    jdbcConnectionFactory.getDatabaseType().createSqlScriptExecutorFactory(jdbcConnectionFactory,
                                                                                           callbackExecutor, statementInterceptor);

            effectiveCallbacks.addAll(new SqlScriptCallbackFactory(
                    resourceProvider, sqlScriptExecutorFactory, sqlScriptFactory, configuration).getCallbacks());
        }





        return effectiveCallbacks;
    }

    private MigrationResolver createMigrationResolver(ResourceProvider resourceProvider,
                                                      ClassProvider<JavaMigration> classProvider,
                                                      SqlScriptExecutorFactory sqlScriptExecutorFactory,
                                                      SqlScriptFactory sqlScriptFactory,
                                                      ParsingContext parsingContext) {
        return new CompositeMigrationResolver(resourceProvider, classProvider, configuration,
                                              sqlScriptExecutorFactory, sqlScriptFactory, parsingContext, configuration.getResolvers());
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