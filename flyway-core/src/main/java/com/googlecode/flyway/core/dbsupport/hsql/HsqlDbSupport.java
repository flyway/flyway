/**
 * Copyright (C) 2010-2011 the original author or authors.
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

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.migration.sql.SqlStatement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * HsqlDb-specific support
 */
public class HsqlDbSupport extends DbSupport {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(HsqlDbSupport.class);

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public HsqlDbSupport(Connection connection) {
        super(new HsqlJdbcTemplate(connection));
        LOG.info("Hsql does not support locking. No concurrent migration supported.");
    }

    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/hsql/";
    }

    public String getCurrentUserFunction() {
        return "USER()";
    }

    public String getCurrentSchema() throws SQLException {
        ResultSet resultSet = jdbcTemplate.getMetaData().getSchemas();
        while (resultSet.next()) {
            if (resultSet.getBoolean("IS_DEFAULT")) {
                return resultSet.getString("TABLE_SCHEM");
            }
        }
        return null;
    }

    public boolean isSchemaEmpty(final String schema) throws SQLException {
        ResultSet resultSet = jdbcTemplate.getMetaData().getTables(null, schema, null, null);
        return !resultSet.next();
    }

    public boolean tableExists(final String schema, final String table) throws SQLException {
        ResultSet resultSet = jdbcTemplate.getMetaData().getTables(null, schema.toUpperCase(),
                table.toUpperCase(), null);
        return resultSet.next();
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    public void lockTable(String schema, String table) {
        //Locking is not supported by Hsql
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }

    public SqlScript createSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        return new HsqlSqlScript(sqlScriptSource, placeholderReplacer);
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
        return new SqlScript(sqlStatements);
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

        ResultSet resultSet = jdbcTemplate.getMetaData().getTables(null, schema, null, new String[]{"TABLE"});
        while (resultSet.next()) {
            statements.add("DROP TABLE \"" + schema + "\".\"" + resultSet.getString("TABLE_NAME") + "\" CASCADE");
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
}
