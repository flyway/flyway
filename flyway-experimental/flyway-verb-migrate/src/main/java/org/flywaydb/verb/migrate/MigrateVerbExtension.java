/*-
 * ========================LICENSE_START=================================
 * flyway-verb-migrate
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
package org.flywaydb.verb.migrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.CustomLog;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.output.ValidateResult;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.verb.VerbUtils;
import org.flywaydb.verb.info.ExperimentalMigrationInfoService;
import org.flywaydb.verb.migrate.migrators.ApiMigrator;
import org.flywaydb.verb.migrate.migrators.JdbcMigrator;
import org.flywaydb.verb.migrate.migrators.Migrator;
import org.flywaydb.verb.schemas.SchemasVerbExtension;

@CustomLog
public class MigrateVerbExtension implements VerbExtension {

    @Override
    public boolean handlesVerb(final String verb) {
        return "migrate".equals(verb);
    }

    @Override
    public Object executeVerb(final Configuration configuration) {
        if (configuration.isValidateOnMigrate()) {
            validate(configuration);
        }
        final ExperimentalDatabase experimentalDatabase;
        try {
            experimentalDatabase = VerbUtils.getExperimentalDatabase(configuration);
        } catch (final Exception e) {
            throw new FlywayException(e);
        }

        if (configuration.isCreateSchemas()) {
            try {
                new SchemasVerbExtension().executeVerb(configuration);
            } catch (final NoClassDefFoundError e) {
                throw new FlywayException("Schemas verb extension is required for creating schemas but is not present", e);
            }
        }

        if (!experimentalDatabase.schemaHistoryTableExists(configuration.getTable())) {
            final List<String> populatedSchemas = Arrays.stream(VerbUtils.getAllSchemasFromConfiguration(configuration))
                .filter(experimentalDatabase::isSchemaExists)
                .filter(x -> !experimentalDatabase.isSchemaEmpty(x))
                .toList();
            if (!populatedSchemas.isEmpty() && !configuration.isSkipExecutingMigrations()) {
                if (configuration.isBaselineOnMigrate()) {
                    new Flyway(configuration).baseline();
                } else {
                    throw new FlywayException("Found non-empty schema(s) "
                        + StringUtils.collectionToCommaDelimitedString(populatedSchemas)
                        + " but no schema history table. Use baseline()"
                        + " or set baselineOnMigrate to true to initialize the schema history table.",
                        CoreErrorCode.NON_EMPTY_SCHEMA_WITHOUT_SCHEMA_HISTORY_TABLE);
                }
            }
        }

        experimentalDatabase.createSchemaHistoryTableIfNotExists(configuration.getTable());

        final SchemaHistoryModel schemaHistoryModel = VerbUtils.getSchemaHistoryModel(configuration, experimentalDatabase);
        final MigrationInfo[] migrations = VerbUtils.getMigrationInfos(configuration,
            experimentalDatabase,
            VerbUtils.getSchemaHistoryModel(configuration, experimentalDatabase));

        final MigrateResult migrateResult = new MigrateResult(VersionPrinter.getVersion(),
            experimentalDatabase.getDatabaseMetaData().databaseName(),
            "",
            experimentalDatabase.getDatabaseType());

        final MigrationInfoService migrationInfoService = new ExperimentalMigrationInfoService(migrations,
            configuration,
            experimentalDatabase.getName(),
            experimentalDatabase.allSchemasEmpty(VerbUtils.getAllSchemasFromConfiguration(configuration)));

        final MigrationInfo current = migrationInfoService.current();
        MigrationVersion initialSchemaVersion =  current != null && current.isVersioned()  ?
            current.getVersion() : MigrationVersion.EMPTY;
        migrateResult.initialSchemaVersion = initialSchemaVersion.getVersion();
        MigrationInfo[] allPendingMigrations = migrationInfoService.pending();

        if (allPendingMigrations.length > 1 && configuration.getTarget().equals(MigrationVersion.NEXT)) {
            allPendingMigrations = Arrays.copyOf(allPendingMigrations, 1);
        }

        LOG.info("Current version of schema "
            + experimentalDatabase.doQuote(experimentalDatabase.getCurrentSchema())
            + ": "
            + initialSchemaVersion);

        if (configuration.isOutOfOrder()) {
            final String outOfOrderWarning = "outOfOrder mode is active. Migration of schema " + experimentalDatabase.doQuote(
                experimentalDatabase.getCurrentSchema()) + " may not be reproducible.";
            LOG.warn(outOfOrderWarning);
            migrateResult.addWarning(outOfOrderWarning);
        }

        final ParsingContext parsingContext = new ParsingContext();
        parsingContext.populate(experimentalDatabase, configuration);

        final Migrator migrator = switch (experimentalDatabase.getDatabaseMetaData().connectionType()) {
            case API -> new ApiMigrator();
            case JDBC -> new JdbcMigrator();
            case EXECUTABLE -> null;
        };

        final List<MigrationExecutionGroup> executionGroups = migrator.createGroups(allPendingMigrations, configuration, experimentalDatabase, migrateResult, parsingContext);

        int installedRank = experimentalDatabase.getSchemaHistoryModel(configuration.getTable()).calculateInstalledRank(CoreMigrationType.SQL);
        for (final MigrationExecutionGroup executionGroup : executionGroups) {
            installedRank = migrator.doExecutionGroup(configuration,
                executionGroup,
                experimentalDatabase,
                migrateResult,
                parsingContext,
                installedRank);
        }
        logSummary(migrateResult.migrationsExecuted,
            migrateResult.getTotalMigrationTime(),
            migrateResult.targetSchemaVersion,
            experimentalDatabase);

        try {
            experimentalDatabase.close();
        } catch (Exception e) {
            throw new FlywayException(e);
        }

        return migrateResult;
    }

    private static void validate(final Configuration configuration) {
        final FluentConfiguration validateConfig = new FluentConfiguration().configuration(configuration);
        final List<ValidatePattern> ignorePatterns = new ArrayList<>(Arrays.asList(configuration.getIgnoreMigrationPatterns()));
        ignorePatterns.add(ValidatePattern.fromPattern("*:pending"));
        validateConfig.ignoreMigrationPatterns(ignorePatterns.toArray(ValidatePattern[]::new));
        final ValidateResult validateResult = new Flyway(validateConfig).validateWithResult();
        if (!validateResult.validationSuccessful) {
            throw new FlywayValidateException(validateResult.errorDetails, validateResult.getAllErrorMessages());
        }
    }    

    private void logSummary(final int migrationSuccessCount,
        final long executionTime,
        final String targetVersion,
        final ExperimentalDatabase experimentalDatabase) {
        final String schemaName = experimentalDatabase.doQuote(experimentalDatabase.getCurrentSchema());
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
