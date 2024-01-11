package org.flywaydb.core.internal.logging.apachecommons;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.logging.Log;

@RequiredArgsConstructor
public class ApacheCommonsLog implements Log {

    private final org.apache.commons.logging.Log logger;

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(String message, Exception e) {
        logger.error(message, e);
    }

    public void notice(String message) {}
}