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

import java.util.List;

/**
 * The statement relevant to an event.
 * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
 */
public interface Statement {
    /**
     * @return The SQL statement.
     */
    String getSql();

    /**
     * @return The warnings that were raised during the execution of the statement.
     * {@code null} if the statement hasn't been executed yet.
     */
    List<Warning> getWarnings();

    /**
     * @return The errors that were thrown during the execution of the statement.
     * {@code null} if the statement hasn't been executed yet.
     */
    List<Error> getErrors();

    /**
     * Retrieves the current state of error suppression.
     *
     * @return {@code true} if errors should be suppressed or {@code false} if they should result in an exception being
     * thrown.
     * @deprecated Will be removed in Flyway 6.0. Use Error.isHandled() and Warning.isHandled() instead.
     */
    @Deprecated
    boolean isSuppressErrors();

    /**
     * Sets the current state of error suppression.
     *
     * @param suppressErrors {@code true} if errors should be suppressed or {@code false} if they should result in an
     *                       exception being thrown.
     * @deprecated Will be removed in Flyway 6.0. Use Error.setHandled() and Warning.setHandled() instead.
     */
    @Deprecated
    void setSuppressErrors(boolean suppressErrors);
}