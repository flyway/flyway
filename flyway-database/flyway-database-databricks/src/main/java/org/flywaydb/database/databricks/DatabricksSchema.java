/*-
 * ========================LICENSE_START=================================
 * flyway-database-databricks
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.database.databricks;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DatabricksSchema extends Schema<DatabricksDatabase, DatabricksTable> {
    public DatabricksSchema(final JdbcTemplate jdbcTemplate, final DatabricksDatabase database, final String name) {
        super(jdbcTemplate, database, name);
    }

    private List<String> fetchAllObjs(final String obj, final String column) throws SQLException {
        final List<Map<String, String>> tableInfos = jdbcTemplate.queryForList("show "
            + obj
            + "s from "
            + database.quote(name));
        final List<String> tableNames = new ArrayList<>();
        for (final Map<String, String> tableInfo : tableInfos) {
            tableNames.add(tableInfo.get(column));
        }
        return tableNames;
    }

    private List<String> fetchAllSchemas() throws SQLException {
        final List<Map<String, String>> schemaInfos = jdbcTemplate.queryForList("show schemas");
        final List<String> schemaNames = new ArrayList<>();
        for (final Map<String, String> schemaInfo : schemaInfos) {
            schemaNames.add(schemaInfo.get("databaseName"));
        }
        return schemaNames;
    }

    private List<String> fetchAllTables() throws SQLException {
        final var tables = fetchAllObjs("table", "tableName");
        final var views = fetchAllObjs("view", "viewName");
        return tables.stream().filter(t -> !views.contains(t)).toList();
    }

    @Override
    protected boolean doExists() throws SQLException {
        return fetchAllSchemas().stream().anyMatch(schema -> Objects.equals(schema, name));
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return fetchAllTables().isEmpty();
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("create database if not exists " + database.quote(name) + ";");
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("drop database if exists " + database.quote(name) + " cascade;");
    }

    @Override
    protected void doClean() throws SQLException {
        for (final String statement : generateDropStatements("TABLE", fetchAllTables())) {
            jdbcTemplate.execute(statement);
        }
        for (final String statement : generateDropStatements("VIEW", fetchAllObjs("VIEW", "viewName"))) {
            jdbcTemplate.execute(statement);
        }
        for (final String statement : generateDropStatements("FUNCTION", fetchAllObjs("USER FUNCTION", "function"))) {
            jdbcTemplate.execute(statement);
        }
    }

    private List<String> generateDropStatements(final String objType, final List<String> names) {
        final List<String> statements = new ArrayList<>();
        for (final String name : names) {
            statements.add("drop " + objType + " if exists " + database.quote(this.name, name) + ";");
        }
        return statements;
    }

    @Override
    protected DatabricksTable[] doAllTables() throws SQLException {
        final List<String> tableNames = fetchAllTables();
        final DatabricksTable[] tables = new DatabricksTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new DatabricksTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(final String tableName) {
        return new DatabricksTable(jdbcTemplate, database, this, tableName);
    }
}
