package org.flywaydb.commandline.logging.console;

import lombok.RequiredArgsConstructor;
import org.flywaydb.commandline.configuration.CommandLineArguments;
import org.flywaydb.commandline.configuration.CommandLineArguments.Color;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;

@RequiredArgsConstructor
public class ConsoleLogCreator implements LogCreator {
    private final CommandLineArguments commandLineArguments;

    public Log createLogger(Class<?> clazz) {
        ConsoleLog log = new ConsoleLog(commandLineArguments.getLogLevel());
        Color color = commandLineArguments.getColor();

        if (Color.NEVER.equals(color) || (Color.AUTO.equals(color) && System.console() == null)) {
            return log;
        }

        ColorizedConsoleLog.install(Color.ALWAYS.equals(color));
        return new ColorizedConsoleLog(log);
    }
}