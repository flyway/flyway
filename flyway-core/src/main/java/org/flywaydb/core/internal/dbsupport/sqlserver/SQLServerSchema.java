/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.sqlserver;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQLServer implementation of Schema.
 */
public class SQLServerSchema extends Schema<SQLServerDbSupport> {
    /**
     * Creates a new SQLServer schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public SQLServerSchema(JdbcTemplate jdbcTemplate, SQLServerDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME=?", name) > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        int objectCount = jdbcTemplate.queryForInt("Select count(*) FROM " +
                "( " +
                "Select TABLE_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from INFORMATION_SCHEMA.TABLES " +
                "Union " +
                "Select TABLE_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from INFORMATION_SCHEMA.VIEWS " +
                "Union " +
                "Select CONSTRAINT_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                "Union " +
                "Select ROUTINE_NAME as OBJECT_NAME, ROUTINE_SCHEMA as OBJECT_SCHEMA from INFORMATION_SCHEMA.ROUTINES " +
                ") R where OBJECT_SCHEMA = ?", name);
        return objectCount == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.execute("CREATE SCHEMA " + dbSupport.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        clean();
        jdbcTemplate.execute("DROP SCHEMA " + dbSupport.quote(name));
    }

    @Override
    protected void doClean() throws SQLException {
        for (String statement : cleanForeignKeys()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanDefaultConstraints()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanRoutines("PROCEDURE")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanViews()) {
            jdbcTemplate.execute(statement);
        }

        for (Table table : allTables()) {
            table.drop();
        }

        for (String statement : cleanRoutines("FUNCTION")) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanTypes()) {
            jdbcTemplate.execute(statement);
        }

        for (String statement : cleanSynonyms()) {
            jdbcTemplate.execute(statement);
        }

        if (jdbcTemplate.getMetaData().getDatabaseMajorVersion() >= 11) {
            for (String statement : cleanSequences()) {
                jdbcTemplate.execute(statement);
            }
        }
    }

    /**
     * Cleans the foreign keys in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanForeignKeys() throws SQLException {
        @SuppressWarnings({"unchecked"})
        List<Map<String, String>> constraintNames =
                jdbcTemplate.queryForList(
                        "SELECT table_name, constraint_name FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS" +
                                " WHERE constraint_type in ('FOREIGN KEY','CHECK') and table_schema=?",
                        name
                );

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : constraintNames) {
            String tableName = row.get("table_name");
            String constraintName = row.get("constraint_name");
            statements.add("ALTER TABLE " + dbSupport.quote(name, tableName) + " DROP CONSTRAINT " + dbSupport.quote(constraintName));
        }
        return statements;
    }

    /**
     * Cleans the default constraints in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanDefaultConstraints() throws SQLException {
        @SuppressWarnings({"unchecked"})
        List<Map<String, String>> constraintNames =
                jdbcTemplate.queryForList(
                        "select t.name as table_name, d.name as constraint_name" +
                                " from sys.tables t" +
                                " inner join sys.default_constraints d on d.parent_object_id = t.object_id\n" +
                                " inner join sys.schemas s on s.schema_id = t.schema_id\n" +
                                " where s.name = ?",
                        name
                );

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : constraintNames) {
            String tableName = row.get("table_name");
            String constraintName = row.get("constraint_name");
            statements.add("ALTER TABLE " + dbSupport.quote(name, tableName) + " DROP CONSTRAINT " + dbSupport.quote(constraintName));
        }
        return statements;
    }

    /**
     * Cleans the routines in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanRoutines(String routineType) throws SQLException {
        @SuppressWarnings({"unchecked"})
        List<Map<String, String>> routineNames =
                jdbcTemplate.queryForList("SELECT routine_name FROM INFORMATION_SCHEMA.ROUTINES" +
                                " WHERE routine_schema=? AND routine_type=?",
                        name, routineType
                );

        List<String> statements = new ArrayList<String>();
        for (Map<String, String> row : routineNames) {
            String routineName = row.get("routine_name");
            statements.add("DROP " + routineType + " " + dbSupport.quote(name, routineName));
        }
        return statements;
    }

    /**
     * Cleans the views in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanViews() throws SQLException {
        List<String> viewNames =
                jdbcTemplate.queryForStringList("SELECT table_name FROM INFORMATION_SCHEMA.VIEWS WHERE table_schema=?",
                        name);

        List<String> statements = new ArrayList<String>();
        for (String viewName : viewNames) {
            statements.add("DROP VIEW " + dbSupport.quote(name, viewName));
        }
        return statements;
    }

    /**
     * Cleans the types in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanTypes() throws SQLException {
        List<String> typeNames =
                jdbcTemplate.queryForStringList(
                        "SELECT t.name FROM sys.types t INNER JOIN sys.schemas s ON t.schema_id = s.schema_id" +
                                " WHERE t.is_user_defined = 1 AND s.name = ?",
                        name
                );

        List<String> statements = new ArrayList<String>();
        for (String typeName : typeNames) {
            statements.add("DROP TYPE " + dbSupport.quote(name, typeName));
        }
        return statements;
    }

    /**
     * Cleans the synonyms in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanSynonyms() throws SQLException {
        List<String> synonymNames =
                jdbcTemplate.queryForStringList(
                        "SELECT sn.name FROM sys.synonyms sn INNER JOIN sys.schemas s ON sn.schema_id = s.schema_id" +
                                " WHERE s.name = ?",
                        name
                );

        List<String> statements = new ArrayList<String>();
        for (String synonymName : synonymNames) {
            statements.add("DROP SYNONYM " + dbSupport.quote(name, synonymName));
        }
        return statements;
    }

    /**
     * Cleans the sequences in this schema.
     *
     * @return The drop statements.
     * @throws SQLException when the clean statements could not be generated.
     */
    private List<String> cleanSequences() throws SQLException {
        List<String> names =
                jdbcTemplate.queryForStringList(
                        "SELECT sequence_name FROM INFORMATION_SCHEMA.SEQUENCES WHERE sequence_schema=?", name);

        List<String> statements = new ArrayList<String>();
        for (String sequenceName : names) {
            statements.add("DROP SEQUENCE " + dbSupport.quote(name, sequenceName));
        }
        return statements;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = jdbcTemplate.queryForStringList(
                "SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_type='BASE TABLE' and table_schema=?",
                name);


        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new SQLServerTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new SQLServerTable(jdbcTemplate, dbSupport, this, tableName);
    }
}
