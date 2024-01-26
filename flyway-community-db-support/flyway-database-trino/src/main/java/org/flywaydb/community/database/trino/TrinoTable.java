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

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

public class TrinoTable
        extends Table<TrinoDatabase, TrinoSchema>
{
    /**
     * Creates a new Trino table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param schema The schema this table lives in.
     * @param name The name of the table.
     */
    protected TrinoTable(
            JdbcTemplate jdbcTemplate,
            TrinoDatabase database,
            TrinoSchema schema,
            String name)
    {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop()
            throws SQLException
    {
        jdbcTemplate.executeStatement("DROP TABLE " + database.quote(schema.getName(), name));
    }

    @Override
    protected boolean doExists()
            throws SQLException
    {
        return jdbcTemplate.queryForBoolean("SELECT EXISTS (\n" +
                "  SELECT table_name\n" +
                "  FROM   information_schema.tables\n" +
                "  WHERE  table_schema = ?\n" +
                "  AND    table_name = ?\n" +
                "  AND    table_type = 'BASE TABLE'\n" +
                ")", schema.getName(), name);
    }

    @Override
    protected void doLock()
    {
    }
}
