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
    public boolean tableExists(final String table) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getTables(null, getCurrentSchema(),
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
    public SqlScript createCleanScript() {
        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        sqlStatements.addAll(generateDropStatements("TABLE", "TABLE_TYPE = 'TABLE'", "CASCADE", sqlStatements.size()));
        sqlStatements.addAll(generateDropStatements("SEQUENCE", "IS_GENERATED = false", "", sqlStatements.size()));
        sqlStatements.addAll(generateDropStatements("CONSTANT", "", "", sqlStatements.size()));
        sqlStatements.addAll(generateDropStatements("DOMAIN", "", "", sqlStatements.size()));
        return new SqlScript(sqlStatements);
    }

    /**
     * Generate the statements for dropping all the objects of this type in the current schema.
     *
     * @param objectType The type of object to drop (Sequence, constant, ...)
     * @param querySuffix Suffix to append to the query to find the objects to drop.
     * @param dropStatementSuffix Suffix to append to the statement for dropping the objects.
     * @param initialLineNumber The initial line number of the first statement that will be added to the drop script.
     *
     * @return The list of statements.
     */
    private List<SqlStatement> generateDropStatements(String objectType, String querySuffix, String dropStatementSuffix, int initialLineNumber) {
        String query = "SELECT " + objectType + "_NAME FROM information_schema." + objectType + "s WHERE " + objectType + "_schema = ?";
        if (StringUtils.hasLength(querySuffix)) {
            query += " AND " + querySuffix;
        }

        @SuppressWarnings({"unchecked"})
        List<Map<String, Object>> objectNames = jdbcTemplate.queryForList(query, new Object[]{getCurrentSchema()});

        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int count = initialLineNumber;
        for (Map<String, Object> objectName : objectNames) {
            String dropStatement =
                    "DROP " + objectType + " \"" + objectName.get(objectType + "_NAME") + "\"" + " " + dropStatementSuffix;

            sqlStatements.add(new SqlStatement(count, dropStatement));
            count++;
        }
        return sqlStatements;
    }
}