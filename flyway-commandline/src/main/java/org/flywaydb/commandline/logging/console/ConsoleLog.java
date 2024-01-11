package org.flywaydb.commandline.logging.console;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.logging.Log;

@RequiredArgsConstructor
public class ConsoleLog implements Log {
    public enum Level {
        DEBUG, INFO, WARN
    }

    private final Level level;

    @Override
    public boolean isDebugEnabled() {
        return level == Level.DEBUG;
    }

    public void debug(String message) {
        if (isDebugEnabled()) {
            System.out.println("DEBUG: " + message);
        }
    }

    public void info(String message) {
        if (level.compareTo(Level.INFO) <= 0) {
            System.out.println(message);
        }
    }

    public void notice(String message) {
        if (level.compareTo(Level.INFO) <= 0) {
            System.out.println(message);
        }
    }

    public void warn(String message) {
        System.out.println("WARNING: " + message);
    }

    public void error(String message) {
        System.err.println("ERROR: " + message);
    }

    public void error(String message, Exception e) {
        System.err.println("ERROR: " + message);
        e.printStackTrace(System.err);
    }
}