/*
 * Copyright 2010-2018 Boxfuse GmbH
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

import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;

import java.util.concurrent.Callable;

/**
 * Handles the validate command.
 *
 * @author Axel Fontaine
 */
public class DbValidate {
    private static final Log LOG = LogFactory.getLog(DbValidate.class);

    /**
     * The database schema history table.
     */
    private final SchemaHistory schemaHistory;

    /**
     * The schema containing the schema history table.
     */
    private final Schema schema;

    /**
     * The migration resolver.
     */
    private final MigrationResolver migrationResolver;

    /**
     * The connection to use.
     */
    private final Connection connection;

    /**
     * The current configuration.
     */
    private final Configuration configuration;

    /**
     * Whether pending migrations are allowed.
     */
    private final boolean pending;

    /**
     * The callback executor.
     */
    private final CallbackExecutor callbackExecutor;

    /**
     * Creates a new database validator.
     *
     * @param database          The DB support for the connection.
     * @param schemaHistory     The database schema history table.
     * @param schema            The database schema to use by default.
     * @param migrationResolver The migration resolver.
     * @param configuration     The current configuration.
     * @param pending           Whether pending migrations are allowed.
     * @param callbackExecutor  The callback executor.
     */
    public DbValidate(Database database, SchemaHistory schemaHistory, Schema schema, MigrationResolver migrationResolver,
                      Configuration configuration, boolean pending, CallbackExecutor callbackExecutor) {
        this.connection = database.getMainConnection();
        this.schemaHistory = schemaHistory;
        this.schema = schema;
        this.migrationResolver = migrationResolver;
        this.configuration = configuration;
        this.pending = pending;
        this.callbackExecutor = callbackExecutor;
    }

    /**
     * Starts the actual migration.
     *
     * @return The validation error, if any.
     */
    public String validate() {
        if (!schema.exists()) {
            if (!migrationResolver.resolveMigrations(new Context() {
                @Override
                public Configuration getConfiguration() {
                    return configuration;
                }
            }).isEmpty() && !pending) {
                return "Schema " + schema + " doesn't exist yet";
            }
            return null;
        }

        callbackExecutor.onEvent(Event.BEFORE_VALIDATE);

        LOG.debug("Validating migrations ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Pair<Integer, String> result = new TransactionTemplate(connection.getJdbcConnection()).execute(new Callable<Pair<Integer, String>>() {
            @Override
            public Pair<Integer, String> call() {
                MigrationInfoServiceImpl migrationInfoService =
                        new MigrationInfoServiceImpl(migrationResolver, schemaHistory, configuration,
                                configuration.getTarget(),
                                configuration.isOutOfOrder(),
                                pending,
                                configuration.isIgnoreMissingMigrations(),
                                configuration.isIgnoreIgnoredMigrations(),
                                configuration.isIgnoreFutureMigrations());

                migrationInfoService.refresh();

                int count = migrationInfoService.all().length;
                String validationError = migrationInfoService.validate();
                return Pair.of(count, validationError);
            }
        });

        stopWatch.stop();

        String error = result.getRight();
        if (error == null) {
            int count = result.getLeft();
            if (count == 1) {
                LOG.info(String.format("Successfully validated 1 migration (execution time %s)",
                        TimeFormat.format(stopWatch.getTotalTimeMillis())));
            } else {
                LOG.info(String.format("Successfully validated %d migrations (execution time %s)",
                        count, TimeFormat.format(stopWatch.getTotalTimeMillis())));
            }
            callbackExecutor.onEvent(Event.AFTER_VALIDATE);
        } else {
            callbackExecutor.onEvent(Event.AFTER_VALIDATE_ERROR);
        }


        return error;
    }
}