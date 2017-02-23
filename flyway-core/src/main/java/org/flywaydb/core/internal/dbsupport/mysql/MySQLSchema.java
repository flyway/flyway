/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.mysql;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MySQL implementation of Schema.
 */
public class MySQLSchema extends Schema<MySQLDbSupport> {
    /**
     * Creates a new MySQL schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public MySQLSchema(JdbcTemplate jdbcTemplate, MySQLDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = jdbcTemplate.queryForInt("Select "
                        + "(Select count(*) from information_schema.TABLES Where TABLE_SCHEMA=?) + "
                        + "(Select count(*) from information_schema.VIEWS Where TABLE_SCHEMA=?) + "
                        + "(Select count(*) from information_schema.TABLE_CONSTRAINTS Where TABLE_SCHEMA=?) + "
                        + "(Select count(*) from information_schema.EVENTS Where EVENT_SCHEMA=?) + "
                        + "(Select count(*) from information_schema.TRIGGERS Where TRIGGER_SCHEMA=?) + "
                        + "(Select count(*) from information_schema.ROUTINES Where ROUTINE_SCHEMA=?)",
                name, name, name, name, name, name
        );
        return objectCount == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.execute("DROP SCHEMA " + dbSupport.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        for (String statement : cleanEvents()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanRoutines()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanViews()) {
            jdbcTemplate.execute(statement);
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        for (Table table : allTables()) {
            table.drop();
        }
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    /**
     * Generate the statements to clean the events in this schema.
     *
     * @return The list of statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanEvents() throws SQLException {
        List<Map<String, String>> eventNames =
                jdbcTemplate.queryForList(
                        "SELECT event_name FROM information_schema.events WHERE event_schema=?",
                        name);

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : eventNames) {
            statements.add("DROP EVENT " + dbSupport.quote(name, row.get("event_name")));
        }
        return statements;
    }

    /**
     * Generate the statements to clean the routines in this schema.
     *
     * @return The list of statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanRoutines() throws SQLException {
        List<Map<String, String>> routineNames =
                jdbcTemplate.queryForList(
                        "SELECT routine_name, routine_type FROM information_schema.routines WHERE routine_schema=?",
                        name);

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : routineNames) {
            String routineName = row.get("routine_name");
            String routineType = row.get("routine_type");
            statements.add("DROP " + routineType + " " + dbSupport.quote(name, routineName));
        }
        return statements;
    }

    /**
     * Generate the statements to clean the views in this schema.
     *
     * @return The list of statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanViews() throws SQLException {
        List<String> viewNames =
                jdbcTemplate.queryForStringList(
                        "SELECT table_name FROM information_schema.views WHERE table_schema=?", name);

        List<String> statements = new ArrayList<String>();
        for (String viewName : viewNames) {
            statements.add("DROP VIEW " + dbSupport.quote(name, viewName));
        }
        return statements;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema=? AND table_type='BASE TABLE'", name);

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new MySQLTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new MySQLTable(jdbcTemplate, dbSupport, this, tableName);
    }
}
