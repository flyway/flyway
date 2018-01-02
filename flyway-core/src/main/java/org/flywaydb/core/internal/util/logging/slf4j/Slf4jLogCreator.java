/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.util.logging.slf4j;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.slf4j.LoggerFactory;

/**
 * Log Creator for Slf4j.
 */
public class Slf4jLogCreator implements LogCreator {
    public Log createLogger(Class<?> clazz) {
        return new Slf4jLog(LoggerFactory.getLogger(clazz));
    }
}
