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
import org.flywaydb.core.internal.util.jdbc.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        List<HashMap<String, String>> schemasMetadata = getMetadataForObjectType("SCHEMAS", name,"name");

        return schemasMetadata.size() > 0;
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        if (doExists()) {
            List<HashMap<String, String>> objectsMetadata = getMetadataForObjectType("OBJECTS", "%", "name");
            return objectsMetadata.size() == 0;
        }
        else
        {
            return true;
        }
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
        List<HashMap<String, String>> tablesMetadata = getMetadataForObjectType("TABLES", "%","name");

        Table[] tables = new Table[tablesMetadata.size()];
        for (int i = 0; i < tablesMetadata.size(); i++) {
            String tableName = tablesMetadata.get(i).get("name");
            tables[i] = new SnowflakeTable(jdbcTemplate, database, this, tableName);
        }

        return tables;
    }

    protected List<String> generateDropStatementsForViews() throws SQLException {
        List<HashMap<String, String>> viewsMetadata = getMetadataForObjectType("VIEWS", "%","name");

        List<String> statements = new ArrayList<String>();
        for (int i = 0; i < viewsMetadata.size(); i++) {
            String viewName = viewsMetadata.get(i).get("name");
            statements.add("DROP VIEW " + database.quote(name, viewName));
        }
        return statements;
    }

    protected List<String> generateDropStatementsForStages() throws SQLException {
        List<HashMap<String, String>> stagesMetadata = getMetadataForObjectType("STAGES", "%","name");

        List<String> statements = new ArrayList<String>();
        for (int i = 0; i < stagesMetadata.size(); i++) {
            String stageName = stagesMetadata.get(i).get("name");
            statements.add("DROP STAGE " + database.quote(name, stageName));
        }
        return statements;
    }

    protected List<String> generateDropStatementsForFileFormats() throws SQLException {
        List<HashMap<String, String>> fileFormatsMetadata = getMetadataForObjectType("FILE FORMATS", "%","name");

        List<String> statements = new ArrayList<String>();
        for (int i = 0; i < fileFormatsMetadata.size(); i++) {
            String fileFormatName = fileFormatsMetadata.get(i).get("name");
            statements.add("DROP FILE FORMAT " + database.quote(name, fileFormatName));
        }
        return statements;
    }

    protected List<String> generateDropStatementsForSequences() throws SQLException {
        List<HashMap<String, String>> sequencesMetadata = getMetadataForObjectType("SEQUENCES", "%","name");

        List<String> statements = new ArrayList<String>();
        for (int i = 0; i < sequencesMetadata.size(); i++) {
            String sequenceName = sequencesMetadata.get(i).get("name");
            statements.add("DROP SEQUENCE " + database.quote(name, sequenceName));
        }
        return statements;
    }

    protected List<String> generateDropStatementsForFunctions() throws SQLException {
        List<HashMap<String, String>> functionsMetadata = getMetadataForObjectType("USER FUNCTIONS", "%", "arguments");

        List<String> statements = new ArrayList<String>();
        for (int i = 0; i < functionsMetadata.size(); i++) {
            String functionNameWithArguments = functionsMetadata.get(i).get("arguments");
            functionNameWithArguments = functionNameWithArguments.replaceAll("\\sRETURN\\s.*", "");
            statements.add("DROP FUNCTION " + functionNameWithArguments);
        }
        return statements;
    }

    /**
     * Helper method to retrieve a result set of metadata using SHOW
     *
     * @param objectType The type of object to return metadata for (expects plural)
     * @param resultColumnNames Set of column names to select from the result set
     * @return A list of result set rows
     * @throws SQLException when the query execution failed.
     */
    private List<HashMap<String, String>> getMetadataForObjectType(String objectType, String objectFilter, String... resultColumnNames) throws SQLException {
        String inSchemaString;
        if (objectType != "SCHEMAS") {
            inSchemaString = " IN SCHEMA " + database.quote(name);
        }
        else {
            inSchemaString = "";
        }

        String metadataQuery = "SHOW " + objectType + " LIKE '" + objectFilter + "'" + inSchemaString;
        List<HashMap<String, String>> resultRows = jdbcTemplate.query(
                metadataQuery,
                new RowMapper<HashMap<String, String>>() {
                    @Override
                    public HashMap<String, String> mapRow(ResultSet rs) throws SQLException {
                        HashMap<String, String> objectList = new HashMap<String, String>();
                        for(String resultColumnName : resultColumnNames) {
                            objectList.put(resultColumnName, rs.getString(resultColumnName));
                        }
                        return objectList;
                    }
                });

        return resultRows;
    }

    @Override
    public Table getTable(String tableName) {
        return new SnowflakeTable(jdbcTemplate, database, this, tableName);
    }
}
