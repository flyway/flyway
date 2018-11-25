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
package org.flywaydb.core.internal.jdbc;

import org.flywaydb.core.api.callback.Warning;

public class WarningImpl implements Warning {
    private final int code;
    private final String state;
    private final String message;
    private boolean handled;

    /**
     * An warning that occurred while executing a statement.
     * @param code The warning code.
     * @param state The warning state.
     * @param message The warning message.
     */
    public WarningImpl(int code, String state, String message) {
        this.code = code;
        this.state = state;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isHandled() {
        return handled;
    }

    @Override
    public void setHandled(boolean handled) {
        this.handled = handled;
    }
}