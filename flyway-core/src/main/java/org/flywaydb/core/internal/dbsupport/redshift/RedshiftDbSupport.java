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
package org.flywaydb.core.internal.dbsupport.redshift;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.FlywaySqlException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * PostgreSQL-specific support.
 */
public class RedshiftDbSupport extends DbSupport {
    /**
     * Checks whether this connection is pointing at a Redshift instance.
     *
     * @param connection The connection.
     * @return {@code true} if it is, {@code false} if not.
     */
    public static boolean isRedshift(Connection connection) {
        try {
            return new JdbcTemplate(connection).queryForString("SELECT version()").contains("Redshift");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public RedshiftDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.VARCHAR));
    }

    @Override
    protected final void ensureSupported() {
        // Always latest Redshift version.
    }

    public String getDbName() {
        return "redshift";
    }

    public String getCurrentUserFunction() {
        return "current_user";
    }

    @Override
    public Schema getOriginalSchema() {
        if (originalSchema == null) {
            return null;
        }

        return getSchema(getFirstSchemaFromSearchPath(this.originalSchema));
    }

    /* private -> testing */ String getFirstSchemaFromSearchPath(String searchPath) {
        String result = searchPath
                .replace("\"$user\"", "")
                .replace("$user", "").trim();
        if (result.startsWith(",")) {
            result = result.substring(1);
        }
        if (result.contains(",")) {
            result = result.substring(0, result.indexOf(","));
        }
        result = result.trim();
        // Unquote if necessary
        if (result.startsWith("\"") && result.endsWith("\"") && !result.endsWith("\\\"") && (result.length() > 1)) {
            result = result.substring(1, result.length() - 1);
        }
        return result;
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.queryForString("SHOW search_path");
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        try {
            if (schema.getName().equals(originalSchema) || originalSchema.startsWith(schema.getName() + ",") || !schema.exists()) {
                return;
            }

            if (StringUtils.hasText(originalSchema) && !"unset".equals(originalSchema)) {
                doChangeCurrentSchemaTo(schema.toString() + "," + originalSchema);
            } else {
                doChangeCurrentSchemaTo(schema.toString());
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
        if ("unset".equals(schema)) {
            schema = "";
        }
        jdbcTemplate.execute("SELECT set_config('search_path', ?, false)", schema);
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
        return new RedshiftSqlStatementBuilder();
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new RedshiftSchema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return false;
    }
}