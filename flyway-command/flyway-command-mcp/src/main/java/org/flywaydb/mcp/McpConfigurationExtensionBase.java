/*-
 * ========================LICENSE_START=================================
 * flyway-command-mcp
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
package org.flywaydb.mcp;

import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.extensibility.ConfigurationExtension;

/**
 * Base class for MCP configuration extensions. This is an experimental API and may be removed or changed in future
 * versions.
 */
@Getter
@Setter
@SuppressWarnings("unused")
public abstract class McpConfigurationExtensionBase implements ConfigurationExtension {
    public static final int DEFAULT_MAX_LOGS = 100;

    private int maxLogs = DEFAULT_MAX_LOGS;

    @Override
    public String getNamespace() {
        return "mcp";
    }
}
