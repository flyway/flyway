/*-
 * ========================LICENSE_START=================================
 * flyway-sqlserver
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.database.sqlserver.fabricDataWarehouse;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.flywaydb.database.sqlserver.SQLServerDatabase;
import org.flywaydb.database.sqlserver.SQLServerSchema;
import org.flywaydb.database.sqlserver.SQLServerTable;

/**
 * Synapse implementation of Schema.
 */
public class FabricDataWarehouseSchema extends SQLServerSchema {

    /**
     * Creates a new Synapse schema.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database The database-specific support.
     * @param databaseName The database name.
     * @param name The name of the schema.
     */
    FabricDataWarehouseSchema(JdbcTemplate jdbcTemplate, SQLServerDatabase database, String databaseName, String name) {
        super(jdbcTemplate, database, databaseName, name);
    }

    @Override
    protected FabricDataWarehouseTable[] doAllTables() throws SQLException {
        return queryDBObjects(ObjectType.USER_TABLE).stream()
            .map(table -> new FabricDataWarehouseTable(jdbcTemplate, database, databaseName, this, table.name))
            .toArray(FabricDataWarehouseTable[]::new);
    }

    @Override
    public Table getTable(String tableName) {
        return new FabricDataWarehouseTable(jdbcTemplate, database, databaseName, this, tableName);
    }

    @Override
    protected StringBuilder getObjectWithParentQuery() {
        return new StringBuilder("SELECT obj.object_id, obj.name FROM sys.objects AS obj " +
            "LEFT JOIN sys.extended_properties AS eps " +
            "ON obj.object_id = eps.major_id " +
            "AND eps.class = 1 " +    // Class 1 = objects and columns (we are only interested in objects).
            "AND eps.minor_id = 0 " + // Minor ID, always 0 for objects.
            "AND eps.name='microsoft_database_tools_support' " + // Select all objects generated from MS database tools.
            "WHERE SCHEMA_NAME(obj.schema_id) = '" + name + "' " +
            "AND eps.major_id IS NULL " + // Left Excluding JOIN (we are only interested in user defined entries).
            "AND obj.is_ms_shipped = 0 " + // Make sure we do not return anything MS shipped.
            "AND obj.type IN (" // Select the object types.
        );
    }
}
