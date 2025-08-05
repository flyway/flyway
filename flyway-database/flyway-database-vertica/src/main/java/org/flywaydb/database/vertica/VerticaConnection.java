/*-
 * ========================LICENSE_START=================================
 * flyway-database-vertica
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.database.vertica;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;


public class VerticaConnection extends Connection<VerticaDatabase> {

    protected VerticaConnection(VerticaDatabase database, java.sql.Connection connection) {
        super(database, connection);
        this.jdbcTemplate = new VerticaJdbcTemplate(connection, database.getDatabaseType());
    }

    @Override
    protected void doRestoreOriginalState() throws SQLException {
        /* 
         * Setting the search path to default to avoid unexpected results:
         * https://docs.vertica.com/24.1.x/en/sql-reference/statements/set-statements/set-search-path/
         * 
         * Exepcted:
         *      "$user", public, v_catalog, v_monitor, v_internal, v_func
         */
        jdbcTemplate.execute("SET SEARCH_PATH TO DEFAULT");
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        /* 
         * Getting current existing schema name:
         * https://docs.vertica.com/24.1.x/en/admin/configuring-db/designing-logical-schema/using-multiple-schemas/setting-search-paths/#viewing-the-current-search-path
         * 
         * With the default search path: 
         *      "$user", public, v_catalog, v_monitor, v_internal, v_func
         * Exepcted:
         *      public
         */
        return jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA()");
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        if (schema.getName().equals(originalSchemaNameOrSearchPath) || !schema.exists()) {
            return;
        }

        try {
            doChangeCurrentSchemaOrSearchPathTo(schema.toString());
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        /* 
         * Setting the current via search path:
         * https://docs.vertica.com/24.1.x/en/sql-reference/statements/set-statements/set-search-path/
         * 
         * With the default path:
         *      "$user", public, v_catalog, v_monitor, v_internal, v_func
         * For schema "example" expected:
         *      example, v_catalog, v_monitor, v_internal, v_func
         */
        jdbcTemplate.execute("SET SEARCH_PATH TO " + schema);
    }

    @Override
    public Schema getSchema(String name) {
        return new VerticaSchema(jdbcTemplate, database, name);
    }
}
