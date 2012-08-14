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
package com.googlecode.flyway.core.dbsupport.h2;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.migration.sql.SqlStatement;
import com.googlecode.flyway.core.migration.sql.SqlStatementBuilder;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.jdbc.JdbcUtils;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * H2 database specific support
 */
public class H2DbSupport extends DbSupport {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(H2DbSupport.class);

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public H2DbSupport(Connection connection) {
        super(new H2JdbcTemplate(connection));
    }

    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/h2/";
    }

    public String getCurrentUserFunction() {
        return "USER()";
    }

    public String getCurrentSchema() throws SQLException {
        ResultSet resultSet = null;
        String schema = null;
        try {
            resultSet = jdbcTemplate.getMetaData().getSchemas();
            while (resultSet.next()) {
                if (resultSet.getBoolean("IS_DEFAULT")) {
                    schema = resultSet.getString("TABLE_SCHEM");
                    break;
                }
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
        }

        return schema;
    }

    public boolean isSchemaEmpty(String schema) throws SQLException {
        List<String> tables = jdbcTemplate.queryForStringList("SHOW TABLES FROM " + schema);
        return tables.isEmpty();
    }

    public boolean tableExists(final String schema, final String table) throws SQLException {
        return jdbcTemplate.hasTables(null, schema.toUpperCase(), table.toUpperCase());
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    public void lockTable(String schema, String table) throws SQLException {
        jdbcTemplate.execute("select * from " + schema + "." + table + " for update");
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new H2SqlStatementBuilder();
    }

    public SqlScript createCleanScript(String schema) throws SQLException {
        List<String> tableNames = listObjectNames("TABLE", "TABLE_TYPE = 'TABLE'", schema);
        List<String> statements = generateDropStatements("TABLE", tableNames, "CASCADE", schema);

        List<String> sequenceNames = listObjectNames("SEQUENCE", "IS_GENERATED = false", schema);
        statements.addAll(generateDropStatements("SEQUENCE", sequenceNames, "", schema));

        List<String> constantNames = listObjectNames("CONSTANT", "", schema);
        statements.addAll(generateDropStatements("CONSTANT", constantNames, "", schema));

        List<String> domainNames = listObjectNames("DOMAIN", "", schema);
        if (!domainNames.isEmpty()) {
            if (schema.equals(getCurrentSchema())) {
                statements.addAll(generateDropStatementsForCurrentSchema("DOMAIN", domainNames, ""));
            } else {
                LOG.error("Unable to drop DOMAIN objects in schema '" + schema
                        + "' due to H2 bug! (More info: http://code.google.com/p/h2database/issues/detail?id=306)");
            }
        }

        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int lineNumber = 1;
        for (String statement : statements) {
            sqlStatements.add(new SqlStatement(lineNumber, statement));
            lineNumber++;
        }
        return new SqlScript(sqlStatements, this);
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
     * Generate the statements for dropping all the objects of this type in the current schema.
     *
     * @param objectType          The type of object to drop (Sequence, constant, ...)
     * @param objectNames         The names of the objects to drop.
     * @param dropStatementSuffix Suffix to append to the statement for dropping the objects.
     * @return The list of statements.
     */
    private List<String> generateDropStatementsForCurrentSchema(String objectType, List<String> objectNames, String dropStatementSuffix) {
        List<String> statements = new ArrayList<String>();
        for (String objectName : objectNames) {
            String dropStatement =
                    "DROP " + objectType + " \"" + objectName + "\"" + " " + dropStatementSuffix;

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
        String query = "SELECT " + objectType + "_NAME FROM information_schema." + objectType + "s WHERE " + objectType + "_schema = ?";
        if (StringUtils.hasLength(querySuffix)) {
            query += " AND " + querySuffix;
        }

        return jdbcTemplate.queryForStringList(query, schema);
    }
}