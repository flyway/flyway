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

import static org.flywaydb.core.internal.util.TelemetryUtils.getTelemetryManager;

import java.util.List;
import lombok.CustomLog;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.TelemetrySpan;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.extensibility.EventTelemetryModel;

/**
 * Command extension that starts a STDIO MCP server for flyway for use with external MCP clients. This is an
 * experimental API and may be removed or changed in future versions.
 */
@CustomLog
@SuppressWarnings({ "unused", "WeakerAccess" })
public abstract class McpCommandExtension implements CommandExtension<McpCommandExtension.McpCommandResult> {
    public static final String MCP_VERB = "mcp";
    private final McpServerLogFileActions logFileActions;

    protected McpCommandExtension() {
        this(new McpServerLogFileActions(LOG));
    }

    McpCommandExtension(final McpServerLogFileActions logFileActions) {
        this.logFileActions = logFileActions;
    }

    @Override
    public boolean handlesCommand(final String command) {
        return MCP_VERB.equals(command);
    }

    @Override
    public boolean handlesParameter(final String parameter) {
        return false;
    }

    @Override
    public boolean requiresFlywayInstance() {
        return false;
    }

    @Override
    public McpCommandResult handle(final Configuration config, final List<String> flags) throws FlywayException {
        LOG.info("Starting flyway MCP server");

        final FlywayTelemetryManager telemetryManager = getTelemetryManager(config);
        return TelemetrySpan.trackSpan(new EventTelemetryModel(MCP_VERB, telemetryManager), model -> {
            final McpConfigurationLoader configurationLoader = config.getPluginRegister()
                .getInstanceOf(McpConfigurationLoader.class);
            if (configurationLoader == null) {
                throw new FlywayException("Configuration loader not provided for MCP Server.");
            }

            final McpConfigurationExtensionBase configurationExtension = config.getPluginRegister()
                .getInstanceOf(McpConfigurationExtensionBase.class);
            if (configurationExtension == null) {
                throw new FlywayException("Configuration extension not provided for MCP Server.");
            }

            logFileActions.pruneLogs(configurationExtension.getMaxLogs());
            start(config, configurationLoader);

            return new McpCommandResult();
        });
    }

    protected abstract void start(Configuration serverConfiguration, McpConfigurationLoader configurationLoader);

    public static class McpCommandResult implements OperationResult {}
}
