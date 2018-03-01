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
package org.flywaydb.core.internal.callback;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * Executes the callbacks for a specific event.
 */
public class CallbackExecutor {
    private final Configuration configuration;
    private final Database database;
    private final Schema schema;
    private final Collection<Callback> callbacks;

    /**
     * Creates a new callback executor.
     *
     * @param configuration The configuration.
     * @param database      The database.
     * @param schema        The current schema to use for the connection.
     * @param callbacks     The callbacks to execute.
     */
    public CallbackExecutor(Configuration configuration, Database database, Schema schema, Collection<Callback> callbacks) {
        this.configuration = configuration;
        this.database = database;
        this.schema = schema;
        this.callbacks = callbacks;
    }

    /**
     * Executes the callbacks for this event on the main connection, within a separate transaction per callback if possible.
     *
     * @param event The vent to handle.
     */
    public void executeOnMainConnection(final Event event) {
        execute(event, database.getMainConnection());
    }

    /**
     * Executes the callbacks for this event on the migration connection, within a separate transaction per callback if possible.
     *
     * @param event The vent to handle.
     */
    public void executeOnMigrationConnection(final Event event) {
        execute(event, database.getMigrationConnection());
    }

    /**
     * Executes the callbacks for an "each" event within the same transaction (if any) as the main operation.
     *
     * @param event         The event to handle.
     * @param migrationInfo The current migration.
     */
    public void executeOnMigrationConnection(final Event event, MigrationInfo migrationInfo) {
        final Context context = new SimpleContext(configuration, database.getMigrationConnection(), migrationInfo);
        for (Callback callback : callbacks) {
            if (callback.supports(event, context)) {
                callback.handle(event, context);
            }
        }
    }

    private void execute(final Event event, final Connection connection) {
        final Context context = new SimpleContext(configuration, connection, null);

        for (final Callback callback : callbacks) {
            if (callback.supports(event, context)) {
                if (callback.canHandleInTransaction(event, context)) {
                    new TransactionTemplate(connection.getJdbcConnection()).execute(new Callable<Object>() {
                        @Override
                        public Object call() {
                            CallbackExecutor.this.execute(connection, callback, event, context);
                            return null;
                        }
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
        callback.handle(event, context);
    }

    private static class SimpleContext implements Context {
        private final Configuration configuration;
        private final Connection connection;
        private final MigrationInfo migrationInfo;

        private SimpleContext(Configuration configuration, Connection connection, MigrationInfo migrationInfo) {
            this.configuration = configuration;
            this.connection = connection;
            this.migrationInfo = migrationInfo;
        }

        @Override
        public Configuration getConfiguration() {
            return configuration;
        }

        @Override
        public java.sql.Connection getConnection() {
            return connection.getJdbcConnection();
        }

        @Override
        public MigrationInfo getMigrationInfo() {
            return migrationInfo;
        }
    }
}