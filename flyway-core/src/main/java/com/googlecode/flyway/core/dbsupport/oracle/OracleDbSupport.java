/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.oracle;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.exception.FlywayException;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.migration.sql.SqlStatement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Oracle-specific support.
 */
public class OracleDbSupport extends DbSupport {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(OracleDbSupport.class);

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public OracleDbSupport(Connection connection) {
        super(new OracleJdbcTemplate(connection));
    }


    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/oracle/";
    }

    public String getCurrentUserFunction() {
        return "USER";
    }

    public String getCurrentSchema() throws SQLException {
        return jdbcTemplate.queryForString("SELECT USER FROM dual");
    }

    public boolean isSchemaEmpty(String schema) throws SQLException {
        int objectCount = jdbcTemplate.queryForInt("SELECT count(*) FROM all_objects WHERE owner = ?", schema);
        return objectCount == 0;
    }

    public boolean tableExists(final String schema, final String table) throws SQLException {
        return jdbcTemplate.hasTables(null, schema.toUpperCase(), table.toUpperCase());
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    public void lockTable(String schema, String table) throws SQLException {
        jdbcTemplate.update("select * from " + schema + "." + table + " for update");
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }

    public SqlScript createSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        return new OracleSqlScript(sqlScriptSource, placeholderReplacer);
    }

    public SqlScript createCleanScript(String schema) throws SQLException {
        if ("SYSTEM".equals(schema.toUpperCase())) {
            throw new FlywayException("Clean not supported on Oracle for user 'SYSTEM'! You should NEVER add your own objects to the SYSTEM schema!");
        }

        final List<String> allDropStatements = new ArrayList<String>();
        allDropStatements.add("PURGE RECYCLEBIN");
        allDropStatements.addAll(generateDropStatementsForSpatialExtensions(schema));
        allDropStatements.addAll(generateDropStatementsForObjectType("SEQUENCE", "", schema));
        allDropStatements.addAll(generateDropStatementsForObjectType("FUNCTION", "", schema));
        allDropStatements.addAll(generateDropStatementsForObjectType("MATERIALIZED VIEW", "PRESERVE TABLE", schema));
        allDropStatements.addAll(generateDropStatementsForObjectType("PACKAGE", "", schema));
        allDropStatements.addAll(generateDropStatementsForObjectType("PROCEDURE", "", schema));
        allDropStatements.addAll(generateDropStatementsForObjectType("SYNONYM", "", schema));
        allDropStatements.addAll(generateDropStatementsForObjectType("VIEW", "CASCADE CONSTRAINTS", schema));
        allDropStatements.addAll(generateDropStatementsForObjectType("TABLE", "CASCADE CONSTRAINTS PURGE", schema));
        allDropStatements.addAll(generateDropStatementsForObjectType("TYPE", "FORCE", schema));

        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int lineNumber = 1;
        for (String dropStatement : allDropStatements) {
            sqlStatements.add(new SqlStatement(lineNumber, dropStatement));
            lineNumber++;
        }
        return new SqlScript(sqlStatements);
    }

    /**
     * Generates the drop statements for all database objects of this type.
     *
     * @param objectType     The type of database object to drop.
     * @param extraArguments The extra arguments to add to the drop statement.
     * @param schema         The schema for which to generate the statements.
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForObjectType(String objectType, final String extraArguments, final String schema) throws SQLException {
        String query = "SELECT object_name FROM all_objects WHERE object_type = ? AND owner = ?"
                // Ignore Recycle bin objects
                + " AND object_name NOT LIKE 'BIN$%'"
                // Ignore Spatial Index Tables and Sequences as they get dropped automatically when the index gets dropped.
                + " AND object_name NOT LIKE 'MDRT_%$' AND object_name NOT LIKE 'MDRS_%$'"
                // Ignore Materialized View Logs
                + " AND object_name NOT LIKE 'MLOG$%' AND object_name NOT LIKE 'RUPD$%'"
                // Ignore Oracle Text Index Tables
                + " AND object_name NOT LIKE 'DR$%'";

        List<String> objectNames = jdbcTemplate.queryForStringList(query, objectType, schema.toUpperCase());
        List<String> dropStatements = new ArrayList<String>();
        for (String objectName : objectNames) {
            dropStatements.add("DROP " + objectType + " " + schema + ".\"" + objectName + "\" " + extraArguments);
        }
        return dropStatements;
    }

    /**
     * Generates the drop statements for Oracle Spatial Extensions-related database objects.
     *
     * @param schema The schema for which to generate the statements.
     * @return The complete drop statements, ready to execute.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForSpatialExtensions(String schema) throws SQLException {
        List<String> statements = new ArrayList<String>();

        if (!spatialExtensionsAvailable()) {
            LOG.debug("Oracle Spatial Extensions are not available. No cleaning of MDSYS tables and views.");
            return statements;
        }
        if (!getCurrentSchema().equalsIgnoreCase(schema)) {
            int count = jdbcTemplate.queryForInt("SELECT COUNT (*) FROM all_sdo_geom_metadata WHERE owner=?", schema.toUpperCase());
            count += jdbcTemplate.queryForInt("SELECT COUNT (*) FROM all_sdo_index_info WHERE sdo_index_owner=?", schema.toUpperCase());
            if (count > 0) {
                LOG.warn("Unable to clean Oracle Spatial objects for schema '" + schema + "' as they do not belong to the default schema for this connection!");
            }
            return statements;
        }


        statements.add("DELETE FROM mdsys.user_sdo_geom_metadata");

        List<String> indexNames = jdbcTemplate.queryForStringList("select INDEX_NAME from USER_SDO_INDEX_INFO");
        for (String indexName : indexNames) {
            statements.add("DROP INDEX \"" + indexName + "\"");
        }

        return statements;
    }

    /**
     * Checks whether Oracle Spatial extensions are available or not.
     *
     * @return {@code true} if they are available, {@code false} if not.
     * @throws SQLException when checking availability of the spatial extensions failed.
     */
    private boolean spatialExtensionsAvailable() throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM all_views WHERE owner = 'MDSYS' AND view_name = 'USER_SDO_GEOM_METADATA'") > 0;
    }
}
