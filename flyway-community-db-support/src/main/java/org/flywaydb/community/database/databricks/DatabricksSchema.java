package org.flywaydb.community.database.databricks;

import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabricksSchema extends Schema<DatabricksDatabase, DatabricksTable> {
    /**
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    public DatabricksSchema(JdbcTemplate jdbcTemplate, DatabricksDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    private List<String> fetchAllObjs(String obj) throws SQLException  {
        List<Map<String, String>> tableInfos = jdbcTemplate.queryForList(
                "show " + obj + "s from " + database.quote(name)
        );
        List<String> tableNames = new ArrayList<String>();
        for (Map<String, String> tableInfo : tableInfos) {
            tableNames.add(tableInfo.get("tableName"));
        }
        return tableNames;
    }

    @Override
    protected boolean doExists() throws SQLException {
        return fetchAllObjs("table").size() > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return fetchAllObjs("table").size() == 0;
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
        for (String statement : generateDropStatements("TABLE")) {
            jdbcTemplate.execute(statement);
        }
        for (String statement : generateDropStatements("VIEW")) {
            jdbcTemplate.execute(statement);
        }
        for (String statement : generateDropStatements("FUNCTION")) {
            jdbcTemplate.execute(statement);
        }
    }

    private List<String> generateDropStatements(String objType) throws SQLException {
        List<String> names = fetchAllObjs(objType);
        List<String> statements = new ArrayList<>();
        for (String domainName : names) {
            statements.add("drop " + objType + " if exists " + database.quote(name, domainName) + ";");
        }
        return statements;
    }


    @Override
    protected DatabricksTable[] doAllTables() throws SQLException {
        List<String> tableNames = fetchAllObjs("TABLE");
        DatabricksTable[] tables = new DatabricksTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new DatabricksTable(jdbcTemplate, database, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new DatabricksTable(jdbcTemplate, database, this, tableName);
    }
}
