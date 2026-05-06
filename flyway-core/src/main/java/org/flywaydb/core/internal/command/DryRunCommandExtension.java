package org.flywaydb.core.internal.command;

import static org.flywaydb.core.internal.util.TelemetryUtils.getTelemetryManager;

import java.util.List;
import org.flywaydb.core.FlywayExecutor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.DryRunResult;
import org.flywaydb.core.extensibility.CommandExtension;

public class DryRunCommandExtension implements CommandExtension<DryRunResult> {

    @Override
    public boolean handlesCommand(String command) {
        return "dryrun".equals(command);
    }

    @Override
    public boolean handlesParameter(String parameter) {
        return false;
    }

    @Override
    public DryRunResult handle(Configuration config, List<String> flags) throws FlywayException {
        return new FlywayExecutor(config).execute(
                (resolver, history, db, defaultSchema, schemas, callbacks, interceptor) ->
                        new DbDryRun(db, history, defaultSchema, resolver, config).dryRun(),
                true,
                getTelemetryManager(config));
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
