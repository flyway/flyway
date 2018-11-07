/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.mysql;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * MySQL connection.
 */
public class MySQLConnection extends Connection<MySQLDatabase> {
    private static final Log LOG = LogFactory.getLog(MySQLConnection.class);

    MySQLConnection(Configuration configuration, MySQLDatabase database, java.sql.Connection connection
            , boolean originalAutoCommit



    ) {
        super(configuration, database, connection, originalAutoCommit



        );
    }

    @Override
    protected void doRestoreOriginalState() throws SQLException {
        if ((database.isMariaDB() && database.getVersion().isAtLeast("10.2"))
            || (!database.isMariaDB() && database.getVersion().isAtLeast("5.7"))) {

            // #2197: prevent user-defined variables from leaking beyond the scope of a migration
            String variablesQuery = database.isMariaDB()
                    ? "SELECT variable_name FROM information_schema.user_variables WHERE variable_value IS NOT NULL"
                    : "SELECT variable_name FROM performance_schema.user_variables_by_thread WHERE variable_value IS NOT NULL";
            List<String> userVariables = jdbcTemplate.queryForStringList(variablesQuery);
            if (!userVariables.isEmpty()) {
                boolean first = true;
                StringBuilder setStatement = new StringBuilder("SET ");
                for (String userVariable : userVariables) {
                    if (first) {
                        first = false;
                    } else {
                        setStatement.append(",");
                    }
                    setStatement.append("@").append(userVariable).append("=NULL");
                }
                jdbcTemplate.executeStatement(setStatement.toString());
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
    public Schema getSchema(String name) {
        return new MySQLSchema(jdbcTemplate, database, name);
    }

    @Override
    public <T> T lock(Table table, Callable<T> callable) {
        if (database.isPxcStrict()) {
            return super.lock(table, callable);
        }
        return new MySQLNamedLockTemplate(jdbcTemplate, table.toString().hashCode()).execute(callable);
    }
}