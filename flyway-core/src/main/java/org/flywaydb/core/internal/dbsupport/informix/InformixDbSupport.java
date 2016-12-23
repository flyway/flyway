/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.core.internal.dbsupport.informix;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class InformixDbSupport extends DbSupport {

    private static boolean schemaMessagePrinted;
    private static final Log LOG = LogFactory.getLog(InformixDbSupport.class);

    public InformixDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.NULL));
    }

    @Override
    public Schema getSchema(String name) {
        return new InformixSchema(jdbcTemplate, this, name);
    }

    @Override
    public SqlStatementBuilder createSqlStatementBuilder() {
        return new InformixSqlStatementBuilder();
    }

    @Override
    public String getDbName() {
        return "informix";
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        DatabaseMetaData databaseMetaData = jdbcTemplate.getMetaData();
        String currentSchemaName = databaseMetaData.getUserName();
        return currentSchemaName;
        //return jdbcTemplate.queryForString("select scs_currdb from sysmaster:syssqlcurses");
    }

    @Override
    protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
        if (!schemaMessagePrinted) {
            LOG.info("Informix does not support setting the schema for the current session. Default schema NOT changed to " + schema);
            schemaMessagePrinted = true;
        }
    }

    @Override
    public String getCurrentUserFunction() {
        return "select user from systables where tabid = 1";
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    public String getBooleanTrue() {
        return "t";
    }

    @Override
    public String getBooleanFalse() {
        return "f";
    }

    @Override
    protected String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

}
