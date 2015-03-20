/**
 * Copyright 2010-2015 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport.phoenix;

import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.RowMapper;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Phoenix implementation of Schema.
 */
public class PhoenixSchema extends Schema<PhoenixDbSupport> {
    private static final Log LOG = LogFactory.getLog(PhoenixSchema.class);

    /**
     * Creates a new Phoenix schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param dbSupport    The database-specific support.
     * @param name         The name of the schema.
     */
    public PhoenixSchema(JdbcTemplate jdbcTemplate, PhoenixDbSupport dbSupport, String name) {
        super(jdbcTemplate, dbSupport, name);
    }

    @Override
    protected boolean doExists() throws SQLException {
        if (name == null) {
            return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYSTEM.CATALOG WHERE table_schem IS NULL") > 0;
        }
        else {
            return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYSTEM.CATALOG WHERE table_schem=?", name) > 0;
        }
    }

    @Override
    protected boolean doEmpty() throws SQLException {
        return allTables().length == 0;
    }

    @Override
    protected void doCreate() throws SQLException {
        LOG.info("Phoenix does not support creating schemas. Schema not created: " + name);
    }

    @Override
    protected void doDrop() throws SQLException {
        LOG.info("Phoenix does not support dropping schemas directly. Running clean of objects instead");
        doClean();
    }

    @Override
    protected void doClean() throws SQLException {
        // Clean sequences
        List<String> sequenceNames = listObjectsOfType("sequence");
        for (String statement : generateDropStatements("SEQUENCE", sequenceNames, "")) {
            jdbcTemplate.execute(statement);
        }

        // Clean views
        List<String> viewNames = listObjectsOfType("view");
        for (String statement : generateDropStatements("VIEW", viewNames, "")) {
            jdbcTemplate.execute(statement);
        }

        // Clean indices - bit more complicated, the statement needs an index name and a table name
        // listObjectsOfType("index") gives us a comma separated list of each

        List<String> indexPairs = listObjectsOfType("index");
        List<String> indexNames = new ArrayList<String>();
        List<String> indexTables = new ArrayList<String>();
        for(String indexPair : indexPairs) {
            String[] splits = indexPair.split(",");
            indexNames.add(splits[0]);
            indexTables.add("ON " + dbSupport.quote(name, splits[1]));
        }

        // Generate statements for each index
        List<String> statements = generateDropIndexStatements(indexNames, indexTables);
        for(String statement: statements) {
            jdbcTemplate.execute(statement);
        }

        // Drop each table
        for (Table table : allTables()) {
            table.drop();
        }
    }

    /**
     * Generate the statements for dropping all the objects of this type in this schema.
     *
     * @param objectType          The type of object to drop (Sequence, constant, ...)
     * @param objectNames         The names of the objects to drop.
     * @param dropStatementSuffix Suffix to append to the statement for dropping the objects.
     * @return The list of statements.
     */
    private List<String> generateDropStatements(String objectType, List<String> objectNames, String dropStatementSuffix) {
        List<String> statements = new ArrayList<String>();
        for (String objectName : objectNames) {
            String dropStatement =
                    "DROP " + objectType + dbSupport.quote(name, objectName) + " " + dropStatementSuffix;

            statements.add(dropStatement);
        }
        return statements;
    }

    private List<String> generateDropIndexStatements(List<String> objectNames, List<String> dropStatementSuffixes) {
        List<String> statements = new ArrayList<String>();
        for (int i = 0; i < objectNames.size(); i++) {
            String dropStatement =
                    "DROP INDEX " + dbSupport.quote(objectNames.get(i)) + " " + dropStatementSuffixes.get(i);

            statements.add(dropStatement);
        }
        return statements;
    }

    @Override
    protected Table[] doAllTables() throws SQLException {
        List<String> tableNames = listObjectsOfType("table");

        Table[] tables = new Table[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new PhoenixTable(jdbcTemplate, dbSupport, this, tableNames.get(i));
        }
        return tables;
    }

    /**
     * List the names of the objects of this type in this schema.
     *
     * @return The names of the objects.
     * @throws java.sql.SQLException when the object names could not be listed.
     */

    protected List<String> listObjectsOfType(String type) throws SQLException {
        if (type.equalsIgnoreCase("sequence")) {
            if(name == null) {
                String query = "SELECT SEQUENCE_NAME FROM SYSTEM.\"SEQUENCE\" WHERE SEQUENCE_SCHEMA IS NULL";
                return jdbcTemplate.queryForStringList(query);
            }
            else {
                String query = "SELECT SEQUENCE_NAME FROM SYSTEM.\"SEQUENCE\" WHERE SEQUENCE_SCHEMA = ?";
                return jdbcTemplate.queryForStringList(query, name);
            }
        }

        // Construct a general query structure for objects in the catalog
        String queryStart = "SELECT TABLE_NAME FROM SYSTEM.CATALOG WHERE TABLE_SCHEM";
        String queryMid = "";
        String queryEnd = "";

        if(name == null) {
            queryMid += " IS NULL";
        }
        else {
            queryMid += " = ?";
        }

        String tableType = "";
        if (type.equalsIgnoreCase("table")) {
            tableType = "u";
        }
        else if (type.equalsIgnoreCase("view")) {
            tableType = "v";
        }
        else if (type.equalsIgnoreCase("index")) {
            tableType = "i";

            // Indices have two components, index name and table name so we'll have to fix up the query
            queryStart = "SELECT TABLE_NAME, DATA_TABLE_NAME FROM SYSTEM.CATALOG WHERE TABLE_SCHEM";
            queryEnd = " AND TABLE_TYPE = '" + tableType + "'";

            // Create the final query, however jdbcTemplate.query doesn't take parameters
            // We'll populate the schema name ourselves if needed

            String query = queryStart + queryMid.replaceFirst("\\?", "'" + name + "'") + queryEnd;

            // Return the index and table as a comma separated string
            return jdbcTemplate.query(query, new RowMapper<String> () {
                @Override
                public String mapRow(ResultSet rs) throws SQLException {
                    return rs.getString("TABLE_NAME") + "," + rs.getString("DATA_TABLE_NAME");
                }
            });
        }

        // Assemble the query for an index or a view
        queryEnd = " AND TABLE_TYPE = '" + tableType + "'";
        String query = queryStart + queryMid + queryEnd;

        if(name == null) {
            return jdbcTemplate.queryForStringList(query);
        }
        else {
            return jdbcTemplate.queryForStringList(query, name);
        }
    }

    @Override
    public Table getTable(String tableName) {
        return new PhoenixTable(jdbcTemplate, dbSupport, this, tableName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schema schema = (Schema) o;
        if(name == null) {
            return name == schema.getName();
        }
        else {
            return name.equals(schema.getName());
        }
    }
}
