/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.callback.Warning;

@RequiredArgsConstructor
public class WarningImpl implements Warning {
    private final int code;
    private final String state;
    private final String message;
    private boolean handled;

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