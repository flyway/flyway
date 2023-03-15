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
package org.flywaydb.core.internal.proprietaryStubs;

import lombok.CustomLog;
import lombok.SneakyThrows;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

import java.util.Arrays;
import java.util.List;

@CustomLog
public class CommandExtensionStub implements CommandExtension {
    public static final List<String> COMMANDS = Arrays.asList("check", "undo");

    @Override
    public boolean handlesCommand(String command) {
        return COMMANDS.contains(command);
    }

    @Override
    public boolean handlesParameter(String parameter) {
        return false;
    }

    @SneakyThrows
    @Override
    public OperationResult handle(String command, Configuration config, List<String> flags, FlywayTelemetryManager flywayTelemetryManager) throws FlywayException {
        try (EventTelemetryModel telemetryModel = new EventTelemetryModel(command, flywayTelemetryManager)) {
            FlywayProprietaryRequiredException flywayProprietaryRequiredException = new FlywayProprietaryRequiredException(command, FlywayDbWebsiteLinks.UPGRADE_TO_REDGATE_FLYWAY);
            telemetryModel.setException(flywayProprietaryRequiredException);
            throw  flywayProprietaryRequiredException;
        }
    }

    @Override
    public String getDescription() {
        return "Produces reports to increase confidence in your migrations";
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }
}