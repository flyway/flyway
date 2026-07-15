/*-
 * ========================LICENSE_START=================================
 * flyway-starrocks
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
package org.flywaydb.database.starrocks;

import lombok.CustomLog;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;
import java.util.UUID;

/**
 * StarRocks-specific connection.
 * <p>
 * Key differences from MySQLConnection:
 * <ul>
 *   <li>Does NOT query {@code @@foreign_key_checks} — StarRocks does not support this variable</li>
 *   <li>Does NOT query {@code @@sql_safe_updates} — likewise unsupported</li>
 *   <li>Does NOT attempt GET_LOCK() for migration locking</li>
 * </ul>
 */
@CustomLog
public class StarRocksConnection extends Connection<StarRocksDatabase> {

    public StarRocksConnection(final StarRocksDatabase database, final java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected void doRestoreOriginalState() throws SQLException {
        // StarRocks does not support foreign_key_checks or sql_safe_updates,
        // so there is nothing to restore.
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("SELECT DATABASE()");
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(final String schema) throws SQLException {
        if (StringUtils.hasLength(schema)) {
            jdbcTemplate.getConnection().setCatalog(schema);
        } else {
            try {
                // Weird hack to switch back to no database selected...
                final String newDb = database.quote(UUID.randomUUID().toString());
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
        final String schemaName = getCurrentSchemaNameOrSearchPath();

        // #2206: MySQL and MariaDB (and StarRocks) can have URLs where no
        // current schema is set, so we must handle this case explicitly.
        return schemaName == null ? null : getSchema(schemaName);
    }

    @Override
    public Schema getSchema(final String name) {
        return new StarRocksSchema(jdbcTemplate, database, name);
    }
}
