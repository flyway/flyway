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

    public Schema getCurrentSchema() throws SQLException {
        return getSchema(jdbcTemplate.queryForString("SELECT current_schema()"));
    }

    @Override
    public void setCurrentSchema(Schema schema) throws SQLException {
        String searchPath = jdbcTemplate.queryForString("SHOW search_path");
        jdbcTemplate.execute("SET search_path = " + schema + "," + searchPath);
    }

    public boolean supportsDdlTransactions() {
        return true;
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

    @Override
    public boolean catalogIsSchema() {
        return false;
    }
}
