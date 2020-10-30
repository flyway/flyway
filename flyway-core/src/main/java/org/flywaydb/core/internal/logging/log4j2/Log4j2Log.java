/*
 * Copyright © Red Gate Software Ltd 2010-2020
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
package org.flywaydb.core.internal.logging.log4j2;

import org.apache.logging.log4j.Logger;
import org.flywaydb.core.api.logging.Log;

/**
 * Wrapper for a Log4j v2 logger.
 */
public class Log4j2Log implements Log {
    /**
     * Log4j2 Logger.
     */
    private final Logger logger;

    /**
     * Creates a new wrapper around this logger.
     *
     * @param logger The original Slf4j Logger.
     */
    public Log4j2Log(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void debug(String message) { logger.debug(message); }

    public void info(String message) {
        logger.info(message);
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void error(String message) { logger.error(message); }

    public void error(String message, Exception e) {
        logger.error(message, e);
    }
}