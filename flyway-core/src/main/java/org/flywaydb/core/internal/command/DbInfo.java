/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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
package org.flywaydb.core.internal.command;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.ValidatePatternUtils;

@RequiredArgsConstructor
public class DbInfo {
    private final CompositeMigrationResolver migrationResolver;
    private final SchemaHistory schemaHistory;
    private final Configuration configuration;
    private final Database database;
    private final CallbackExecutor callbackExecutor;
    private final Schema[] schemas;

    public MigrationInfoService info() {

        callbackExecutor.onEvent(Event.BEFORE_INFO);

        MigrationInfoServiceImpl migrationInfoService;
        try {
            migrationInfoService =
                    new MigrationInfoServiceImpl(migrationResolver, schemaHistory, database, configuration,
                                                 configuration.getTarget(), configuration.isOutOfOrder(), ValidatePatternUtils.getIgnoreAllPattern(), configuration.getCherryPick());
            migrationInfoService.refresh();
            migrationInfoService.setAllSchemasEmpty(schemas);
        } catch (FlywayException e) {
            callbackExecutor.onEvent(Event.AFTER_INFO_ERROR);
            throw e;
        }

        callbackExecutor.onEvent(Event.AFTER_INFO);

        return migrationInfoService;
    }
}