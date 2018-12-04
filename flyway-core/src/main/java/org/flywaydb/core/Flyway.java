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
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.callback.DefaultCallbackExecutor;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
 */
public class Flyway {
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
        return new CompositeMigrationResolver(database,
                resourceProvider, classProvider, configuration,
                sqlStatementBuilderFactory



                , configuration.getResolvers());
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