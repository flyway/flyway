/*-
 * ========================LICENSE_START=================================
 * flyway-verb-migrate
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
package org.flywaydb.verb.migrate.migrators;

import static org.flywaydb.verb.VerbUtils.toMigrationText;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.CommandResultFactory;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.experimental.ExperimentalDatabase;
import org.flywaydb.core.internal.exception.FlywayMigrateException;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.verb.migrate.MigrationExecutionGroup;

@CustomLog
public class ApiMigrator extends Migrator{
    
    @Override
    public List<MigrationExecutionGroup> createGroups(final MigrationInfo[] allPendingMigrations,
        final Configuration configuration, final ExperimentalDatabase experimentalDatabase, final MigrateResult migrateResult, final ParsingContext parsingContext) {
        return List.of(new MigrationExecutionGroup(List.of(allPendingMigrations), true));        
    }

    @Override
    public int doExecutionGroup(final Configuration configuration,
        final MigrationExecutionGroup executionGroup,
        final ExperimentalDatabase experimentalDatabase,
        final MigrateResult migrateResult,
        final ParsingContext parsingContext,
        final int installedRank) {
        int rank = installedRank;
        final boolean executeInTransaction = configuration.isExecuteInTransaction()
            && executionGroup.shouldExecuteInTransaction();
        if (executeInTransaction) {
            experimentalDatabase.startTransaction();
        }
        for (final MigrationInfo migrationInfo : executionGroup.migrations()) {
            doIndividualMigration(migrationInfo, experimentalDatabase, configuration, migrateResult, rank);
            rank++;
        }
        if (executeInTransaction) {
            experimentalDatabase.commitTransaction();
        }
        return rank;
    }

    private void doIndividualMigration(final MigrationInfo migrationInfo, final ExperimentalDatabase experimentalDatabase,
        final Configuration configuration, final MigrateResult migrateResult, final int installedRank) {
        final StopWatch watch = new StopWatch();
        watch.start();

        final boolean outOfOrder = migrationInfo.getState() == MigrationState.OUT_OF_ORDER && configuration.isOutOfOrder();
        final String migrationText = toMigrationText(migrationInfo, true, experimentalDatabase, outOfOrder);

        try {
            if (configuration.isSkipExecutingMigrations()) {
                LOG.debug("Skipping execution of migration of " + migrationText);
            } else {
                LOG.debug("Starting migration of " + migrationText + " ...");
                LOG.info("Migrating " + migrationText);
                final String executionUnit = String.join("\n", Files.readAllLines(Path.of(migrationInfo.getScript())));
                experimentalDatabase.doExecute(executionUnit);
            }
        } catch (final Exception e) {
            watch.stop();
            final int totalTimeMillis = (int) watch.getTotalTimeMillis();
            handleMigrationError(e, experimentalDatabase, migrationInfo,
                migrateResult,
                configuration.getTable(),
                configuration.isOutOfOrder(),
                installedRank,
                experimentalDatabase.getInstalledBy(configuration),
                totalTimeMillis);
            throw new FlywayException(e);
        }

        watch.stop();

        migrateResult.migrationsExecuted += 1;
        final int totalTimeMillis = (int) watch.getTotalTimeMillis();
        migrateResult.putSuccessfulMigration(migrationInfo, totalTimeMillis);
        if (migrationInfo.isVersioned()) {
            migrateResult.targetSchemaVersion = migrationInfo.getVersion().getVersion();
        }
        migrateResult.migrations.add(CommandResultFactory.createMigrateOutput(migrationInfo, totalTimeMillis, null));
        updateSchemaHistoryTable(configuration.getTable(),
            migrationInfo,
            totalTimeMillis,
            installedRank,
            experimentalDatabase,
            experimentalDatabase.getInstalledBy(configuration),
            true);
    }

    private void handleMigrationError(final Exception e,
        final ExperimentalDatabase experimentalDatabase,
        final MigrationInfo migrationInfo,
        final MigrateResult migrateResult,
        final String schemaHistoryTableName,
        final boolean outOfOrder,
        final int installedRank,
        final String installedBy,
        final int totalTimeMillis) {
        final String migrationText = toMigrationText(migrationInfo, true, experimentalDatabase, outOfOrder);
        final String failedMsg = "Migration of " + migrationText + " failed!";

        migrateResult.putFailedMigration(migrationInfo, totalTimeMillis);
        migrateResult.setSuccess(false);

        LOG.error(failedMsg + " Please restore backups and roll back database and code!");
        updateSchemaHistoryTable(schemaHistoryTableName,
            migrationInfo,
            totalTimeMillis,
            installedRank,
            experimentalDatabase,
            installedBy,
            false);

        throw new FlywayMigrateException(migrationInfo,
            e.getMessage(),
            true, migrateResult);
    }
}
