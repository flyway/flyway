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
package org.flywaydb.core.internal.database.snowflake;

import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Snowflake implementation of Schema.
 */
public class SnowflakeSchema extends Schema<SnowflakeDatabase> {
    /**
     * Creates a new Snowflake schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param name         The name of the schema.
     */
    public SnowflakeSchema(JdbcTemplate jdbcTemplate, SnowflakeDatabase database, String name) {
        super(jdbcTemplate, database, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        ArrayList<ArrayList<String>> schemasMetadata = getMetadataForObjectType("SCHEMAS", "name");

        return schemasMetadata.size() > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        ArrayList<ArrayList<String>> tablesMetadata = getMetadataForObjectType("TABLES", "name");

        return tablesMetadata.size() == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        jdbcTemplate.executeStatement("CREATE SCHEMA " + database.quote(name));
    }

    @Override
    protected void doDrop() throws SQLException {
        jdbcTemplate.executeStatement("DROP SCHEMA " + database.quote(name) + " CASCADE");
    }

    @Override
    protected void doClean() throws SQLException {
        for (String statement : generateDropStatementsForViews()) {
            jdbcTemplate.executeStatement(statement);
        }
        for (Table table : allTables()) {
            table.drop();
        }
        for (String statement : generateDropStatementsForStages()) {
            jdbcTemplate.executeStatement(statement);
        }
        for (String statement : generateDropStatementsForFileFormats()) {
            jdbcTemplate.executeStatement(statement);
        }
        for (String statement : generateDropStatementsForSequences()) {
            jdbcTemplate.executeStatement(statement);
        }
        for (String statement : generateDropStatementsForFunctions()) {
            jdbcTemplate.executeStatement(statement);
        }
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        ArrayList<ArrayList<String>> tablesMetadata = getMetadataForObjectType("TABLES", "name");

        Table[] tables = new Table[tablesMetadata.size()];
        for (int i = 0; i < tablesMetadata.size(); i++) {
            String tableName = tablesMetadata.get(i).get(0);
            tables[i] = new SnowflakeTable(jdbcTemplate, database, this, tableName);
        }

        return tables;
    }

    protected List<String> generateDropStatementsForViews() throws SQLException {
        ArrayList<ArrayList<String>> viewsMetadata = getMetadataForObjectType("VIEWS", "name");

        List<String> statements = new ArrayList<String>();
        for (int i = 0; i < viewsMetadata.size(); i++) {
            String viewName = viewsMetadata.get(i).get(0);
            statements.add("DROP VIEW " + database.quote(name, viewName));
        }
        return statements;
    }

    protected List<String> generateDropStatementsForStages() throws SQLException {
        ArrayList<ArrayList<String>> stagesMetadata = getMetadataForObjectType("STAGES", "name");

        List<String> statements = new ArrayList<String>();
        for (int i = 0; i < stagesMetadata.size(); i++) {
            String stageName = stagesMetadata.get(i).get(0);
            statements.add("DROP STAGE " + database.quote(name, stageName));
        }
        return statements;
    }

    protected List<String> generateDropStatementsForFileFormats() throws SQLException {

        ArrayList<ArrayList<String>> fileFormatsMetadata = getMetadataForObjectType("FILE FORMATS", "name");

        List<String> statements = new ArrayList<String>();
        for (int i = 0; i < fileFormatsMetadata.size(); i++) {
            String fileFormatName = fileFormatsMetadata.get(i).get(0);
            statements.add("DROP FILE FORMAT " + database.quote(name, fileFormatName));
        }
        return statements;
    }

    protected List<String> generateDropStatementsForSequences() throws SQLException {
        ArrayList<ArrayList<String>> sequencesMetadata = getMetadataForObjectType("SEQUENCES", "name");

        List<String> statements = new ArrayList<String>();
        for (int i = 0; i < sequencesMetadata.size(); i++) {
            String sequenceName = sequencesMetadata.get(i).get(0);
            statements.add("DROP SEQUENCE " + database.quote(name, sequenceName));
        }
        return statements;
    }

    protected List<String> generateDropStatementsForFunctions() throws SQLException {
        ArrayList<ArrayList<String>> functionsMetadata = getMetadataForObjectType("USER FUNCTIONS", "arguments");

        List<String> statements = new ArrayList<String>();
        for (int i = 0; i < functionsMetadata.size(); i++) {
            String functionNameWithArguments = functionsMetadata.get(i).get(0);
            functionNameWithArguments = functionNameWithArguments.replaceAll("\\sRETURN\\s.*", "");
            statements.add("DROP FUNCTION " + functionNameWithArguments);
        }
        return statements;
    }

    /**
     * Helper method to retrieve a result set of metadata using SHOW
     *
     * @param objectType The type of object to return metadata for (expects plural)
     * @param params Set of column names to select from the result set
     * @return A list of result set rows
     * @throws SQLException when the query execution failed.
     */
    private ArrayList<ArrayList<String>> getMetadataForObjectType(String objectType, String... resultColumnNames) throws SQLException {
        String inSchemaString;
        String objectPattern;
        if (objectType != "SCHEMAS") {
            inSchemaString = " IN SCHEMA " + database.quote(name);
            objectPattern = "%";
        }
        else {
            inSchemaString = "";
            objectPattern = name;
        }

        List<Map<String, String>> resultSet = jdbcTemplate.queryForList("SHOW " + objectType + " LIKE '" + objectPattern + "'" + inSchemaString);
        ArrayList<ArrayList<String>> resultRows = new ArrayList<>();
        for (final Map<String, String> result : resultSet) {
            ArrayList<String> resultRow = new ArrayList<>();
            for (String resultColumnName : resultColumnNames) {
                resultRow.add(result.get(resultColumnName));
            }
            resultRows.add(resultRow);
        }

        return resultRows;
    }

    @Override
    public Table getTable(String tableName) {
        return new SnowflakeTable(jdbcTemplate, database, this, tableName);
    }
}
