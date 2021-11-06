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

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TrinoSchema
        extends Schema<TrinoDatabase, TrinoTable>
{
    protected TrinoSchema(JdbcTemplate jdbcTemplate, TrinoDatabase database, String name)
    {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists()
            throws SQLException
    {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name=?", name) > 0;
    }

    @Override
    protected boolean doEmpty()
            throws SQLException
    {
        return !jdbcTemplate.queryForBoolean("SELECT EXISTS (\n" +
                "    SELECT table_name FROM information_schema.tables t\n" +
                "    WHERE  t.table_schema = ?\n" +
                ")", name);
    }

    @Override
    protected void doCreate()
            throws SQLException
    {
        jdbcTemplate.executeStatement("CREATE SCHEMA " + TrinoDatabase.quote(name));
    }

    @Override
    protected void doDrop()
            throws SQLException
    {
        jdbcTemplate.executeStatement("DROP SCHEMA " + TrinoDatabase.quote(name));
    }

    @Override
    protected void doClean()
            throws SQLException
    {
        for (String statement : generateDropStatementsForMaterializedViews()) {
            jdbcTemplate.executeStatement(statement);
        }

        for (String statement : generateDropStatementsForViews()) {
            jdbcTemplate.executeStatement(statement);
        }

        for (TrinoTable table : allTables()) {
            table.drop();
        }
    }

    /**
     * Generates the statements for dropping the materialized views in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForMaterializedViews()
            throws SQLException
    {
        List<String> viewNames =
                jdbcTemplate.queryForStringList(
                        "SELECT table_name FROM information_schema.tables" +
                                " WHERE table_type = 'BASE TABLE' AND table_schema = ?",
                        name);

        List<String> statements = new ArrayList<>();
        for (String domainName : viewNames) {
            statements.add("DROP MATERIALIZED VIEW IF EXISTS " + database.quote(name, domainName));
        }

        return statements;
    }

    /**
     * Generates the statements for dropping the views in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForViews()
            throws SQLException
    {
        List<String> viewNames =
                jdbcTemplate.queryForStringList(
                        "SELECT table_name FROM information_schema.tables" +
                                " WHERE table_type = 'VIEW' AND table_schema = ?",
                        name);
        List<String> statements = new ArrayList<>();
        for (String domainName : viewNames) {
            statements.add("DROP VIEW IF EXISTS " + database.quote(name, domainName));
        }

        return statements;
    }

    @Override
    protected TrinoTable[] doAllTables()
            throws SQLException
    {
        List<String> tableNames =
                jdbcTemplate.queryForStringList(
                        "SELECT table_name FROM information_schema.tables" +
                                " WHERE table_type = 'BASE TABLE' AND table_schema = ?",
                        name);
        TrinoTable[] tables = new TrinoTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new TrinoTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public TrinoTable getTable(String tableName)
    {
        return new TrinoTable(jdbcTemplate, database, this, tableName);
    }
}
