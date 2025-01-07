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
package org.flywaydb.core.extensibility;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class RootTelemetryModel {
    private String userId;
    private String sessionId;
    private String projectId;
    private String projectName;
    private String operationId;
    private String databaseEngine;
    private String databaseVersion;
    private String environment;
    private String applicationVersion;
    private String applicationEdition;
    private boolean redgateEmployee;
    private boolean isTrial;
    private boolean isSignedIn;
    private boolean CustomMigrationResolver;
    private String containerType;
    private String secretsManagementType;
    private String databaseHosting;
    private boolean isExperimentalMode;
    private String connectionType;
    private String customParameters;
    private boolean isLegacyMode;

    private Instant startTime = Instant.now();
}
