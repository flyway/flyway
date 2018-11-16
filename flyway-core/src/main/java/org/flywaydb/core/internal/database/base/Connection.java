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
package org.flywaydb.core.internal.database.base;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;
import org.flywaydb.core.internal.jdbc.TransactionTemplate;

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
    protected final String originalSchemaNameOrSearchPath;

    /**
     * The original autocommit state of the connection.
     */
    private boolean originalAutoCommit;

    protected Connection(Configuration configuration, D database, java.sql.Connection connection
            , boolean originalAutoCommit



    ) {
        this.database = database;
        this.originalAutoCommit = originalAutoCommit;
















            this.jdbcConnection = connection;



        jdbcTemplate = new JdbcTemplate(jdbcConnection);
        try {
            originalSchemaNameOrSearchPath = getCurrentSchemaNameOrSearchPath();
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
    protected abstract String getCurrentSchemaNameOrSearchPath() throws SQLException;

    /**
     * @return The current schema for this connection.
     */
    public final Schema getCurrentSchema() {
        try {
            return doGetCurrentSchema();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to retrieve the current schema for the connection", e);
        }
    }

    protected Schema doGetCurrentSchema() throws SQLException {
        return getSchema(getCurrentSchemaNameOrSearchPath());
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
            doChangeCurrentSchemaOrSearchPathTo(schema.getName());
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    /**
     * Sets the current schema to this schema.
     *
     * @param schemaNameOrSearchPath The new current schema for this connection.
     * @throws SQLException when the current schema could not be set.
     */
    protected void doChangeCurrentSchemaOrSearchPathTo(String schemaNameOrSearchPath) throws SQLException {
    }

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

    public final JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public final void close() {
        restoreOriginalState();
        restoreOriginalSchema();
        restoreOriginalAutoCommit();
        JdbcUtils.closeConnection(jdbcConnection);
    }

    private void restoreOriginalSchema() {
        new TransactionTemplate(jdbcConnection).execute(new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    doChangeCurrentSchemaOrSearchPathTo(originalSchemaNameOrSearchPath);
                } catch (SQLException e) {
                    throw new FlywaySqlException("Unable to restore original schema", e);
                }
                return null;
            }
        });
    }

    /**
     * Restores this connection to its original state.
     */
    public final void restoreOriginalState() {
        try {
            doRestoreOriginalState();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to restore connection to its original state", e);
        }
    }

    /**
     * Restores this connection to its original auto-commit setting.
     */
    private void restoreOriginalAutoCommit() {
        try {
            jdbcConnection.setAutoCommit(originalAutoCommit);
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to restore connection to its original auto-commit setting", e);
        }
    }

    /**
     * Restores this connection to its original state.
     */
    protected void doRestoreOriginalState() throws SQLException {
    }

    public final java.sql.Connection getJdbcConnection() {
        return jdbcConnection;
    }
}