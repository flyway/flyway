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
                            .findFirst()
                            .map(commandExtension -> commandExtension.handle(command, configuration, flags, telemetryManager))
                            .orElseThrow(() -> new FlywayException("No command extension found to handle command: " + command));
    }
}