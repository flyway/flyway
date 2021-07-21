/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.internal.database.h2;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;

public class H2Connection extends Connection<H2Database> {
    private final boolean requiresV2Metadata;

    H2Connection(H2Database database, java.sql.Connection connection, boolean requiresV2Metadata) {
        super(database, connection);
        this.requiresV2Metadata = requiresV2Metadata;
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + database.quote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new H2Schema(jdbcTemplate, database, name, requiresV2Metadata);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("CALL SCHEMA()");
    }
}