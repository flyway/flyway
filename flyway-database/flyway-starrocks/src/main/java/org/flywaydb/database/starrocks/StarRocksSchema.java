/*-
 * ========================LICENSE_START=================================
 * flyway-starrocks
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
package org.flywaydb.database.starrocks;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * StarRocks-specific schema.
 * <p>
 * StarRocks supports information_schema queries for tables and views,
 * but does NOT support events, triggers, or sequences like MySQL.
 */
public class StarRocksSchema extends Schema<StarRocksDatabase, StarRocksTable> {

    StarRocksSchema(final JdbcTemplate jdbcTemplate, final StarRocksDatabase database, final String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt(
            "SELECT COUNT(1) FROM information_schema.schemata WHERE schema_name=? LIMIT 1",
            name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT SUM(found) FROM ("
            + "(SELECT 1 as found FROM information_schema.tables WHERE table_schema=? LIMIT 1) UNION ALL "
            + "(SELECT 1 as found FROM information_schema.views WHERE table_schema=? LIMIT 1)"
            + ") as all_found", name, name) == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + database.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        for (final String statement : cleanViews()) {
            jdbcTemplate.execute(statement);
        }
        for (final Table table : allTables()) {
            table.drop();
        }
    }

    private List<String> cleanViews() throws SQLException {
        final List<String> viewNames = jdbcTemplate.queryForStringList(
            "SELECT table_name FROM information_schema.views WHERE table_schema=?",
            name);

        final List<String> statements = new ArrayList<>();
        for (final String viewName : viewNames) {
            statements.add("DROP VIEW " + database.quote(name, viewName));
        }
        return statements;
    }

    @Override
    protected StarRocksTable[] doAllTables() throws SQLException {
        final List<String> tableNames = jdbcTemplate.queryForStringList(
            "SELECT table_name FROM information_schema.tables WHERE table_schema=?",
            name);

        final StarRocksTable[] tables = new StarRocksTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new StarRocksTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(final String tableName) {
        return new StarRocksTable(jdbcTemplate, database, this, tableName);
    }
}
