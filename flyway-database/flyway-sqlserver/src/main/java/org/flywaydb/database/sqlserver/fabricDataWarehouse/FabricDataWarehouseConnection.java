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

import static org.flywaydb.core.internal.logging.PreviewFeatureWarning.logPreviewFeature;

import java.util.concurrent.Callable;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.database.sqlserver.SQLServerConnection;

/**
 * Fabric Data Warehouse connection.
 */
public class FabricDataWarehouseConnection extends SQLServerConnection {

    FabricDataWarehouseConnection(FabricDataWarehouseDatabase database, java.sql.Connection connection) {
        super(database, connection);
        logPreviewFeature("Fabric Data Warehouse Support");
    }

    @Override
    public Schema getSchema(String name) {
        return new FabricDataWarehouseSchema(jdbcTemplate, database, originalDatabaseName, name);
    }

    @Override
    public <T> T lock(Table table, Callable<T> callable) {
        return ExecutionTemplateFactory
                .createTableExclusiveExecutionTemplate(jdbcTemplate.getConnection(), table, database)
                .execute(callable);
    }

}
