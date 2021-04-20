/*
 * Copyright © Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.internal.database.bigquery;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL implementation of Schema.
 */
public class BigQuerySchema extends Schema<BigQueryDatabase, BigQueryTable> {
    /**
     * Creates a new PostgreSQL schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    BigQuerySchema(JdbcTemplate jdbcTemplate, BigQueryDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        /*
        We have to provide region to query INFORMATION_SCHEMA.SCHEMATA correctly.
        Otherwise, it defaults to US.
        So we make a workaround to query the schema.INFORMATION_SCHEMA.TABLES view.
         */
        boolean schemaExists = false;
        try {
            schemaExists = jdbcTemplate.queryForInt(
                    "SELECT COUNT(*) FROM " + database.quote(name) + ".INFORMATION_SCHEMA.TABLES"
            ) >= 0;
        } catch (SQLException e) {
            if (e.getMessage().contains("NOT_FOUND")) {
                schemaExists = false;
            } else {
                throw e;
            }
        }
        return schemaExists;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        // The TABLES table contains one record for each table, view, materialized view, and external table.
        return doExists() &&
                (jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + database.quote(name) + ".INFORMATION_SCHEMA.TABLES")
                        + jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + database.quote(name) + ".INFORMATION_SCHEMA.ROUTINES")
                ) == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + database.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException {
        for (String statement : generateDropStatements("BASE TABLE", "TABLE")) {
            jdbcTemplate.execute(statement);
        }
        for (String statement : generateDropStatements("EXTERNAL", "EXTERNAL TABLE")) {
            jdbcTemplate.execute(statement);
        }
        for (String statement : generateDropStatements("VIEW", "VIEW")) {
            jdbcTemplate.execute(statement);
        }
        for (String statement : generateDropStatements("MATERIALIZED VIEW", "MATERIALIZED VIEW")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : generateDropStatementsForRoutines("FUNCTION")) {
            jdbcTemplate.execute(statement);
        }
        for (String statement : generateDropStatementsForRoutines("PROCEDURE")) {
            jdbcTemplate.execute(statement);
        }
    }

    /**
     * Generates the statements for dropping the routines in this schema.
     *
     * @param objType The type of object for the DROP statement; FUNCTION or PROCEDURE
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatementsForRoutines(String objType) throws SQLException {
        List<String> objNames =
                jdbcTemplate.queryForStringList(
                        // Search for all functions
                        "SELECT routine_name FROM " + database.quote(name) + ".INFORMATION_SCHEMA.ROUTINES WHERE routine_type='?'",
                        objType
                );

        List<String> statements = new ArrayList<>();
        for (String objName : objNames) {
            statements.add("DROP " + objType + " IF EXISTS " + database.quote(name, objName));
        }
        return statements;
    }

    /**
     * Generates the statements for dropping the TABLE, EXTERNAL TABLE, VIEW, MATERIALIZED VIEW, in this schema.
     *
     * @param type    The object type, BASE TABLE, EXTERNAL, VIEW, or MATERIALIZED VIEW.
     * @param objType The type of object for the DROP statement; TABLE, EXTERNAL TABLE, VIEW or MATERIALIZED VIEW.
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> generateDropStatements(String type, String objType) throws SQLException {
        List<String> names =
                jdbcTemplate.queryForStringList(
                        // Search for all views
                        "SELECT table_name FROM " + database.quote(name) + ".INFORMATION_SCHEMA.TABLES WHERE table_type='?'",
                        type
                );
        List<String> statements = new ArrayList<>();
        for (String domainName : names) {
            statements.add("DROP " + objType + " IF EXISTS " + database.quote(name, domainName));
        }

        return statements;
    }

    @Override
    protected BigQueryTable[] doAllTables() throws SQLException {
        List<String> tableNames =
                jdbcTemplate.queryForStringList(
                        //Search for all the table names
                        "SELECT table_name FROM " + database.quote(name) + ".INFORMATION_SCHEMA.TABLES WHERE table_type='BASE TABLE'"
                );
        BigQueryTable[] tables = new BigQueryTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new BigQueryTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new BigQueryTable(jdbcTemplate, database, this, tableName);
    }
}