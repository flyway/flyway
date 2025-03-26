/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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

import static org.flywaydb.core.experimental.ExperimentalModeUtils.canUseExperimentalMode;
import static org.flywaydb.core.internal.logging.PreviewFeatureWarning.NATIVE_CONNECTORS;
import static org.flywaydb.core.internal.logging.PreviewFeatureWarning.logPreviewFeature;

import lombok.CustomLog;
import lombok.Setter;
import lombok.SneakyThrows;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.output.*;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.extensibility.ConfigurationExtension;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.extensibility.LicenseGuard;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.command.*;
import org.flywaydb.core.internal.command.clean.DbClean;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.CommandExtensionUtils;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;









/**
 * This is the centre point of Flyway, and for most users, the only class they will ever have to deal with.
 *
 * It is THE public API from which all important Flyway functions such as clean, validate and migrate can be called.
 *
 * To get started all you need to do is create a configured Flyway object and then invoke its principal methods.
 * <pre>
 * Flyway flyway = Flyway.configure().dataSource(url, user, password).load();
 * flyway.migrate();
 * </pre>
 * Note that a configured Flyway object is immutable. If you change the configuration you will end up creating a new Flyway object.
 */
@CustomLog
public class Flyway {
    private final ClassicConfiguration configuration;
    private final FlywayExecutor flywayExecutor;

    @Deprecated
    @Setter
    private FlywayTelemetryManager flywayTelemetryManager;

    /**
     * This is your starting point. This creates a configuration which can be customized to your needs before being
     * loaded into a new Flyway instance using the load() method.
     *
     * In its simplest form, this is how you configure Flyway with all defaults to get started:
     * <pre>Flyway flyway = Flyway.configure().dataSource(url, user, password).load();</pre>
     *
     * After that you have a fully-configured Flyway instance at your disposal which can be used to invoke Flyway
     * functionality such as migrate() or clean().
     *
     * @return A new configuration from which Flyway can be loaded.
     */
    public static FluentConfiguration configure() {
        return new FluentConfiguration();
    }

    /**
     * This is your starting point. This creates a configuration which can be customized to your needs before being
     * loaded into a new Flyway instance using the load() method.
     *
     * In its simplest form, this is how you configure Flyway with all defaults to get started:
     * <pre>Flyway flyway = Flyway.configure().dataSource(url, user, password).load();</pre>
     *
     * After that you have a fully-configured Flyway instance at your disposal which can be used to invoke Flyway
     * functionality such as migrate() or clean().
     *
     * @param classLoader The class loader to use when loading classes and resources.
     *
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
        List<Callback> callbacks = this.configuration.loadCallbackLocation("db/callback", false);
        if (!callbacks.isEmpty()) {
            this.configuration.setCallbacks(callbacks.toArray(new Callback[0]));
        }
        this.flywayExecutor = new FlywayExecutor(this.configuration);

        LogFactory.setConfiguration(this.configuration);
        
        if (LicenseGuard.isLicensed(this.configuration, List.of(Tier.ENTERPRISE))) {
            FlywayDbWebsiteLinks.FEEDBACK_SURVEY_LINK = FlywayDbWebsiteLinks.FEEDBACK_SURVEY_LINK_ENTERPRISE;
        } else {
            FlywayDbWebsiteLinks.FEEDBACK_SURVEY_LINK = FlywayDbWebsiteLinks.FEEDBACK_SURVEY_LINK_COMMUNITY;
        }
    }

    /**
     * @return The configuration that Flyway is using.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * @return The configuration extension type requested from the plugin register.
     */
    public <T extends ConfigurationExtension> T getConfigurationExtension(Class<T> configClass) {
        return getConfiguration().getPluginRegister().getPlugin(configClass);
    }

