/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.core.internal.command.clean;

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CleanResult;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.command.clean.CleanModeConfigurationExtension.Mode;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;

import java.util.Collections;

@CustomLog
public class DbClean {
    private final SchemaHistory schemaHistory;
    protected final Schema defaultSchema;
    protected final Schema[] schemas;
    protected final Connection connection;
    protected final Database database;
    protected final CallbackExecutor callbackExecutor;
    protected final Configuration configuration;

    public DbClean(Database database, SchemaHistory schemaHistory, Schema defaultSchema, Schema[] schemas, CallbackExecutor callbackExecutor, Configuration configuration) {
        this.schemaHistory = schemaHistory;
        this.defaultSchema = defaultSchema;
        this.schemas = schemas;
        this.connection = database.getMainConnection();
        this.database = database;
        this.callbackExecutor = callbackExecutor;
        this.configuration = configuration;
    }

    public CleanResult clean() throws FlywayException {
        if (configuration.isCleanDisabled()) {
            throw new FlywayException("Unable to execute clean as it has been disabled with the 'flyway.cleanDisabled' property.");
        }

        callbackExecutor.onEvent(Event.BEFORE_CLEAN);

        Mode cleanMode = configuration.getPluginRegister().getPlugin(CleanModeConfigurationExtension.class).getCleanMode();

        CleanResult cleanResult;

        if (cleanMode == Mode.DEFAULT) {
            cleanResult = CommandResultFactory.createCleanResult(database.getCatalog());
            new CleanExecutor(connection, database, schemaHistory, callbackExecutor).clean(defaultSchema, schemas, cleanResult);
        } else {
            String command = toCommand(cleanMode);
            cleanResult = configuration.getPluginRegister().getPlugins(CommandExtension.class).stream()
                                       .filter(e -> e.handlesCommand(command))
                                       .findFirst()
                                       .map(e -> (CleanResult) e.handle(command, configuration, Collections.emptyList(), null))
                                       .orElseThrow(() -> new FlywayException("No command extension found to handle clean mode " + cleanMode));
        }

        callbackExecutor.onEvent(Event.AFTER_CLEAN);

        schemaHistory.clearCache();

        return cleanResult;
    }

    private String toCommand(Mode mode) {
        switch (mode) {
            case SCHEMA:
                return "clean-schemas";
            case ALL:
                return "clean-all";
            default:
                return "clean";
        }
    }
}