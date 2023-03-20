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
package org.flywaydb.core.internal.util;

import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;

import java.util.Comparator;
import java.util.List;

public class CommandExtensionUtils {
    public static OperationResult runCommandExtension(Configuration configuration, String command, List<String> flags, FlywayTelemetryManager telemetryManager) {
        return configuration.getPluginRegister().getPlugins(CommandExtension.class).stream()
                            .filter(commandExtension -> commandExtension.handlesCommand(command))
                            .max(Comparator.comparingInt(CommandExtension::getPriority))
                            .map(commandExtension -> commandExtension.handle(command, configuration, flags, telemetryManager))
                            .orElseThrow(() -> new FlywayException("No command extension found to handle command: " + command));
    }
}