    /**
     * Starts the database migration. All pending migrations will be applied in order.
     * Calling migrate on an up-to-date database has no effect.
     * <img src="https://flywaydb.org/assets/balsamiq/command-migrate.png" alt="migrate">
     *
     * @return An object summarising the successfully applied migrations.
     *
     * @throws FlywayException when the migration failed.
     */
    @SneakyThrows
    public MigrateResult migrate() throws FlywayException {
        try (EventTelemetryModel telemetryModel = new EventTelemetryModel("migrate", flywayTelemetryManager)) {
            if (canUseExperimentalMode(configuration, "migrate")) {
                logPreviewFeature(NATIVE_CONNECTORS);
                final var verb = configuration.getPluginRegister().getPlugins(VerbExtension.class).stream().filter(verbExtension -> verbExtension.handlesVerb("migrate")).findFirst();
                if (verb.isPresent()) {
                    LOG.debug("Native Connectors for migrate is set and a verb is present");
                    return (MigrateResult) verb.get().executeVerb(configuration);
                } else {
                    LOG.warn("Native Connectors for migrate is set but no verb is present");
                }
            }

            try {
                return flywayExecutor.execute((migrationResolver, schemaHistory, database, defaultSchema, schemas, callbackExecutor, statementInterceptor) -> {







                    if (configuration.isValidateOnMigrate()) {
                        List<ValidatePattern> ignorePatterns = new ArrayList<>(Arrays.asList(configuration.getIgnoreMigrationPatterns()));
                        ignorePatterns.add(ValidatePattern.fromPattern("*:pending"));
                        ValidateResult validateResult = doValidate(database, migrationResolver, schemaHistory, defaultSchema, schemas, callbackExecutor, ignorePatterns.toArray(new ValidatePattern[0]));
                        if (!validateResult.validationSuccessful) {
                            throw new FlywayValidateException(validateResult.errorDetails, validateResult.getAllErrorMessages());
                        }
                    }

                    if (configuration.isCreateSchemas()) {
                        new DbSchemas(database, schemas, schemaHistory, callbackExecutor).create(false);
                    } else if (!defaultSchema.exists()) {
                        LOG.warn("The configuration option 'createSchemas' is false.\n" +
                                         "However, the schema history table still needs a schema to reside in.\n" +
                                         "You must manually create a schema for the schema history table to reside in.\n" +
                                         "See " + FlywayDbWebsiteLinks.MIGRATIONS);
                    }

                    if (!schemaHistory.exists()) {
                        List<Schema> nonEmptySchemas = new ArrayList<>();
                        for (Schema schema : schemas) {
                            if (schema.exists() && !schema.empty()) {
                                nonEmptySchemas.add(schema);
                            }
                        }

                        if (nonEmptySchemas.isEmpty() && configuration.isBaselineOnMigrate()) {
                            LOG.info("All configured schemas are empty; baseline operation skipped. "
                                + "A baseline or migration script with a lower version than the baseline version may execute if available. Check the Schemas parameter if this is not intended.");
                        }

                        if (!nonEmptySchemas.isEmpty() && !configuration.isSkipExecutingMigrations()) {
                            if (configuration.isBaselineOnMigrate()) {
                                doBaseline(schemaHistory, callbackExecutor, database);



                            } else {
                                // Second check for MySQL which is sometimes flaky otherwise
                                if (!schemaHistory.exists()) {
                                    throw new FlywayException("Found non-empty schema(s) "
                                                                      + StringUtils.collectionToCommaDelimitedString(nonEmptySchemas)
                                                                      + " but no schema history table. Use baseline()"
                                                                      + " or set baselineOnMigrate to true to initialize the schema history table.", CoreErrorCode.NON_EMPTY_SCHEMA_WITHOUT_SCHEMA_HISTORY_TABLE);
                                }
                            }
                        }

                        schemaHistory.create(false);
                    }

                    MigrateResult result = new DbMigrate(database, schemaHistory, defaultSchema, migrationResolver, configuration, callbackExecutor).migrate();

                    callbackExecutor.onOperationFinishEvent(Event.AFTER_MIGRATE_OPERATION_FINISH, result);

                    return result;
                }, true, flywayTelemetryManager);
            } catch (Exception e) {
                telemetryModel.setException(e);
                throw e;
            }
        }
    }

