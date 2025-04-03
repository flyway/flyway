/*-
 * ========================LICENSE_START=================================
 * flyway-nc-utils
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
package org.flywaydb.nc.preparation;

import static org.flywaydb.core.experimental.ExperimentalModeUtils.logExperimentalDataTelemetry;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.Getter;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.extensibility.Plugin;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.nc.VerbUtils;

@Getter
public final class PreparationContext implements Plugin {

    private ExperimentalDatabase database;

    private SchemaHistoryModel schemaHistoryModel;

    private Collection<LoadableResourceMetadata> resources;

    private MigrationInfo[] migrations;

    private ParsingContext parsingContext;

    private boolean isInitialized;

    private String cacheString;

    public void initialize(final Configuration configuration) {
        try {
            database = VerbUtils.getExperimentalDatabase(configuration);

            final CompletableFuture<SchemaHistoryModel> schemaHistoryModelFuture = CompletableFuture.supplyAsync(() -> VerbUtils.getSchemaHistoryModel(
                configuration,
                database));
            CompletableFuture.runAsync(() -> logExperimentalDataTelemetry(VerbUtils.getFlywayTelemetryManager(
                configuration), database.getDatabaseMetaData()));
            final Future<Collection<LoadableResourceMetadata>> resourcesFuture = CompletableFuture.supplyAsync(() -> VerbUtils.scanForResources(
                configuration,
                database));

            cacheString = getCacheString(configuration);

            try {
                schemaHistoryModel = schemaHistoryModelFuture.get();
                resources = resourcesFuture.get();
            } catch (final ExecutionException | InterruptedException e) {
                if (e.getCause() != null) {
                    throw new FlywayException(e.getCause());
                } else {
                    throw new FlywayException(e);
                }
            }

            migrations = VerbUtils.getMigrations(schemaHistoryModel,
                resources.toArray(LoadableResourceMetadata[]::new),
                configuration);

            parsingContext = new ParsingContext();
            parsingContext.populate(database, configuration);

            isInitialized = true;
        } catch (final SQLException sqlException) {
            throw new FlywayException(sqlException);
        }
    }

    private String getCacheString(final Configuration configuration) {
        return String.join(",",
            Arrays.stream(configuration.getLocations()).map(Object::toString).toArray(String[]::new))
            + configuration.getPlaceholders()
            .entrySet()
            .stream()
            .map(x -> "[" + x.getKey() + "=" + x.getValue() + "]")
            .reduce("", String::concat);
    }

    public void refresh(final Configuration configuration) {
        if (database.isClosed()) {
            try {
                database = VerbUtils.getExperimentalDatabase(configuration);
            } catch (final SQLException sqlException) {
                throw new FlywayException(sqlException);
            }
        }

        schemaHistoryModel = VerbUtils.getSchemaHistoryModel(configuration, database);
        migrations = VerbUtils.getMigrations(schemaHistoryModel,
            resources.toArray(LoadableResourceMetadata[]::new),
            configuration);
    }

    public static PreparationContext get(final Configuration configuration, final boolean cached) {
        final PreparationContext preparationContext = configuration.getPluginRegister()
            .getPlugin(PreparationContext.class);

        if (cached) {
            preparationContext.refresh(configuration);
        } else {
            preparationContext.initialize(configuration);
        }

        return preparationContext;
    }
}
