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
package org.flywaydb.core.internal.dbsupport.vertica;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.FlywaySqlException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.RowMapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Vertica-specific support.
 */
public class VerticaDbSupport extends DbSupport {
    /**
     * Creates a new instance.
     *
     * @param connection The connection to use.
     */
    public VerticaDbSupport(Connection connection) {
        super(new JdbcTemplate(connection, Types.NULL));
    }

    public String getDbName() {
        return "vertica";
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
            return getSchema(result.substring(0, result.indexOf(",")));
        }
        return getSchema(result);
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.query("SHOW search_path", new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs) throws SQLException {
                // All "show" commands in Vertica return two columns: "name" and "setting",
                // but we just want the value in the second column:
                //
                //     name     |                      setting
                // -------------+---------------------------------------------------
                // search_path | "$user", public, v_catalog, v_monitor, v_internal
                return rs.getString("setting");
            }

        }).get(0);
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
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    protected void doChangeCurrentSchemaTo(String schema) throws SQLException {
        if (!StringUtils.hasLength(schema)) {
            jdbcTemplate.execute("SET search_path = v_catalog");
            return;
        }
        jdbcTemplate.execute("SET search_path = " + schema);
    }

    public boolean supportsDdlTransactions() {
        return false;
    }

    public String getBooleanTrue() {
        return "TRUE";
    }

    public String getBooleanFalse() {
        return "FALSE";
    }

    public SqlStatementBuilder createSqlStatementBuilder() {
        return new VerticaStatementBuilder();
    }

    @Override
    public String doQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
    }

    @Override
    public Schema getSchema(String name) {
        return new VerticaSchema(jdbcTemplate, this, name);
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

}
