/*
 * Copyright © Red Gate Software Ltd 2010-2021
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
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.output.*;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.*;
import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.internal.clazz.NoopClassProvider;
import org.flywaydb.core.internal.command.*;
import org.flywaydb.core.internal.configuration.ConfigurationValidator;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.strategy.RetryStrategy;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;

import org.flywaydb.core.internal.resource.*;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.internal.scanner.LocationScannerCache;
import org.flywaydb.core.internal.scanner.ResourceNameCache;
import org.flywaydb.core.internal.scanner.Scanner;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.schemahistory.SchemaHistoryFactory;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;
import org.flywaydb.core.internal.util.IOUtils;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.util.*;

/**
 * This is the centre point of Flyway, and for most users, the only class they will ever have to deal with.
 * <p>
 * It is THE public API from which all important Flyway functions such as clean, validate and migrate can be called.
 * </p>
 * <p>To get started all you need to do is create a configured Flyway object and then invoke its principal methods.</p>
 * <pre>
 * Flyway flyway = Flyway.configure().dataSource(url, user, password).load();
 * flyway.migrate();
 * </pre>
 * Note that a configured Flyway object is immutable. If you change the configuration you will end up creating a new Flyway
 * object.
 * <p>
 */
public class Flyway {
    private static final Log LOG = LogFactory.getLog(Flyway.class);

    private final ClassicConfiguration configuration;

    /**
     * Whether the database connection info has already been printed in the logs.
     */
    private boolean dbConnectionInfoPrinted;

    /**
     * Designed so we can fail fast if the configuration is invalid
     */
    private ConfigurationValidator  configurationValidator = new ConfigurationValidator();

    /**
     * Designed so we can fail fast if the SQL file resources are invalid
     */
    private ResourceNameValidator resourceNameValidator = new ResourceNameValidator();

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
     * Creates a new instance of Flyway with this configuration. In general the Flyway.configure() factory method should
     * be preferred over this constructor, unless you need to create or reuse separate Configuration objects.
     *
     * @param configuration The configuration to use.
     */
    public Flyway(Configuration configuration) {
        this.configuration = new ClassicConfiguration(configuration);

        // Load callbacks from default package
        this.configuration.loadCallbackLocation("db/callback", false);
    }

    /**
     * @return The configuration that Flyway is using.
     */
    public Configuration getConfiguration() {
        return new ClassicConfiguration(configuration);
    }

    /**
     * Used to cache resource names for classpath scanning between commands
     */
    private ResourceNameCache resourceNameCache = new ResourceNameCache();

    /**
     * Used to cache LocationScanners between commands
     */
    private final LocationScannerCache locationScannerCache = new LocationScannerCache();

