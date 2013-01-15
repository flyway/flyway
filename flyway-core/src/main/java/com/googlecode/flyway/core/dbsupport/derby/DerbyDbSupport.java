/**
 * Copyright (C) 2010-2013 the original author or authors.
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
package com.googlecode.flyway.core.dbsupport.derby;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Derby database specific support
 */
public class DerbyDbSupport extends DbSupport {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public DerbyDbSupport(Connection connection) {
        super(new DerbyJdbcTemplate(connection));
    }

    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/derby/";
    }

    public String getCurrentUserFunction() {
        return "CURRENT_USER";
    }

    @Override
    protected String doGetCurrentSchema() throws SQLException {
        return jdbcTemplate.queryForString("SELECT CURRENT SCHEMA FROM SYSIBM.SYSDUMMY1");
    }

    @Override
    protected void doSetCurrentSchema(Schema schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + schema);
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public String getBooleanTrue() {
        return "true";
    }

    public String getBooleanFalse() {
        return "false";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new DerbySqlStatementBuilder();
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new DerbySchema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }
}