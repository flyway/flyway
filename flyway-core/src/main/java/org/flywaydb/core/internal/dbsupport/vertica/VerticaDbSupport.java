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
package org.flywaydb.core.internal.dbsupport.vertica;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.RowMapper;

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
    protected String doGetCurrentSchema() throws SQLException {
        return jdbcTemplate.queryForString("SELECT current_schema()");
    }

    @Override
    protected void doSetCurrentSchema(Schema schema) throws SQLException {
        if (schema == null) {
            jdbcTemplate.execute("SET search_path = v_catalog");
            return;
        }

        String searchPath = doGetSearchPath();
        if (StringUtils.hasText(searchPath)) {
            jdbcTemplate.execute("SET search_path = " + schema + "," + searchPath);
        } else {
            jdbcTemplate.execute("SET search_path = " + schema);
        }
    }

    String doGetSearchPath() throws SQLException {
        String searchPath = jdbcTemplate.query("SHOW search_path", new RowMapper<String>() {
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
        return searchPath;
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
