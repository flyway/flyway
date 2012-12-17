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
package com.googlecode.flyway.core.dbsupport.postgresql;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * PostgreSQL-specific support.
 */
public class PostgreSQLDbSupport extends DbSupport {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public PostgreSQLDbSupport(Connection connection) {
        super(new PostgreSQLJdbcTemplate(connection));
    }


    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/postgresql/";
    }

    public String getCurrentUserFunction() {
        return "current_user";
    }

    public String getCurrentSchema() throws SQLException {
        return jdbcTemplate.queryForString("SELECT current_schema()");
    }

    @Override
    public void setCurrentSchema(String schema) throws SQLException {
        String searchPath = jdbcTemplate.queryForString("SHOW search_path");
        jdbcTemplate.execute("SET search_path = " + quote(schema) + "," + searchPath);
    }

    public boolean isSchemaEmpty(String schema) throws SQLException {
        int objectCount = jdbcTemplate.queryForInt(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema=? AND table_type='BASE TABLE'",
                schema);
        return objectCount == 0;
    }

    @Override
    public boolean schemaExists(String schema) throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM pg_namespace WHERE nspname=?", schema) > 0;
    }

    public boolean tableExistsNoQuotes(final String schema, final String table) throws SQLException {
        return tableExists(null, schema.toLowerCase(), table.toLowerCase(), "TABLE");
    }

    public boolean tableExists(String schema, String table) throws SQLException {
        return tableExists(null, schema, table);
    }

    public boolean columnExists(String schema, String table, String column) throws SQLException {
        return columnExists(null, schema, table, column);
    }

    @Override
    public boolean primaryKeyExists(String schema, String table) throws SQLException {
        return primaryKeyExists(null, schema, table);
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public void lockTable(String schema, String table) throws SQLException {
        jdbcTemplate.execute("select * from " + quote(schema) + "." + quote(table) + " for update");
    }

    public String getBooleanTrue() {
        return "TRUE";
    }

    public String getBooleanFalse() {
        return "FALSE";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new PostgreSQLSqlStatementBuilder();
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new PostgreSQLSchema(jdbcTemplate, this, name);
    }
}
