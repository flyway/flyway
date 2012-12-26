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
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.SqlStatementBuilder;
import com.googlecode.flyway.core.util.jdbc.JdbcUtils;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        if (version18) {
            //Do nothing -> Locking is not supported by HsqlDb 1.8
        } else {
            jdbcTemplate.execute("select * from " + quote(schema, table) + " for update");
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

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new HsqlSchema(jdbcTemplate, this, name);
    }
}
