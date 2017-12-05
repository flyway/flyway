/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util.logging.javautil;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;

import java.util.logging.Logger;

/**
 * Log Creator for java.util.logging.
 */
public class JavaUtilLogCreator implements LogCreator {
    public Log createLogger(Class<?> clazz) {
        return new JavaUtilLog(Logger.getLogger(clazz.getName()));
    }
}
