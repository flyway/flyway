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
package com.googlecode.flyway.core.dbsupport.h2;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.migration.sql.PlaceholderReplacer;
import com.googlecode.flyway.core.migration.sql.SqlScript;
import com.googlecode.flyway.core.migration.sql.SqlStatement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * H2 database specific support
 */
public class H2DbSupport implements DbSupport {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(H2DbSupport.class);

    /**
     * The jdbcTemplate to use.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new instance.
     *
     * @param jdbcTemplate The jdbcTemplate to use.
     */
    public H2DbSupport(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/h2/";
    }

    @Override
    public String getCurrentUserFunction() {
        return "USER()";
    }

    @Override
    public String getCurrentSchema() {
        return (String) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public String doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getSchemas();
                while (resultSet.next()) {
                    if (resultSet.getBoolean("IS_DEFAULT")) {
                        return resultSet.getString("TABLE_SCHEM");
                    }
                }
                return null;
            }
        });
    }

    @Override
    public boolean isSchemaEmpty() {
        @SuppressWarnings({"unchecked"})
        List<Map<String, Object>> tables = jdbcTemplate.queryForList("SHOW TABLES FROM " + getCurrentSchema());
        return tables.isEmpty();
    }

    @Override
    public boolean tableExists(final String schema, final String table) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getTables(null, schema.toUpperCase(),
                        table.toUpperCase(), null);
                return resultSet.next();
            }
        });
    }

    @Override
    public boolean columnExists(final String table, final String column) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getColumns(null, getCurrentSchema(),
                        table.toUpperCase(), column.toUpperCase());
                return resultSet.next();
            }
        });
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public void lockTable(String table) {
        jdbcTemplate.execute("select * from " + table + " for update");
    }

    @Override
    public String getBooleanTrue() {
        return "1";
    }

    @Override
    public String getBooleanFalse() {
        return "0";
    }

    @Override
    public SqlScript createSqlScript(String sqlScriptSource, PlaceholderReplacer placeholderReplacer) {
        return new H2SqlScript(sqlScriptSource, placeholderReplacer);
    }

    @Override
    public SqlScript createCleanScript(String schema) {
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
        return new SqlScript(sqlStatements);
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
     */
    @SuppressWarnings({"unchecked"})
    private List<String> listObjectNames(String objectType, String querySuffix, String schema) {
        String query = "SELECT " + objectType + "_NAME FROM information_schema." + objectType + "s WHERE " + objectType + "_schema = ?";
        if (StringUtils.hasLength(querySuffix)) {
            query += " AND " + querySuffix;
        }

        return jdbcTemplate.queryForList(query, new String[]{schema}, String.class);
    }
}