/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
     * The schema containing the schema history table.
     */
    private final Schema schema;
    private final Connection connection;
    private final ValidatePattern[] ignorePatterns;
    private FlywayCommandSupport flywayCommandSupport = new FlywayCommandSupport(null, null, null, null, null);

    /**
     * Creates a new database validator.
     *
     * @param schemaHistory The database schema history table.
     * @param schema The schema containing the schema history table.
     */
    public DbValidate(Database database, SchemaHistory schemaHistory, Schema schema, CompositeMigrationResolver migrationResolver,
                      Configuration configuration, CallbackExecutor callbackExecutor, ValidatePattern[] ignorePatterns) {
        this.flywayCommandSupport.setSchemaHistory(schemaHistory);
        this.schema = schema;
        this.flywayCommandSupport.setDatabase(database);
        this.flywayCommandSupport.setMigrationResolver(migrationResolver);
        this.flywayCommandSupport.setConfiguration(configuration);
        this.flywayCommandSupport.setCallbackExecutor(callbackExecutor);
        this.connection = database.getMainConnection();
        this.ignorePatterns = ignorePatterns;
    }

    /**
     * @return The validation error, if any.
     */
    public ValidateResult validate() {
        if (!schema.exists()) {
            if (!flywayCommandSupport.getMigrationResolver().resolveMigrations(flywayCommandSupport.getConfiguration()).isEmpty() && !ValidatePatternUtils.isPendingIgnored(ignorePatterns)) {
                String validationErrorMessage = "Schema " + schema + " doesn't exist yet";
                ErrorDetails validationError = new ErrorDetails(CoreErrorCode.SCHEMA_DOES_NOT_EXIST, validationErrorMessage);
                return CommandResultFactory.createValidateResult(flywayCommandSupport.getDatabase().getCatalog(), validationError, 0, null, new ArrayList<>());
            }
            return CommandResultFactory.createValidateResult(flywayCommandSupport.getDatabase().getCatalog(), null, 0, null, new ArrayList<>());
        }

        flywayCommandSupport.getCallbackExecutor().onEvent(Event.BEFORE_VALIDATE);

        LOG.debug("Validating migrations ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Pair<Integer, List<ValidateOutput>> result = ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(), flywayCommandSupport.getDatabase())
                .execute(() -> {
                    MigrationInfoServiceImpl migrationInfoService = new MigrationInfoServiceImpl(flywayCommandSupport.getMigrationResolver(), flywayCommandSupport.getSchemaHistory(), flywayCommandSupport.getDatabase(), flywayCommandSupport.getConfiguration(),
                                                                                                 flywayCommandSupport.getConfiguration().getTarget(),
                                                                                                 flywayCommandSupport.getConfiguration().isOutOfOrder(),
                                                                                                 ignorePatterns,
                                                                                                 flywayCommandSupport.getConfiguration().getCherryPick());

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
            flywayCommandSupport.getCallbackExecutor().onEvent(Event.AFTER_VALIDATE);
        } else {
            validationError = new ErrorDetails(CoreErrorCode.VALIDATE_ERROR, "Migrations have failed validation");
            flywayCommandSupport.getCallbackExecutor().onEvent(Event.AFTER_VALIDATE_ERROR);
        }

        return CommandResultFactory.createValidateResult(flywayCommandSupport.getDatabase().getCatalog(), validationError, count, invalidMigrations, warnings);
    }
}