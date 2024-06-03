/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.api.output;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public MigrateResult(String flywayVersion,
                         String database,
                         String schemaName,
                         String databaseType) {
        super(LocalDateTime.now(), COMMAND);
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.schemaName = schemaName;
        this.migrations = new ArrayList<>();
        this.success = true;
        this.databaseType = databaseType;
    }

    MigrateResult(MigrateResult migrateResult) {
        super(migrateResult.getTimestamp(), migrateResult.getOperation());
        this.flywayVersion = migrateResult.flywayVersion;
        this.database = migrateResult.database;
        this.schemaName = migrateResult.schemaName;
        this.migrations = migrateResult.migrations;
        this.success = migrateResult.success;
        this.migrationsExecuted = migrateResult.migrationsExecuted;
        this.initialSchemaVersion = migrateResult.initialSchemaVersion;
        this.targetSchemaVersion = migrateResult.targetSchemaVersion;
        this.warnings = migrateResult.warnings;
        this.databaseType = migrateResult.databaseType;
    }

    public void addWarning(String warning) {
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
}
