/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.api.output;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Setter;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.internal.info.MigrationInfoImpl;

@Setter
public class MigrateResult extends HtmlResult {
    public static final String COMMAND = "migrate";
    public String initialSchemaVersion;
    public String targetSchemaVersion;
    public String schemaName;
    public List<MigrateOutput> migrations;
    public int migrationsExecuted;
    public boolean success;
    public String flywayVersion;
    public String database;
    public List<String> warnings = new ArrayList<>();
    public String databaseType;

    private transient Map<MigrationKey, MigrateOutput> pendingMigrations = new HashMap<>();
    private transient Map<MigrationKey, MigrateOutput> failedMigrations = new HashMap<>();
    private transient Map<MigrationKey, MigrateOutput> successfulMigrations = new HashMap<>();

    public MigrateResult() {
        super(LocalDateTime.now(), COMMAND);
        migrations = new ArrayList<>();
    }

    public MigrateResult(final String flywayVersion,
        final String database,
        final String schemaName,
        final String databaseType) {
        super(LocalDateTime.now(), COMMAND);
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.schemaName = schemaName;
        this.migrations = new ArrayList<>();
        this.success = true;
        this.databaseType = databaseType;
    }

    MigrateResult(final MigrateResult migrateResult) {
        super(migrateResult.getTimestamp(), migrateResult.getOperation());
        this.flywayVersion = migrateResult.flywayVersion;
        this.database = migrateResult.database;
        this.schemaName = migrateResult.schemaName;
        this.migrations = migrateResult.migrations;
        this.success = migrateResult.success;
        this.migrationsExecuted = migrateResult.migrationsExecuted;
        this.initialSchemaVersion = migrateResult.initialSchemaVersion;
        this.pendingMigrations = migrateResult.pendingMigrations;
        this.failedMigrations = migrateResult.failedMigrations;
        this.successfulMigrations = migrateResult.successfulMigrations;
        this.targetSchemaVersion = migrateResult.targetSchemaVersion;
        this.warnings = migrateResult.warnings;
        this.databaseType = migrateResult.databaseType;
    }

    public void putSuccessfulMigration(final MigrationInfo migrationInfo, final int executionTime) {
        final var key = new MigrationKey(migrationInfo);
        final var migrateOutput = CommandResultFactory.createMigrateOutput(migrationInfo, executionTime);

        successfulMigrations.put(key, migrateOutput);
        pendingMigrations.remove(key);
    }

    public void putPendingMigration(final MigrationInfo migrationInfo) {
        pendingMigrations.put(new MigrationKey(migrationInfo),
            CommandResultFactory.createMigrateOutput(migrationInfo, 0));
    }

    public void putFailedMigration(final MigrationInfo migrationInfo, final int executionTime) {
        final var key = new MigrationKey(migrationInfo);
        failedMigrations.put(key, CommandResultFactory.createMigrateOutput(migrationInfo, executionTime));
        pendingMigrations.remove(key);
    }

    public List<MigrateOutput> getPendingMigrations() {
        return List.copyOf(pendingMigrations.values());
    }

    public List<MigrateOutput> getSuccessfulMigrations() {
        return List.copyOf(successfulMigrations.values());
    }

    public List<MigrateOutput> getFailedMigrations() {
        return List.copyOf(failedMigrations.values());
    }

    public void addWarning(final String warning) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }

        warnings.add(warning);
    }

    public long getTotalMigrationTime() {

        if (migrations == null) {
            return 0;
        }

        return migrations.stream()
            .filter(Objects::nonNull)
            .mapToLong(migrateOutput -> migrateOutput.executionTime)
            .sum();
    }

    public void markAsRolledBack(final List<MigrationInfoImpl> rolledBackMigrations) {
        for (final var migrationInfo : rolledBackMigrations) {
            final var key = new MigrationKey(migrationInfo);

            if (failedMigrations.containsKey(key)) {
                failedMigrations.get(key).rolledBack = true;
            }

            if (successfulMigrations.containsKey(key)) {
                successfulMigrations.get(key).rolledBack = true;
            }
        }
    }
}
