/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.vertica;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.List;

/**
 * Vertica Schema
 */
public class VerticaSchema extends Schema<VerticaDatabase, VerticaTable> {
    /**
     * Creates a new Vertica schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    VerticaSchema(JdbcTemplate jdbcTemplate, VerticaDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT 1 FROM v_catalog.schemata WHERE schema_name=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return jdbcTemplate.queryForInt("select count(*) FROM v_catalog.tables WHERE table_schema=?", name) > 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() {
        for (Table table : allTables()) {
            table.drop();
        }
    }

    @Override
    protected VerticaTable[] doAllTables() throws SQLException {
        // Search for all the table names
        List<String> tableNames = jdbcTemplate.queryForStringList("SELECT table_name FROM v_catalog.tables where table_schema=", name);

        // Views and child tables are excluded as they are dropped with the parent table when using cascade.

        VerticaTable[] tables = new VerticaTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new VerticaTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new VerticaTable(jdbcTemplate, database, this, tableName);
    }
}