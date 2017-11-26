/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.database;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public abstract class Connection<D extends Database> implements Closeable {
    protected final D database;
    protected final JdbcTemplate jdbcTemplate;
    private final java.sql.Connection jdbcConnection;

    /**
     * The original schema of the connection that should be restored later.
     */
    protected final String originalSchema;

    protected Connection(FlywayConfiguration configuration, D database, java.sql.Connection connection, int nullType
                      // [pro]
            , org.flywaydb.core.internal.util.jdbc.pro.DryRunStatementInterceptor dryRunStatementInterceptor
                      // [/pro]
    ) {
        this.database = database;

        // [pro]
        if (dryRunStatementInterceptor != null) {
            if (database.supportsReadOnlyTransactions()) {
                try {
                    connection.setReadOnly(true);
                } catch (SQLException e) {
                    throw new FlywaySqlException("Unable to switch connection to read-only", e);
                }
            }
            this.jdbcConnection = org.flywaydb.core.internal.util.jdbc.pro.JdbcProxies.createConnectionProxy(configuration.getClassLoader(),
                    connection, dryRunStatementInterceptor);
        } else {
            // [/pro]
            this.jdbcConnection = connection;
            // [pro]
        }
        // [/pro]
        jdbcTemplate = new JdbcTemplate(jdbcConnection, nullType);
        originalSchema = jdbcTemplate.getConnection() == null ? null : getCurrentSchemaName();
    }

    /**
     * Retrieves the current schema.
     *
     * @return The current schema for this connection.
     */
    public String getCurrentSchemaName() {
        try {
            return doGetCurrentSchemaName();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to retrieve the current schema for the connection", e);
        }
    }

    /**
     * Retrieves the current schema.
     *
     * @return The current schema for this connection.
     * @throws SQLException when the current schema could not be retrieved.
     */
    protected abstract String doGetCurrentSchemaName() throws SQLException;

    /**
     * Retrieves the original schema of the connection.
     *
     * @return The original schema for this connection.
     */
    public Schema getOriginalSchema() {
        if (originalSchema == null) {
            return null;
        }

        return getSchema(originalSchema);
    }

    /**
     * Retrieves the schema with this name in the database.
     *
     * @param name The name of the schema.
     * @return The schema.
     */
    public abstract Schema getSchema(String name);

    /**
     * Sets the current schema to this schema.
     *
     * @param schema The new current schema for this connection.
     */
    public void changeCurrentSchemaTo(Schema schema) {
        try {
            if (!schema.exists()) {
                return;
            }
            doChangeCurrentSchemaTo(schema.getName());
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    /**
     * Restores the current schema of the connection to its original setting.
     */
    public void restoreCurrentSchema() {
        try {
            doChangeCurrentSchemaTo(originalSchema);
        } catch (SQLException e) {
            throw new FlywaySqlException("Error restoring current schema to its original setting", e);
        }
    }

    /**
     * Sets the current schema to this schema.
     *
     * @param schema The new current schema for this connection.
     * @throws SQLException when the current schema could not be set.
     */
    public abstract void doChangeCurrentSchemaTo(String schema) throws SQLException;

    /**
     * Locks this table and executes this callable.
     *
     * @param table    The table to lock.
     * @param callable The callable to execute.
     * @return The result of the callable.
     */
    public <T> T lock(final Table table, final Callable<T> callable) {
        return new TransactionTemplate(jdbcTemplate.getConnection(), database.supportsDdlTransactions()).execute(new Callable<T>() {
            @Override
            public T call() throws Exception {
                table.lock();
                return callable.call();
            }
        });
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public void close() {
        JdbcUtils.closeConnection(jdbcConnection);
    }

    public java.sql.Connection getJdbcConnection() {
        return jdbcConnection;
    }
}
