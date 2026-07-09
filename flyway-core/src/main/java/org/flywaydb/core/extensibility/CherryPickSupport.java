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
package org.flywaydb.core.extensibility;

import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationPattern;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.ResolvedMigration;

public interface CherryPickSupport extends Plugin {

    MigrationState getStateOverride(MigrationPattern[] patterns, MigrationVersion version, String description);

    boolean shouldSkipValidation(MigrationPattern[] patterns, MigrationVersion version, String description);

    OptionalInt compareByPickOrder(MigrationPattern[] patterns, MigrationInfo a, MigrationInfo b);

    void validatePatterns(MigrationPattern[] patterns,
        Collection<ResolvedMigration> resolved,
        List<? extends AppliedMigration> applied,
        Configuration config);
}
