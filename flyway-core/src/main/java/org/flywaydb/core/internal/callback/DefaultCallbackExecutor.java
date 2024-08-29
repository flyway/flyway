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
package org.flywaydb.core.internal.callback;

import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.*;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.exception.FlywayBlockStatementExecutionException;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Executes the callbacks for a specific event.
 */
public class DefaultCallbackExecutor implements CallbackExecutor {

    private final Configuration configuration;
    private final Database database;
    private final Schema schema;
    private final FlywayTelemetryManager flywayTelemetryManager;
    private final List<Callback> callbacks;
    private MigrationInfo migrationInfo;


    /**
     * Creates a new callback executor.
     *
     * @param configuration The configuration.
     * @param database The database.
     * @param schema The current schema to use for the connection.
     * @param callbacks The callbacks to execute.
     */
    public DefaultCallbackExecutor(Configuration configuration, Database database, Schema schema, FlywayTelemetryManager flywayTelemetryManager, Collection<Callback> callbacks) {
        this.configuration = configuration;
        this.database = database;
        this.schema = schema;
        this.flywayTelemetryManager = flywayTelemetryManager;

        this.callbacks = new ArrayList<>(callbacks);
        this.callbacks.sort(Comparator.comparing(Callback::getCallbackName));
    }

    @Override
    public void onEvent(final Event event) {
        execute(event, database.getMainConnection());
    }

    @Override
    public void onMigrateOrUndoEvent(final Event event) {

        if (callbacks.stream().anyMatch(callback -> callback.supports(event, null))) {
            execute(event, database.getEventConnection());
            database.disposeEventConnection();
        }
    }

    @Override
    public void setMigrationInfo(MigrationInfo migrationInfo) {
        this.migrationInfo = migrationInfo;
    }

    @Override
    public void onEachMigrateOrUndoEvent(Event event) {
        final Context context = new SimpleContext(configuration, database.getMigrationConnection(), migrationInfo, null);
        for (Callback callback : callbacks) {
            if (callback.supports(event, context)) {
                handleEvent(callback, event, context);
            }
        }
    }

    @Override
    public void onEachMigrateOrUndoStatementEvent(Event event, String sql, List<Warning> warnings, List<Error> errors) {
        final Context context = new SimpleContext(configuration, database.getMigrationConnection(), migrationInfo, sql, warnings, errors);
        for (Callback callback : callbacks) {
            if (callback.supports(event, context)) {
                handleEvent(callback, event, context);
            }
        }
    }

    public void onOperationFinishEvent(Event event, OperationResult operationResult) {
        final Context context = new SimpleContext(configuration, database.getMigrationConnection(), migrationInfo, operationResult);
        for (Callback callback : callbacks) {
            if (callback.supports(event, context)) {
                handleEvent(callback, event, context);
            }
        }
    }

    private void execute(final Event event, final Connection connection) {
        final Context context = new SimpleContext(configuration, connection, null, null);
        for (final Callback callback : callbacks) {
            if (callback.supports(event, context)) {
                if (callback.canHandleInTransaction(event, context)) {
                    ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(), database).execute(
                            (Callable<Void>) () -> {
                                DefaultCallbackExecutor.this.execute(connection, callback, event, context);
                                return null;
                            });
                } else {
                    execute(connection, callback, event, context);
                }
            }
        }
    }

    private void execute(Connection connection, Callback callback, Event event, Context context) {
        connection.restoreOriginalState();
        connection.changeCurrentSchemaTo(schema);
        handleEvent(callback, event, context);
    }

    private void handleEvent(Callback callback, Event event, Context context) {
        try (EventTelemetryModel telemetryModel = new EventTelemetryModel(event.getId(), flywayTelemetryManager)) {
            callback.handle(event, context);
        } catch (FlywayBlockStatementExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new FlywayException("Error while executing " + event.getId() + " callback: " + e.getMessage(), e);
        }
    }
}