    /**
     * Retrieves the complete information about all the migrations including applied, pending and current migrations with
     * details and status.
     * <img src="https://flywaydb.org/assets/balsamiq/command-info.png" alt="info">
     *
     * @return All migrations sorted by version, oldest first.
     *
     * @throws FlywayException when the info retrieval failed.
     */
    public MigrationInfoService info() {
        if (canUseExperimentalMode(configuration, "info")) {
            logPreviewFeature(NATIVE_CONNECTORS);
            final var verb = configuration.getPluginRegister().getPlugins(VerbExtension.class).stream().filter(verbExtension -> verbExtension.handlesVerb("info")).findFirst();
            if (verb.isPresent()) {
                LOG.debug("Native Connectors for info is set and a verb is present");
                return (MigrationInfoService) verb.get().executeVerb(configuration);
            } else {
                LOG.warn("Native Connectors for info is set but no verb is present");
            }
        }
        return flywayExecutor.execute((migrationResolver, schemaHistory, database, defaultSchema, schemas, callbackExecutor, statementInterceptor) -> {
            MigrationInfoService migrationInfoService = new DbInfo(migrationResolver, schemaHistory, configuration, database, callbackExecutor, schemas).info();

            callbackExecutor.onOperationFinishEvent(Event.AFTER_INFO_OPERATION_FINISH, migrationInfoService.getInfoResult());

            return migrationInfoService;
        }, true, flywayTelemetryManager);
    }

    /**
     * Drops all objects (tables, views, procedures, triggers, ...) in the configured schemas.
     * The schemas are cleaned in the order specified by the {@code schemas} property.
     * <img src="https://flywaydb.org/assets/balsamiq/command-clean.png" alt="clean">
     *
     * @return An object summarising the actions taken
     *
     * @throws FlywayException when the clean fails.
     */
    @SneakyThrows
    public CleanResult clean() {
        try (EventTelemetryModel telemetryModel = new EventTelemetryModel("clean", flywayTelemetryManager)) {
            if (canUseExperimentalMode(configuration, "clean")) {
                logPreviewFeature(NATIVE_CONNECTORS);
                final var verb = configuration.getPluginRegister().getPlugins(VerbExtension.class).stream().filter(verbExtension -> verbExtension.handlesVerb("clean")).findFirst();
                if (verb.isPresent()) {
                    LOG.debug("Native Connectors for clean is set and a verb is present");
                    return (CleanResult) verb.get().executeVerb(configuration);
                } else {
                    LOG.warn("Native Connectors for clean is set but no verb is present");
                }
            }

            try {
                return flywayExecutor.execute((migrationResolver, schemaHistory, database, defaultSchema, schemas, callbackExecutor, statementInterceptor) -> {
                    CleanResult cleanResult = doClean(database, schemaHistory, defaultSchema, schemas, callbackExecutor);





                    callbackExecutor.onOperationFinishEvent(Event.AFTER_CLEAN_OPERATION_FINISH, cleanResult);

                    return cleanResult;
                }, false, flywayTelemetryManager);
            } catch (Exception e) {
                telemetryModel.setException(e);
                throw e;
            }
        }
    }

    /**
     * Validate applied migrations against resolved ones (on the filesystem or classpath)
     * to detect accidental changes that may prevent the schema(s) from being recreated exactly.
     * Validation fails if:
     * <ul>
     * <li>differences in migration names, types or checksums are found</li>
     * <li>versions have been applied that aren't resolved locally anymore</li>
     * <li>versions have been resolved that haven't been applied yet</li>
     * </ul>
     *
     * <img src="https://flywaydb.org/assets/balsamiq/command-validate.png" alt="validate">
     *
     * @throws FlywayException when something went wrong during validation.
     * @throws FlywayValidateException when the validation failed.
     */
    public void validate() throws FlywayException {
        final ValidateResult validateResult = validateWithResult();
        if (!validateResult.validationSuccessful) {
            throw new FlywayValidateException(validateResult.errorDetails, validateResult.getAllErrorMessages());
        }
    }

