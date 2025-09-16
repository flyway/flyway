/*-
 * ========================LICENSE_START=================================
 * flyway-sqlserver
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
package org.flywaydb.clean;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.output.CleanResult;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.command.clean.CleanExecutor;
import org.flywaydb.core.internal.command.clean.CleanModeConfigurationExtension.Mode;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.jdbc.PlainExecutionTemplate;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;

import java.util.List;

public class CleanModeCleanExecutor extends CleanExecutor {
    private final String cleanMode;

    public CleanModeCleanExecutor(Connection connection, Database database, SchemaHistory schemaHistory, CallbackExecutor<Event> callbackExecutor, String cleanMode) {
        super(connection, database, schemaHistory, callbackExecutor);
        this.cleanMode = cleanMode;
    }

    public void clean(Schema defaultSchema, Schema[] schemas, CleanResult cleanResult, List<String> dropSchemas) {
        try {
            connection.changeCurrentSchemaTo(defaultSchema);
            clean(schemas, cleanResult, dropSchemas);
        } catch (FlywayException e) {
            callbackExecutor.onEvent(Event.AFTER_CLEAN_ERROR);
            throw e;
        }
    }

    @Override
    protected void doCleanSchema(Schema schema) {
        if (Mode.ALL.name().equalsIgnoreCase(cleanMode)) {
            new PlainExecutionTemplate(true).execute(() -> {
                schema.clean();
                return null;
            });
        } else {
            super.doCleanSchema(schema);
        }
    }
}
