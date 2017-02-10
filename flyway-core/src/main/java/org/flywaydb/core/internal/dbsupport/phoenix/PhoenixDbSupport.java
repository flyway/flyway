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
package org.flywaydb.core.internal.dbsupport.phoenix;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * H2 database specific support
 */
public class PhoenixDbSupport extends DbSupport {
    private static final Log LOG = LogFactory.getLog(PhoenixDbSupport.class);

    public PhoenixDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.VARCHAR));
    }

    public String getDbName() {
        return "phoenix";
    }

    // Don't return null Schema if original schema string is null
    @Override
    public Schema getOriginalSchema() {
        return getSchema(originalSchema);
    }

    // Support quoting when given a null identifier. This happens when Phoenix
    // has a null schema
    public String quote(String... identifiers) {
        String result = "";

        boolean first = true;
        boolean lastNull = false;
        for (String identifier : identifiers) {
            if (!first && !lastNull) {
                result += ".";
            }
            first = false;
            if(identifier == null) {
                lastNull = true;
            }
            else {
                result += doQuote(identifier);
                lastNull = false;
            }
        }

        return result;
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return null;
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        LOG.info("Phoenix does not support setting the schema. Default schema NOT changed to " + schema);
    }

    @Override
    protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
        LOG.info("Phoenix does not support setting the schema. Default schema NOT changed to " + schema);
    }

    @Override
    public String getCurrentUserFunction()  {
        try {
            return jdbcTemplate.getMetaData().getUserName();
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public boolean supportsDdlTransactions() {
        return false;
    }

    @Override
    public String getBooleanTrue() {
        return "TRUE";
    }

    @Override
    public String getBooleanFalse() {
        return "FALSE";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new SqlStatementBuilder();
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new PhoenixSchema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }
}