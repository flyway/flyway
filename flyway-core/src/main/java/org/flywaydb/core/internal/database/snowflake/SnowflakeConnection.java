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
package org.flywaydb.core.internal.database.snowflake;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;

/**
 * Snowflake implementation of a Flyway Connection.
 */
public class SnowflakeConnection extends Connection<SnowflakeDatabase> {

    private static final Log LOG = LogFactory.getLog(SnowflakeConnection.class);

    public SnowflakeConnection(Configuration configuration, SnowflakeDatabase database, java.sql.Connection connection, boolean originalAutoCommit) {
        super(configuration, database, connection, originalAutoCommit);
        LOG.debug("Creating new SnowflakeConnection");
    }

    //
    // NOTE: the following methods are overridden to provide an implementation
    //

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        String result = jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA()");
        LOG.debug("Current Snowflake schema is " + (result != null ? result : "none"));
        return result;
    }

    @Override
    public Schema getSchema(String name) {
        return new SnowflakeSchema(jdbcTemplate, database, name);
    }

    //
    // NOTE: the following methods are overridden to change the implementation
    //

    @Override
    protected void doChangeCurrentSchemaOrSearchPathTo(String schemaNameOrSearchPath) throws SQLException {
        LOG.debug("Switching to Snowflake schema " + schemaNameOrSearchPath);
        jdbcTemplate.execute("USE SCHEMA " + database.quote(schemaNameOrSearchPath));
    }
}
