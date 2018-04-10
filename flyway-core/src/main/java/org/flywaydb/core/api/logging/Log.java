/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.api.logging;

/**
 * A logger.
 */
public interface Log {
    /**
     * @return Whether debug logging is enabled.
     */
    boolean isDebugEnabled();

    /**
     * Logs a debug message.
     *
     * @param message The message to log.
     */
    void debug(String message);

    /**
     * Logs an info message.
     *
     * @param message The message to log.
     */
    void info(String message);

    /**
     * Logs a warning message.
     *
     * @param message The message to log.
     */
    void warn(String message);

    /**
     * Logs an error message.
     *
     * @param message The message to log.
     */
    void error(String message);

    /**
     * Logs an error message and the exception that caused it.
     *
     * @param message The message to log.
     * @param e The exception that caused the error.
     */
    void error(String message, Exception e);
}