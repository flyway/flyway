package org.flywaydb.core.internal.proprietaryStubs;

import lombok.CustomLog;
import lombok.SneakyThrows;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.extensibility.EventTelemetryModel;
import org.flywaydb.core.internal.license.FlywayRedgateEditionRequiredException;
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
            FlywayRedgateEditionRequiredException flywayRedgateEditionRequiredException = new FlywayRedgateEditionRequiredException(command);
            telemetryModel.setException(flywayRedgateEditionRequiredException);
            throw flywayRedgateEditionRequiredException;
        }
    }

    @Override
    public String getDescription() {
        return "Produces reports to increase confidence in your migrations";
    }

    @Override
    public int getPriority() {
        return -100;
    }
}