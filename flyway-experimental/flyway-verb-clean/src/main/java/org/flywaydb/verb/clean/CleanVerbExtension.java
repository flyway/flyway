/*-
 * ========================LICENSE_START=================================
 * flyway-verb-clean
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
package org.flywaydb.verb.clean;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CleanResult;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.nc.callbacks.CallbackManager;
import org.flywaydb.nc.preparation.PreparationContext;

public class CleanVerbExtension implements VerbExtension {
    @Override
    public boolean handlesVerb(final String verb) {
        return "clean".equals(verb);
    }

    @Override
    public Object executeVerb(final Configuration configuration) {
        if (configuration.isCleanDisabled()) {
            throw new FlywayException(
                "Unable to execute clean as it has been disabled with the 'flyway.cleanDisabled' property.");
        }

        final PreparationContext context = PreparationContext.get(configuration);

        final ExperimentalDatabase database = context.getDatabase();

        final CallbackManager callbackManager = new CallbackManager(configuration, context.getResources());

        callbackManager.handleEvent(Event.BEFORE_CLEAN, database, configuration, context.getParsingContext());

        final List<String> schemas = new LinkedList<>(Arrays.asList(configuration.getSchemas()));
        final String defaultSchema = database.getDefaultSchema(configuration);
        if (!schemas.contains(defaultSchema)) {
            schemas.add(0, defaultSchema);
        }
        final List<String> flywayCreatedSchemas = getFlywayCreatedSchemas(context, database, schemas);

        final CleanResult cleanResult = new CleanResult(VersionPrinter.getVersion(),
            database.getDatabaseMetaData().databaseName());
        cleanResult.operation = "clean";

        try {
            database.doClean(schemas, flywayCreatedSchemas, cleanResult);
        } catch (FlywayException e) {
            callbackManager.handleEvent(Event.AFTER_CLEAN_ERROR, database, configuration, context.getParsingContext());
            throw e;
        }

        callbackManager.handleEvent(Event.AFTER_CLEAN, database, configuration, context.getParsingContext());

        return cleanResult;
    }

    private static List<String> getFlywayCreatedSchemas(final PreparationContext context,
        final ExperimentalDatabase database,
        final List<String> schemas) {
        return context.getSchemaHistoryModel()
            .getSchemaHistoryItems()
            .stream()
            .filter(x -> Objects.equals(x.getType(), CoreMigrationType.SCHEMA.name()))
            .map(SchemaHistoryItem::getScript)
            .flatMap(x -> Arrays.stream(x.split(",")))
            .map(x -> x.replaceAll(database.getOpenQuote(), ""))
            .map(x -> x.replaceAll(database.getCloseQuote(), ""))
            .filter(schemas::contains)
            .toList();
    }
}
