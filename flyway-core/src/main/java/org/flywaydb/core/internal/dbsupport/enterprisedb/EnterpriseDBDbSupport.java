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
package org.flywaydb.core.internal.dbsupport.enterprisedb;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Oracle-specific support.
 */
public class EnterpriseDBDbSupport extends DbSupport {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public EnterpriseDBDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.NULL));
    }

    public String getDbName() {
        return "enterprisedb";
    }

    public String getCurrentUserFunction() {
        return "current_user";
    }

    @Override
    public Schema getOriginalSchema() {
        if (originalSchema == null) {
            return null;
        }

        String result = originalSchema.replace(doQuote("$user"), "").trim();
        if (result.startsWith(",")) {
            result = result.substring(1);
        }
        if (result.contains(",")) {
            result = result.substring(0, result.indexOf(","));
        }
        return getSchema(result.trim());
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.queryForString("SHOW search_path");
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        if (schema.getName().equals(originalSchema) || originalSchema.startsWith(schema.getName() + ",") || !schema.exists()) {
            return;
        }

        try {
            if (StringUtils.hasText(originalSchema)) {
                doChangeCurrentSchemaTo(schema.toString() + "," + originalSchema);
            } else {
                doChangeCurrentSchemaTo(schema.toString());
            }
        } catch (SQLException e) {
            throw new FlywayException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
        if (!StringUtils.hasLength(schema)) {
            jdbcTemplate.execute("SELECT set_config('search_path', '', false)");
            return;
        }
        jdbcTemplate.execute("SET search_path = " + schema);
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
        return new EnterpriseDBSqlStatementBuilder();
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new EnterpriseDBSchema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public void executePgCopy(Connection connection, String sql) throws SQLException {
        int split = sql.indexOf(";");
        String statement = sql.substring(0, split);
        String data = sql.substring(split + 1).trim();

        CopyManager copyManager = new CopyManager(connection.unwrap(BaseConnection.class));
        try {
            copyManager.copyIn(statement, new StringReader(data));
        } catch (IOException e) {
            throw new SQLException("Unable to execute COPY operation", e);
        }
    }
}
