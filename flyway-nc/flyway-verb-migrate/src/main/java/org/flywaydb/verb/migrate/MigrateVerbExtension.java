/*-
 * ========================LICENSE_START=================================
 * flyway-verb-migrate
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
package org.flywaydb.verb.migrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.CustomLog;
import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.api.output.ValidateResult;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.internal.Topic;
import org.flywaydb.core.internal.nc.NativeConnectorsDatabase;
import org.flywaydb.core.extensibility.ConfigurationParameter;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.ValidatePatternUtils;
import org.flywaydb.nc.callbacks.CallbackManager;
import org.flywaydb.nc.utils.VerbUtils;
import org.flywaydb.verb.baseline.BaselineVerbExtension;
import org.flywaydb.nc.info.NativeConnectorsMigrationInfoService;
import org.flywaydb.verb.migrate.migrators.Migrator;
import org.flywaydb.nc.preparation.PreparationContext;
import org.flywaydb.verb.migrate.migrators.MigratorFactory;
import org.flywaydb.verb.schemas.SchemasVerbExtension;
import org.flywaydb.verb.validate.ValidateVerbExtension;

@CustomLog
public class MigrateVerbExtension implements VerbExtension {
    private static final String COMMAND = "migrate";

    @Override
    public String getCommand() {
        return COMMAND;
    }

    @Override
    public String getDescription() {
        return "Migrates the database";
    }

    @Override
    public List<ConfigurationParameter> getConfigurationParameters() {
        return List.of(new ConfigurationParameter("target",
                "Target version up to which Flyway should use migrations",
                false),
            new ConfigurationParameter("outOfOrder", "Allows migrations to be run \"out of order\"", false),
            new ConfigurationParameter("baselineOnMigrate",
                "Baseline on migrate against uninitialized non-empty schema",
                false),
            new ConfigurationParameter("validateOnMigrate", "Validate when running migrate", false),
            new ConfigurationParameter("cherryPick",
                "[teams] Comma separated list of migrations that Flyway should consider when migrating",
                false),
            new ConfigurationParameter("group",
                "Whether to group all pending migrations together in the same transaction when applying them",
                false),
            new ConfigurationParameter("skipExecutingMigrations",
                "Whether Flyway should skip actually executing the contents of the migrations",
                false),
            new ConfigurationParameter("createSchemas",
                "Whether Flyway should attempt to create the schemas specified in the schemas property",
                false),
            new ConfigurationParameter("executeInTransaction",
                "Whether SQL should execute within a transaction",
                false),
            new ConfigurationParameter("mixed", "Allow mixing transactional and non-transactional statements", false));
    }

    @Override
    public String getExample() {
        return "flyway migrate -target=2.0 -outOfOrder=true";
    }

    @Override
    public OperationResult executeVerb(final Configuration configuration) {

        final PreparationContext context = PreparationContext.get(configuration, false);

        if (configuration.isValidateOnMigrate()) {
            validate(configuration);
            context.refresh(configuration);
        }

        final NativeConnectorsDatabase database = context.getDatabase();

        if (configuration.isCreateSchemas()) {
            try {
                final SchemasVerbExtension schemasVerbExtension = new SchemasVerbExtension();
                schemasVerbExtension.useCaching();
                schemasVerbExtension.executeVerb(configuration);
                context.refresh(configuration);
            } catch (final NoClassDefFoundError e) {
                throw new FlywayException("Schemas verb extension is required for creating schemas but is not present",
                    e);
            }
        } else if (!database.isSchemaExists(database.getCurrentSchema())) {
            LOG.warn("""
                     The configuration option 'createSchemas' is false.
                     Even though Flyway is configured not to create any schemas, the schema history table still needs a schema to reside in.
                     You must manually create a schema for the schema history table to reside in.
                     See\s""" + FlywayDbWebsiteLinks.MIGRATIONS);
        }

        if (!database.schemaHistoryTableExists(configuration.getTable())) {
            final NativeConnectorsDatabase finalDatabase = database;
            final List<String> populatedSchemas = Arrays.stream(VerbUtils.getAllSchemas(configuration.getSchemas(),
                    database.getCurrentSchema()))
                .filter(database::isSchemaExists)
                .filter(x -> !finalDatabase.isSchemaEmpty(x))
                .toList();

            if (populatedSchemas.isEmpty() && configuration.isBaselineOnMigrate()) {
                LOG.info(
                    "All configured schemas are empty; a baseline marker will not be added to Flyway's schema history table. "
                        + "A baseline or migration script with a lower version than the baseline version may execute if available. Check the Schemas parameter if this is not intended."
                        + "See "
                        + FlywayDbWebsiteLinks.getRedirectLinkFromTopic(Topic.BASELINE_ON_MIGRATE)
                        + " for more info");
            }

            if (!populatedSchemas.isEmpty() && !configuration.isSkipExecutingMigrations()) {
                if (configuration.isBaselineOnMigrate()) {
                    final BaselineVerbExtension baselineVerbExtension = new BaselineVerbExtension();
                    baselineVerbExtension.useCaching();
                    baselineVerbExtension.executeVerb(configuration);
                    context.refresh(configuration);
                } else {
                    throw new FlywayException("Found non-empty schema(s) "
                        + StringUtils.collectionToCommaDelimitedString(populatedSchemas)
                        + " but no schema history table. Use baseline()"
                        + " or set baselineOnMigrate to true to initialize the schema history table.",
                        CoreErrorCode.NON_EMPTY_SCHEMA_WITHOUT_SCHEMA_HISTORY_TABLE);
                }
            }
        }

        final CallbackManager callbackManager = new CallbackManager(configuration,
            context.getCallbackResources(),
            Event::fromId);

        database.createSchemaHistoryTableIfNotExists(configuration);

        final MigrateResult migrateResult = new MigrateResult(VersionPrinter.getVersion(),
            database.getDatabaseMetaData().databaseName(),
            "",
            database.getDatabaseType());

        final MigrationInfoService migrationInfoService = new NativeConnectorsMigrationInfoService(context.getMigrations(),
            configuration,
            database.getName(),
            database.allSchemasEmpty(VerbUtils.getAllSchemas(configuration.getSchemas(), database.getCurrentSchema())));

        final MigrationInfo current = migrationInfoService.current();
        final MigrationVersion initialSchemaVersion = current != null && current.isVersioned()
            ? current.getVersion()
            : MigrationVersion.EMPTY;
        migrateResult.initialSchemaVersion = initialSchemaVersion.getVersion();
        MigrationInfo[] allPendingMigrations = migrationInfoService.pending();

        if (allPendingMigrations.length > 1 && configuration.getTarget().equals(MigrationVersion.NEXT)) {
            allPendingMigrations = Arrays.copyOf(allPendingMigrations, 1);
        }

        LOG.info("Current version of schema "
            + database.doQuote(database.getCurrentSchema())
            + ": "
            + initialSchemaVersion);

        // To maintain consistency with legacy code, perform an additional round of validation regardless of whether validateOnMigrate is enabled
        secondValidate(migrationInfoService, configuration, database.doQuote(database.getCurrentSchema()));

        if (configuration.isOutOfOrder()) {
            LOG.info("outOfOrder mode is active. Migration of schema "
                + database.doQuote(database.getCurrentSchema())
                + " may not be reproducible.");
        } else {
            allPendingMigrations = removeOutOfOrderPendingMigrations(allPendingMigrations);
        }

        final Migrator migrator = MigratorFactory.getMigrator(database);

        final List<MigrationExecutionGroup> executionGroups = migrator.createGroups(allPendingMigrations,
            configuration,
            database,
            migrateResult,
            context.getParsingContext());

        callbackManager.handleEvent(Event.BEFORE_MIGRATE, database, configuration, context.getParsingContext());

        try {
            final ProgressLogger progress = configuration.createProgress("migrate");
            int installedRank = context.getSchemaHistoryModel().calculateInstalledRank(CoreMigrationType.SQL);
            progress.pushSteps(allPendingMigrations.length);
            for (final MigrationExecutionGroup executionGroup : executionGroups) {
                installedRank = migrator.doExecutionGroup(configuration,
                    executionGroup,
                    database,
                    migrateResult,
                    context.getParsingContext(),
                    installedRank,
                    callbackManager,
                    progress);
            }
        } catch (FlywayException e) {
            callbackManager.handleEvent(Event.AFTER_MIGRATE_ERROR,
                database,
                configuration,
                context.getParsingContext());
            throw e;
        }

        logSummary(migrateResult.migrationsExecuted,
            migrateResult.getTotalMigrationTime(),
            migrateResult.targetSchemaVersion,
            database);

        if (migrateResult.migrationsExecuted > 0) {
            callbackManager.handleEvent(Event.AFTER_MIGRATE_APPLIED,
                database,
                configuration,
                context.getParsingContext());
        }
        callbackManager.handleEvent(Event.AFTER_MIGRATE, database, configuration, context.getParsingContext());

        return migrateResult;
    }

    private static void validate(final Configuration configuration) {
        final FluentConfiguration validateConfig = new FluentConfiguration().configuration(configuration);
        final List<ValidatePattern> ignorePatterns = new ArrayList<>(Arrays.asList(configuration.getIgnoreMigrationPatterns()));
        ignorePatterns.add(ValidatePattern.fromPattern("*:pending"));
        validateConfig.ignoreMigrationPatterns(ignorePatterns.toArray(ValidatePattern[]::new));
        final ValidateVerbExtension validateVerbExtension = new ValidateVerbExtension();
        validateVerbExtension.useCaching();
        final ValidateResult validateResult = (ValidateResult) validateVerbExtension.executeVerb(validateConfig);
        if (!validateResult.validationSuccessful) {
            throw new FlywayValidateException(validateResult.errorDetails, validateResult.getAllErrorMessages());
        }
    }

    private static void secondValidate(final MigrationInfoService infoService,
        final Configuration configuration,
        final String schema) {
        final List<MigrationInfo> failed = Arrays.stream(infoService.all())
            .filter(migrationInfo -> migrationInfo.getState().isFailed())
            .toList();

        if (failed.isEmpty()) {
            return;
        }

        final MigrationInfo firstFailure = failed.get(0);

        if (failed.size() == 1
            && firstFailure.getState() == MigrationState.FUTURE_FAILED
            && ValidatePatternUtils.isFutureIgnored(configuration.getIgnoreMigrationPatterns())) {
            LOG.warn("Schema "
                + schema
                + " contains a failed future migration to version "
                + firstFailure.getVersion()
                + " !");
            return;
        }

        if (firstFailure.isRepeatable()) {
            throw new FlywayException("Schema "
                + schema
                + " contains a failed repeatable migration ("
                + "\""
                + firstFailure.getDescription()
                + "\""
                + ") !");
        }

        throw new FlywayException("Schema "
            + schema
            + " contains a failed migration to version "
            + firstFailure.getVersion()
            + " !");
    }

    private MigrationInfo[] removeOutOfOrderPendingMigrations(final MigrationInfo[] migrations) {
        final List<MigrationInfo> result = new ArrayList<>();

        for (final MigrationInfo migration : migrations) {
            if (!migration.isVersioned() || result.isEmpty() || migration.getVersion()
                .isNewerThan(result.get(result.size() - 1).getVersion())) {
                result.add(migration);
            }
        }

        return result.toArray(MigrationInfo[]::new);
    }

    private void logSummary(final int migrationSuccessCount,
        final long executionTime,
        final String targetVersion,
        final NativeConnectorsDatabase database) {
        final String schemaName = database.doQuote(database.getCurrentSchema());
        if (migrationSuccessCount == 0) {
            LOG.info("Schema " + schemaName + " is up to date. No migration necessary.");
            return;
        }

        final String targetText = (targetVersion != null) ? ", now at version v" + targetVersion : "";

        final String migrationText = "migration" + StringUtils.pluralizeSuffix(migrationSuccessCount);

        LOG.info("Successfully applied %d %s to schema %s%s (execution time %s)".formatted(migrationSuccessCount,
            migrationText,
            schemaName,
            targetText,
            TimeFormat.format(executionTime)));
    }
}
