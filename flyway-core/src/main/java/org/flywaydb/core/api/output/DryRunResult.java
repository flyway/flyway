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
package org.flywaydb.core.api.output;

import java.util.ArrayList;
import java.util.List;

public class DryRunResult extends OperationResultBase {
    public String schemaName;
    public String currentSchemaVersion;
    public List<DryRunOutput> pendingMigrations;
    public int migrationCount;

    public DryRunResult(String flywayVersion, String database, String schemaName) {
        this.flywayVersion = flywayVersion;
        this.database = database;
        this.schemaName = schemaName;
        this.operation = "dryrun";
        this.pendingMigrations = new ArrayList<>();
    }
}
