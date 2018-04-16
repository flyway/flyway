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
package org.flywaydb.core.internal.database.snowflake;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.Callable;

/**
 * PostgreSQL connection.
 */
public class SnowflakeConnection extends Connection<SnowflakeDatabase> {
    //protected final JdbcTemplate jdbcTemplate;

    SnowflakeConnection(FlywayConfiguration configuration, SnowflakeDatabase database, java.sql.Connection connection
    ) {
        super(configuration, database, connection, Types.NULL);
    }


    @Override
    public Schema getOriginalSchema() {
        return getSchema("PUBLIC");
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        String currentSchemaName = jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA()");
        if (currentSchemaName == null)
            currentSchemaName = "PUBLIC";
        return currentSchemaName;
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        if (schema.getName().equals(getOriginalSchema()) || !schema.exists()) {
            return;
        }

        try {
            doChangeCurrentSchemaTo(schema.toString());
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    public void doChangeCurrentSchemaTo(String schema) throws SQLException {
        jdbcTemplate.execute("USE SCHEMA " + schema);
    }

    @Override
    public Schema getSchema(String name) {
        return new SnowflakeSchema(jdbcTemplate, database, name);
    }
}
