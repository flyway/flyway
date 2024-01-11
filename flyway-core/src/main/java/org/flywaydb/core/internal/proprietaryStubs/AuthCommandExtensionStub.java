package org.flywaydb.core.internal.proprietaryStubs;

import lombok.CustomLog;
import org.flywaydb.core.FlywayTelemetryManager;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.CommandExtension;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.license.FlywayRedgateEditionRequiredException;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

import java.util.List;

@CustomLog
public class AuthCommandExtensionStub implements CommandExtension {
    private static final String FEATURE_NAME = "Auth";
    public static final String COMMAND = FEATURE_NAME.toLowerCase();
    public static final String DESCRIPTION = "Authenticates Flyway with Redgate licensing";

    @Override
    public boolean handlesCommand(String command) {
        return COMMAND.equals(command);
    }

    @Override
    public boolean handlesParameter(String parameter) {
        return false;
    }

    @Override
    public OperationResult handle(String command, Configuration config, List<String> flags, FlywayTelemetryManager flywayTelemetryManager) throws FlywayException {
        throw new FlywayRedgateEditionRequiredException(FEATURE_NAME);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public int getPriority() {
        return -100;
    }
}