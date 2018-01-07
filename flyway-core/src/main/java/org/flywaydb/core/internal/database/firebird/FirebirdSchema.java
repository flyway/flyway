/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.firebird;

import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Firebird implementation of Schema.
 */
public class FirebirdSchema extends Schema<FirebirdDatabase> {
    private static final List<String> IGNORED_SYSTEM_TABLE_NAMES =
            Arrays.asList(
                    "RDB$BACKUP_HISTORY",
                    "RDB$CHARACTER_SETS",
                    "RDB$CHECK_CONSTRAINTS",
                    "RDB$COLLATIONS",
                    "RDB$DATABASE",
                    "RDB$DEPENDENCIES",
                    "RDB$EXCEPTIONS",
                    "RDB$FIELDS",
                    "RDB$FIELD_DIMENSIONS",
                    "RDB$FILES",
                    "RDB$FILTERS",
                    "RDB$FORMATS",
                    "RDB$FUNCTIONS",
                    "RDB$FUNCTION_ARGUMENTS",
                    "RDB$GENERATORS",
                    "RDB$INDICES",
                    "RDB$INDEX_SEGMENTS",
                    "RDB$LOG_FILES",
                    "RDB$PAGES",
                    "RDB$PROCEDURES",
                    "RDB$PROCEDURE_PARAMETERS",
                    "RDB$REF_CONSTRAINTS",
                    "RDB$RELATIONS",
                    "RDB$RELATION_CONSTRAINTS",
                    "RDB$RELATION_FIELDS",
                    "RDB$ROLES",
                    "RDB$SECURITY_CLASSES",
                    "RDB$TRANSACTIONS",
                    "RDB$TRIGGERS",
                    "RDB$TRIGGER_MESSAGES",
                    "RDB$TYPES",
                    "RDB$USER_PRIVILEGES",
                    "RDB$VIEW_RELATIONS");

    /**
     * Creates a new Firebird schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database    The database-specific support.
     * @param name         The name of the schema.
     */
    FirebirdSchema(JdbcTemplate jdbcTemplate, FirebirdDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        try {
            doAllTables();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        Table[] tables = allTables();
        List<String> tableNames = new ArrayList<>();
        for (Table table : tables) {
            String tableName = table.getName();
            if (!IGNORED_SYSTEM_TABLE_NAMES.contains(tableName)) {
                tableNames.add(tableName);
            }
        }
        return tableNames.isEmpty();
    }

    @Override
    protected void doCreate() throws SQLException {
    }

    @Override
    protected void doDrop() throws SQLException {
    }

    @Override
    protected void doClean() throws SQLException {
        //Get all views to drop
        List<String> viewNames = jdbcTemplate.queryForStringList("select rdb$relation_name as viewName\n" +
                "from rdb$relations\n" +
                "where rdb$view_blr is not null\n" +
                "and (rdb$system_flag is null or rdb$system_flag = 0)");

        for (String viewName : viewNames) {
            jdbcTemplate.execute("DROP VIEW " + database.quote(viewName));
        }

        List<String> storedProcNames = jdbcTemplate.queryForStringList(
                "select rdb$procedure_name as procName\n" +
                        "from rdb$procedures \n" +
                        "where (rdb$system_flag is null or rdb$system_flag = 0)");

        //First make the procs all blank in case of dependencies
        for (String storedProcName : storedProcNames) {
            jdbcTemplate.execute("ALTER PROCEDURE " + database.quote(storedProcName)+" as begin\n --blank for deleting by flyway\n end");
        }

        for (String storedProcName : storedProcNames) {
            jdbcTemplate.execute("DROP PROCEDURE " + database.quote(storedProcName));
        }

        for (Table table : allTables()) {
            table.drop();
        }

        //Finally get rid of generators
         List<String> generatorNames = jdbcTemplate.queryForStringList(
                "select rdb$generator_name as generatorName \n" +
                        "from rdb$generators \n" +
                        "where (rdb$system_flag is null or  rdb$system_flag = 0)");

        for (String generatorName : generatorNames) {
            jdbcTemplate.execute("DROP GENERATOR " + database.quote(generatorName));
        }
    }

    @Override
    public String getName() {
        return "null";
    }


    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
                "select rdb$relation_name as tableName\n" +
                "from rdb$relations\n" +
                "where rdb$view_blr is null\n" +
                "and (rdb$system_flag is null or rdb$system_flag = 0)");

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {

            tables[i] = new FirebirdTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new FirebirdTable(jdbcTemplate, database, this, tableName);
    }
}