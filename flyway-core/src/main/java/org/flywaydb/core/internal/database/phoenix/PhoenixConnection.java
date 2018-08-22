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
package org.flywaydb.core.internal.database.phoenix;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;
import java.sql.Types;

public class PhoenixConnection extends Connection<PhoenixDatabase> {
    protected PhoenixConnection(Configuration configuration, PhoenixDatabase database, java.sql.Connection connection, boolean originalAutoCommit) {
        super(configuration, database, connection, originalAutoCommit, Types.NULL);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        String schemaName = getJdbcConnection().getSchema();
        if (schemaName == null) {
            schemaName = "";
        }
        return schemaName;
    }

    @Override
    public Schema getSchema(String name) {
        return new PhoenixSchema(jdbcTemplate, database, name);
    }

    @Override
    protected void doChangeCurrentSchemaOrSearchPathTo(String schemaNameOrSearchPath) throws SQLException {
        String schemaToUse;
        if (PhoenixSchema.isDefaultSchemaName(schemaNameOrSearchPath)) {
            schemaToUse = "DEFAULT";
        } else {
            schemaToUse = database.quote(schemaNameOrSearchPath);
        }
        jdbcTemplate.execute("USE " + schemaToUse);
    }

}
