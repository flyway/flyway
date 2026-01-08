/*-
 * ========================LICENSE_START=================================
 * flyway-verb-migrate
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
import org.flywaydb.core.ProgressLogger;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.nc.NativeConnectorsHybrid;
import org.flywaydb.nc.callbacks.CallbackManager;
import org.flywaydb.verb.migrate.MigrationExecutionGroup;

public class HybridMigrator extends Migrator<NativeConnectorsHybrid>{
    private Migrator getMigrator(NativeConnectorsHybrid database) {
        return switch (database.getDatabaseMetaData().connectionType()) {
            case API -> new ApiMigrator();
            case JDBC -> new JdbcMigrator();
            case EXECUTABLE -> new ExecutableMigrator();
        };
    }

    @Override
    public List<MigrationExecutionGroup> createGroups(final MigrationInfo[] allPendingMigrations,
        final Configuration configuration,
        final NativeConnectorsHybrid experimentalDatabase,
        final MigrateResult migrateResult,
        final ParsingContext parsingContext) {
        final Migrator migrator = getMigrator(experimentalDatabase);
        return migrator.createGroups(allPendingMigrations, configuration,
            migrator instanceof JdbcMigrator
                ? experimentalDatabase.toNativeConnectorsJdbc()
                : experimentalDatabase,
            migrateResult, parsingContext);
    }

    @Override
    public int doExecutionGroup(final Configuration configuration,
        final MigrationExecutionGroup executionGroup,
        final NativeConnectorsHybrid experimentalDatabase,
        final MigrateResult migrateResult,
        final ParsingContext parsingContext,
        final int installedRank,
        final CallbackManager callbackManager,
        final ProgressLogger progress) {
        final Migrator migrator = getMigrator(experimentalDatabase);
        return migrator.doExecutionGroup(configuration, executionGroup,
            migrator instanceof JdbcMigrator
                ? experimentalDatabase.toNativeConnectorsJdbc()
                : experimentalDatabase,
            migrateResult, parsingContext, installedRank, callbackManager, progress);
    }
}
