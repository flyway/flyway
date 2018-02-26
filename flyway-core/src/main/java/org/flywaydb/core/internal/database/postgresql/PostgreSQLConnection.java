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

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.Callable;

/**
 * PostgreSQL connection.
 */
public class PostgreSQLConnection extends Connection<PostgreSQLDatabase> {
    private final String originalSearchPath;

    PostgreSQLConnection(FlywayConfiguration configuration, PostgreSQLDatabase database, java.sql.Connection connection



    ) {
        super(configuration, database, connection, Types.NULL



        );

        this.originalSearchPath = getSearchPath();
    }

    String getSearchPath() {
        try {
            return jdbcTemplate.queryForString("SHOW search_path");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to read search_path", e);
        }
    }

    private void setSearchPath(String searchPath) {
        try {
            jdbcTemplate.execute("SELECT set_config('search_path', ?, false)", searchPath);
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to set search_path to " + searchPath, e);
        }
    }

    @Override
    protected void doRestoreOriginalState() throws SQLException {
        // Reset the role in case a migration or callback changed it
        jdbcTemplate.execute("RESET ROLE");
    }

    @Override
    public Schema doGetCurrentSchema() throws SQLException {
        return getSchema(jdbcTemplate.queryForString("SELECT current_schema"));
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() {
        return getSearchPath();
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
        setSearchPath(schema);
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