    /**
     * Validate applied migrations against resolved ones (on the filesystem or classpath)
     * to detect accidental changes that may prevent the schema(s) from being recreated exactly.
     * Validation fails if:
     * <ul>
     * <li>differences in migration names, types or checksums are found</li>
     * <li>versions have been applied that aren't resolved locally anymore</li>
     * <li>versions have been resolved that haven't been applied yet</li>
     * </ul>
     *
     * <img src="https://flywaydb.org/assets/balsamiq/command-validate.png" alt="validate">
     *
     * @return An object summarising the validation results
     *
     * @throws FlywayException when something went wrong during validation.
     */
    public ValidateResult validateWithResult() throws FlywayException {
        if (canUseExperimentalMode(configuration, "validate")) {
            logPreviewFeature(NATIVE_CONNECTORS);
            final var verb = configuration.getPluginRegister().getPlugins(VerbExtension.class).stream().filter(verbExtension -> verbExtension.handlesVerb("validate")).findFirst();
            if (verb.isPresent()) {
                LOG.debug("Native Connectors for validate is set and a verb is present");
                return (ValidateResult) verb.get().executeVerb(configuration);
            } else {
                LOG.warn("Native Connectors for validate is set but no verb is present");
            }
        }
        return flywayExecutor.execute((migrationResolver, schemaHistory, database, defaultSchema, schemas, callbackExecutor, statementInterceptor) -> {
            ValidateResult validateResult = doValidate(database, migrationResolver, schemaHistory, defaultSchema, schemas, callbackExecutor, configuration.getIgnoreMigrationPatterns());

            callbackExecutor.onOperationFinishEvent(Event.AFTER_VALIDATE_OPERATION_FINISH, validateResult);

            return validateResult;
        }, true, flywayTelemetryManager);
    }

