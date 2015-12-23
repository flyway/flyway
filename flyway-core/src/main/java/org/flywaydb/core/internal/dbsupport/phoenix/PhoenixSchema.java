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
import org.flywaydb.core.internal.util.jdbc.RowMapper;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        // Find a matching schema
        ResultSet rs = jdbcTemplate.getMetaData().getSchemas();
        while(rs.next()) {
            String schemaName = rs.getString("TABLE_SCHEM");
            if(schemaName == null) {
                if(name == null) {
                    return true;
                }
            }
            else {
                if(name != null && schemaName.equals(name)) {
                    return true;
                }
            }
        }
        return false;
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

        // Generate statements for each table
        List<String> tableNames = listObjectsOfType("table");
        for (String statement : generateDropStatements("TABLE", tableNames, "")) {
            jdbcTemplate.execute(statement);
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
                    "DROP " + objectType + " " + dbSupport.quote(name, objectName) + " " + dropStatementSuffix;

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
        List<String> retVal = new ArrayList<String>();

        // A null schema name actually does a cross-schema search in Phoenix, change to 0-length
        String finalName = (name == null ? "" : name);

        // Available through metadata interface
        if (type.equalsIgnoreCase("view")) {
            ResultSet rs = jdbcTemplate.getConnection().getMetaData().getTables(null, finalName, null, new String[]{"VIEW"});
            while(rs.next()) {
                String viewName = rs.getString("TABLE_NAME");
                if(viewName != null) {
                    retVal.add(viewName);
                }

            }
        }
        else if (type.equalsIgnoreCase("table")) {
            ResultSet rs = jdbcTemplate.getMetaData().getTables(null, finalName, null, new String[] {"TABLE"} );
            while(rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                Set<String> tables = new HashSet<String>();
                if(tableName != null) {
                    tables.add(tableName);
                }
                retVal.addAll(tables);
            }
        }
        // Sequences aren't available through the DatabaseMetaData interface
        else if (type.equalsIgnoreCase("sequence")) {
            if(name == null) {
                String query = "SELECT SEQUENCE_NAME FROM SYSTEM.\"SEQUENCE\" WHERE SEQUENCE_SCHEMA IS NULL";
                return jdbcTemplate.queryForStringList(query);
            }
            else {
                String query = "SELECT SEQUENCE_NAME FROM SYSTEM.\"SEQUENCE\" WHERE SEQUENCE_SCHEMA = ?";
                return jdbcTemplate.queryForStringList(query, name);
            }
        }
        // Neither are indices, unless we know the table ahead of time
        else if (type.equalsIgnoreCase("index")) {
            String query = "SELECT TABLE_NAME, DATA_TABLE_NAME FROM SYSTEM.CATALOG WHERE TABLE_SCHEM";

            if(name == null) {
                query = query + " IS NULL";
            }
            else {
                query = query + " = ?";
            }
            query = query + " AND TABLE_TYPE = 'i'";

            String finalQuery = query.replaceFirst("\\?", "'" + name + "'");
            // Return the index and table as a comma separated string
            retVal = jdbcTemplate.query(finalQuery, new RowMapper<String> () {
                @Override
                public String mapRow(ResultSet rs) throws SQLException {
                    return rs.getString("TABLE_NAME") + "," + rs.getString("DATA_TABLE_NAME");
                }
            });
        }
        return retVal;
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
