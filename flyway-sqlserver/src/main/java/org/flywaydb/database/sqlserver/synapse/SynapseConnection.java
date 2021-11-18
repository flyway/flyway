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
package org.flywaydb.database.sqlserver.synapse;

import org.flywaydb.database.sqlserver.SQLServerConnection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;

import java.util.concurrent.Callable;

/**
 * Azure Synapse connection.
 */
public class SynapseConnection extends SQLServerConnection {

    SynapseConnection(SynapseDatabase database, java.sql.Connection connection) {
        super(database, connection);
    }

    @Override
    public Schema getSchema(String name) {
        return new SynapseSchema(jdbcTemplate, database, originalDatabaseName, name);
    }

    @Override
    public <T> T lock(Table table, Callable<T> callable) {
        return ExecutionTemplateFactory
                .createTableExclusiveExecutionTemplate(jdbcTemplate.getConnection(), table, database)
                .execute(callable);
    }

}