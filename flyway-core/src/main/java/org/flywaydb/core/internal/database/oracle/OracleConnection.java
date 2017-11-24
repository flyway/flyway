/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.database.oracle;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Schema;

import java.sql.SQLException;

/**
 * Oracle connection.
 */
public class OracleConnection extends Connection<OracleDatabase> {
    OracleConnection(FlywayConfiguration configuration, OracleDatabase database, java.sql.Connection connection, int nullType



    ) {
        super(configuration, database, connection, nullType



        );
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        return jdbcTemplate.queryForString("SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') FROM DUAL");
    }

    @Override
    public void doChangeCurrentSchemaTo(String schema) throws SQLException {
        jdbcTemplate.execute("ALTER SESSION SET CURRENT_SCHEMA=" + database.quote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new OracleSchema(jdbcTemplate, database, name);
    }
}