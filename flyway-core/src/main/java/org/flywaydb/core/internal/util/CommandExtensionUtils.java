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
package org.flywaydb.core.internal.util;

import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;

public class CommandExtensionUtils {
    public static OperationResult runCommandExtension(final Configuration configuration,
        final String command,
        final List<String> flags) {
        return configuration.getPluginRegister()
            .getInstancesOf(CommandExtension.class)
            .stream()
            .filter(commandExtension -> commandExtension.handlesCommand(command))
            .findFirst()
            .map(commandExtension -> commandExtension.handle(configuration, flags))
            .orElseThrow(() -> new FlywayException("No command extension found to handle command: " + command));
    }
}
