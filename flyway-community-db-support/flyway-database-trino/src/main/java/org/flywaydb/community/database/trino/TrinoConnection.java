/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.community.database.trino;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;

import static org.flywaydb.community.database.trino.TrinoDatabase.quote;

public class TrinoConnection
        extends Connection<TrinoDatabase>
{
    protected TrinoConnection(TrinoDatabase database, java.sql.Connection connection)
    {
        super(database, connection);
    }

    @Override
    protected void doRestoreOriginalState()
    {
    }

    @Override
    public Schema doGetCurrentSchema()
            throws SQLException
    {
        String currentSchema = getCurrentSchemaNameOrSearchPath();

        if (!StringUtils.hasText(currentSchema)) {
            throw new FlywayException("Unable to determine current schema. " +
                    "Set the current schema with schema parameter of the JDBC URL or with Flyway's schemas property.");
        }

        return getSchema(currentSchema);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath()
            throws SQLException
    {
        return jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA");
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema)
    {
        try {
            if (schema.getName().equals(originalSchemaNameOrSearchPath) || !schema.exists()) {
                return;
            }

            doChangeCurrentSchemaOrSearchPathTo(schema.toString());
        }
        catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema)
            throws SQLException
    {
        jdbcTemplate.executeStatement("USE " + quote(schema));
    }

    @Override
    public Schema getSchema(String name)
    {
        return new TrinoSchema(jdbcTemplate, database, name);
    }
}
