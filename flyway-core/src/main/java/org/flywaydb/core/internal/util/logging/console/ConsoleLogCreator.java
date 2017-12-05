/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.util.logging.console;

import org.flywaydb.core.internal.util.logging.console.ConsoleLog.Level;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;

/**
 * Log Creator for the Command-Line console.
 */
public class ConsoleLogCreator implements LogCreator {
    private final Level level;

    /**
     * Creates a new Console Log Creator.
     *
     * @param level The minimum level to log at.
     */
    public ConsoleLogCreator(Level level) {
        this.level = level;
    }

    public Log createLogger(Class<?> clazz) {
        return new ConsoleLog(level);
    }
}