    /**
     * Baselines an existing database, excluding all migrations up to and including baselineVersion.
     *
     * <img src="https://flywaydb.org/assets/balsamiq/command-baseline.png" alt="baseline">
     *
     * @return An object summarising the actions taken
     *
     * @throws FlywayException when the schema baseline failed.
     */
    @SneakyThrows
    public BaselineResult baseline() throws FlywayException {
        try (EventTelemetryModel telemetryModel = new EventTelemetryModel("baseline", flywayTelemetryManager)) {
            if (canUseExperimentalMode(configuration, "baseline")) {
                logPreviewFeature(NATIVE_CONNECTORS);
                final var verb = configuration.getPluginRegister().getPlugins(VerbExtension.class).stream().filter(verbExtension -> verbExtension.handlesVerb("baseline")).findFirst();
                if (verb.isPresent()) {
                    LOG.debug("Native Connectors for baseline is set and a verb is present");
                    return (BaselineResult) verb.get().executeVerb(configuration);
                } else {
                    LOG.warn("Native Connectors for baseline is set but no verb is present");
                }
            }

            try {
                return flywayExecutor.execute((migrationResolver, schemaHistory, database, defaultSchema, schemas, callbackExecutor, statementInterceptor) -> {
                    if (configuration.isCreateSchemas()) {
                        new DbSchemas(database, schemas, schemaHistory, callbackExecutor).create(true);
                    } else {
                        LOG.warn("The configuration option 'createSchemas' is false.\n" +
                                         "Even though Flyway is configured not to create any schemas, the schema history table still needs a schema to reside in.\n" +
                                         "You must manually create a schema for the schema history table to reside in.\n" +
                                         "See " + FlywayDbWebsiteLinks.MIGRATIONS);
                    }

                    BaselineResult baselineResult = doBaseline(schemaHistory, callbackExecutor, database);





                    callbackExecutor.onOperationFinishEvent(Event.AFTER_BASELINE_OPERATION_FINISH, baselineResult);

                    return baselineResult;
                }, false, flywayTelemetryManager);
            } catch (Exception e) {
                telemetryModel.setException(e);
                throw e;
            }
        }
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
     *
     * @throws FlywayException when the schema history table repair failed.
     */
    @SneakyThrows
    public RepairResult repair() throws FlywayException {
        try (EventTelemetryModel telemetryModel = new EventTelemetryModel("repair", flywayTelemetryManager)) {
            if (canUseExperimentalMode(configuration, "repair")) {
                logPreviewFeature(NATIVE_CONNECTORS);
                final var verb = configuration.getPluginRegister().getPlugins(VerbExtension.class).stream().filter(verbExtension -> verbExtension.handlesVerb("repair")).findFirst();
                if (verb.isPresent()) {
                    LOG.debug("Native Connectors for repair is set and a verb is present");
                    return (RepairResult) verb.get().executeVerb(configuration);
                } else {
                    LOG.warn("Native Connectors for repair is set but no verb is present");
                }
            }

            try {
                return flywayExecutor.execute((migrationResolver, schemaHistory, database, defaultSchema, schemas, callbackExecutor, statementInterceptor) -> {
                    RepairResult repairResult = new DbRepair(database, migrationResolver, schemaHistory, callbackExecutor, configuration).repair();





                    callbackExecutor.onOperationFinishEvent(Event.AFTER_REPAIR_OPERATION_FINISH, repairResult);

                    return repairResult;
                }, true, flywayTelemetryManager);
            } catch (Exception e) {
                telemetryModel.setException(e);
                throw e;
            }
        }
    }

    /**
     * Undoes the most recently applied versioned migration. If target is specified, Flyway will attempt to undo
     * versioned migrations in the order they were applied until it hits one with a version below the target. If there
     * is no versioned migration to undo, calling undo has no effect.
     * <i>Flyway Teams only</i>
     * <img src="https://flywaydb.org/assets/balsamiq/command-undo.png" alt="undo">
     *
     * @return An object summarising the successfully undone migrations.
     *
     * @throws FlywayException when undo failed.
     */
    public OperationResult undo() throws FlywayException {
        try (EventTelemetryModel telemetryModel = new EventTelemetryModel("undo", flywayTelemetryManager)) {
            if (canUseExperimentalMode(configuration, "undo")) {
                logPreviewFeature(NATIVE_CONNECTORS);
                final var verb = configuration.getPluginRegister().getPlugins(VerbExtension.class).stream().filter(verbExtension -> verbExtension.handlesVerb("undo")).findFirst();
                if (verb.isPresent()) {
                    LOG.debug("Native Connectors for undo is set and a verb is present");
                    return (OperationResult) verb.get().executeVerb(configuration);
                } else {
                    LOG.warn("Native Connectors for undo is set but no verb is present");
                }
            }
            try {
                return runCommand("undo", Collections.emptyList());
            } catch (FlywayException e) {
                if (e.getMessage().startsWith("No command extension found")) {
                    throw new FlywayException("The command 'undo' was not recognized. Make sure you have added 'flyway-proprietary' as a dependency.", e);
                }
                throw e;
            }
        }
    }

    private OperationResult runCommand(String command, List<String> flags) {
        return CommandExtensionUtils.runCommandExtension(configuration, command, flags, flywayTelemetryManager);
    }

    private CleanResult doClean(Database database, SchemaHistory schemaHistory, Schema defaultSchema, Schema[] schemas, CallbackExecutor callbackExecutor) {
        return new DbClean(database, schemaHistory, defaultSchema, schemas, callbackExecutor, configuration).clean();
    }

    private ValidateResult doValidate(Database database, CompositeMigrationResolver migrationResolver, SchemaHistory schemaHistory,
                                      Schema defaultSchema, Schema[] schemas, CallbackExecutor callbackExecutor, ValidatePattern[] ignorePatterns) {
        ValidateResult validateResult = new DbValidate(database, schemaHistory, defaultSchema, migrationResolver, configuration, callbackExecutor, ignorePatterns).validate();

        if (configuration.isCleanOnValidationError()) {
            throw new FlywayException("cleanOnValidationError has been removed");
        }
        return validateResult;
    }

    private BaselineResult doBaseline(SchemaHistory schemaHistory, CallbackExecutor callbackExecutor, Database database) {
        return new DbBaseline(schemaHistory, configuration.getBaselineVersion(), configuration.getBaselineDescription(), callbackExecutor, database).baseline();
    }
}
