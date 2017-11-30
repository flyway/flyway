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

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.database.Connection;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.ObjectUtils;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Handles Flyway's repair command.
 */
public class DbRepair {
    private static final Log LOG = LogFactory.getLog(DbRepair.class);

    /**
     * The database connection to use for accessing the schema history table.
     */
    private final Connection connection;

    /**
     * The migration infos.
     */
    private final MigrationInfoServiceImpl migrationInfoService;

    /**
     * The schema containing the schema history table.
     */
    private final Schema schema;

    /**
     * The schema history table.
     */
    private final SchemaHistory schemaHistory;

    /**
     * This is a list of callbacks that fire before or after the repair task is executed.
     * You can add as many callbacks as you want.  These should be set on the Flyway class
     * by the end user as Flyway will set them automatically for you here.
     */
    private final List<FlywayCallback> callbacks;

    /**
     * The database-specific support.
     */
    private final Database database;

    /**
     * Creates a new DbRepair.
     *
     * @param database          The database-specific support.
     * @param schema            The database schema to use by default.
     * @param migrationResolver The migration resolver.
     * @param schemaHistory     The schema history table.
     * @param callbacks         Callbacks for the Flyway lifecycle.
     */
    public DbRepair(Database database, Schema schema, MigrationResolver migrationResolver, SchemaHistory schemaHistory,
                    List<FlywayCallback> callbacks) {
        this.database = database;
        this.connection = database.getMainConnection();
        this.schema = schema;
        this.migrationInfoService = new MigrationInfoServiceImpl(migrationResolver, schemaHistory, MigrationVersion.LATEST, true, true, true, true);
        this.schemaHistory = schemaHistory;
        this.callbacks = callbacks;
    }

    /**
     * Repairs the schema history table.
     */
    public void repair() {
        try {
            for (final FlywayCallback callback : callbacks) {
                new TransactionTemplate(connection.getJdbcConnection()).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        connection.changeCurrentSchemaTo(schema);
                        callback.beforeRepair(connection.getJdbcConnection());
                        return null;
                    }
                });
            }

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            new TransactionTemplate(connection.getJdbcConnection()).execute(new Callable<Object>() {
                public Void call() {
                    connection.changeCurrentSchemaTo(schema);
                    schemaHistory.removeFailedMigrations();
                    alignAppliedMigrationsWithResolvedMigrations();
                    return null;
                }
            });

            stopWatch.stop();

            LOG.info("Successfully repaired schema history table " + schemaHistory + " (execution time "
                    + TimeFormat.format(stopWatch.getTotalTimeMillis()) + ").");
            if (!database.supportsDdlTransactions()) {
                LOG.info("Manual cleanup of the remaining effects the failed migration may still be required.");
            }

            for (final FlywayCallback callback : callbacks) {
                new TransactionTemplate(connection.getJdbcConnection()).execute(new Callable<Object>() {
                    @Override
                    public Object call() throws SQLException {
                        connection.changeCurrentSchemaTo(schema);
                        callback.afterRepair(connection.getJdbcConnection());
                        return null;
                    }
                });
            }
        } finally {
            connection.restoreCurrentSchema();
        }
    }

    private void alignAppliedMigrationsWithResolvedMigrations() {
        migrationInfoService.refresh();
        for (MigrationInfo migrationInfo : migrationInfoService.all()) {
            MigrationInfoImpl migrationInfoImpl = (MigrationInfoImpl) migrationInfo;

            ResolvedMigration resolved = migrationInfoImpl.getResolvedMigration();
            AppliedMigration applied = migrationInfoImpl.getAppliedMigration();
            if (resolved != null && applied != null && resolved.getVersion() != null
                    && (checksumUpdateNeeded(resolved, applied)
                    || descriptionUpdateNeeded(resolved, applied)
                    || typeUpdateNeeded(resolved, applied))) {
                schemaHistory.update(applied, resolved);
            }
        }
    }

    private boolean checksumUpdateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
        return !ObjectUtils.nullSafeEquals(resolved.getChecksum(), applied.getChecksum());
    }

    private boolean descriptionUpdateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
        return !ObjectUtils.nullSafeEquals(resolved.getDescription(), applied.getDescription());
    }

    private boolean typeUpdateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
        return !ObjectUtils.nullSafeEquals(resolved.getType(), applied.getType())
                && applied.getType().isSynthetic();
    }
}
