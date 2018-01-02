/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
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
