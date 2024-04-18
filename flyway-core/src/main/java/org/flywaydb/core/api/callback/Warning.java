/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.api.callback;

import org.flywaydb.core.api.configuration.Configuration;

/**
 * A warning that occurred while executing a statement.
 * <p><i>Flyway Teams only</i></p>
 */
public interface Warning {
    /**
     * @return The warning code.
     */
    int getCode();

    /**
     * @return The warning state.
     */
    String getState();

    /**
     * @return The warning message.
     */
    String getMessage();

    /**
     * Checks whether this warning has already been handled.
     *
     * @return {@code true} {@code true} if this warning has already be handled or {@code false} if it should flow
     * via the default warning handler.
     */
    boolean isHandled();

    /**
     * Sets whether this warning has already been handled.
     *
     * @param handled {@code true} if this warning has already be handled or {@code false} if it should flow via the
     * default warning handler.
     */
    void setHandled(boolean handled, Configuration config);
}
