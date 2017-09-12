/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.clickhouse;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ClickHouse-specific support.
 */
public class ClickHouseDbSupport extends DbSupport {

    public ClickHouseDbSupport(Connection connection) {
        super(new JdbcTemplate(connection));
    }

    @Override
    public Schema getSchema(String name) {
        return new ClickHouseSchema(getJdbcTemplate(), this, name);
    }

    @Override
    public SqlStatementBuilder createSqlStatementBuilder() {
        return new SqlStatementBuilder();
    }

    @Override
    public String getDbName() {
        return "clickhouse";
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return getJdbcTemplate().getConnection().getCatalog();
    }

    @Override
    protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
        getJdbcTemplate().getConnection().setCatalog(schema);
    }

    @Override
    public String getCurrentUserFunction() {
        // ClickHouse doesn't appear to have any concept of users
        return "null";
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
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
    protected String doQuote(String identifier) {
        return "`" + identifier + "`";
    }

    @Override
    public boolean catalogIsSchema() {
        return true;
    }
}
