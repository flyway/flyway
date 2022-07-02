/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.database.exasol;

import java.sql.SQLException;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.util.StringUtils;

/**
 * @author artem
 * @date 14.09.2020
 * @time 16:26
 */
public class ExasolConnection extends Connection<ExasolDatabase> {

    protected ExasolConnection(final ExasolDatabase database, final java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA;");
    }

    @Override
    public ExasolSchema getSchema(final String name) {
        return new ExasolSchema(jdbcTemplate, database, name);
    }

    @Override
    public void changeCurrentSchemaTo(final Schema schema) {
        try {
            if (schemaAlreadySet(schema)) {
                return;
            }

            if (StringUtils.hasText(originalSchemaNameOrSearchPath)) {
                doChangeCurrentSchemaOrSearchPathTo(schema.toString() + "," + originalSchemaNameOrSearchPath);
            } else {
                doChangeCurrentSchemaOrSearchPathTo(schema.toString());
            }
        } catch (SQLException e) {
            throw new FlywaySqlException("Error setting current schema to " + schema, e);
        }
    }

    private boolean schemaAlreadySet(final Schema schema) {

        final String originalSchema
          = this.originalSchemaNameOrSearchPath == null ? "" : this.originalSchemaNameOrSearchPath;

        return schema.getName().equals(originalSchema)
          || originalSchema.startsWith(schema.getName() + ",")
          || !schema.exists();
    }

    @Override
    protected void doChangeCurrentSchemaOrSearchPathTo(final String schemaNameOrSearchPath) throws SQLException {

        try {
            jdbcTemplate.execute("OPEN SCHEMA " + schemaNameOrSearchPath);
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to change schema", e);
        }


    }
}
