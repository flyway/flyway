/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.core.internal.callback;

import java.util.Collection;
import java.util.List;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.CallbackEvent;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.output.OperationResult;

/**
 * Executes the callbacks for a specific event.
 */
public interface CallbackExecutor<E extends CallbackEvent<E>> {
    /**
     * Executes the callbacks for this event on the main connection, within a separate transaction per callback if
     * possible.
     *
     * @param event The event to handle.
     * @return The names of the executed callbacks
     */
    Collection<String> onEvent(E event);

    /**
     * Executes the callbacks for this event on the migration connection, within a separate transaction per callback if
     * possible.
     *
     * @param event The event to handle.
     */
    void onMigrateOrUndoEvent(E event);

    /**
     * Sets the current migration info.
     *
     * @param migrationInfo The current migration.
     */
    void setMigrationInfo(MigrationInfo migrationInfo);

    /**
     * Executes the callbacks for an "each" event within the same transaction (if any) as the main operation.
     *
     * @param event The event to handle.
     */
    void onEachMigrateOrUndoEvent(E event);

    /**
     * Executes the callbacks for an "each statement" event within the same transaction (if any) as the main operation.
     *
     * @param event    The event to handle.
     * @param sql      The sql from the statement.
     * @param warnings The warnings from the statement. {@code null} if it hasn't been executed yet.
     * @param errors   The errors from the statement. {@code null} if it hasn't been executed yet.
     */
    void onEachMigrateOrUndoStatementEvent(E event, String sql, List<Warning> warnings, List<Error> errors);

    /**
     * Executes the callbacks for an operation finish event.
     *
     * @param event           The event to handle.
     * @param operationResult The operation result.
     */
    void onOperationFinishEvent(E event, OperationResult operationResult);
}
