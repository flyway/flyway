/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport.firebird;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

/**
 * Firebird-specific support.
 */
public class FirebirdDbSupport extends DbSupport {

    private static final Log LOG = LogFactory.getLog(FirebirdDbSupport.class);

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public FirebirdDbSupport(Connection connection) {
        super(new FirebirdJdbcTemplate(connection, Types.VARCHAR));
    }

    @Override
    public String getDbName() {
        return "firebird";
    }

    @Override
    public String getCurrentUserFunction() {
        return "CURRENT_USER";
    }

    @Override
    protected String doGetCurrentSchema() throws SQLException {
        return "";
    }

    @Override
    protected void doSetCurrentSchema(Schema schema) throws SQLException {
        LOG.info("Firebird does not support schemas! Current schema NOT changed to " + schema);
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
    public SqlStatementBuilder createSqlStatementBuilder() {
        return new FirebirdSqlStatementBuilder();
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
        //return identifier;
    }

    @Override
    public Schema getSchema(String name) {
        return new FirebirdSchema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return true;
    }
}