    /**
     * <p>Starts the database migration. All pending migrations will be applied in order.
     * Calling migrate on an up-to-date database has no effect.</p>
     * <img src="https://flywaydb.org/assets/balsamiq/command-migrate.png" alt="migrate">
     *
     * @return An object summarising the successfully applied migrations.
     * @throws FlywayException when the migration failed.
     */
    public MigrateResult migrate() throws FlywayException {
        return execute(new Command<MigrateResult>() {
            public MigrateResult execute(MigrationResolver migrationResolver,
                                   SchemaHistory schemaHistory, Database database, Schema[] schemas, CallbackExecutor callbackExecutor,
                                   StatementInterceptor statementInterceptor) {
                if (configuration.isValidateOnMigrate()) {
                    ValidateResult validateResult = doValidate(database, migrationResolver, schemaHistory, schemas, callbackExecutor, true);
                    if (!validateResult.validationSuccessful && !configuration.isCleanOnValidationError()) {
                        throw new FlywayValidateException(validateResult.errorDetails, validateResult.getAllErrorMessages());
                    }
                }

                if (!schemaHistory.exists()) {
                    List<Schema> nonEmptySchemas = new ArrayList<>();
                    for (Schema schema : schemas) {
                        if (schema.exists() && !schema.empty()) {
                            nonEmptySchemas.add(schema);
                        }
                    }

                    if (!nonEmptySchemas.isEmpty()



                    ) {
                        if (configuration.isBaselineOnMigrate()) {
                            doBaseline(schemaHistory, callbackExecutor, database);
                        } else {
                            // Second check for MySQL which is sometimes flaky otherwise
                            if (!schemaHistory.exists()) {
                                throw new FlywayException("Found non-empty schema(s) "
                                        + StringUtils.collectionToCommaDelimitedString(nonEmptySchemas)
                                        + " but no schema history table. Use baseline()"
                                        + " or set baselineOnMigrate to true to initialize the schema history table.");
                            }
                        }
                    } else {
                        if (configuration.getCreateSchemas()) {
                            new DbSchemas(database, schemas, schemaHistory, callbackExecutor).create(false);
                        } else if(!schemas[0].exists()) {
                            LOG.warn("The configuration option 'createSchemas' is false.\n" +
                                    "However, the schema history table still needs a schema to reside in.\n" +
                                    "You must manually create a schema for the schema history table to reside in.\n" +
                                    "See https://flywaydb.org/documentation/concepts/migrations.html#the-createschemas-option-and-the-schema-history-table");
                        }

                        schemaHistory.create(false);
                    }
                }

                MigrateResult result = new DbMigrate(database, schemaHistory, schemas[0], migrationResolver, configuration, callbackExecutor).migrate();

                callbackExecutor.onOperationFinishEvent(Event.AFTER_MIGRATE_OPERATION_FINISH, result);

                return result;
            }
        }, true);
    }

    private BaselineResult doBaseline(SchemaHistory schemaHistory, CallbackExecutor callbackExecutor, Database database) {
        return new DbBaseline(schemaHistory, configuration.getBaselineVersion(), configuration.getBaselineDescription(),
                callbackExecutor, database).baseline();
    }

