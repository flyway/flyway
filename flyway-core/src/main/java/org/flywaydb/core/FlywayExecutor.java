/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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

import static org.flywaydb.core.api.callback.Event.AFTER_CONNECT;
import static org.flywaydb.core.api.callback.Event.CREATE_SCHEMA;
import static org.flywaydb.core.internal.database.DatabaseTypeRegister.redactJdbcUrl;
import static org.flywaydb.core.internal.util.DataUnits.MEGABYTE;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.CustomLog;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.callback.GenericCallback;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.callback.DefaultCallbackExecutor;
import org.flywaydb.core.internal.callback.InternalCallback;
import org.flywaydb.core.internal.callback.NoopCallback;
import org.flywaydb.core.internal.callback.NoopCallbackExecutor;
import org.flywaydb.core.internal.callback.SqlScriptCallbackFactory;

import org.flywaydb.core.internal.clazz.NoopClassProvider;
import org.flywaydb.core.internal.configuration.ConfigurationValidator;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.base.CommunityDatabaseType;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.ErrorOverrideInitializer;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.resolver.script.ScriptMigrationResolver;
import org.flywaydb.core.internal.resource.NoopResourceProvider;
import org.flywaydb.core.internal.resource.ResourceNameValidator;
import org.flywaydb.core.internal.resource.StringResource;
import org.flywaydb.core.internal.scanner.Scanner;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.schemahistory.SchemaHistoryFactory;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;
import org.flywaydb.core.internal.strategy.RetryStrategy;
import org.flywaydb.core.internal.util.FileUtils;
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.util.Pair;

@CustomLog
public class FlywayExecutor {
    public interface Command<T> {
        T execute(CompositeMigrationResolver migrationResolver,
            SchemaHistory schemaHistory,
            Database database,
            Schema defaultSchema,
            Schema[] schemas,
            CallbackExecutor<Event> callbackExecutor,
            StatementInterceptor statementInterceptor);
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
     * Whether the database connection info has already been printed in the logs
     */
    private boolean dbConnectionInfoPrinted;
    private final Configuration configuration;

    public FlywayExecutor(final Configuration configuration) {
        this.configurationValidator = new ConfigurationValidator();
        this.resourceNameValidator = new ResourceNameValidator();
        this.configuration = configuration;
    }

    /**
     * Executes this command with proper resource handling and cleanup.
     *
     * @param command The command to execute.
     * @param <T>     The type of the result.
     * @return The result of the command.
     */
    public <T> T execute(final Command<T> command,
        final boolean scannerRequired,
        final FlywayTelemetryManager flywayTelemetryManager) {
        return execute(command, scannerRequired, flywayTelemetryManager, init());
    }

