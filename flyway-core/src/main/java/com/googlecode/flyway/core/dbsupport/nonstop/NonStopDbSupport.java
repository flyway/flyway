/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.core.dbsupport.nonstop;

import com.googlecode.flyway.core.dbsupport.DbSupport;
import com.googlecode.flyway.core.dbsupport.JdbcTemplate;
import com.googlecode.flyway.core.dbsupport.Schema;
import com.googlecode.flyway.core.dbsupport.SqlStatementBuilder;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

import com.tandem.t4jdbc.SQLMXConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * NonStop-specific support.
 */
public class NonStopDbSupport extends DbSupport {
    private static final Log LOG = LogFactory.getLog(NonStopDbSupport.class);
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public NonStopDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.VARCHAR));
    }


    public String getScriptLocation() {
        return "com/googlecode/flyway/core/dbsupport/nonstop/";
    }

    public String getCurrentUserFunction() {
        return "CURRENT_USER";
    }

    @Override
    protected String doGetCurrentSchema() throws SQLException {
        String schema = "";
        Connection conn = jdbcTemplate.getConnection();
        LOG.info("Connection Class"+conn.getClass());
        if(conn instanceof SQLMXConnection){
            LOG.info("instane of SQLMXConnection");
            SQLMXConnection con = (SQLMXConnection)conn;
            schema = con.getSchema();
        }
        return schema;
    }

    @Override
    protected void doSetCurrentSchema(Schema schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + schema);
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    public String getBooleanTrue() {
        return "1";
    }

    public String getBooleanFalse() {
        return "0";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new NonStopSqlStatementBuilder();
    }

    @Override
    public String doQuote(String identifier) {
        //return "\"" + identifier + "\"";
        return "" + identifier + "";
    }

    @Override
    public Schema getSchema(String name) {
        return new NonStopSchema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }
}
