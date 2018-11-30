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
package org.flywaydb.core.internal.command;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.ObjectUtils;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.core.internal.jdbc.TransactionTemplate;

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
     * The schema history table.
     */
    private final SchemaHistory schemaHistory;

    /**
     * The callback executor.
     */
    private final CallbackExecutor callbackExecutor;

    /**
     * The database-specific support.
     */
    private final Database database;

    /**
     * Creates a new DbRepair.
     *
     * @param database          The database-specific support.
     * @param migrationResolver The migration resolver.
     * @param schemaHistory     The schema history table.
     * @param callbackExecutor  The callback executor.
     */
    public DbRepair(Database database, MigrationResolver migrationResolver, SchemaHistory schemaHistory,
                    CallbackExecutor callbackExecutor, Configuration configuration) {
        this.database = database;
        this.connection = database.getMainConnection();
        this.migrationInfoService = new MigrationInfoServiceImpl(migrationResolver, schemaHistory, configuration,
                MigrationVersion.LATEST, true, true, true, true, true);
        this.schemaHistory = schemaHistory;
        this.callbackExecutor = callbackExecutor;
    }

    /**
     * Repairs the schema history table.
     */
    public void repair() {
        callbackExecutor.onEvent(Event.BEFORE_REPAIR);

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            boolean repaired = new TransactionTemplate(connection.getJdbcConnection()).execute(new Callable<Boolean>() {
                public Boolean call() {
                    schemaHistory.removeFailedMigrations();
                    migrationInfoService.refresh();

                    return alignAppliedMigrationsWithResolvedMigrations();
                }
            });

            stopWatch.stop();

            LOG.info("Successfully repaired schema history table " + schemaHistory + " (execution time "
                    + TimeFormat.format(stopWatch.getTotalTimeMillis()) + ").");
            if (repaired && !database.supportsDdlTransactions()) {
                LOG.info("Manual cleanup of the remaining effects the failed migration may still be required.");
            }
        } catch (FlywayException e) {
            callbackExecutor.onEvent(Event.AFTER_REPAIR_ERROR);
            throw e;
        }

        callbackExecutor.onEvent(Event.AFTER_REPAIR);
    }

    private boolean alignAppliedMigrationsWithResolvedMigrations() {
        boolean repaired = false;
        for (MigrationInfo migrationInfo : migrationInfoService.all()) {
            MigrationInfoImpl migrationInfoImpl = (MigrationInfoImpl) migrationInfo;

            ResolvedMigration resolved = migrationInfoImpl.getResolvedMigration();
            AppliedMigration applied = migrationInfoImpl.getAppliedMigration();
            if (resolved != null
                    && resolved.getVersion() != null
                    && applied != null
                    && !applied.getType().isSynthetic()



                    && updateNeeded(resolved, applied)) {
                schemaHistory.update(applied, resolved);
                repaired = true;
            }
        }
        return repaired;
    }

    private boolean updateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
        return checksumUpdateNeeded(resolved, applied)
        || descriptionUpdateNeeded(resolved, applied)
        || typeUpdateNeeded(resolved, applied);
    }

    private boolean checksumUpdateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
        return !ObjectUtils.nullSafeEquals(resolved.getChecksum(), applied.getChecksum());
    }

    private boolean descriptionUpdateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
        return !ObjectUtils.nullSafeEquals(resolved.getDescription(), applied.getDescription());
    }

    private boolean typeUpdateNeeded(ResolvedMigration resolved, AppliedMigration applied) {
        return !ObjectUtils.nullSafeEquals(resolved.getType(), applied.getType());
    }
}