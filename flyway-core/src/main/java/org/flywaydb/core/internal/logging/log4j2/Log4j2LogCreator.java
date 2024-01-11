package org.flywaydb.core.internal.logging.log4j2;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.apache.logging.log4j.LogManager;

public class Log4j2LogCreator implements LogCreator {
    public Log createLogger(Class<?> clazz) {
        return new Log4j2Log(LogManager.getLogger(clazz.getName()));
    }
}