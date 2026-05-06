/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.command;

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.DryRunOutput;
import org.flywaydb.core.api.output.DryRunResult;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoImpl;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.license.VersionPrinter;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.ValidatePatternUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@CustomLog
public class DbDryRun {

    private static final Set<String> SQL_TYPES = Set.of("SQL", "UNDO_SCRIPT", "SCRIPT");

    private final Database database;
    private final SchemaHistory schemaHistory;
    private final Schema schema;
    private final CompositeMigrationResolver migrationResolver;
    private final Configuration configuration;

    public DbDryRun(Database database, SchemaHistory schemaHistory, Schema schema,
                    CompositeMigrationResolver migrationResolver, Configuration configuration) {
        this.database = database;
        this.schemaHistory = schemaHistory;
        this.schema = schema;
        this.migrationResolver = migrationResolver;
        this.configuration = configuration;
    }

    public DryRunResult dryRun() throws FlywayException {
        DryRunResult result = new DryRunResult(
                VersionPrinter.getVersion(),
                database.getCatalog(),
                String.join(", ", configuration.getSchemas()));

        MigrationInfoServiceImpl infoService = new MigrationInfoServiceImpl(
                migrationResolver, schemaHistory, database, configuration,
                configuration.getTarget(), configuration.isOutOfOrder(),
                ValidatePatternUtils.getIgnoreAllPattern(), configuration.getCherryPick());
        infoService.refresh();

        MigrationInfo current = infoService.current();
        result.currentSchemaVersion = current == null || current.getVersion() == null
                ? MigrationVersion.EMPTY.getVersion()
                : current.getVersion().getVersion();

        MigrationInfoImpl[] pending = infoService.pending();

        if (pending.length == 0) {
            LOG.info("Schema " + schema + " is up to date. No pending migrations.");
            return result;
        }

        for (MigrationInfoImpl migration : pending) {
            String version = migration.getVersion() != null ? migration.getVersion().getVersion() : null;
            String description = migration.getDescription();
            String type = migration.getType().toString();
            String filepath = migration.getPhysicalLocation() != null ? migration.getPhysicalLocation() : "";
            String sqlContent = readSqlContent(type, filepath);

            if (version != null) {
                LOG.info(">> Would apply SQL migration version " + version + " (" + description + ") [" + filepath + "]");
            } else {
                LOG.info(">> Would apply repeatable migration (" + description + ") [" + filepath + "]");
            }

            result.pendingMigrations.add(new DryRunOutput(version, description, type, filepath, sqlContent));
        }

        result.migrationCount = result.pendingMigrations.size();
        return result;
    }

    private String readSqlContent(String type, String filepath) {
        if (!SQL_TYPES.contains(type) || filepath.isEmpty()) {
            return null;
        }
        try {
            return Files.readString(Path.of(filepath));
        } catch (IOException e) {
            LOG.warn("Could not read SQL content from " + filepath + ": " + e.getMessage());
            return null;
        }
    }
}
