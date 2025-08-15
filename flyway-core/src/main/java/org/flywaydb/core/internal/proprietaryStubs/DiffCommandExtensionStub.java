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

import java.util.List;
import java.util.Locale;
import org.flywaydb.core.TelemetrySpan;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.internal.license.FlywayRedgateEditionRequiredException;

public class DiffCommandExtensionStub implements CommandExtension {
    private static final String FEATURE_NAME = "Diff";
    public static final String COMMAND = FEATURE_NAME.toLowerCase(Locale.ROOT);
    public static final String DESCRIPTION = "Calculates the differences between a specified source and target. The result of a diff command can then be used with the generate, model and diffText commands to generate scripts and apply changes";

    @Override
    public boolean handlesCommand(final String command) {
        return COMMAND.equals(command);
    }

    @Override
    public boolean handlesParameter(final String parameter) {
        return false;
    }

    @Override
    public OperationResult handle(final String command,
        final Configuration config,
        final List<String> flags) throws FlywayException {
        return TelemetrySpan.trackSpan(new EventTelemetryModel(command, getTelemetryManager(config)), (telemetryModel) -> {
            throw new FlywayRedgateEditionRequiredException(FEATURE_NAME);
        });
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public int getPriority() {
        return -100;
    }
}
