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
package org.flywaydb.core.internal.database.postgresql;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * PostgreSQL connection.
 */
public class PostgreSQLConnection extends Connection<PostgreSQLDatabase> {
    private final String originalRole;

    PostgreSQLConnection(Configuration configuration, PostgreSQLDatabase database, java.sql.Connection connection
            , boolean originalAutoCommit



    ) {
        super(configuration, database, connection, originalAutoCommit



        );

        try {
            originalRole = jdbcTemplate.queryForString("SELECT CURRENT_USER");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine current user", e);
        }
    }

    @Override
    protected void doRestoreOriginalState() throws SQLException {
        // Reset the role to its original value in case a migration or callback changed it
        jdbcTemplate.execute("SET ROLE '" + originalRole + "'");
    }

    @Override
    public Schema doGetCurrentSchema() throws SQLException {
        String currentSchema = jdbcTemplate.queryForString("SELECT current_schema");
        if (!StringUtils.hasText(currentSchema)) {
            throw new FlywayException("Unable to determine current schema as search_path is empty. " +
                    "Set the current schema in currentSchema parameter of the JDBC URL or in Flyway's schemas property.");
        }
        return getSchema(currentSchema);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("SHOW search_path");
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        try {
            if (schema.getName().equals(originalSchemaNameOrSearchPath) || originalSchemaNameOrSearchPath.startsWith(schema.getName() + ",") || !schema.exists()) {
                return;
            }

            if (StringUtils.hasText(originalSchemaNameOrSearchPath)) {
                doChangeCurrentSchemaOrSearchPathTo(schema.toString() + "," + originalSchemaNameOrSearchPath);
            } else {
                doChangeCurrentSchemaOrSearchPathTo(schema.toString());
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        jdbcTemplate.execute("SELECT set_config('search_path', ?, false)", schema);
    }

    @Override
    public Schema getSchema(String name) {
        return new PostgreSQLSchema(jdbcTemplate, database, name);
    }

    @Override
    public <T> T lock(Table table, Callable<T> callable) {
        return new PostgreSQLAdvisoryLockTemplate(jdbcTemplate, table.toString().hashCode()).execute(callable);
    }
}