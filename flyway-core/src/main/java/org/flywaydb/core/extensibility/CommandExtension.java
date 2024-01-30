package org.flywaydb.core.extensibility;

import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;

import java.util.List;

/**
 * @apiNote This interface is under development and not recommended for use.
 */
public interface CommandExtension extends PluginMetadata {
    /**
     * @param command The CLI command to check is handled
     * @return Whether this extension handles the specified command
     */
    boolean handlesCommand(String command);

    /**
     * @param flag The CLI flag to get the command for
     * @return The command, or null if no action is to be taken
     */
    default String getCommandForFlag(String flag) {
        return null;
    }

    /**
     * @param parameter The parameter to check is handled
     * @return Whether this extension handles the specified parameter
     */
    boolean handlesParameter(String parameter);

    /**
     * @param command The command to handle
     * @param config The configuration provided to Flyway
     * @param flags The CLI flags provided to Flyway
     * @return The result of this command being handled
     */
    OperationResult handle(String command, Configuration config, List<String> flags, FlywayTelemetryManager flywayTelemetryManager) throws FlywayException;
}