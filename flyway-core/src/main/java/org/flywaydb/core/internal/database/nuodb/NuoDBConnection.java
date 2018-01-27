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
package org.flywaydb.core.internal.database.nuodb;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Schema;

import java.sql.SQLException;

/**
 * Oracle connection.
 */
public class NuoDBConnection extends Connection<NuoDBDatabase> {
	private static final Log LOGGER = LogFactory.getLog(NuoDBConnection.class);

    NuoDBConnection(FlywayConfiguration configuration, NuoDBDatabase database, java.sql.Connection connection, int nullType) {
        super(configuration, database, connection, nullType);
    }

    @Override
    protected String doGetCurrentSchemaName() throws SQLException {
        String currentSchema = jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA FROM DUAL");
        if ("".equals(currentSchema)) {
        	LOGGER.info("NuoDB current schema seem to be empty: using USERS");
        	currentSchema = "USER";
        }
        return currentSchema;
    }

    @Override
    public void doChangeCurrentSchemaTo(String schema) throws SQLException {
    	if ("".equals(schema)) {
    		LOGGER.info("NuoDB does not support setting the schema to empty string. Default schema NOT changed to " + schema);
    	} else {
    		jdbcTemplate.execute("USE " + database.quote(schema));
    	}
    }

    @Override
    public Schema getSchema(String name) {
        return new NuoDBSchema(jdbcTemplate, database, name);
    }
}