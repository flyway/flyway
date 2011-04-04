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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * HsqlDb-specific support
 */
public class HsqlDbSupport implements DbSupport {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(HsqlDbSupport.class);

    /**
     * The jdbcTemplate to use.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new instance.
     *
     * @param jdbcTemplate The jdbcTemplate to use.
     */
    public HsqlDbSupport(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        LOG.info("Hsql does not support locking. No concurrent migration supported.");
    }

    @Override
    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/hsql/";
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
    public boolean isSchemaEmpty(final String schema) {
        return (Boolean) jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Object doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getTables(null, schema, null, null);
                return !resultSet.next();
            }
        });
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
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public void lockTable(String schema, String table) {
        //Locking is not supported by Hsql
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
        return new HsqlSqlScript(sqlScriptSource, placeholderReplacer);
    }

    @Override
    public SqlScript createCleanScript(final String schema) {
        final List<String> statements = new ArrayList<String>();

        jdbcTemplate.execute(new ConnectionCallback() {
            @Override
            public Object doInConnection(Connection connection) throws SQLException, DataAccessException {
                ResultSet resultSet = connection.getMetaData().getTables(null, schema,
                        null, new String[] {"TABLE"});
                while (resultSet.next()) {
                    statements.add("DROP TABLE \"" + schema + "\".\"" + resultSet.getString("TABLE_NAME") + "\" CASCADE");
                }
                return null;
            }
        });

        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        int lineNumber = 1;
        for (String statement : statements) {
            sqlStatements.add(new SqlStatement(lineNumber, statement));
            lineNumber++;
        }
        return new SqlScript(sqlStatements);
    }
}
