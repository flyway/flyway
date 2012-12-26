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
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.SqlStatementBuilder;
import com.googlecode.flyway.core.util.jdbc.JdbcUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * H2 database specific support
 */
public class H2DbSupport extends DbSupport {
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

    public Schema getCurrentSchema() throws SQLException {
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

        return getSchema(schema);
    }

    @Override
    public void setCurrentSchema(Schema schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + schema);
    }

    public boolean tableExistsNoQuotes(final String schema, final String table) throws SQLException {
        return tableExists(null, schema.toUpperCase(), table.toUpperCase());
    }

    public boolean columnExists(String schema, String table, String column) throws SQLException {
        return columnExists(null, schema, table, column);
    }

    @Override
    public boolean primaryKeyExists(String schema, String table) throws SQLException {
        return primaryKeyExists(null, schema, table);
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    public void lockTable(String schema, String table) throws SQLException {
        jdbcTemplate.execute("select * from " + quote(schema) + "." + quote(table) + " for update");
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

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new H2Schema(jdbcTemplate, this, name);
    }
}