    private <T> T execute(final Command<T> command,
        final boolean scannerRequired,
        final FlywayTelemetryManager flywayTelemetryManager,
        final JdbcConnectionFactory jdbcConnectionFactory) {
        T result;

        final StatementInterceptor statementInterceptor = configuration.getPluginRegister()
            .getInstancesOf(StatementInterceptor.class)
            .stream()
            .filter(i -> i.isConfigured(configuration))
            .findFirst()
            .orElse(null);

        final Pair<ResourceProvider, ClassProvider<JavaMigration>> resourceProviderClassProviderPair = createResourceAndClassProviders(
            scannerRequired);
        final ResourceProvider resourceProvider = resourceProviderClassProviderPair.getLeft();
        final ClassProvider<JavaMigration> classProvider = resourceProviderClassProviderPair.getRight();
        final ParsingContext parsingContext = new ParsingContext();









        final DatabaseType databaseType = jdbcConnectionFactory.getDatabaseType();
        final SqlScriptFactory sqlScriptFactory = databaseType.createSqlScriptFactory(configuration, parsingContext);

        resourceNameValidator.validateSQLMigrationNaming(resourceProvider, configuration, databaseType);

        RetryStrategy.setNumberOfRetries(configuration.getLockRetryCount());

        final SqlScriptExecutorFactory noCallbackSqlScriptExecutorFactory = databaseType.createSqlScriptExecutorFactory(
            jdbcConnectionFactory,
            NoopCallbackExecutor.INSTANCE,
            null);

        jdbcConnectionFactory.setConnectionInitializer((jdbcConnectionFactory1, connection) -> {
            if (configuration.getInitSql() == null) {
                return;
            }
            final StringResource resource = new StringResource(configuration.getInitSql());

            final SqlScript sqlScript = sqlScriptFactory.createSqlScript(resource, true, resourceProvider);

            final boolean outputQueryResults = configuration.isOutputQueryResults();

            noCallbackSqlScriptExecutorFactory.createSqlScriptExecutor(connection, false, false, outputQueryResults)
                .execute(sqlScript, configuration);
        });

        Database database = null;
        try {
            database = databaseType.createDatabase(configuration, jdbcConnectionFactory, statementInterceptor);

            if (!dbConnectionInfoPrinted) {
                dbConnectionInfoPrinted = true;

                if (database.getDatabaseType() instanceof CommunityDatabaseType) {
                    LOG.info(((CommunityDatabaseType) database.getDatabaseType()).announcementForCommunitySupport());
                }

                if (flywayTelemetryManager != null) {
                    flywayTelemetryManager.notifyDatabaseChanged(database.getDatabaseType().getName(),
                        database.getVersion().toString(),
                        configuration.getUrl() != null ? database.getDatabaseHosting() : null);
                }
            }

            LOG.debug("DDL Transactions Supported: " + database.supportsDdlTransactions());

            final Pair<Schema, List<Schema>> schemas = SchemaHistoryFactory.prepareSchemas(configuration, database);
            final Schema defaultSchema = schemas.getLeft();

            if (statementInterceptor != null) {
                statementInterceptor.init(configuration, database, defaultSchema.getTable(configuration.getTable()));
            }

            parsingContext.populate(database, configuration);

            database.ensureSupported(configuration);

            final ResourceProvider callbackResourceProvider = configuration.getCallbackLocations().length > 0
                ? createScanner(configuration.getCallbackLocations())
                : resourceProvider;
            final CallbackExecutor<Event> callbackExecutor = new DefaultCallbackExecutor<>(configuration,
                database,
                defaultSchema,
                flywayTelemetryManager,
                prepareCallbacks(database,
                    callbackResourceProvider,
                    jdbcConnectionFactory,
                    sqlScriptFactory,
                    statementInterceptor,
                    defaultSchema,
                    parsingContext,
                    flywayTelemetryManager));

            callbackExecutor.onEvent(AFTER_CONNECT);
            final SqlScriptExecutorFactory sqlScriptExecutorFactory = databaseType.createSqlScriptExecutorFactory(
                jdbcConnectionFactory,
                callbackExecutor,
                statementInterceptor);

            final SchemaHistory schemaHistory = SchemaHistoryFactory.getSchemaHistory(configuration,
                noCallbackSqlScriptExecutorFactory,
                sqlScriptFactory,
                database,
                defaultSchema,
                statementInterceptor);

            result = command.execute(createMigrationResolver(resourceProvider,
                    classProvider,
                    sqlScriptExecutorFactory,
                    sqlScriptFactory,
                    parsingContext,
                    statementInterceptor),
                schemaHistory,
                database,
                defaultSchema,
                schemas.getRight().toArray(Schema[]::new),
                callbackExecutor,
                statementInterceptor);
        } finally {
            IOUtils.close(database);
            if (statementInterceptor instanceof AutoCloseable) {
                IOUtils.close((AutoCloseable) statementInterceptor);
            }
            showMemoryUsage();
        }

        final File permitFile = new File(FileUtils.getAppDataFlywayCLILocation(), "permit");
        if (LicenseGuard.getTier(configuration) == Tier.COMMUNITY && !permitFile.exists()) {
            LOG.info("");
            LOG.info("You are not signed in to Flyway, to sign in please run auth");
        }

        return result;
    }

    public JdbcConnectionFactory init() {
        configurationValidator.validate(configuration);

        final StatementInterceptor statementInterceptor = configuration.getPluginRegister()
            .getInstancesOf(StatementInterceptor.class)
            .stream()
            .filter(i -> i.isConfigured(configuration))
            .findFirst()
            .orElse(null);

        final JdbcConnectionFactory jdbcConnectionFactory = new JdbcConnectionFactory(configuration.getDataSource(),
            configuration,
            statementInterceptor);

        LOG.info("Database: "
            + redactJdbcUrl(jdbcConnectionFactory.getJdbcUrl())
            + " ("
            + jdbcConnectionFactory.getProductName()
            + ")");
        LOG.debug("Database Type: " + jdbcConnectionFactory.getDatabaseType().getName());
        LOG.debug("Driver: " + jdbcConnectionFactory.getDriverInfo());

        return jdbcConnectionFactory;
    }

