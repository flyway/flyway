/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.community.database.singlestore;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SingleStoreSchema extends Schema<SingleStoreDatabase, SingleStoreTable> {

    SingleStoreSchema(JdbcTemplate jdbcTemplate, SingleStoreDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(1) FROM information_schema.schemata WHERE schema_name=? LIMIT 1", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        List<String> params = new ArrayList<>(Arrays.asList(name, name, name, name, name));

        return jdbcTemplate.queryForInt("SELECT SUM(found) FROM ("
                        + "(SELECT 1 as found FROM information_schema.tables WHERE table_schema=? LIMIT 1) UNION ALL "
                        + "(SELECT 1 as found FROM information_schema.views WHERE table_schema=? LIMIT 1) UNION ALL "
                        + "(SELECT 1 as found FROM information_schema.routines WHERE routine_schema=? LIMIT 1)"
                        + ") as all_found",
                params.toArray(new String[0])
        ) == 0;
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
        for (String statement : cleanRoutines()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanViews()) {
            jdbcTemplate.execute(statement);
        }

        for (Table table : allTables()) {
            table.drop();
        }
    }

    private List<String> cleanRoutines() throws SQLException {
        List<Map<String, String>> routineNames =
                jdbcTemplate.queryForList(
                        "SELECT routine_name as 'N', routine_type as 'T' FROM information_schema.routines WHERE routine_schema=?",
                        name);

        List<String> statements = new ArrayList<>();
        for (Map<String, String> row : routineNames) {
            String routineName = row.get("N");
            String routineType = row.get("T");
            statements.add("DROP " + routineType + " " + database.quote(name, routineName));
        }
        return statements;
    }

    private List<String> cleanViews() throws SQLException {
        List<String> viewNames =
                jdbcTemplate.queryForStringList(
                        "SELECT table_name FROM information_schema.views WHERE table_schema=?", name);

        List<String> statements = new ArrayList<>();
        for (String viewName : viewNames) {
            statements.add("DROP VIEW " + database.quote(name, viewName));
        }
        return statements;
    }

    @Override
    protected SingleStoreTable[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema=?", name);

        SingleStoreTable[] tables = new SingleStoreTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new SingleStoreTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new SingleStoreTable(jdbcTemplate, database, this, tableName);
    }
}
