package org.flywaydb.core.internal.logging.slf4j;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.slf4j.LoggerFactory;

public class Slf4jLogCreator implements LogCreator {
    public Log createLogger(Class<?> clazz) {
        return new Slf4jLog(LoggerFactory.getLogger(clazz.getName()));
    }
}