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
package org.flywaydb.core.internal.database.h2;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Schema;

import java.sql.SQLException;
import java.sql.Types;

/**
 * H2 connection.
 */
public class H2Connection extends Connection<H2Database> {
    H2Connection(Configuration configuration, H2Database database, java.sql.Connection connection



    ) {
        super(configuration, database, connection, Types.VARCHAR



        );
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + database.quote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new H2Schema(jdbcTemplate, database, name);
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("CALL SCHEMA()");
    }
}