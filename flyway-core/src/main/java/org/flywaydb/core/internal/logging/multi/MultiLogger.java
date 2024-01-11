package org.flywaydb.core.internal.logging.multi;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.logging.Log;

import java.util.List;

/**
 * Log implementation that forwards method calls to multiple implementations
 */
@RequiredArgsConstructor
public class MultiLogger implements Log {

    private final List<Log> logs;

    @Override
    public boolean isDebugEnabled() {
        for (Log log : logs) {
            if (!log.isDebugEnabled()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void debug(String message) {
        for (Log log : logs) {
            log.debug(message);
        }
    }

    @Override
    public void info(String message) {
        for (Log log : logs) {
            log.info(message);
        }
    }

    @Override
    public void warn(String message) {
        for (Log log : logs) {
            log.warn(message);
        }
    }

    @Override
    public void error(String message) {
        for (Log log : logs) {
            log.error(message);
        }
    }

    @Override
    public void error(String message, Exception e) {
        for (Log log : logs) {
            log.error(message, e);
        }
    }

    @Override
    public void notice(String message) {
        for (Log log : logs) {
            log.notice(message);
        }
    }
}