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
package org.flywaydb.core.internal.callback;

import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.extensibility.EventTelemetryModel;

@Getter
@Setter
@SuppressWarnings("WeakerAccess") // telemetry model properties are read via reflection
public class CallbackTelemetryModel extends EventTelemetryModel {
    private String callbackType;

    CallbackTelemetryModel(final String id,
        final String callbackType,
        final FlywayTelemetryManager flywayTelemetryManager) {
        super(id, flywayTelemetryManager);
        this.callbackType = callbackType;
    }
}
