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

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.Plugin;

/**
 * Provides a way to load flyway configuration for use with tools within an MCP server. This is an experimental API and
 * may be removed or changed in future versions.
 */
@SuppressWarnings({ "SameReturnValue", "unused" })
public interface McpConfigurationLoader extends Plugin {
    Configuration loadConfiguration(final String projectRoot);
}
