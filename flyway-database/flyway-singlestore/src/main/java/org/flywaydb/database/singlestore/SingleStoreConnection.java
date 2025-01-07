/*-
 * ========================LICENSE_START=================================
 * flyway-singlestore
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
package org.flywaydb.database.singlestore;

import lombok.CustomLog;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@CustomLog
public class SingleStoreConnection extends Connection<SingleStoreDatabase> {

    private static final String USER_VARIABLES_TABLE = "information_schema.user_variables";
    private static final String USER_VARIABLES_QUERY = "SELECT variable_name FROM "
            + USER_VARIABLES_TABLE
            + " WHERE variable_value IS NOT NULL";
    private final boolean canResetUserVariables;

    public SingleStoreConnection(SingleStoreDatabase database, java.sql.Connection connection) {
        super(database, connection);
        canResetUserVariables = hasUserVariableResetCapability();
    }

    // ensure the database is recent enough and the current user has the necessary SELECT grant
    private boolean hasUserVariableResetCapability() {
        try {
            jdbcTemplate.queryForStringList(USER_VARIABLES_QUERY);
        } catch (SQLException e) {
            LOG.debug("Disabled user variable reset as "
                    + USER_VARIABLES_TABLE
                    + " cannot be queried (SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode() + ")");
            return false;
        }
        return true;
    }

    @Override
    protected void doRestoreOriginalState() throws SQLException {
        // prevent user-defined variables from leaking beyond the scope of a migration
        if (canResetUserVariables) {
            List<String> userVariables = jdbcTemplate.queryForStringList(USER_VARIABLES_QUERY);
            if (!userVariables.isEmpty()) {
                String nulls = String.join(",", Collections.nCopies(userVariables.size(), "NULL"));
                String variables = String.join(",", userVariables);
                jdbcTemplate.executeStatement("SELECT " + nulls + " INTO " + variables);
            }
        }
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("SELECT DATABASE()");
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        if (StringUtils.hasLength(schema)) {
            jdbcTemplate.getConnection().setCatalog(schema);
        } else {
            try {
                // Weird hack to switch back to no database selected...
                String newDb = database.quote(UUID.randomUUID().toString());
                jdbcTemplate.execute("CREATE SCHEMA " + newDb);
                jdbcTemplate.execute("USE " + newDb);
                jdbcTemplate.execute("DROP SCHEMA " + newDb);
            } catch (Exception e) {
                LOG.warn("Unable to restore connection to having no default schema: " + e.getMessage());
            }
        }
    }

    @Override
    protected Schema doGetCurrentSchema() throws SQLException {
        String schemaName = getCurrentSchemaNameOrSearchPath();
        // SingleStore can have URLs where no current schema is set, so we must handle this case explicitly.
        return schemaName == null ? null : getSchema(schemaName);
    }

    @Override
    public Schema getSchema(String name) {
        return new SingleStoreSchema(jdbcTemplate, database, name);
    }
}
