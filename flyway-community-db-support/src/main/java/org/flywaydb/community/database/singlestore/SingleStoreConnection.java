/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.community.database.singlestore;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class SingleStoreConnection extends Connection<SingleStoreDatabase> {
    private static final Log LOG = LogFactory.getLog(SingleStoreConnection.class);

    private static final String USER_VARIABLES_TABLE = "information_schema.user_variables";

    private final String userVariablesQuery = "SELECT variable_name FROM "
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
            jdbcTemplate.queryForStringList(userVariablesQuery);
            return true;
        } catch (SQLException e) {
            LOG.debug("Disabled user variable reset as "
                    + USER_VARIABLES_TABLE
                    + "cannot be queried (SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode() + ")");
            return false;
        }
    }

    @Override
    protected void doRestoreOriginalState() throws SQLException {
        // prevent user-defined variables from leaking beyond the scope of a migration
        if (canResetUserVariables) {
            List<String> userVariables = jdbcTemplate.queryForStringList(userVariablesQuery);
            if (!userVariables.isEmpty()) {
                boolean first = true;
                StringBuilder nulls = new StringBuilder();
                StringBuilder variables = new StringBuilder();

                for (String variable : userVariables) {
                    if (first) {
                        first = false;
                    } else {
                        nulls.append(",");
                        variables.append(",");
                    }
                    nulls.append("NULL");
                    variables.append(variable);
                }

                jdbcTemplate.executeStatement("SELECT " + nulls.toString() + " INTO " + variables.toString());
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
