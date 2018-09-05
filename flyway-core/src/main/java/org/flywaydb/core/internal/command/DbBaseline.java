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
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.schemahistory.AppliedMigration;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.jdbc.TransactionTemplate;

import java.util.concurrent.Callable;

/**
 * Handles Flyway's baseline command.
 */
public class DbBaseline {
    private static final Log LOG = LogFactory.getLog(DbBaseline.class);

    /**
     * The database connection to use for accessing the schema history table.
     */
    private final Connection connection;

    /**
     * The schema history table.
     */
    private final SchemaHistory schemaHistory;

    /**
     * The version to tag an existing schema with when executing baseline.
     */
    private final MigrationVersion baselineVersion;

    /**
     * The description to tag an existing schema with when executing baseline.
     */
    private final String baselineDescription;

    /**
     * The callback executor.
     */
    private final CallbackExecutor callbackExecutor;

    /**
     * The schema containing the schema history table.
     */
    private final Schema schema;

    /**
     * Creates a new DbBaseline.
     *
     * @param database            The database to use.
     * @param schemaHistory       The database schema history table.
     * @param schema              The database schema to use by default.
     * @param baselineVersion     The version to tag an existing schema with when executing baseline.
     * @param baselineDescription The description to tag an existing schema with when executing baseline.
     * @param callbackExecutor    The callback executor.
     */
    public DbBaseline(Database database, SchemaHistory schemaHistory, Schema schema, MigrationVersion baselineVersion,
                      String baselineDescription, CallbackExecutor callbackExecutor) {
        this.connection = database.getMainConnection();
        this.schemaHistory = schemaHistory;
        this.schema = schema;
        this.baselineVersion = baselineVersion;
        this.baselineDescription = baselineDescription;
        this.callbackExecutor = callbackExecutor;
    }

    /**
     * Baselines the database.
     */
    public void baseline() {
        callbackExecutor.onEvent(Event.BEFORE_BASELINE);

        try {
            schemaHistory.create();
            new TransactionTemplate(connection.getJdbcConnection()).execute(new Callable<Object>() {
                @Override
                public Void call() {
                    connection.restoreOriginalState();
                    connection.changeCurrentSchemaTo(schema);
                    AppliedMigration baselineMarker = schemaHistory.getBaselineMarker();
                    if (baselineMarker != null) {
                        if (baselineVersion.equals(baselineMarker.getVersion())
                                && baselineDescription.equals(baselineMarker.getDescription())) {
                            LOG.info("Schema history table " + schemaHistory + " already initialized with ("
                                    + baselineVersion + "," + baselineDescription + "). Skipping.");
                            return null;
                        }
                        throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " with ("
                                + baselineVersion + "," + baselineDescription
                                + ") as it has already been initialized with ("
                                + baselineMarker.getVersion() + "," + baselineMarker.getDescription() + ")");
                    }
                    if (schemaHistory.hasSchemasMarker() && baselineVersion.equals(MigrationVersion.fromVersion("0"))) {
                        throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " with version 0 as this version was used for schema creation");
                    }
                    if (schemaHistory.hasNonSyntheticAppliedMigrations()) {
                        throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " as it already contains migrations");
                    }
                    schemaHistory.addBaselineMarker(baselineVersion, baselineDescription);

                    return null;
                }
            });
        } catch (FlywayException e) {
            callbackExecutor.onEvent(Event.AFTER_BASELINE_ERROR);
            throw e;
        }

        LOG.info("Successfully baselined schema with version: " + baselineVersion);

        callbackExecutor.onEvent(Event.AFTER_BASELINE);
    }
}