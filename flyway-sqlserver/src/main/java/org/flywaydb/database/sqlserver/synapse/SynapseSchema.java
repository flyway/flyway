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

import org.flywaydb.database.sqlserver.SQLServerDatabase;
import org.flywaydb.database.sqlserver.SQLServerSchema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Synapse implementation of Schema.
 */
public class SynapseSchema extends SQLServerSchema {

    /**
     * Creates a new Synapse schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param databaseName The database name.
     * @param name         The name of the schema.
     */
    SynapseSchema(JdbcTemplate jdbcTemplate, SQLServerDatabase database, String databaseName, String name) {
        super(jdbcTemplate, database, databaseName, name);
    }

    @Override
    protected SynapseTable[] doAllTables() throws SQLException {
        List<String> tableNames = new ArrayList<>();
        for (DBObject table : queryDBObjects(ObjectType.USER_TABLE)) {
            tableNames.add(table.name);
        }

        SynapseTable[] tables = new SynapseTable[tableNames.size()];
        for (int i = 0; i < tableNames.size(); i++) {
            tables[i] = new SynapseTable(jdbcTemplate, database, databaseName, this, tableNames.get(i));
        }
        return tables;
    }

    @Override
    public Table getTable(String tableName) {
        return new SynapseTable(jdbcTemplate, database, databaseName, this, tableName);
    }
}