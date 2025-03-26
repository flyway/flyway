/*-
 * ========================LICENSE_START=================================
 * flyway-verb-baseline
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
package org.flywaydb.verb.baseline;

import lombok.CustomLog;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.BaselineResult;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;
import org.flywaydb.nc.preparation.PreparationContext;
import org.flywaydb.verb.schemas.SchemasVerbExtension;

@CustomLog
public class BaselineVerbExtension implements VerbExtension {

    @Override
    public boolean handlesVerb(final String verb) {
        return "baseline".equals(verb);
    }

    @Override
    public Object executeVerb(final Configuration configuration) {
        final PreparationContext context = PreparationContext.get(configuration);
        final ExperimentalDatabase database = context.getDatabase();

        final BaselineResult baselineResult = new BaselineResult(VersionPrinter.getVersion(),
            database.getDatabaseMetaData().databaseName());
        final MigrationVersion baselineVersion = configuration.getBaselineVersion();

        final String schemaHistoryName = configuration.getTable();

        if (configuration.isCreateSchemas()) {
            final boolean schemaHistoryTablePreExisting = database.schemaHistoryTableExists(schemaHistoryName);
            new SchemasVerbExtension().executeVerb(configuration);
            if (!schemaHistoryTablePreExisting) {
                createBaselineMarker(configuration, database, baselineResult);
                return baselineResult;
            }
        } else {
            LOG.warn("""
                     The configuration option 'createSchemas' is false.
                     Even though Flyway is configured not to create any schemas, the schema history table still needs a schema to reside in.
                     You must manually create a schema for the schema history table to reside in.
                     See\s""" + FlywayDbWebsiteLinks.MIGRATIONS);
        }

        final boolean schemaHistoryTableExists = database.schemaHistoryTableExists(schemaHistoryName);
        try {
            if (!schemaHistoryTableExists) {
                createBaselineMarker(configuration, database, baselineResult);
                return baselineResult;
            }

            final String schemaHistoryText = database.quote(database.getCurrentSchema()) + "." + database.quote(
                schemaHistoryName);
            final boolean baselinePresent = context.getSchemaHistoryModel()
                .getSchemaHistoryItems()
                .stream()
                .anyMatch(x -> CoreMigrationType.BASELINE.name().equals(x.getType()));

            final boolean onlySchemas = context.getSchemaHistoryModel()
                .getSchemaHistoryItems()
                .stream()
                .allMatch(x -> CoreMigrationType.SCHEMA.name().equals(x.getType()));

            if (baselinePresent) {
                final String baselineDescription = configuration.getBaselineDescription();
                final SchemaHistoryItem baselineMarker = context.getSchemaHistoryModel()
                    .getSchemaHistoryItems()
                    .stream()
                    .filter(x -> CoreMigrationType.BASELINE.name().equals(x.getType()))
                    .findFirst()
                    .get();

                if (baselineVersion.getVersion().equals(baselineMarker.getVersion()) && baselineDescription.equals(
                    baselineMarker.getDescription())) {
                    LOG.info("Schema history table "
                        + schemaHistoryText
                        + " already initialized with ("
                        + baselineVersion
                        + ","
                        + baselineDescription
                        + "). Skipping.");
                    baselineResult.successfullyBaselined = true;
                    baselineResult.baselineVersion = baselineVersion.toString();
                } else {
                    throw new FlywayException("Unable to baseline schema history table "
                        + schemaHistoryText
                        + " with ("
                        + baselineVersion
                        + ","
                        + baselineDescription
                        + ") as it has already been baselined with ("
                        + baselineMarker.getVersion()
                        + ","
                        + baselineMarker.getDescription()
                        + ")\n"
                        + "Need to reset your baseline? Learn more: "
                        + FlywayDbWebsiteLinks.RESET_THE_BASELINE_MIGRATION);
                }
            } else {
                final boolean schemaPresent = context.getSchemaHistoryModel()
                    .getSchemaHistoryItems()
                    .stream()
                    .anyMatch(x -> CoreMigrationType.SCHEMA.name().equals(x.getType()));

                if (schemaPresent && baselineVersion.equals(MigrationVersion.fromVersion("0"))) {
                    throw new FlywayException("Unable to baseline schema history table "
                        + schemaHistoryText
                        + " with version 0 as this version was used for schema creation");
                }
                final boolean nonSyntheticMigrations = context.getSchemaHistoryModel()
                    .getSchemaHistoryItems()
                    .stream()
                    .anyMatch(x -> !CoreMigrationType.fromString(x.getType()).isSynthetic());
                if (nonSyntheticMigrations) {
                    throw new FlywayException("Unable to baseline schema history table "
                        + schemaHistoryText
                        + " as it already contains migrations\n"
                        + "Need to reset your baseline? Learn more: "
                        + FlywayDbWebsiteLinks.RESET_THE_BASELINE_MIGRATION);
                }
                if (context.getSchemaHistoryModel().getSchemaHistoryItems().isEmpty()) {
                    throw new FlywayException("Unable to baseline schema history table "
                        + schemaHistoryText
                        + " as it already exists, and is empty.\n"
                        + "Delete the schema history table, and run baseline again.");
                }

                if (onlySchemas) {
                    createBaselineMarker(configuration, database, baselineResult);
                } else {
                    throw new FlywayException("Unable to baseline schema history table "
                        + schemaHistoryText
                        + " as it already contains migrations.\n"
                        + "Delete the schema history table, and run baseline again.\n"
                        + "Need to reset your baseline? Learn more: "
                        + FlywayDbWebsiteLinks.RESET_THE_BASELINE_MIGRATION);
                }
            }
        } catch (final FlywayException e) {
            baselineResult.successfullyBaselined = false;
            throw e;
        }

        return baselineResult;
    }

    private static void createBaselineMarker(final Configuration configuration,
        final ExperimentalDatabase database,
        final BaselineResult baselineResult) {

        database.createSchemaHistoryTableIfNotExists(configuration);

        final String baselineVersion = configuration.getBaselineVersion().getVersion();
        database.appendSchemaHistoryItem(SchemaHistoryItem.builder()
            .description(configuration.getBaselineDescription())
            .installedRank(1)
            .type("BASELINE")
            .script("<< Flyway Baseline >>")
            .installedBy(database.getInstalledBy(configuration))
            .version(baselineVersion)
            .executionTime(0)
            .success(true)
            .build(), configuration.getTable());

        LOG.info("Successfully baselined schema with version: " + baselineVersion);
        baselineResult.successfullyBaselined = true;
        baselineResult.baselineVersion = baselineVersion;
    }
}
