/*
 * Copyright © Red Gate Software Ltd 2010-2020
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
package org.flywaydb.core.internal.database.cockroachdb;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;

/**
 * CockroachDB connection.
 */
public class CockroachDBConnection extends Connection<CockroachDBDatabase> {
    CockroachDBConnection(CockroachDBDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    public Schema getSchema(String name) {
        return new CockroachDBSchema(jdbcTemplate, database, name);
    }

    @Override
    public Schema doGetCurrentSchema() throws SQLException {
        if (database.supportsSchemas()) {
            String currentSchema = jdbcTemplate.queryForString("SELECT current_schema");
            if (StringUtils.hasText(currentSchema)) {
                return getSchema(currentSchema);
            }

            String searchPath = getCurrentSchemaNameOrSearchPath();
            if (!StringUtils.hasText(searchPath)) {
                throw new FlywayException("Unable to determine current schema as search_path is empty. Set the current schema in currentSchema parameter of the JDBC URL or in Flyway's schemas property.");
            }
        }
        return super.doGetCurrentSchema();
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        if (database.supportsSchemas()) {
            return jdbcTemplate.queryForString("SHOW search_path");
        } else {
            return jdbcTemplate.queryForString("SHOW database");
        }
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        try {
            // Avoid unnecessary schema changes as this trips up CockroachDB
            if (schema.getName().equals(originalSchemaNameOrSearchPath) || !schema.exists()) {
                return;
            }
            doChangeCurrentSchemaOrSearchPathTo(schema.getName());
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        if (database.supportsSchemas()) {
            if (!StringUtils.hasLength(schema)) {
                schema = "public";
            }
            jdbcTemplate.execute("SET search_path = " + schema);
        } else {
            if (!StringUtils.hasLength(schema)) {
                schema = "DEFAULT";
            }
            jdbcTemplate.execute("SET database = " + schema);
        }
    }
}