    private Pair<ResourceProvider, ClassProvider<JavaMigration>> createResourceAndClassProviders(final boolean scannerRequired) {
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
                final Scanner<JavaMigration> scanner = createScanner(configuration.getLocations());
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

    private Scanner<JavaMigration> createScanner(final Location[] locations) {
        return new Scanner<>(JavaMigration.class, configuration, locations);
    }

    private List<GenericCallback<Event>> prepareCallbacks(final Database database,
        final ResourceProvider resourceProvider,
        final JdbcConnectionFactory jdbcConnectionFactory,
        final SqlScriptFactory sqlScriptFactory,
        final StatementInterceptor statementInterceptor,
        final Schema schema,
        final ParsingContext parsingContext,
        final FlywayTelemetryManager flywayTelemetryManager) {
        final List<GenericCallback<Event>> effectiveCallbacks = new ArrayList<>();
        CallbackExecutor<Event> callbackExecutor = NoopCallbackExecutor.INSTANCE;

        if (statementInterceptor != null) {
            effectiveCallbacks.addAll(statementInterceptor.getCallbacks());
        }

        effectiveCallbacks.addAll(Arrays.asList(configuration.getCallbacks()));

        final ErrorOverrideInitializer errorOverride = configuration.getPluginRegister()
            .getInstanceOf(ErrorOverrideInitializer.class);
        if (configuration.getErrorOverrides().length > 0) {
            errorOverride.setCallback(configuration.getErrorOverrides());
            callbackExecutor = errorOverride.getCallbackExecutor(configuration,
                database,
                schema,
                flywayTelemetryManager);
        }

        LOG.debug("Scanning for script callbacks ...");
        final ScriptMigrationResolver<Event> scriptMigrationResolver = new ScriptMigrationResolver<>(resourceProvider,
            configuration,
            parsingContext,
            statementInterceptor);
        scriptMigrationResolver.resolveCallbacks((String id) -> Optional.ofNullable(Event.fromId(id)));
        effectiveCallbacks.addAll(scriptMigrationResolver.scriptCallbacks);

        if (!configuration.isSkipDefaultCallbacks()) {
            final SqlScriptExecutorFactory sqlScriptExecutorFactory = jdbcConnectionFactory.getDatabaseType()
                .createSqlScriptExecutorFactory(jdbcConnectionFactory, callbackExecutor, statementInterceptor);

            effectiveCallbacks.addAll(new SqlScriptCallbackFactory<>(resourceProvider,
                sqlScriptExecutorFactory,
                sqlScriptFactory,
                configuration,
                (String id) -> Optional.ofNullable(Event.fromId(id))).getCallbacks());
        }

        if (!(errorOverride.getCallback() instanceof NoopCallback)) {
            effectiveCallbacks.add(errorOverride.getCallback());
        }

        if (effectiveCallbacks.stream().anyMatch(x -> x.supports(CREATE_SCHEMA, null))) {
            LOG.warn(
                "'createSchema' callback is deprecated and will be removed in a later release. Please use 'beforeCreateSchema' callback instead.");
        }

        @SuppressWarnings("unchecked") final var internalCallbacks = configuration.getPluginRegister()
            .getInstancesOf(InternalCallback.class)
            .stream()
            .filter(x -> x.supportsEventType(Event.class))
            .map(x -> (GenericCallback<Event>) x)
            .toList();
        effectiveCallbacks.addAll(internalCallbacks);

        return effectiveCallbacks;
    }

    private CompositeMigrationResolver createMigrationResolver(final ResourceProvider resourceProvider,
        final ClassProvider<JavaMigration> classProvider,
        final SqlScriptExecutorFactory sqlScriptExecutorFactory,
        final SqlScriptFactory sqlScriptFactory,
        final ParsingContext parsingContext,
        final StatementInterceptor statementInterceptor) {
        return new CompositeMigrationResolver(resourceProvider,
            classProvider,
            configuration,
            sqlScriptExecutorFactory,
            sqlScriptFactory,
            parsingContext,
            statementInterceptor,
            configuration.getResolvers());
    }

    private void showMemoryUsage() {
        final Runtime runtime = Runtime.getRuntime();
        final long free = runtime.freeMemory();
        final long total = runtime.totalMemory();
        final long used = total - free;

        final long totalMB = MEGABYTE.fromBytes(total);
        final long usedMB = MEGABYTE.fromBytes(used);
        LOG.debug("Memory usage: " + usedMB + " of " + totalMB + "M");
    }
}
