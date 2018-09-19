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
package org.flywaydb.core.internal.util.jdbc;

import org.flywaydb.core.api.callback.Error;

public class ErrorImpl implements Error {
    private final int code;
    private final String state;
    private final String message;

    /**
     * An error that occurred while executing a statement.
     * @param code The error code.
     * @param state The error state.
     * @param message The error message.
     */
    public ErrorImpl(int code, String state, String message) {
        this.code = code;
        this.state = state;
        this.message = message;
    }

    /**
     * @return The error code.
     */
    public int getCode() {
        return code;
    }

    /**
     * @return The error state.
     */
    public String getState() {
        return state;
    }

    /**
     * @return The error message.
     */
    public String getMessage() {
        return message;
    }
}