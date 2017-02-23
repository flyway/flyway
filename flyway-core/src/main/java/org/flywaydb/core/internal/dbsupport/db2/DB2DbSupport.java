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
package org.flywaydb.core.internal.dbsupport.db2;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.FlywaySqlException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * DB2 Support.
 */
public class DB2DbSupport extends DbSupport {
    /**
     * The major version of DB2. (9, 10, ...)
     */
    private final int majorVersion;

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public DB2DbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.VARCHAR));
        try {
            majorVersion = connection.getMetaData().getDatabaseMajorVersion();
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine DB2 major version", e);
        }
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new DB2SqlStatementBuilder();
    }

    public String getDbName() {
        return "db2";
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.queryForString("select current_schema from sysibm.sysdummy1");
    }

    @Override
    protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + quote(schema));
    }

    public String getCurrentUserFunction() {
        return "CURRENT_USER";
    }

    public boolean supportsDdlTransactions() {
        return true;
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new DB2Schema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    /**
     * @return The major version of DB2. (9, 10, ...)
     */
    public int getDb2MajorVersion() {
        return majorVersion;
    }
}