    /**
     * <p>Undoes the most recently applied versioned migration. If target is specified, Flyway will attempt to undo
     * versioned migrations in the order they were applied until it hits one with a version below the target. If there
     * is no versioned migration to undo, calling undo has no effect.</p>
     * <p><i>Flyway Teams only</i></p>
     * <img src="https://flywaydb.org/assets/balsamiq/command-undo.png" alt="undo">
     *
     * @return An object summarising the successfully undone migrations.
     * @throws FlywayException when the undo failed.
     */
    public UndoResult undo() throws FlywayException {

        throw new org.flywaydb.core.internal.license.FlywayTeamsUpgradeRequiredException("undo");















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
                                Schema[] schemas, CallbackExecutor callbackExecutor,
                                StatementInterceptor statementInterceptor) {
                ValidateResult validateResult = doValidate(database, migrationResolver, schemaHistory, schemas, callbackExecutor,
                        configuration.isIgnorePendingMigrations());

                callbackExecutor.onOperationFinishEvent(Event.AFTER_VALIDATE_OPERATION_FINISH, validateResult);

                if (!validateResult.validationSuccessful && !configuration.isCleanOnValidationError()) {
                    throw new FlywayValidateException(validateResult.errorDetails, validateResult.getAllErrorMessages());
                }

                return null;
            }
        }, true);
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
     * @returns An object summarising the validation results
     * @throws FlywayException when the validation failed.
     */
    public ValidateResult validateWithResult() throws FlywayException {
        return execute(new Command<ValidateResult>() {
            public ValidateResult execute(MigrationResolver migrationResolver, SchemaHistory schemaHistory, Database database,
                                          Schema[] schemas, CallbackExecutor callbackExecutor,
                                          StatementInterceptor statementInterceptor) {
                ValidateResult validateResult = doValidate(database, migrationResolver, schemaHistory, schemas, callbackExecutor,
                        configuration.isIgnorePendingMigrations());

                callbackExecutor.onOperationFinishEvent(Event.AFTER_VALIDATE_OPERATION_FINISH, validateResult);

                return validateResult;
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
    private ValidateResult doValidate(Database database, MigrationResolver migrationResolver, SchemaHistory schemaHistory,
                            Schema[] schemas, CallbackExecutor callbackExecutor, boolean ignorePending) {
        ValidateResult validateResult = new DbValidate(database, schemaHistory, schemas[0], migrationResolver,
                configuration, ignorePending, callbackExecutor).validate();

        if (!validateResult.validationSuccessful && configuration.isCleanOnValidationError()) {
            doClean(database, schemaHistory, schemas, callbackExecutor);
        }

        return validateResult;
    }

    private CleanResult doClean(Database database, SchemaHistory schemaHistory, Schema[] schemas, CallbackExecutor callbackExecutor) {
        return new DbClean(database, schemaHistory, schemas, callbackExecutor, configuration.isCleanDisabled()).clean();
    }

    /**
     * <p>Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas.
     * The schemas are cleaned in the order specified by the {@code schemas} property.</p>
     * <img src="https://flywaydb.org/assets/balsamiq/command-clean.png" alt="clean">
     *
     * @return An object summarising the actions taken
     * @throws FlywayException when the clean fails.
     */
    public CleanResult clean() {
        return execute(new Command<CleanResult>() {
            public CleanResult execute(MigrationResolver migrationResolver, SchemaHistory schemaHistory, Database database,
                                Schema[] schemas, CallbackExecutor callbackExecutor,
                                StatementInterceptor statementInterceptor) {
                CleanResult cleanResult = doClean(database, schemaHistory, schemas, callbackExecutor);

                callbackExecutor.onOperationFinishEvent(Event.AFTER_CLEAN_OPERATION_FINISH, cleanResult);

                return cleanResult;
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
                                                final Database database, final Schema[] schemas, CallbackExecutor callbackExecutor,
                                                StatementInterceptor statementInterceptor) {
                MigrationInfoService migrationInfoService = new DbInfo(migrationResolver, schemaHistory, configuration, database,
                        callbackExecutor, schemas).info();

                callbackExecutor.onOperationFinishEvent(Event.AFTER_INFO_OPERATION_FINISH, migrationInfoService.getInfoResult());

                return migrationInfoService;
            }
        }, true);
    }

    /**
     * <p>Baselines an existing database, excluding all migrations up to and including baselineVersion.</p>
     *
     * <img src="https://flywaydb.org/assets/balsamiq/command-baseline.png" alt="baseline">
     *
     * @return An object summarising the actions taken
     * @throws FlywayException when the schema baselining failed.
     */
    public BaselineResult baseline() throws FlywayException {
        return execute(new Command<BaselineResult>() {
            public BaselineResult execute(MigrationResolver migrationResolver,
                                SchemaHistory schemaHistory, Database database, Schema[] schemas, CallbackExecutor callbackExecutor,
                                StatementInterceptor statementInterceptor) {
                if (configuration.getCreateSchemas()) {
                    new DbSchemas(database, schemas, schemaHistory, callbackExecutor).create(true);
                } else {
                    LOG.warn("The configuration option 'createSchemas' is false.\n" +
                            "Even though Flyway is configured not to create any schemas, the schema history table still needs a schema to reside in.\n" +
                            "You must manually create a schema for the schema history table to reside in.\n" +
                            "See http://flywaydb.org/documentation/migrations#the-createschemas-option-and-the-schema-history-table");
                }

                BaselineResult baselineResult = doBaseline(schemaHistory, callbackExecutor, database);

                callbackExecutor.onOperationFinishEvent(Event.AFTER_BASELINE_OPERATION_FINISH, baselineResult);

                return baselineResult;
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
     * @return An object summarising the actions taken
     * @throws FlywayException when the schema history table repair failed.
     */
    public RepairResult repair() throws FlywayException {
        return execute(new Command<RepairResult>() {
            public RepairResult execute(MigrationResolver migrationResolver,
                                        SchemaHistory schemaHistory, Database database, Schema[] schemas, CallbackExecutor callbackExecutor,
                                        StatementInterceptor statementInterceptor) {
                RepairResult repairResult = new DbRepair(database, migrationResolver, schemaHistory, callbackExecutor, configuration).repair();

                callbackExecutor.onOperationFinishEvent(Event.AFTER_REPAIR_OPERATION_FINISH, repairResult);

                return repairResult;
            }
        }, true);
    }

    /**
     * Creates the MigrationResolver.
     *
     * @param resourceProvider The resource provider.
     * @param classProvider    The class provider.
     * @param sqlScriptFactory The SQL statement builder factory.
     * @param parsingContext   The parsing context.
     * @return A new, fully configured, MigrationResolver instance.
     */
    private MigrationResolver createMigrationResolver(ResourceProvider resourceProvider,
                                                      ClassProvider<JavaMigration> classProvider,
                                                      SqlScriptExecutorFactory sqlScriptExecutorFactory,
                                                      SqlScriptFactory sqlScriptFactory,
                                                      ParsingContext parsingContext) {
        return new CompositeMigrationResolver(resourceProvider, classProvider, configuration,
                sqlScriptExecutorFactory, sqlScriptFactory, parsingContext, configuration.getResolvers());
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

        configurationValidator.validate(configuration);

        StatementInterceptor statementInterceptor = null;











        final Pair<ResourceProvider, ClassProvider<JavaMigration>> resourceProviderClassProviderPair = createResourceAndClassProviders(scannerRequired);
        final ResourceProvider resourceProvider = resourceProviderClassProviderPair.getLeft();
        final ClassProvider<JavaMigration> classProvider = resourceProviderClassProviderPair.getRight();

        if (configuration.isValidateMigrationNaming()) {
            resourceNameValidator.validateSQLMigrationNaming(resourceProvider, configuration);
        }

        JdbcConnectionFactory jdbcConnectionFactory = new JdbcConnectionFactory(
                configuration.getDataSource(),
                configuration,
                statementInterceptor
        );

        final DatabaseType databaseType = jdbcConnectionFactory.getDatabaseType();
        final ParsingContext parsingContext = new ParsingContext();
        final SqlScriptFactory sqlScriptFactory = databaseType.createSqlScriptFactory(configuration, parsingContext);
        RetryStrategy.setNumberOfRetries(configuration.getLockRetryCount());

        final SqlScriptExecutorFactory noCallbackSqlScriptExecutorFactory = databaseType.createSqlScriptExecutorFactory(
                jdbcConnectionFactory,
                NoopCallbackExecutor.INSTANCE,
                null
        );

        jdbcConnectionFactory.setConnectionInitializer(new JdbcConnectionFactory.ConnectionInitializer() {
            @Override
            public void initialize(JdbcConnectionFactory jdbcConnectionFactory, Connection connection) {
                if (configuration.getInitSql() == null) {
                    return;
                }
                StringResource resource = new StringResource(configuration.getInitSql());

                SqlScript sqlScript = sqlScriptFactory.createSqlScript(resource, true, resourceProvider);

                boolean outputQueryResults = false;




                noCallbackSqlScriptExecutorFactory.createSqlScriptExecutor(connection, false, false, outputQueryResults).execute(sqlScript);
            }
        });

        Database database = null;
        try {
            database = databaseType.createDatabase(configuration, !dbConnectionInfoPrinted, jdbcConnectionFactory, statementInterceptor);

            dbConnectionInfoPrinted = true;
            LOG.debug("DDL Transactions Supported: " + database.supportsDdlTransactions());

            Pair<Schema, List<Schema>> schemas = prepareSchemas(database);
            Schema defaultSchema = schemas.getLeft();







            parsingContext.populate(database, configuration);

            database.ensureSupported();

            DefaultCallbackExecutor callbackExecutor =
                    new DefaultCallbackExecutor(configuration, database, defaultSchema,
                        prepareCallbacks(
                                database, resourceProvider, jdbcConnectionFactory, sqlScriptFactory, statementInterceptor, defaultSchema, parsingContext
                        ));

            SqlScriptExecutorFactory sqlScriptExecutorFactory =
                    databaseType.createSqlScriptExecutorFactory(jdbcConnectionFactory, callbackExecutor, statementInterceptor);

            result = command.execute(
                    createMigrationResolver(resourceProvider, classProvider, sqlScriptExecutorFactory, sqlScriptFactory, parsingContext),
                    SchemaHistoryFactory.getSchemaHistory(configuration, noCallbackSqlScriptExecutorFactory, sqlScriptFactory, database, defaultSchema, statementInterceptor),
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
                        stream,
                        resourceNameCache,
                        locationScannerCache,
                        configuration.getFailOnMissingLocations()
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

    private void showMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long free = runtime.freeMemory();
        long total = runtime.totalMemory();
        long used = total - free;

        long totalMB = total / (1024 * 1024);
        long usedMB = used / (1024 * 1024);
        LOG.debug("Memory usage: " + usedMB + " of " + totalMB + "M");
    }

    private Pair<Schema, List<Schema>> prepareSchemas(Database database) {
        String defaultSchemaName = configuration.getDefaultSchema();
        String[] schemaNames = configuration.getSchemas();

        if (!isDefaultSchemaValid(defaultSchemaName, schemaNames)) {
            throw new FlywayException("The defaultSchema property is specified but is not a member of the schemas property");
        }

        LOG.debug("Schemas: " + StringUtils.arrayToCommaDelimitedString(schemaNames));
        LOG.debug("Default schema: " + defaultSchemaName);

        List<Schema> schemas = new ArrayList<>();

        if (schemaNames.length == 0) {
            Schema currentSchema = database.getMainConnection().getCurrentSchema();
            if (currentSchema == null) {
                throw new FlywayException("Unable to determine schema for the schema history table." +
                        " Set a default schema for the connection or specify one using the defaultSchema property!");
            }
            schemas.add(currentSchema);
        } else {
            for (String schemaName : schemaNames) {
                schemas.add(database.getMainConnection().getSchema(schemaName));
            }
        }

        if (defaultSchemaName == null && schemaNames.length > 0) {
            defaultSchemaName = schemaNames[0];
        }

        Schema defaultSchema = (defaultSchemaName != null)
                ? database.getMainConnection().getSchema(defaultSchemaName)
                : database.getMainConnection().getCurrentSchema();

        return Pair.of(defaultSchema, schemas);
    }

    private boolean isDefaultSchemaValid(String defaultSchema, String[] schemas) {
        // No default schema specified
        if (defaultSchema == null) {
            return true;
        }
        // Default schema is one of those Flyway is managing
        for (String schema : schemas) {
            if (defaultSchema.equals(schema)) {
                return true;
            }
        }
        return false;
    }

    private List<Callback> prepareCallbacks(Database database, ResourceProvider resourceProvider,
                                            JdbcConnectionFactory jdbcConnectionFactory,
                                            SqlScriptFactory sqlScriptFactory,
                                            StatementInterceptor statementInterceptor,
                                            Schema schema,
                                            ParsingContext parsingContext) {
        List<Callback> effectiveCallbacks = new ArrayList<>();
        CallbackExecutor callbackExecutor = NoopCallbackExecutor.INSTANCE;

















        effectiveCallbacks.addAll(Arrays.asList(configuration.getCallbacks()));








        if (!configuration.isSkipDefaultCallbacks()) {
            SqlScriptExecutorFactory sqlScriptExecutorFactory =
                    jdbcConnectionFactory.getDatabaseType().createSqlScriptExecutorFactory(jdbcConnectionFactory,
                            callbackExecutor,
                            statementInterceptor
                    );

            effectiveCallbacks.addAll(
                    new SqlScriptCallbackFactory(
                            resourceProvider,
                            sqlScriptExecutorFactory,
                            sqlScriptFactory,
                            configuration
                    ).getCallbacks());
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
                  Database database, Schema[] schemas, CallbackExecutor callbackExecutor,
                  StatementInterceptor statementInterceptor
        );
    }
}