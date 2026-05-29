/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
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
package org.flywaydb.commandline.configuration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.flywaydb.commandline.Main;
import org.flywaydb.core.api.CoreErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.mcp.McpConfigurationLoader;

/**
 * Provides a way to load toml flyway configuration for use with tools within an MCP server. This is an experimental API
 * and may be removed or changed in future versions.
 */
public class ModernConfigurationMcpConfigurationLoader implements McpConfigurationLoader {
    private final ModernConfigurationManager configurationManager = new ModernConfigurationManager();

    @Override
    public Configuration loadConfiguration(final String projectRoot) {
        if (projectRoot == null || projectRoot.isBlank()) {
            throw new FlywayException("Project root path must be provided.", CoreErrorCode.CONFIGURATION);
        }

        if (!Files.exists(Path.of(projectRoot))) {
            throw new FlywayException("Project root path does not exist: " + projectRoot, CoreErrorCode.CONFIGURATION);
        }

        final File installDir = new File(ClassUtils.getInstallDir(Main.class));
        final List<File> tomlFiles = ConfigUtils.getDefaultTomlConfigFileLocations(installDir, projectRoot)
            .stream()
            .filter(File::exists)
            .toList();

        if (tomlFiles.isEmpty()) {
            throw new FlywayException("No .toml configuration files found in project root.",
                CoreErrorCode.CONFIGURATION);
        }

        if (ConfigUtils.getDefaultLegacyConfigurationFiles(installDir, projectRoot).stream().anyMatch(File::exists)) {
            throw new FlywayException(
                "Legacy configuration files found in project root. Please migrate all configuration to .toml format.",
                CoreErrorCode.CONFIGURATION);
        }

        return configurationManager.getMcpActionConfiguration(tomlFiles, projectRoot);
    }
}
