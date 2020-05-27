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
package org.flywaydb.core.internal.database.firebird;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.jdbc.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FirebirdSchema extends Schema<FirebirdDatabase, FirebirdTable> {
    /**
     * Creates a new Firebird schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    public FirebirdSchema(JdbcTemplate jdbcTemplate, FirebirdDatabase database, String name) {
        super(jdbcTemplate, database, name);

    }

    @Override
    protected boolean doExists() throws SQLException {
        // database == schema, always return true
        return true;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        // database == schema, check content of database
        // Check for all object types except custom collations and roles
        return 0 == jdbcTemplate.queryForInt("select count(*)\n" +
                "from (\n" +
                "  -- views and tables\n" +
                "  select RDB$RELATION_NAME AS OBJECT_NAME\n" +
                "  from RDB$RELATIONS\n" +
                "  where (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)\n" +
                "  union all\n" +
                "  -- stored procedures\n" +
                "  select RDB$PROCEDURE_NAME\n" +
                "  from RDB$PROCEDURES\n" +
                "  where (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)\n" +
                "  union all\n" +
                "  -- triggers\n" +
                "  select RDB$TRIGGER_NAME\n" +
                "  from RDB$TRIGGERS\n" +
                "  where (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)\n" +
                "  union all\n" +
                "  -- functions\n" +
                "  select RDB$FUNCTION_NAME\n" +
                "  from RDB$FUNCTIONS\n" +
                "  where (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)\n" +
                "  union all\n" +
                "  -- sequences\n" +
                "  select RDB$GENERATOR_NAME\n" +
                "  from RDB$GENERATORS\n" +
                "  where (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)\n" +
                "  union all\n" +
                "  -- exceptions\n" +
                "  select RDB$EXCEPTION_NAME\n" +
                "  from RDB$EXCEPTIONS\n" +
                "  where (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)\n" +
                "  union all\n" +
                "  -- domains\n" +
                "  select RDB$FIELD_NAME\n" +
                "  from RDB$FIELDS\n" +
                "  where RDB$FIELD_NAME not starting with 'RDB$'\n" +
                "  and (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)\n" +
                "union all\n" +
                "-- packages\n" +
                "select RDB$PACKAGE_NAME\n" +
                "from RDB$PACKAGES\n" +
                "where (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)) a");
    }

    @Override
    protected void doCreate() throws SQLException {
        // database == schema, do nothing for creation
    }

    @Override
    protected void doDrop() throws SQLException {
        // database == schema, doClean() instead
        doClean();
    }

    @Override
    protected void doClean() throws SQLException {
        // Dropping everything except custom collations and roles
        for (String dropPackageStmt : generateDropPackageStatements()) {
            jdbcTemplate.execute(dropPackageStmt);
        }
        for (String dropProcedureStmt : generateDropProcedureStatements()) {
            jdbcTemplate.execute(dropProcedureStmt);
        }
        for (String dropViewStmt : generateDropViewStatements()) {
            jdbcTemplate.execute(dropViewStmt);
        }

        for (String dropConstraintStmt: generateDropConstraintStatements()) {
            jdbcTemplate.execute(dropConstraintStmt);
        }

        for (Table table : allTables()) {
            table.drop();
        }
        for (String dropTriggerStmt : generateDropTriggerStatements()) {
            jdbcTemplate.execute(dropTriggerStmt);
        }
        for (String dropFunctionStmt : generateDropFunctionStatements()) {
            jdbcTemplate.execute(dropFunctionStmt);
        }
        for (String dropSequenceStmt : generateDropSequenceStatements()) {
            jdbcTemplate.execute(dropSequenceStmt);
        }
        for (String dropExceptionStmt : generateDropExceptionStatements()) {
            jdbcTemplate.execute(dropExceptionStmt);
        }
        for (String dropDomainStmt : generateDropDomainStatements()) {
            jdbcTemplate.execute(dropDomainStmt);
        }
    }

    private List<String> generateDropConstraintStatements() throws SQLException {
        return jdbcTemplate.query(
                    "select RDB$RELATION_NAME, RDB$CONSTRAINT_NAME\n" +
                            "from RDB$RELATION_CONSTRAINTS\n" +
                            "where RDB$RELATION_NAME NOT LIKE 'RDB$%'\n" +
                            "and RDB$CONSTRAINT_TYPE='FOREIGN KEY'",
                    new RowMapper<String>() {
                        @Override
                        public String mapRow(ResultSet rs) throws SQLException {
                            String tableName = rs.getString(1);
                            String constraintName = rs.getString(2);
                            return "ALTER TABLE " + tableName + " DROP CONSTRAINT " + constraintName;
                        }
                    });
    }

    private List<String> generateDropPackageStatements() throws SQLException {
        List<String> packageNames = jdbcTemplate.queryForStringList(
                "select RDB$PACKAGE_NAME as packageName\n" +
                        "from RDB$PACKAGES\n" +
                        "where (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)");

        return generateDropStatements("package", packageNames);
    }

    private List<String> generateDropProcedureStatements() throws SQLException {
        List<String> procedureNames = jdbcTemplate.queryForStringList(
                "select RDB$PROCEDURE_NAME as procedureName\n" +
                        "from RDB$PROCEDURES\n" +
                        "where (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)" +
                        "\nand RDB$PACKAGE_NAME is null");

        return generateDropStatements("procedure", procedureNames);
    }

    private List<String> generateDropViewStatements() throws SQLException {
        List<String> viewNames = jdbcTemplate.queryForStringList(
                "select RDB$RELATION_NAME as viewName\n" +
                        "from RDB$RELATIONS\n" +
                        "where RDB$VIEW_BLR is not null\n" +
                        "and (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)");

        return generateDropStatements("view", viewNames);
    }

    private List<String> generateDropTriggerStatements() throws SQLException {
        List<String> triggerNames = jdbcTemplate.queryForStringList(
                "select RDB$TRIGGER_NAME as triggerName\n" +
                        "from RDB$TRIGGERS\n" +
                        "where (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)\n");

        return generateDropStatements("trigger", triggerNames);
    }

    private List<String> generateDropFunctionStatements() throws SQLException {
        List<String> functionNames = jdbcTemplate.queryForStringList(
                "select RDB$FUNCTION_NAME as functionName\n" +
                        "from RDB$FUNCTIONS\n" +
                        "where (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)");

        String functionTypeName = database.getVersion().isAtLeast("3.0")
                ? "function"
                : "external function";
        return generateDropStatements(functionTypeName, functionNames);
    }

    private List<String> generateDropSequenceStatements() throws SQLException {
        List<String> sequenceNames = jdbcTemplate.queryForStringList(
                "select RDB$GENERATOR_NAME as sequenceName\n" +
                        "from RDB$GENERATORS\n" +
                        "where (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)\n");

        return generateDropStatements("sequence", sequenceNames);
    }

    private List<String> generateDropExceptionStatements() throws SQLException {
        List<String> exceptionNames = jdbcTemplate.queryForStringList(
                "select RDB$EXCEPTION_NAME as exceptionName\n" +
                        "from RDB$EXCEPTIONS\n" +
                        "where (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)\n");

        return generateDropStatements("exception", exceptionNames);
    }

    private List<String> generateDropDomainStatements() throws SQLException {
        List<String> domainNames = jdbcTemplate.queryForStringList(
                "select RDB$FIELD_NAME as domainName\n" +
                        "from RDB$FIELDS\n" +
                        "where RDB$FIELD_NAME not starting with 'RDB$'\n" +
                        "and (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)\n");

        return generateDropStatements("domain", domainNames);
    }

    private List<String> generateDropStatements(String objectType, List<String> objectNames) {
        List<String> statements = new ArrayList<>(objectNames.size());
        for (String objectName : objectNames) {
            statements.add("drop " + objectType + " " + database.quote(objectName));
        }
        return statements;
    }

    @Override
    protected FirebirdTable[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
                "select RDB$RELATION_NAME as tableName\n" +
                        "from RDB$RELATIONS\n" +
                        "where RDB$VIEW_BLR is null\n" +
                        "and (RDB$SYSTEM_FLAG is null or RDB$SYSTEM_FLAG = 0)");

        FirebirdTable[] tables = new FirebirdTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = getTable(tableNames.get(i));
        }

        return tables;
    }

    @Override
    public FirebirdTable getTable(String tableName) {
        return new FirebirdTable(jdbcTemplate, database, this, tableName);
    }
}