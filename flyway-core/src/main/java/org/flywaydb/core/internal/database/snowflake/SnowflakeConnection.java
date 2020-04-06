/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.database.snowflake;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;






public class SnowflakeConnection extends Connection<SnowflakeDatabase> {

    private final String originalRole;

    SnowflakeConnection(SnowflakeDatabase database, java.sql.Connection connection) {
        super(database, connection);
        try {
            this.originalRole = jdbcTemplate.queryForString("SELECT CURRENT_ROLE()");
        } catch (SQLException e) {
            throw new FlywayException("Unable to determine current role", e);
        }
    }

    @Override
    protected void doRestoreOriginalState() throws SQLException {
        // Reset the role to its original value in case a migration or callback changed it
        jdbcTemplate.execute("USE ROLE " + originalRole);
    }

     @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        String schemaName = jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA()");
        return (schemaName != null) ? schemaName : "PUBLIC";
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        jdbcTemplate.execute("USE SCHEMA " + database.doQuote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new SnowflakeSchema(jdbcTemplate, database, name);
    }
}