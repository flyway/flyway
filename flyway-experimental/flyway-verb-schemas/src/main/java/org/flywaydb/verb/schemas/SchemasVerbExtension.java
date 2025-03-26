/*-
 * ========================LICENSE_START=================================
 * flyway-verb-schemas
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
package org.flywaydb.verb.schemas;

import java.util.ArrayList;
import java.util.Collection;
import lombok.CustomLog;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.nc.preparation.PreparationContext;

@CustomLog
public class SchemasVerbExtension implements VerbExtension {

    @Override
    public boolean handlesVerb(final String verb) {
        return "schemas".equals(verb);
    }

    @Override
    public Object executeVerb(final Configuration configuration) {
        final PreparationContext context = PreparationContext.get(configuration);
        final ExperimentalDatabase database = context.getDatabase();

        final Collection<String> missingSchemas = getMissingSchemas(configuration, database);

        if (missingSchemas.contains(null)) {
            throw new FlywayException("Unable to determine schema for the schema history table."
                + " Set a default schema for the connection or specify one using the defaultSchema property!");
        }

        if (missingSchemas.isEmpty()) {
            return null;
        }

        if (configuration.isCreateSchemas()) {
            database.createSchemas(missingSchemas.toArray(String[]::new));
        }

        database.createSchemaHistoryTableIfNotExists(configuration);

        if (!missingSchemas.isEmpty()) {
            // Update SHT with created Schemas
            final int installedRank = context.getSchemaHistoryModel().calculateInstalledRank(CoreMigrationType.SCHEMA);
            createSchemaMarker(database, configuration, installedRank, missingSchemas);
            context.refresh(configuration);
        }
        return null;
    }

    private Collection<String> getMissingSchemas(final Configuration configuration,
        final ExperimentalDatabase database) {
        final Collection<String> missingSchemas = new ArrayList<>();
        final String defaultSchema = database.getDefaultSchema(configuration);
        if (defaultSchema != null) {
            if (!database.isSchemaExists(defaultSchema)) {
                missingSchemas.add(defaultSchema);
            }
        }

        for (final String schema : configuration.getSchemas()) {
            if (!database.isSchemaExists(schema) && !missingSchemas.contains(schema)) {
                missingSchemas.add(schema);
            }
        }
        return missingSchemas;
    }

    private void createSchemaMarker(final ExperimentalDatabase experimentalDatabase,
        final Configuration configuration,
        final int installedRank,
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
