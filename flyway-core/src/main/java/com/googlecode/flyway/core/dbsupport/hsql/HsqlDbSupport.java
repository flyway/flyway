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
package com.googlecode.flyway.core.dbsupport.hsql;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.resolver.sql.SqlScript;
import com.googlecode.flyway.core.dbsupport.SqlStatement;
import com.googlecode.flyway.core.dbsupport.SqlStatementBuilder;
import com.googlecode.flyway.core.util.jdbc.JdbcUtils;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * HsqlDb-specific support
 */
public class HsqlDbSupport extends DbSupport {
    private static final Log LOG = LogFactory.getLog(HsqlDbSupport.class);

    /**
     * Flag indicating whether we are running against the old Hsql 1.8 instead of the newer 2.x.
     */
    private boolean version18;

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public HsqlDbSupport(Connection connection) {
        super(new HsqlJdbcTemplate(connection));

        try {
            int majorVersion = jdbcTemplate.getMetaData().getDatabaseMajorVersion();
            version18 = majorVersion < 2;
        } catch (SQLException e) {
            throw new FlywayException("Unable to determine the Hsql version", e);
        }

        if (version18) {
            LOG.info("Hsql 1.8 does not support locking. No concurrent migration supported.");
        }
    }

    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/hsql/";
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

    @Override
    public void setCurrentSchema(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + quote(schema));
    }

    public boolean isSchemaEmpty(final String schema) throws SQLException {
        return !jdbcTemplate.tableExists(null, schema.toUpperCase(), null);
    }

    public boolean tableExistsNoQuotes(final String schema, final String table) throws SQLException {
        return jdbcTemplate.tableExists(null, schema.toUpperCase(), table.toUpperCase());
    }

    public boolean tableExists(String schema, String table) throws SQLException {
        return jdbcTemplate.tableExists(null, schema, table);
    }

    public boolean columnExists(String schema, String table, String column) throws SQLException {
        return jdbcTemplate.columnExists(null, schema, table, column);
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    public void lockTable(String schema, String table) throws SQLException {
        if (version18) {
            //Do nothing -> Locking is not supported by HsqlDb 1.8
        } else {
            jdbcTemplate.execute("select * from " + quote(schema) + "." + quote(table) + " for update");
        }
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new HsqlSqlStatementBuilder();
    }

    public SqlScript createCleanScript(final String schema) throws SQLException {
        final List<String> statements = generateDropStatementsForTables(schema);
        statements.addAll(generateDropStatementsForSequences(schema));

        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int lineNumber = 1;
        for (String statement : statements) {
            sqlStatements.add(new SqlStatement(lineNumber, statement));
            lineNumber++;
        }
        return new SqlScript(sqlStatements, this);
    }

    /**
     * Generates the statements to drop the tables in this schema.
     *
     * @param schema The schema to generate the statements for.
     * @return The drop statements.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForTables(final String schema) throws SQLException {
        final List<String> statements = new ArrayList<String>();

        ResultSet resultSet = null;
        try {
            resultSet = jdbcTemplate.getMetaData().getTables(null, schema, null, new String[]{"TABLE"});
            while (resultSet.next()) {
                statements.add("DROP TABLE \"" + schema + "\".\"" + resultSet.getString("TABLE_NAME") + "\" CASCADE");
            }
        } finally {
            JdbcUtils.closeResultSet(resultSet);
        }

        return statements;
    }

    /**
     * Generates the statements to drop the sequences in this schema.
     *
     * @param schema The schema to generate the statements for.
     * @return The drop statements.
     * @throws SQLException when the drop statements could not be generated.
     */
    private List<String> generateDropStatementsForSequences(String schema) throws SQLException {
        List<String> sequenceNames = jdbcTemplate.queryForStringList(
                "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES where SEQUENCE_SCHEMA = ?", schema);

        List<String> statements = new ArrayList<String>();
        for (String seqName : sequenceNames) {
            statements.add("DROP SEQUENCE \"" + schema + "\".\"" + seqName + "\"");
        }

        return statements;
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }
}
