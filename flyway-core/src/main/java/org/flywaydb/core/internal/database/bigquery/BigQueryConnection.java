/*
 * Copyright © Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.internal.database.bigquery;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;

import java.sql.SQLException;

/**
 * BigQuery connection.
 */
public class BigQueryConnection extends Connection<BigQueryDatabase> {
    BigQueryConnection(BigQueryDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    // FIXME Remove useless code
    private String getProjectID() throws SQLException {
        return jdbcTemplate.queryForString("SELECT @@project_id");
    }

    private String getASchemaName() throws SQLException {
        return jdbcTemplate.queryForString("SELECT schema_name FROM INFORMATION_SCHEMA.SCHEMATA LIMIT 1");
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        // BigQuery doesn't have a concept of current schema. We return a dataset (schema) name.
        return getASchemaName();
    }

    @Override
    public void changeCurrentSchemaTo(Schema schema) {
        try {
            if (schema.getName().equals(originalSchemaNameOrSearchPath) || originalSchemaNameOrSearchPath.startsWith(schema.getName() + ",") || !schema.exists()) {
                return;
            }

            doChangeCurrentSchemaOrSearchPathTo(schema.toString());
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        String sn = jdbcTemplate.queryForString(
                "SELECT schema_name FROM INFORMATION_SCHEMA.SCHEMATA WHERE schema_name=? LIMIT 1",
                schema
        );

        if (!StringUtils.hasText(sn)) {
            throw new SQLException("schema " + schema + " does not exist");
        }
    }

    @Override
    public Schema doGetCurrentSchema() throws SQLException {
        String schemaName = getCurrentSchemaNameOrSearchPath();

        if (!StringUtils.hasText(schemaName)) {
            throw new FlywayException("BigQuery does not have any dataset. Please create one.");
        }

        return getSchema(schemaName);
    }

    @Override
    public Schema getSchema(String name) {
        return new BigQuerySchema(jdbcTemplate, database, name);
    }
}