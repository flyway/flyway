package org.flywaydb.commandline.logging;

import lombok.NoArgsConstructor;
import org.flywaydb.commandline.configuration.CommandLineArguments;
import org.flywaydb.commandline.logging.console.ConsoleLogCreator;
import org.flywaydb.commandline.logging.file.FileLogCreator;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.logging.multi.MultiLogCreator;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class LoggingUtils {

    public static LogCreator getLogCreator(CommandLineArguments commandLineArguments) {
        // JSON output uses a different mechanism, so we do not create any loggers
        if (commandLineArguments.shouldOutputJson() || (commandLineArguments.hasOperation("info") && commandLineArguments.isFilterOnMigrationIds())) {
            return MultiLogCreator.empty();
        }

        List<LogCreator> logCreators = new ArrayList<>();
        logCreators.add(new ConsoleLogCreator(commandLineArguments));
        if (commandLineArguments.isOutputFileSet()) {
            logCreators.add(new FileLogCreator(commandLineArguments));
        }

        return new MultiLogCreator(logCreators);
    }

    public static Log initLogging(Class<?> clazz, CommandLineArguments commandLineArguments) {
        LogFactory.setFallbackLogCreator(getLogCreator(commandLineArguments));
        return LogFactory.getLog(clazz);
    }
}