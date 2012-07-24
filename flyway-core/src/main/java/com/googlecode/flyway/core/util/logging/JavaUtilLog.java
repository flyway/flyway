/**
 * Copyright (C) 2010-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.flyway.core.util.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper for a java.util.Logger.
 */
public class JavaUtilLog implements Log {
    /**
     * Java Util Logger.
     */
    private final Logger logger;

    /**
     * Creates a new wrapper around this logger.
     *
     * @param logger The original java.util Logger.
     */
    public JavaUtilLog(Logger logger) {
        this.logger = logger;
    }

    public void debug(String message) {
        logger.fine(message);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warn(String message) {
        logger.warning(message);
    }

    public void error(String message) {
        logger.severe(message);
    }

    public void error(String message, Exception e) {
        logger.log(Level.SEVERE, message, e);
    }
}
