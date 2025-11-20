/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.callback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.CallbackEvent;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.GenericCallback;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.exception.FlywayBlockStatementExecutionException;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;

/**
 * Executes the callbacks for a specific event.
 */
public class DefaultCallbackExecutor<E extends CallbackEvent<E>> implements CallbackExecutor<E> {

    private final Configuration configuration;
    private final Database database;
    private final Schema schema;
    private final FlywayTelemetryManager flywayTelemetryManager;
    private final List<GenericCallback<E>> callbacks;
    private MigrationInfo migrationInfo;

    /**
     * Creates a new callback executor.
     *
     * @param configuration The configuration.
     * @param database      The database.
     * @param schema        The current schema to use for the connection.
     * @param callbacks     The callbacks to execute.
     */
    public DefaultCallbackExecutor(final Configuration configuration,
        final Database database,
        final Schema schema,
        final FlywayTelemetryManager flywayTelemetryManager,
        final Collection<GenericCallback<E>> callbacks) {
        this.configuration = configuration;
        this.database = database;
        this.schema = schema;
        this.flywayTelemetryManager = flywayTelemetryManager;

        this.callbacks = new ArrayList<>(callbacks);
        this.callbacks.sort(Comparator.comparing(GenericCallback::getCallbackName));
    }

    @Override
    public Collection<String> onEvent(final E event) {
        return execute(event, database.getMainConnection());
    }

    @Override
    public void onMigrateOrUndoEvent(final E event) {
        final Context context = new SimpleContext(configuration, null, migrationInfo, null);
        if (callbacks.stream().anyMatch(callback -> callback.supports(event, context))) {
            execute(event, database.getEventConnection());
            database.disposeEventConnection();
        }
    }

    @Override
    public void setMigrationInfo(final MigrationInfo migrationInfo) {
        this.migrationInfo = migrationInfo;
    }

    @Override
    public void onEachMigrateOrUndoEvent(final E event) {
        final Context context = new SimpleContext(configuration,
            database.getMigrationConnection(),
            migrationInfo,
            null);
        for (final GenericCallback<E> callback : callbacks) {
            if (callback.supports(event, context)) {
                handleEvent(callback, event, context);
            }
        }
    }

    @Override
    public void onEachMigrateOrUndoStatementEvent(final E event,
        final String sql,
        final List<Warning> warnings,
        final List<Error> errors) {
        final Context context = new SimpleContext(configuration,
            database.getMigrationConnection(),
            migrationInfo,
            sql,
            warnings,
            errors);
        for (final GenericCallback<E> callback : callbacks) {
            if (callback.supports(event, context)) {
                handleEvent(callback, event, context);
            }
        }
    }

    public void onOperationFinishEvent(final E event, final OperationResult operationResult) {
        final Context context = new SimpleContext(configuration,
            database.getMigrationConnection(),
            migrationInfo,
            operationResult);
        for (final GenericCallback<E> callback : callbacks) {
            if (callback.supports(event, context)) {
                handleEvent(callback, event, context);
            }
        }
    }

    private Collection<String> execute(final E event, final Connection connection) {
        final Context context = new SimpleContext(configuration, connection, null, null);

        final Collection<GenericCallback<E>> callbacksToExecute = callbacks.stream()
            .filter(x -> x.supports(event, context))
            .toList();
        callbacksToExecute.forEach(callback -> {
            if (callback.canHandleInTransaction(event, context)) {
                ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(), database)
                    .execute((Callable<Void>) () -> {
                        DefaultCallbackExecutor.this.execute(connection, callback, event, context);
                        return null;
                    });
            } else {
                execute(connection, callback, event, context);
            }
        });

        return callbacksToExecute.stream().map(GenericCallback::getCallbackName).toList();
    }

    private void execute(final Connection connection,
        final GenericCallback<? super E> callback,
        final E event,
        final Context context) {
        connection.restoreOriginalState();
        connection.changeCurrentSchemaTo(schema);
        handleEvent(callback, event, context);
    }

    private void handleEvent(final GenericCallback<? super E> callback, final E event, final Context context) {
        final String callbackType = Optional.ofNullable(callback.getClass().getCanonicalName())
            .map(x -> x.startsWith("org.flywaydb"))
            .orElse(false) ? callback.getClass().getSimpleName() : "(custom callback class)";
        try (final EventTelemetryModel ignored = new CallbackTelemetryModel(event.getId(),
            callbackType,
            flywayTelemetryManager)) {
            callback.handle(event, context);
        } catch (final FlywayBlockStatementExecutionException e) {
            throw e;
        } catch (final Exception e) {
            throw new FlywayException("Error while executing " + event.getId() + " callback: " + e.getMessage(), e);
        }
    }
}
