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
package org.flywaydb.core.api.callback;

/**
 * An error that occurred while executing a statement.
 * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
 */
public interface Error {
    /**
     * @return The error code.
     */
    int getCode();

    /**
     * @return The error state.
     */
    String getState();

    /**
     * @return The error message.
     */
    String getMessage();

    /**
     * Checks whether this error has already been handled.
     *
     * @return {@code true} {@code true} if this error has already be handled or {@code false} if it should flow
     * via the default error handler.
     */
    boolean isHandled();

    /**
     * Sets whether this error has already been handled.
     *
     * @param handled {@code true} if this error has already be handled or {@code false} if it should flow via the
     *                default error handler.
     */
    void setHandled(boolean handled);
}