/*-
 * ========================LICENSE_START=================================
 * flyway-verb-repair
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.verb.repair;

import static org.flywaydb.core.experimental.ExperimentalModeUtils.logExperimentalDataTelemetry;

import java.util.Arrays;
import java.util.List;
import lombok.CustomLog;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.RepairOutput;
import org.flywaydb.core.api.output.RepairResult;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.experimental.schemahistory.SchemaHistoryModel;
import org.flywaydb.core.extensibility.VerbExtension;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;
import org.flywaydb.verb.VerbUtils;

@CustomLog
public class RepairVerbExtension implements VerbExtension {
    @Override
    public boolean handlesVerb(final String verb) {
        return "repair".equals(verb);
    }

    @Override
    public Object executeVerb(final Configuration configuration, FlywayTelemetryManager flywayTelemetryManager) {
        final ExperimentalDatabase experimentalDatabase;
        try {
            experimentalDatabase = VerbUtils.getExperimentalDatabase(configuration);
        } catch (final Exception e) {
            throw new FlywayException(e);
        }

        logExperimentalDataTelemetry(flywayTelemetryManager, experimentalDatabase.getDatabaseMetaData());

        final RepairResult repairResult = new RepairResult(VersionPrinter.getVersion(), experimentalDatabase.getDatabaseMetaData().databaseName());

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        removeFailedMigrations(configuration, repairResult, experimentalDatabase);

        final SchemaHistoryModel postRemovalSchemaHistoryModel = VerbUtils.getSchemaHistoryModel(configuration, experimentalDatabase);
        final MigrationInfo[] migrations = VerbUtils.getMigrationInfos(configuration, experimentalDatabase, postRemovalSchemaHistoryModel);

        markRemovedMigrationsAsDeleted(configuration, migrations, repairResult, postRemovalSchemaHistoryModel, experimentalDatabase);

        alignAppliedMigrationsWithResolvedMigrations(configuration, migrations, repairResult, experimentalDatabase);

        stopWatch.stop();
        if (repairResult.repairActions.isEmpty()) {
            LOG.info("Repair of schema history table "
                + experimentalDatabase.quote(experimentalDatabase.getCurrentSchema(), configuration.getTable())
                + " not needed, no migrations need repairing.");
        } else {
            LOG.info("Successfully repaired schema history table "
                + experimentalDatabase.quote(experimentalDatabase.getCurrentSchema(), configuration.getTable())
                + " (execution time "
                + TimeFormat.format(stopWatch.getTotalTimeMillis())
                + ").");
            if (repairResult.repairActions.contains("Marked missing migrations as deleted")) {
                LOG.info("Please ensure the previous contents of the deleted migrations are removed from the database, or moved into an existing migration.");
            }
        }
        return repairResult;
    }

    private void alignAppliedMigrationsWithResolvedMigrations(final Configuration configuration,
        final MigrationInfo[] migrations,
        final RepairResult repairResult,
        final ExperimentalDatabase experimentalDatabase) {
        final List<MigrationInfo> appliedVersionedInfos = Arrays.stream(migrations).filter(x -> x.getVersion() != null)
            .filter(x -> x.getState().isResolved())
            .filter(x -> x.getState().isApplied())
            .filter(x -> !x.getType().isSynthetic())
            .filter(x -> x.getState() != MigrationState.UNDONE)
            .filter(this::updateNeeded).toList();
        if (!appliedVersionedInfos.isEmpty()) {
            repairResult.migrationsAligned = appliedVersionedInfos.stream().map(x -> new RepairOutput(x.isRepeatable()
                ? ""
                : x.getVersion().getVersion(), x.getDescription(), x.getPhysicalLocation())).toList();
            final SchemaHistoryModel postDeletedSchemaHistoryModel = VerbUtils.getSchemaHistoryModel(configuration,
                experimentalDatabase);
            for (final MigrationInfo updatedEntry : appliedVersionedInfos) {
                final SchemaHistoryItem item = postDeletedSchemaHistoryModel.getSchemaHistoryItem(updatedEntry.getInstalledRank())
                    .orElseThrow(() -> new FlywayException("Fatal error when repairing, please contact support!"));
                LOG.info("Repairing Schema History table for version " + item.getVersion()
                    + " (Description: " + updatedEntry.getResolvedDescription()
                    + ", Type: " + updatedEntry.getResolvedType()
                    + ", Checksum: " + updatedEntry.getResolvedChecksum() + ")  ...");
                experimentalDatabase.updateSchemaHistoryItem(item.toBuilder()
                        .type(updatedEntry.getResolvedType().name())
                        .checksum(updatedEntry.getResolvedChecksum())
                        .description(updatedEntry.getResolvedDescription())
                        .build(),
                    configuration.getTable());

            }
            repairResult.repairActions.add("Aligned applied migration checksums");
        } else {
            LOG.info("No migrations to realign in Schema History table "
                + experimentalDatabase.quote(experimentalDatabase.getCurrentSchema(), configuration.getTable()));
        }
    }

    private static void markRemovedMigrationsAsDeleted(final Configuration configuration,
        final MigrationInfo[] migrations,
        final RepairResult repairResult,
        final SchemaHistoryModel postRemovalSchemaHistoryModel,
        final ExperimentalDatabase experimentalDatabase) {
        final List<MigrationInfo> migrationInfo = VerbUtils.removeIgnoredMigrations(configuration, migrations).stream().filter(x -> !x.getState().isResolved()).toList();

        repairResult.migrationsDeleted = migrationInfo.stream()
            .map(x -> new RepairOutput(x.isRepeatable() ? "" : x.getVersion().getVersion(), x.getDescription(), ""))
            .toList();

        if (!repairResult.migrationsDeleted.isEmpty()) {
            int nextInstalledRank = postRemovalSchemaHistoryModel.calculateInstalledRank(CoreMigrationType.DELETE);
            final List<SchemaHistoryItem> schemaHistoryItems = migrationInfo.stream()
                .map(x -> postRemovalSchemaHistoryModel.getSchemaHistoryItem(x.getInstalledRank()))
                .map(x -> x.orElseThrow(() -> new FlywayException("Fatal error when repairing, please contact support!"))).toList();
            for (final SchemaHistoryItem schemaHistoryItem : schemaHistoryItems) {
                if (schemaHistoryItem.getVersion() == null) {
                    LOG.info("Repairing Schema History table for description \"" + schemaHistoryItem.getDescription() + "\" (Marking as DELETED)  ...");
                } else {
                    LOG.info("Repairing Schema History table for version \"" + schemaHistoryItem.getVersion() + "\" (Marking as DELETED)  ...");
                }
                experimentalDatabase.appendSchemaHistoryItem(schemaHistoryItem.toBuilder()
                    .type(CoreMigrationType.DELETE.name())
                    .installedRank(nextInstalledRank++)
                    .build(),
                    configuration.getTable());
            }
            repairResult.repairActions.add("Marked missing migrations as deleted");
        } else {
            LOG.info("No missing or future migrations to be marked as deleted in Schema History table "
                + experimentalDatabase.quote(experimentalDatabase.getCurrentSchema(), configuration.getTable()));
        }
    }

    private static void removeFailedMigrations(final Configuration configuration,
        final RepairResult repairResult, final ExperimentalDatabase experimentalDatabase) {
        final SchemaHistoryModel schemaHistoryModel = VerbUtils.getSchemaHistoryModel(configuration, experimentalDatabase);
        repairResult.migrationsRemoved = schemaHistoryModel.getSchemaHistoryItems().stream()
            .filter(x -> !x.isSuccess())
            .map(x -> new RepairOutput(x.getVersion() == null ? "" : x.getVersion(), x.getDescription(), "")).toList();
        if (!repairResult.migrationsRemoved.isEmpty()) {
            experimentalDatabase.removeFailedSchemaHistoryItems(configuration.getTable());
            
            repairResult.repairActions.add("Removed failed migrations");
            LOG.info("Removed " + repairResult.migrationsRemoved.size() + " failed migration from Schema History table "
                + experimentalDatabase.quote(experimentalDatabase.getCurrentSchema(), configuration.getTable()));
        } else {
            LOG.info("Repair of failed migration in Schema History table "
                + experimentalDatabase.quote(experimentalDatabase.getCurrentSchema(), configuration.getTable())
                + " not necessary. No failed migration detected.");
        }
    }

    private boolean updateNeeded(MigrationInfo migrationInfo) {
        return !(migrationInfo.isChecksumMatching()
            & migrationInfo.isDescriptionMatching()
            & migrationInfo.isTypeMatching());
    }
}
