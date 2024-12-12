/*-
 * ========================LICENSE_START=================================
 * flyway-verb-schemas
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
package org.flywaydb.verb.schemas;

import static org.flywaydb.core.experimental.ExperimentalModeUtils.logExperimentalDataTelemetry;

import java.util.ArrayList;
import java.util.Collection;
import lombok.CustomLog;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.verb.VerbUtils;

@CustomLog
public class SchemasVerbExtension implements VerbExtension {

    @Override
    public boolean handlesVerb(final String verb) {
        return "schemas".equals(verb);
    }

    @Override
    public Object executeVerb(final Configuration configuration, FlywayTelemetryManager flywayTelemetryManager) {
        final ExperimentalDatabase experimentalDatabase;
        try {
            experimentalDatabase = VerbUtils.getExperimentalDatabase(configuration);
        } catch (final Exception e) {
            throw new FlywayException(e);
        }

        logExperimentalDataTelemetry(flywayTelemetryManager, experimentalDatabase.getDatabaseMetaData());

        final Collection<String> missingSchemas = getMissingSchemas(configuration, experimentalDatabase);

        if (missingSchemas.contains(null)) {
            throw new FlywayException("Unable to determine schema for the schema history table." +
                " Set a default schema for the connection or specify one using the defaultSchema property!");
        }

        if (missingSchemas.isEmpty()) {
            return null;
        }

        if (configuration.isCreateSchemas()) {
            experimentalDatabase.createSchemas(missingSchemas.toArray(String[]::new));
        }

        final SchemaHistoryModel schemaHistoryModel = VerbUtils.getSchemaHistoryModel(configuration,
            experimentalDatabase);

        experimentalDatabase.createSchemaHistoryTableIfNotExists(configuration.getTable());

        if (!missingSchemas.isEmpty()) {
            // Update SHT with created Schemas
            final int installedRank = schemaHistoryModel.calculateInstalledRank(CoreMigrationType.SCHEMA);
            createSchemaMarker(experimentalDatabase, configuration, installedRank, missingSchemas);

        }
        return null;
    }

    private Collection<String> getMissingSchemas(final Configuration configuration,
        final ExperimentalDatabase experimentalDatabase) {
        final Collection<String> missingSchemas = new ArrayList<>();
        final String defaultSchema = experimentalDatabase.getDefaultSchema(configuration);
        if (defaultSchema != null) {
            if (!experimentalDatabase.isSchemaExists(defaultSchema)) {
                missingSchemas.add(defaultSchema);
            }
        }

        for (final String schema : configuration.getSchemas()) {
            if (!experimentalDatabase.isSchemaExists(schema) && !missingSchemas.contains(schema)) {
                missingSchemas.add(schema);
            }
        }
        return missingSchemas;
    }

    private void createSchemaMarker(final ExperimentalDatabase experimentalDatabase,
        final Configuration configuration, final int installedRank,
        final Collection<String> missingSchemas) {
        experimentalDatabase.appendSchemaHistoryItem(SchemaHistoryItem.builder()
            .description("<< Flyway Schema Creation >>")
            .installedRank(installedRank)
            .type("SCHEMA")
            .script(experimentalDatabase.doQuote(String.join("\",\"", missingSchemas)))
            .installedBy(experimentalDatabase.getInstalledBy(configuration))
            .executionTime(0)
            .success(true)
            .build(), configuration.getTable());
    }
}
