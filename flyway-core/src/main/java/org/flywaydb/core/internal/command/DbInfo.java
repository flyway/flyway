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
package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

public class DbInfo {
    private final MigrationResolver migrationResolver;
    private final SchemaHistory schemaHistory;
    private final Connection connection;
    private final FlywayConfiguration configuration;
    private final Schema[] schemas;
    private final List<FlywayCallback> effectiveCallbacks;

    public DbInfo(MigrationResolver migrationResolver, SchemaHistory schemaHistory,
                  final Database database, FlywayConfiguration configuration, Schema[] schemas, List<FlywayCallback> effectiveCallbacks) {

        this.migrationResolver = migrationResolver;
        this.schemaHistory = schemaHistory;
        this.connection = database.getMainConnection();
        this.configuration = configuration;
        this.schemas = schemas;
        this.effectiveCallbacks = effectiveCallbacks;
    }

    public MigrationInfoService info() {
        try {
            for (final FlywayCallback callback : effectiveCallbacks) {
                new TransactionTemplate(connection.getJdbcConnection()).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        connection.changeCurrentSchemaTo(schemas[0]);
                        callback.beforeInfo(connection.getJdbcConnection());
                        return null;
                    }
                });
            }

            MigrationInfoServiceImpl migrationInfoService =
                    new MigrationInfoServiceImpl(migrationResolver, schemaHistory, configuration.getTarget(),
                            configuration.isOutOfOrder(), true, true, true);
            migrationInfoService.refresh();

            for (final FlywayCallback callback : effectiveCallbacks) {
                new TransactionTemplate(connection.getJdbcConnection()).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        connection.changeCurrentSchemaTo(schemas[0]);
                        callback.afterInfo(connection.getJdbcConnection());
                        return null;
                    }
                });
            }

            return migrationInfoService;
        } finally {
            connection.restoreCurrentSchema();
        }
    }
}