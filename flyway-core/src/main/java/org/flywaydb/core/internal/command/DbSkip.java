/*
 * Copyright 2010-2019 Boxfuse GmbH
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
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;

/**
 * Handles Flyway's skip command.
 */
public class DbSkip {
    private static final Log LOG = LogFactory.getLog(DbSkip.class);
    /**
     * Database-specific functionality.
     */
    private final Database database;

    /**
     * The database schema history table.
     */
    private final SchemaHistory schemaHistory;

    /**
     * The schema containing the schema history table.
     */
    private final Schema schema;

    /**
     * The migration resolver.
     */
    private final MigrationResolver migrationResolver;

    /**
     * The Flyway configuration.
     */
    private final Configuration configuration;

    /**
     * The callback executor.
     */
    private final CallbackExecutor callbackExecutor;

    /**
     * The connection to use to perform the actual database migrations.
     */
    private final Connection connectionUserObjects;

    /**
     * Creates a new migration skipper.
     *
     * @param database          Database-specific functionality.
     * @param schemaHistory     The database schema history table.
     * @param migrationResolver The migration resolver.
     * @param configuration     The Flyway configuration.
     * @param callbackExecutor  The callbacks executor.
     */
    public DbSkip(Database database,
                     SchemaHistory schemaHistory, Schema schema, MigrationResolver migrationResolver,
                     Configuration configuration, CallbackExecutor callbackExecutor) {
        this.database = database;
        this.connectionUserObjects = database.getMigrationConnection();
        this.schemaHistory = schemaHistory;
        this.schema = schema;
        this.migrationResolver = migrationResolver;
        this.configuration = configuration;
        this.callbackExecutor = callbackExecutor;
    }

    public void skip() {
        callbackExecutor.onEvent(Event.BEFORE_SKIP);

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            MigrationInfoServiceImpl infoService =
                    new MigrationInfoServiceImpl(migrationResolver, schemaHistory, configuration,
                            configuration.getTarget(), configuration.isOutOfOrder(),
                            true, true, true, true);
            infoService.refresh();

            MigrationInfo[] pendingMigrations = infoService.pending();
            int skippedMigrations = 0;
            for (MigrationInfo migrationInfo : pendingMigrations) {
                if (shouldSkip(configuration, migrationInfo)) {
                    LOG.info("Skipping version " +
                            migrationInfo.getVersion().getVersion() +
                            " - " +
                            migrationInfo.getDescription()
                     );
                    schemaHistory.skipMigration(
                            migrationInfo.getVersion(),
                            migrationInfo.getDescription(),
                            migrationInfo.getScript(),
                            migrationInfo.getChecksum());
                    skippedMigrations++;
                }
            }

            stopWatch.stop();
            String executionTime = TimeFormat.format(stopWatch.getTotalTimeMillis());

            if (skippedMigrations == 0) {
                LOG.info("No migrations to skip");
            }
            else {
                LOG.info("Successfully skipped " + skippedMigrations + " migrations.\n(execution time " + executionTime + ").");
            }

        } catch (FlywayException e) {
            callbackExecutor.onEvent(Event.AFTER_SKIP_ERROR);
            throw e;
        }

        callbackExecutor.onEvent(Event.AFTER_SKIP);
    }

    private boolean shouldSkip(Configuration configuration, MigrationInfo migrationInfo) {
        String[] skipVersions = configuration.getSkipVersions();
        MigrationVersion migrationVersion = migrationInfo.getVersion();

        if (migrationVersion == null) {
            return false;
        }

        if (skipVersions.length == 0) {
            return true;
        }

        for (String skipVersion : skipVersions) {
            if (skipVersion.equals(migrationVersion.getVersion())) {
                return true;
            }
        }

        return false;
    }
}