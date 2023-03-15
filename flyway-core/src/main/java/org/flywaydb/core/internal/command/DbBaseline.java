/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.output.BaselineResult;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.extensibility.AppliedMigration;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

/**
 * Handles Flyway's baseline command.
 */
@CustomLog
public class DbBaseline {

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
     * The POJO containing the baseline result
     */
    private final BaselineResult baselineResult;

    /**
     * Creates a new DbBaseline.
     *
     * @param schemaHistory The database schema history table.
     * @param baselineVersion The version to tag an existing schema with when executing baseline.
     * @param baselineDescription The description to tag an existing schema with when executing baseline.
     * @param callbackExecutor The callback executor.
     * @param database Database-specific functionality.
     */
    public DbBaseline(SchemaHistory schemaHistory, MigrationVersion baselineVersion, String baselineDescription,
                      CallbackExecutor callbackExecutor, Database database) {
        this.schemaHistory = schemaHistory;
        this.baselineVersion = baselineVersion;
        this.baselineDescription = baselineDescription;
        this.callbackExecutor = callbackExecutor;

        baselineResult = CommandResultFactory.createBaselineResult(database.getCatalog());
    }

    /**
     * Baselines the database.
     */
    public BaselineResult baseline() {
        callbackExecutor.onEvent(Event.BEFORE_BASELINE);

        try {
            if (!schemaHistory.exists()) {
                schemaHistory.create(true);
                LOG.info("Successfully baselined schema with version: " + baselineVersion);
                baselineResult.successfullyBaselined = true;
                baselineResult.baselineVersion = baselineVersion.toString();
            } else {
                AppliedMigration baselineMarker = schemaHistory.getBaselineMarker();
                if (baselineMarker != null) {
                    if (baselineVersion.equals(baselineMarker.getVersion())
                            && baselineDescription.equals(baselineMarker.getDescription())) {
                        LOG.info("Schema history table " + schemaHistory + " already initialized with ("
                                         + baselineVersion + "," + baselineDescription + "). Skipping.");
                        baselineResult.successfullyBaselined = true;
                        baselineResult.baselineVersion = baselineVersion.toString();
                    } else {
                        throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " with ("
                                                          + baselineVersion + "," + baselineDescription
                                                          + ") as it has already been baselined with ("
                                                          + baselineMarker.getVersion() + "," + baselineMarker.getDescription() + ")\n" +
                                                          "Need to reset your baseline? Learn more: " + FlywayDbWebsiteLinks.RESET_THE_BASELINE_MIGRATION);
                    }
                } else {
                    if (schemaHistory.hasSchemasMarker() && baselineVersion.equals(MigrationVersion.fromVersion("0"))) {
                        throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " with version 0 as this version was used for schema creation");
                    }

                    if (schemaHistory.hasNonSyntheticAppliedMigrations()) {
                        throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " as it already contains migrations\n" +
                                                          "Need to reset your baseline? Learn more: " + FlywayDbWebsiteLinks.RESET_THE_BASELINE_MIGRATION);
                    }

                    if (schemaHistory.allAppliedMigrations().isEmpty()) {
                        throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " as it already exists, and is empty.\n" +
                                                          "Delete the schema history table, and run baseline again.");
                    }

                    throw new FlywayException("Unable to baseline schema history table " + schemaHistory + " as it already contains migrations.\n" +
                                                      "Delete the schema history table, and run baseline again.\n" +
                                                      "Need to reset your baseline? Learn more: " + FlywayDbWebsiteLinks.RESET_THE_BASELINE_MIGRATION);
                }
            }
        } catch (FlywayException e) {
            callbackExecutor.onEvent(Event.AFTER_BASELINE_ERROR);
            baselineResult.successfullyBaselined = false;
            throw e;
        }

        callbackExecutor.onEvent(Event.AFTER_BASELINE);

        return baselineResult;
    }
}