package org.flywaydb.core.internal.logging.javautil;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;

import java.util.logging.Logger;

public class JavaUtilLogCreator implements LogCreator {
    public Log createLogger(Class<?> clazz) {
        return new JavaUtilLog(Logger.getLogger(clazz.getName()));
    }
}