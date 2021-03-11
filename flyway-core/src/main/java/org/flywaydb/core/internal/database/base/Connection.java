/*
 * Copyright © Red Gate Software Ltd 2010-2021
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

import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.JdbcUtils;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public abstract class Connection<D extends Database> implements Closeable {
    protected final D database;
    protected JdbcTemplate jdbcTemplate;
    private final java.sql.Connection jdbcConnection;
    protected final String originalSchemaNameOrSearchPath;
    private final boolean originalAutoCommit;

    protected Connection(D database, java.sql.Connection connection) {
        this.database = database;

        try {
            this.originalAutoCommit = connection.getAutoCommit();
            if (!originalAutoCommit) {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to turn on auto-commit for the connection", e);
        }

        this.jdbcConnection = connection;
        jdbcTemplate = new JdbcTemplate(jdbcConnection, database.getDatabaseType());
        try {
            originalSchemaNameOrSearchPath = getCurrentSchemaNameOrSearchPath();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine the original schema for the connection", e);
        }
    }

    /**
     * @throws SQLException when the current schema name or search path could not be retrieved.
     */
    protected abstract String getCurrentSchemaNameOrSearchPath() throws SQLException;

    public final Schema getCurrentSchema() {
        try {
            return doGetCurrentSchema();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine the current schema for the connection", e);
        }
    }

    protected Schema doGetCurrentSchema() throws SQLException {
        return getSchema(getCurrentSchemaNameOrSearchPath());
    }

    /**
     * Retrieves the schema with this name in the database.
     */
    public abstract Schema getSchema(String name);

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
     * @param schemaNameOrSearchPath The new current schema for this connection.
     *
     * @throws SQLException when the current schema could not be set.
     */
    protected void doChangeCurrentSchemaOrSearchPathTo(String schemaNameOrSearchPath) throws SQLException { }

    /**
     * Locks this table and executes this callable.
     *
     * @return The result of the callable.
     */
    public <T> T lock(final Table table, final Callable<T> callable) {
        return ExecutionTemplateFactory
                .createTableExclusiveExecutionTemplate(jdbcTemplate.getConnection(), table, database)
                .execute(callable);
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
        ExecutionTemplateFactory.createExecutionTemplate(jdbcConnection, database).execute((Callable<Void>) () -> {
            try {
                doChangeCurrentSchemaOrSearchPathTo(originalSchemaNameOrSearchPath);
            } catch (SQLException e) {
                throw new FlywaySqlException("Unable to restore original schema", e);
            }
            return null;
        });
    }

    public final void restoreOriginalState() {
        try {
            doRestoreOriginalState();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to restore connection to its original state", e);
        }
    }

    private void restoreOriginalAutoCommit() {
        try {
            jdbcConnection.setAutoCommit(originalAutoCommit);
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to restore connection to its original auto-commit setting", e);
        }
    }

    protected void doRestoreOriginalState() throws SQLException { }

    public final java.sql.Connection getJdbcConnection() {
        return jdbcConnection;
    }
}