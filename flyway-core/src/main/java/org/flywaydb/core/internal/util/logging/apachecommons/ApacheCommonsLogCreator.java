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
package org.flywaydb.core.internal.util.logging.apachecommons;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.apache.commons.logging.LogFactory;

/**
 * Log Creator for Apache Commons Logging.
 */
public class ApacheCommonsLogCreator implements LogCreator {
    public Log createLogger(Class<?> clazz) {
        return new ApacheCommonsLog(LogFactory.getLog(clazz));
    }
}
