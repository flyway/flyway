/*-
 * ========================LICENSE_START=================================
 * flyway-verb-info
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
package org.flywaydb.verb.info;

import java.util.List;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.internal.nc.NativeConnectorsDatabase;
import org.flywaydb.core.extensibility.ConfigurationParameter;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.nc.callbacks.CallbackManager;
import org.flywaydb.nc.utils.VerbUtils;
import org.flywaydb.nc.info.NativeConnectorsMigrationInfoService;
import org.flywaydb.nc.preparation.PreparationContext;

@CustomLog
public class InfoVerbExtension implements VerbExtension {
    private static final String COMMAND = "info";

    @Override
    public String getCommand() {
        return COMMAND;
    }

    @Override
    public String getDescription() {
        return "Prints the information about applied, current and pending migrations";
    }

    @Override
    public List<ConfigurationParameter> getConfigurationParameters() {
        return List.of(new ConfigurationParameter("infoSinceDate",
                "Limits info to show only migrations applied after this date, and any pending migrations. Must be in the format dd/MM/yyyy HH:mm (e.g. 01/12/2020 13:00)",
                false),
            new ConfigurationParameter("infoUntilDate",
                "Limits info to show only migrations applied before this date. Must be in the format dd/MM/yyyy HH:mm (e.g. 01/12/2020 13:00)",
                false),
            new ConfigurationParameter("infoSinceVersion",
                "Limits info to show only migrations greater than or equal to this version, and any repeatable migrations",
                false),
            new ConfigurationParameter("infoUntilVersion",
                "Limits info to show only migrations less than or equal to this version, and any repeatable migrations",
                false),
            new ConfigurationParameter("infoOfState",
                "Limits info to show only migrations of the provided states. This is a case insensitive, comma-separated list",
                false),
            new ConfigurationParameter("migrationIds",
                "Suppresses all other output and displays a comma-separated list of migration versions for versioned migrations and descriptions for repeatable migrations",
                false));
    }

    @Override
    public String getExample() {
        return "flyway info -infoSinceDate=\"01/12/2020 13:00\"";
    }

    @Override
    public OperationResult executeVerb(final Configuration configuration) {

        final PreparationContext context = PreparationContext.get(configuration, false);

        final NativeConnectorsDatabase<?> database = context.getDatabase();
        final CallbackManager callbackManager = new CallbackManager(configuration,
            context.getCallbackResources(),
            Event::fromId);

        callbackManager.handleEvent(Event.BEFORE_INFO, database, configuration, context.getParsingContext());

        if (!database.schemaHistoryTableExists(configuration.getTable())) {
            LOG.info("Schema history table "
                + database.quote(database.getCurrentSchema(), configuration.getTable())
                + " does not exist yet");
        }

        final NativeConnectorsMigrationInfoService infoResult;
        try {
            infoResult = new NativeConnectorsMigrationInfoService(context.getMigrations(),
                configuration,
                database.getName(),
                database.allSchemasEmpty(VerbUtils.getAllSchemas(configuration.getSchemas(),
                    database.getCurrentSchema())));
        } catch (final FlywayException e) {
            callbackManager.handleEvent(Event.AFTER_INFO_ERROR, database, configuration, context.getParsingContext());
            throw e;
        }

        callbackManager.handleEvent(Event.AFTER_INFO, database, configuration, context.getParsingContext());
        return infoResult;
    }
}
