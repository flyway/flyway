/*-
 * ========================LICENSE_START=================================
 * flyway-database-databricks
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.database.databricks;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;

public class DatabricksConnection extends Connection<DatabricksDatabase> {
    protected DatabricksConnection(final DatabricksDatabase database, final java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        final String defaultSchema = "default";
        final String currentSchema = jdbcTemplate.queryForString("SELECT current_schema();");
        return (currentSchema != null) ? currentSchema : defaultSchema;
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(final String schema) throws SQLException {
        final String sql = "USE SCHEMA " + database.doQuote(schema) + ";";
        jdbcTemplate.execute(sql);
    }

    @Override
    public Schema doGetCurrentSchema() throws SQLException {
        final String currentSchema = getCurrentSchemaNameOrSearchPath();

        if (!StringUtils.hasText(currentSchema)) {
            throw new FlywayException("Unable to determine current schema as currentSchema is empty.");
        }

        return getSchema(currentSchema);
    }

    @Override
    public Schema getSchema(final String name) {
        return new DatabricksSchema(jdbcTemplate, database, name);
    }
}
