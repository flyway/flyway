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
package org.flywaydb.core.internal.database.sqlite;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Schema;

import java.sql.Types;

/**
 * SQLite connection.
 */
public class SQLiteConnection extends Connection<SQLiteDatabase> {
    private static final Log LOG = LogFactory.getLog(SQLiteConnection.class);

    /**
     * Whether the warning message has already been printed.
     */
    private static boolean schemaMessagePrinted;

    SQLiteConnection(FlywayConfiguration configuration, SQLiteDatabase database, java.sql.Connection connection



    ) {
        super(configuration, database, connection, Types.VARCHAR



        );
    }


    @Override
    public void doChangeCurrentSchemaTo(String schema) {
        if (!schemaMessagePrinted) {
            LOG.info("SQLite does not support setting the schema. Default schema NOT changed to " + schema);
            schemaMessagePrinted = true;
        }
    }

    @Override
    public Schema getSchema(String name) {
        return new SQLiteSchema(jdbcTemplate, database, name);
    }

    @Override
    protected String doGetCurrentSchemaName() {
        return "main";
    }
}