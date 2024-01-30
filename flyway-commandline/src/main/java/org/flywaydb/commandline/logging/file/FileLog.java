package org.flywaydb.commandline.logging.file;

import lombok.RequiredArgsConstructor;
import org.flywaydb.commandline.logging.console.ConsoleLog.Level;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@RequiredArgsConstructor
public class FileLog implements Log {

    private final Path path;
    private final Level level;

    @Override
    public boolean isDebugEnabled() {
        return level == Level.DEBUG;
    }

    @Override
    public void debug(String message) {
        if (isDebugEnabled()) {
            writeLogMessage("DEBUG", message);
        }
    }

    @Override
    public void info(String message) {
        if (level.compareTo(Level.INFO) <= 0) {
            writeLogMessage(message);
        }
    }

    @Override
    public void warn(String message) {
        writeLogMessage("WARNING", message);
    }

    @Override
    public void error(String message) {
        writeLogMessage("ERROR", message);
    }

    @Override
    public void error(String message, Exception e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        String stackTrace = stringWriter.toString();

        writeLogMessage("ERROR", message);
        writeLogMessage(stackTrace);
    }

    public void notice(String message) {}

    private void writeLogMessage(String prefix, String message) {
        String logMessage = prefix + ": " + message;
        writeLogMessage(logMessage);
    }

    private void writeLogMessage(String logMessage) {
        try {
            Files.write(path, (logMessage + "\n").getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new FlywayException("Could not write to file at " + path + ".", exception);
        }
    }
}