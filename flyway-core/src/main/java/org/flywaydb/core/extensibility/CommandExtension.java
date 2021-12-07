/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.extensibility;

import org.flywaydb.core.api.output.OperationResultBase;

import java.util.Map;

/**
 * @apiNote This interface is under development and not recommended for use.
 */
public interface CommandExtension extends Plugin {
    /**
     * @param command The CLI command to check is handled
     * @return Whether this extension handles the specified command
     */
    boolean handlesCommand(String command);

    /**
     * @param parameter The parameter to check is handled
     * @return Whether this extension handles the specified parameter
     */
    boolean handlesParameter(String parameter);

    /**
     * @return The text to inject into 'flyway -help' to indicate how this extension is intended to be used
     */
    String getUsage();

    /**
     * @param command The command to handle
     * @param config The configuration provided to Flyway
     * @return The result of this command being handled
     */
    OperationResultBase handle(String command, Map<String, String> config);
}