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
package org.flywaydb.core.internal.callback;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.callback.Error;

import java.util.List;

/**
 * Executes the callbacks for a specific event.
 */
public interface CallbackExecutor {
    /**
     * Executes the callbacks for this event on the main connection, within a separate transaction per callback if possible.
     *
     * @param event The vent to handle.
     */
    void onEvent(Event event);

    /**
     * Executes the callbacks for this event on the migration connection, within a separate transaction per callback if possible.
     *
     * @param event The vent to handle.
     */
    void onMigrateOrUndoEvent(Event event);

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
    void onEachMigrateOrUndoEvent(Event event);













}