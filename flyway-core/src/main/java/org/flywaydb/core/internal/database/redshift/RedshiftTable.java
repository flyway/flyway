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
package org.flywaydb.core.internal.database.redshift;

import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;

/**
 * Redshift-specific table.
 */
public class RedshiftTable extends Table<RedshiftDatabase, RedshiftSchema> {
    /**
     * Creates a new Redshift table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    RedshiftTable(JdbcTemplate jdbcTemplate, RedshiftDatabase database, RedshiftSchema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP TABLE " + database.quote(schema.getName(), name) + " CASCADE");
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForBoolean("SELECT EXISTS (\n" +
                        "  SELECT 1\n" +
                        "  FROM   pg_catalog.pg_class c\n" +
                        "  JOIN   pg_catalog.pg_namespace n ON n.oid = c.relnamespace\n" +
                        "  WHERE  n.nspname = ?\n" +
                        "  AND    c.relname = ?\n" +
                        "  AND    c.relkind = 'r'\n" + // only tables
                        ")", schema.getName(),
                name.toLowerCase() // Redshift table names are case-insensitive and always in lowercase in pg_class.
        );
    }

    @Override
    protected void doLock() throws SQLException {
        jdbcTemplate.execute("DELETE FROM " + this + " WHERE FALSE");
    }
}