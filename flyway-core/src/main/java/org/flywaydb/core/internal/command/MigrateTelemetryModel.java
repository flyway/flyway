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

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.Getter;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.extensibility.EventTelemetryModel;

@Getter
@SuppressWarnings("WeakerAccess") // telemetry model
public class MigrateTelemetryModel extends EventTelemetryModel {
    private Collection<String> migrationCategories;
    private Collection<String> migrationTypes;
    private int migrationsExecuted;
    private boolean success;
    private int warningCount;

    public MigrateTelemetryModel(final FlywayTelemetryManager flywayTelemetryManager) {
        super("migrate", flywayTelemetryManager);
    }

    public void setFromMigrateResult(final MigrateResult result) {
        migrationCategories = result.migrations.stream().map(m -> m.category).collect(Collectors.toSet());
        migrationTypes = result.migrations.stream().map(m -> m.type).collect(Collectors.toSet());
        migrationsExecuted = result.migrationsExecuted;
        success = result.success;
        warningCount = result.warnings.size();
    }
}
