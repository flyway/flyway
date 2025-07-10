/*-
 * ========================LICENSE_START=================================
 * flyway-verb-migrate
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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

import java.util.List;
import lombok.CustomLog;
import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.internal.nc.NativeConnectorsDatabase;
import org.flywaydb.core.internal.nc.schemahistory.SchemaHistoryItem;
import org.flywaydb.core.internal.nc.schemahistory.SchemaHistoryItem.SchemaHistoryItemBuilder;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.nc.callbacks.CallbackManager;
import org.flywaydb.verb.migrate.MigrationExecutionGroup;

@CustomLog
public abstract class Migrator {

    public abstract List<MigrationExecutionGroup> createGroups(final MigrationInfo[] allPendingMigrations,
        final Configuration configuration,
        final NativeConnectorsDatabase experimentalDatabase,
        final MigrateResult migrateResult,
        final ParsingContext parsingContext);

    public abstract int doExecutionGroup(final Configuration configuration,
        final MigrationExecutionGroup executionGroup,
        final NativeConnectorsDatabase experimentalDatabase,
        final MigrateResult migrateResult,
        final ParsingContext parsingContext,
        final int installedRank,
        final CallbackManager callbackManager,
        final ProgressLogger progress);

    static void updateSchemaHistoryTable(final String tableName,
        final MigrationInfo migrationInfo,
        final int totalTimeMillis,
        final int installedRank,
        final NativeConnectorsDatabase experimentalDatabase,
        final String installedBy,
        final boolean success) {
        final SchemaHistoryItemBuilder schemaHistoryItem = SchemaHistoryItem.builder()
            .executionTime(totalTimeMillis)
            .type(migrationInfo.getType().name())
            .description(migrationInfo.getDescription())
            .script(migrationInfo.getScript())
            .installedRank(installedRank)
            .checksum(migrationInfo.getChecksum())
            .installedBy(installedBy)
            .success(success);
        if (migrationInfo.isVersioned()) {
            schemaHistoryItem.version(migrationInfo.getVersion().getVersion());
        }
        experimentalDatabase.appendSchemaHistoryItem(schemaHistoryItem.build(), tableName);
    }
}
