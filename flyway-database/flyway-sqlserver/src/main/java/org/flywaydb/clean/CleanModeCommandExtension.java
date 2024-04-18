/*-
 * ========================LICENSE_START=================================
 * flyway-sqlserver
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
package org.flywaydb.clean;

import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.FlywayExecutor;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CleanResult;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.internal.command.clean.CleanModel;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.database.base.Schema;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.flywaydb.core.internal.command.clean.CleanModeConfigurationExtension.Mode.ALL;
import static org.flywaydb.core.internal.command.clean.CleanModeConfigurationExtension.Mode.SCHEMA;

@ExtensionMethod({ CleanModeSupportedDatabases.class, Arrays.class })
public class CleanModeCommandExtension implements CommandExtension {
    private static final String CLEAN_SCHEMAS = "clean-schemas";
    private static final String CLEAN_ALL = "clean-all";
    private static final List<String> SUPPORTED_COMMANDS = Arrays.asList(CLEAN_SCHEMAS, CLEAN_ALL);

    @Override
    public boolean handlesCommand(String command) {
        return SUPPORTED_COMMANDS.contains(command.toLowerCase());
    }

    @Override
    public boolean handlesParameter(String parameter) {
        return false;
    }

    @Override
    public OperationResult handle(String command, Configuration config, List<String> flags, FlywayTelemetryManager flywayTelemetryManager) throws FlywayException {
        return new FlywayExecutor(config).execute((migrationResolver, schemaHistory, database, defaultSchema, schemas, callbackExecutor, statementInterceptor) -> {
            if (!database.supportsCleanMode()) {
                throw new FlywayException("Clean modes other than default are not supported for " + database.getDatabaseType().getName());
            }
            if (config.getSchemas().length > 0) {
                throw new FlywayException("'flyway.schemas' must be empty when using clean modes other than default");
            }

            CleanResult cleanResult = CommandResultFactory.createCleanResult(database.getCatalog());
            CleanModel clean = ConfigUtils.getCleanModel(config);
            CleanModeCleanExecutor cleanExecutor = new CleanModeCleanExecutor(database.getMainConnection(), database, schemaHistory, callbackExecutor, clean.getMode());

            Schema[] allSchemas = Arrays.stream(database.getAllSchemas())
                                        .filter(s -> clean == null || clean.getSchemas() == null || !clean.getSchemas().getExclude().contains(s.getName()))
                                        .toArray(Schema[]::new);

            if (clean != null) {
                if (SCHEMA.name().equalsIgnoreCase(clean.getMode())) {
                    cleanExecutor.clean(defaultSchema, allSchemas, cleanResult);
                } else if (ALL.name().equalsIgnoreCase(clean.getMode())) {
                    cleanExecutor.clean(defaultSchema, allSchemas, cleanResult, allSchemas.stream().map(Schema::getName).collect(Collectors.toList()));
                }
            }

            return cleanResult;
        }, false, flywayTelemetryManager);
    }
}
