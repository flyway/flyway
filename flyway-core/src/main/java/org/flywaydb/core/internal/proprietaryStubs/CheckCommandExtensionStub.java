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
package org.flywaydb.core.internal.proprietaryStubs;

import static org.flywaydb.core.internal.util.TelemetryUtils.getTelemetryManager;

import lombok.CustomLog;
import lombok.SneakyThrows;
import org.flywaydb.core.TelemetrySpan;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.internal.license.FlywayRedgateEditionRequiredException;

import java.util.List;

@CustomLog
public class CheckCommandExtensionStub implements CommandExtension {
    public static final String COMMAND = "check";

    @Override
    public boolean handlesCommand(final String command) {
        return COMMAND.equals(command);
    }

    @Override
    public boolean handlesParameter(final String parameter) {
        return false;
    }

    @SneakyThrows
    @Override
    public OperationResult handle(final String command, final Configuration config, final List<String> flags) throws FlywayException {
        return TelemetrySpan.trackSpan(new EventTelemetryModel(COMMAND, getTelemetryManager(config)), (telemetryModel) -> {
            throw new FlywayRedgateEditionRequiredException(COMMAND);
        });
    }

    @Override
    public String getDescription() {
        return "Produces reports to increase confidence in your migrations";
    }

    @Override
    public int getPriority() {
        return -100;
    }
}
