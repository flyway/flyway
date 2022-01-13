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
package org.flywaydb.core.api.output;

import java.util.ArrayList;
import java.util.List;

public class MigrateResult extends OperationResultBase {
    public String initialSchemaVersion;
    public String targetSchemaVersion;
    public String schemaName;
    public List<MigrateOutput> migrations;
    public int migrationsExecuted;
    public boolean success;

    public MigrateResult(String flywayVersion,
                         String database,
                         String schemaName) {
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.schemaName = schemaName;
        this.migrations = new ArrayList<>();
        this.operation = "migrate";
        this.success = true;
    }

    MigrateResult(MigrateResult migrateResult) {
        this.flywayVersion = migrateResult.flywayVersion;
        this.database = migrateResult.database;
        this.schemaName = migrateResult.schemaName;
        this.migrations = migrateResult.migrations;
        this.operation = migrateResult.operation;
        this.success = migrateResult.success;
        this.migrationsExecuted = migrateResult.migrationsExecuted;
        this.initialSchemaVersion = migrateResult.initialSchemaVersion;
        this.targetSchemaVersion = migrateResult.targetSchemaVersion;
        this.warnings = migrateResult.warnings;
    }
}