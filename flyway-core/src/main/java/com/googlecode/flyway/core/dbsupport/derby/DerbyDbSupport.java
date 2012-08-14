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
package com.googlecode.flyway.core.dbsupport.derby;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.sqlserver.SQLServerSqlStatementBuilder;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.migration.sql.SqlStatement;
import com.googlecode.flyway.core.migration.sql.SqlStatementBuilder;
import com.googlecode.flyway.core.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Derby database specific support
 */
public class DerbyDbSupport extends DbSupport {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public DerbyDbSupport(Connection connection) {
        super(new DerbyJdbcTemplate(connection));
    }

    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/derby/";
    }

    public String getCurrentUserFunction() {
        return "CURRENT_USER";
    }

    public String getCurrentSchema() throws SQLException {
        return jdbcTemplate.queryForString("SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1");
    }

    public boolean isSchemaEmpty(String schema) throws SQLException {
        return !jdbcTemplate.hasTables(null, schema.toUpperCase(), null);
    }

    public boolean tableExists(final String schema, final String table) throws SQLException {
        return jdbcTemplate.hasTables(null, schema.toUpperCase(), table.toUpperCase());
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public void lockTable(String schema, String table) throws SQLException {
        jdbcTemplate.execute("LOCK TABLE " + schema + "." + table + " IN EXCLUSIVE MODE");
    }

    public String getBooleanTrue() {
        return "true";
    }

    public String getBooleanFalse() {
        return "false";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new DerbySqlStatementBuilder();
    }

    public SqlScript createCleanScript(String schema) throws SQLException {
        List<String> statements = generateDropStatementsForConstraints(schema);

        List<String> viewNames = listObjectNames("TABLE", "TABLETYPE='V'", schema);
        statements.addAll(generateDropStatements("VIEW", viewNames, "", schema));

        List<String> tableNames = listObjectNames("TABLE", "TABLETYPE='T'", schema);
        statements.addAll(generateDropStatements("TABLE", tableNames, "", schema));

        List<String> sequenceNames = listObjectNames("SEQUENCE", "", schema);
        statements.addAll(generateDropStatements("SEQUENCE", sequenceNames, "RESTRICT", schema));

        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int lineNumber = 1;
        for (String statement : statements) {
            sqlStatements.add(new SqlStatement(lineNumber, statement));
            lineNumber++;
        }
        return new SqlScript(sqlStatements, this);
    }

    /**
     * Generate the statements for dropping all the constraints in this schema.
     *
     * @param schema              The schema for which the statements should be generated.
     * @return The list of statements.
     * @throws SQLException when the statements could not be generated.
     */
    private List<String> generateDropStatementsForConstraints(String schema) throws SQLException {
        List<Map<String,String>> results = jdbcTemplate.queryForList("SELECT c.constraintname, t.tablename FROM sys.sysconstraints c" +
                " INNER JOIN sys.systables t ON c.tableid = t.tableid" +
                " INNER JOIN sys.sysschemas s ON c.schemaid = s.schemaid" +
                " WHERE c.type = 'F' AND s.schemaname = ?", schema);

        List<String> statements = new ArrayList<String>();
        for (Map<String,String> result : results) {
            String dropStatement =
                    "ALTER TABLE \"" + schema + "\".\"" + result.get("TABLENAME") + "\""
                            + " DROP CONSTRAINT \"" + result.get("CONSTRAINTNAME") + "\"";

            statements.add(dropStatement);
        }
        return statements;
    }

    /**
     * Generate the statements for dropping all the objects of this type in this schema.
     *
     * @param objectType          The type of object to drop (Sequence, constant, ...)
     * @param objectNames         The names of the objects to drop.
     * @param dropStatementSuffix Suffix to append to the statement for dropping the objects.
     * @param schema              The schema for which the statements should be generated.
     * @return The list of statements.
     */
    private List<String> generateDropStatements(String objectType, List<String> objectNames, String dropStatementSuffix, String schema) {
        List<String> statements = new ArrayList<String>();
        for (String objectName : objectNames) {
            String dropStatement =
                    "DROP " + objectType + " \"" + schema + "\".\"" + objectName + "\"" + " " + dropStatementSuffix;

            statements.add(dropStatement);
        }
        return statements;
    }

    /**
     * List the names of the objects of this type in this schema.
     *
     * @param objectType  The type of objects to list (Sequence, constant, ...)
     * @param querySuffix Suffix to append to the query to find the objects to list.
     * @param schema      The schema of objects to list.
     * @return The names of the objects.
     * @throws SQLException when the object names could not be listed.
     */
    private List<String> listObjectNames(String objectType, String querySuffix, String schema) throws SQLException {
        String query = "SELECT " + objectType + "name FROM sys.sys" + objectType + "s WHERE schemaid in (SELECT schemaid FROM sys.sysschemas where schemaname = ?)";
        if (StringUtils.hasLength(querySuffix)) {
            query += " AND " + querySuffix;
        }

        return jdbcTemplate.queryForStringList(query, schema);
    }
}