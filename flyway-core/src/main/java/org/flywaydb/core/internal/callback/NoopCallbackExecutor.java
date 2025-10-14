/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
import java.util.Collections;
import java.util.List;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.Error;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.callback.Warning;
import org.flywaydb.core.api.output.OperationResult;

/**
 * A callback executor that does nothing.
 */
public enum NoopCallbackExecutor implements CallbackExecutor<Event> {
    INSTANCE;

    @Override
    public Collection<String> onEvent(final Event event) {
        return Collections.emptyList();
    }

    @Override
    public void onMigrateOrUndoEvent(final Event event) {
    }

    @Override
    public void setMigrationInfo(final MigrationInfo migrationInfo) {
    }

    @Override
    public void onEachMigrateOrUndoEvent(final Event event) {
    }

    @Override
    public void onOperationFinishEvent(final Event event, final OperationResult operationResult) {
    }

    @Override
    public void onEachMigrateOrUndoStatementEvent(final Event event,
        final String sql,
        final List<Warning> warnings,
        final List<Error> errors) {
    }
}
