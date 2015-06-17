/**
 * Copyright 2010-2015 Axel Fontaine
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
package org.flywaydb.core.internal.dbsupport.db2as400;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * DB2 Support.
 */
public class DB2AS400DbSupport extends DbSupport {
    private static final Log LOG = LogFactory.getLog(DB2AS400DbSupport.class);
            /**
             * The major version of DB2. (9, 10, ...)
             */
    private final int majorVersion;

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public DB2AS400DbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.VARCHAR));
        try {
            majorVersion = connection.getMetaData().getDatabaseMajorVersion();
        } catch (SQLException e) {
            throw new FlywayException("Unable to determine DB2 major version", e);
        }
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new DB2SqlStatementBuilder();
    }

    public String getDbName() {
        return "db2as400";
    }

    @Override
    protected String doGetCurrentSchema() throws SQLException {
        final String result = jdbcTemplate.queryForString("select current schema from sysibm.sysdummy1");
        LOG.debug(String.format("doGetCurrentSchema=%s", result));
        return result;
    }

    @Override
    protected void doSetCurrentSchema(Schema schema) throws SQLException {
        LOG.debug(String.format("doSetCurrentSchema:%s", schema.getName()));
        if(!schema.getName().contains("*")) {
            jdbcTemplate.execute("SET CURRENT SCHEMA " + schema.getName());
        } else {
            LOG.warn(String.format("Cannot set SCHEMA to %s", schema.getName()));
        }
    }

    public String getCurrentUserFunction() {
        return "USER";
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
        LOG.debug(String.format("getSchema:%s", name));
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
        LOG.debug(String.format("getDb2MajorVersion:%d", majorVersion));
        return majorVersion;
    }
}
