/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.command;

import lombok.CustomLog;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.ErrorDetails;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.ValidateOutput;
import org.flywaydb.core.api.output.ValidateResult;
import org.flywaydb.core.api.pattern.ValidatePattern;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.ValidatePatternUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the validate command.
 */
@CustomLog
public class DbValidate {
    /**
     * The database schema history table.
     */
    private final SchemaHistory schemaHistory;
    /**
     * The schema containing the schema history table.
     */
    private final Schema schema;
    private final Database database;
    private final CompositeMigrationResolver migrationResolver;
    private final Configuration configuration;
    private final CallbackExecutor callbackExecutor;
    private final Connection connection;
    private final ValidatePattern[] ignorePatterns;

    /**
     * Creates a new database validator.
     *
     * @param schemaHistory The database schema history table.
     * @param schema The schema containing the schema history table.
     */
    public DbValidate(Database database, SchemaHistory schemaHistory, Schema schema, CompositeMigrationResolver migrationResolver,
                      Configuration configuration, CallbackExecutor callbackExecutor, ValidatePattern[] ignorePatterns) {
        this.schemaHistory = schemaHistory;
        this.schema = schema;
        this.database = database;
        this.migrationResolver = migrationResolver;
        this.configuration = configuration;
        this.callbackExecutor = callbackExecutor;
        this.connection = database.getMainConnection();
        this.ignorePatterns = ignorePatterns;
    }

    /**
     * @return The validation error, if any.
     */
    public ValidateResult validate() {
        if (!schema.exists()) {
            if (!migrationResolver.resolveMigrations(configuration).isEmpty() && !ValidatePatternUtils.isPendingIgnored(ignorePatterns)) {
                String validationErrorMessage = "Schema " + schema + " doesn't exist yet";
                ErrorDetails validationError = new ErrorDetails(CoreErrorCode.SCHEMA_DOES_NOT_EXIST, validationErrorMessage);
                return CommandResultFactory.createValidateResult(database.getCatalog(), validationError, 0, null, new ArrayList<>());
            }
            return CommandResultFactory.createValidateResult(database.getCatalog(), null, 0, null, new ArrayList<>());
        }

        callbackExecutor.onEvent(Event.BEFORE_VALIDATE);

        LOG.debug("Validating migrations ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Pair<Integer, List<ValidateOutput>> result = ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(), database)
                .execute(() -> {
                    MigrationInfoServiceImpl migrationInfoService = new MigrationInfoServiceImpl(migrationResolver, schemaHistory, database, configuration,
                                                                                                 configuration.getTarget(),
                                                                                                 configuration.isOutOfOrder(),
                                                                                                 ignorePatterns,
                                                                                                 configuration.getCherryPick());

                    migrationInfoService.refresh();

                    int count = migrationInfoService.all().length;
                    List<ValidateOutput> invalidMigrations = migrationInfoService.validate();
                    return Pair.of(count, invalidMigrations);
                });

        stopWatch.stop();

        List<String> warnings = new ArrayList<>();
        List<ValidateOutput> invalidMigrations = result.getRight();
        ErrorDetails validationError = null;
        int count = 0;
        if (invalidMigrations.isEmpty()) {
            count = result.getLeft();
            if (count == 1) {
                LOG.info(String.format("Successfully validated 1 migration (execution time %s)",
                                       TimeFormat.format(stopWatch.getTotalTimeMillis())));
            } else {
                LOG.info(String.format("Successfully validated %d migrations (execution time %s)",
                                       count, TimeFormat.format(stopWatch.getTotalTimeMillis())));

                if (count == 0) {
                    String noMigrationsWarning = "No migrations found. Are your locations set up correctly?";
                    warnings.add(noMigrationsWarning);
                    LOG.warn(noMigrationsWarning);
                }
            }
            callbackExecutor.onEvent(Event.AFTER_VALIDATE);
        } else {
            validationError = new ErrorDetails(CoreErrorCode.VALIDATE_ERROR, "Migrations have failed validation");
            callbackExecutor.onEvent(Event.AFTER_VALIDATE_ERROR);
        }

        return CommandResultFactory.createValidateResult(database.getCatalog(), validationError, count, invalidMigrations, warnings);
    }
}
