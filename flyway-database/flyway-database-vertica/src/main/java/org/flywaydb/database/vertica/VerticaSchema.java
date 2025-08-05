/*-
 * ========================LICENSE_START=================================
 * flyway-database-vertica
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.database.vertica;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VerticaSchema extends Schema<VerticaDatabase, VerticaTable> {
    private Boolean exists = null;

    VerticaSchema(JdbcTemplate jdbcTemplate, VerticaDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        if (exists == null) {
            exists = jdbcTemplate.queryForBoolean("SELECT EXISTS (\n" +
                                                        "  SELECT 1\n" +
                                                        "  FROM   v_catalog.schemata smt\n" +
                                                        "  WHERE  smt.schema_name = ?\n" +
                                                        "  AND  smt.is_system_schema = FALSE\n" +
                                                        ")",
                                                        name
                                                );
        }
        return exists;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return jdbcTemplate.queryForBoolean("SELECT NOT EXISTS (\n" +
                                                    "  SELECT 1\n" +
                                                    "  FROM   v_catalog.tables tbs\n" +
                                                    "  WHERE  tbs.table_schema = ?\n" +
                                                    "  AND    tbs.is_system_table = FALSE\n" +
                                                    ")",
                                                    name
                                            );
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
        exists = true;
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name) + " CASCADE");
        exists = false;
    }

    @Override
    protected void doClean() throws SQLException {
        // Vertica does not support base types, enums or domains

        for (String statement : generateDropStatementsForViews()) {
            jdbcTemplate.execute(statement);
        }

        for (VerticaTable table : allTables()) {
            table.drop();
        }

        for (String statement : generateDropStatementsForSequences()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForFunctions()) {
            jdbcTemplate.execute(statement);
        }
    }

    /**
     * Generates statements to drop views in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForViews() throws SQLException {
        List<String> viewNames = jdbcTemplate.queryForStringList(
            "SELECT t.table_name FROM v_catalog.all_tables t WHERE schema_name = ? AND table_type = 'VIEW'",
            name
        );

        List<String> statements = new ArrayList<>();
        for (String viewName : viewNames) {
            statements.add("DROP VIEW IF EXISTS " + database.quote(name, viewName));
        }

        return statements;
    }

    /**
     * Generates statements to drop sequences in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForSequences() throws SQLException {
        List<String> sequenceNames = jdbcTemplate.queryForStringList(
            "SELECT s.sequence_name FROM v_catalog.sequences s WHERE s.sequence_schema = ?",
            name
        );

        List<String> statements = new ArrayList<>();
        for (String sequenceName : sequenceNames) {
            statements.add("DROP SEQUENCE IF EXISTS " + database.quote(name, sequenceName));
        }

        return statements;
    }

    /**
     * Generates statements to drop functions in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForFunctions() throws SQLException {
        List<Map<String, String>> rows = jdbcTemplate.queryForList(
            "SELECT * FROM user_functions uf WHERE uf.schema_name = ? AND uf.procedure_type = 'User Defined Function'",
            name
        );

        List<String> statements = new ArrayList<>();
        for (Map<String, String> row : rows) {
            statements.add("DROP FUNCTION IF EXISTS " + database.quote(name, row.get("function_name")) + "(" + row.get("function_argument_type") + ")");
        }

        return statements;
    }

    @Override
    protected VerticaTable[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
            "SELECT t.table_name FROM v_catalog.all_tables t WHERE t.schema_name=? AND t.table_type = 'TABLE'",
            name
        );

        // Vertica projections are dropped with the parent table when using CASCADE

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
