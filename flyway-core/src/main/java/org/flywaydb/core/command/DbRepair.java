/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package org.flywaydb.core.command;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.metadatatable.AppliedMigration;
import org.flywaydb.core.metadatatable.MetaDataTable;
import org.flywaydb.core.util.jdbc.TransactionCallback;
import org.flywaydb.core.util.jdbc.TransactionTemplate;
import org.flywaydb.core.util.logging.Log;
import org.flywaydb.core.util.logging.LogFactory;

import java.sql.Connection;

/**
 * Handles Flyway's repair command.
 */
public class DbRepair {
    /**
     * The database connection to use for accessing the metadata table.
     */
    private final Connection connection;

    /**
     * The metadata table.
     */
    private final MetaDataTable metaDataTable;

    /**
     * Creates a new DbRepair.
     *
     * @param connection      The database connection to use for accessing the metadata table.
     * @param metaDataTable   The metadata table.
     */
    public DbRepair(Connection connection, MetaDataTable metaDataTable) {
        this.connection = connection;
        this.metaDataTable = metaDataTable;
    }

    /**
     * Repairs the metadata table.
     */
    public void repair() {
        new TransactionTemplate(connection).execute(new TransactionCallback<Void>() {
            public Void doInTransaction() {
                metaDataTable.repair();
                return null;
            }
        });
    }
}
