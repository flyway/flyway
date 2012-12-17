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
package com.googlecode.flyway.core.dbsupport.mysql;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Mysql-specific support.
 */
public class MySQLDbSupport extends DbSupport {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public MySQLDbSupport(Connection connection) {
        super(new MySQLJdbcTemplate(connection));
    }

    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/mysql/";
    }

    public String getCurrentUserFunction() {
        return "SUBSTRING_INDEX(USER(),'@',1)";
    }

    public String getCurrentSchema() throws SQLException {
        return jdbcTemplate.getConnection().getCatalog();
    }

    @Override
    public void setCurrentSchema(String schema) throws SQLException {
        jdbcTemplate.execute("USE " + quote(schema));
    }

    public boolean isSchemaEmpty(String schema) throws SQLException {
        int objectCount = jdbcTemplate.queryForInt("Select "
                + "(Select count(*) from information_schema.TABLES Where TABLE_SCHEMA=?) + "
                + "(Select count(*) from information_schema.VIEWS Where TABLE_SCHEMA=?) + "
                + "(Select count(*) from information_schema.TABLE_CONSTRAINTS Where TABLE_SCHEMA=?) + "
                + "(Select count(*) from information_schema.ROUTINES Where ROUTINE_SCHEMA=?)", schema,
                schema, schema, schema);
        return objectCount == 0;
    }

    @Override
    public boolean schemaExists(String schema) throws SQLException {
        return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name=?", schema) > 0;
    }

    public boolean tableExistsNoQuotes(final String schema, final String table) throws SQLException {
        return tableExists(schema, null, table);
    }

    public boolean tableExists(String schema, String table) throws SQLException {
        return tableExists(schema, null, table);
    }

    public boolean columnExists(String schema, String table, String column) throws SQLException {
        return columnExists(schema, null, table, column);
    }

    @Override
    public boolean primaryKeyExists(String schema, String table) throws SQLException {
        return primaryKeyExists(schema, null, table);
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
        return new MySQLSqlStatementBuilder();
    }

    @Override
    public String doQuote(String identifier) {
        return "`" + identifier + "`";
    }

    @Override
    public Schema getSchema(String name) {
        return new MySQLSchema(jdbcTemplate, this, name);
    }
}
