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
package org.flywaydb.core.internal.command.clean;

import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.output.CleanResult;
import org.flywaydb.core.internal.callback.CallbackExecutor;
import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.jdbc.ExecutionTemplateFactory;
import org.flywaydb.core.internal.schemahistory.SchemaHistory;
import org.flywaydb.core.internal.util.StopWatch;
import org.flywaydb.core.internal.util.TimeFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CustomLog
public class CleanExecutor {
    protected final Connection connection;
    protected final Database database;
    protected final SchemaHistory schemaHistory;
    protected final CallbackExecutor<Event> callbackExecutor;

    public CleanExecutor(Connection connection, Database database, SchemaHistory schemaHistory, CallbackExecutor<Event> callbackExecutor) {
        this.connection = connection;
        this.database = database;
        this.schemaHistory = schemaHistory;
        this.callbackExecutor = callbackExecutor;
    }

    public void clean(Schema defaultSchema, Schema[] schemas, CleanResult cleanResult) {
        try {
            connection.changeCurrentSchemaTo(defaultSchema);

            List<String> dropSchemas = new ArrayList<>();
            try {
                dropSchemas = schemaHistory.getSchemasCreatedByFlyway();
            } catch (Exception e) {
                LOG.error("Error while checking whether the schemas should be dropped. Schemas will not be dropped", e);
            }

            clean(schemas, cleanResult, dropSchemas);
        } catch (FlywayException e) {
            callbackExecutor.onEvent(Event.AFTER_CLEAN_ERROR);
            throw e;
        }
    }

    protected void clean(Schema[] schemas, CleanResult cleanResult, List<String> dropSchemas) {
        dropDatabaseObjectsPreSchemas();

        List<Schema> schemaList = new ArrayList<>(Arrays.asList(schemas));
        for (int i = 0; i < schemaList.size(); ) {
            Schema schema = schemaList.get(i);
            if (!schema.exists()) {
                String unknownSchemaWarning = "Unable to clean unknown schema: " + schema;
                cleanResult.addWarning(unknownSchemaWarning);
                LOG.warn(unknownSchemaWarning);
                schemaList.remove(i);
            } else {
                i++;
            }
        }
        cleanSchemas(schemaList.toArray(Schema[]::new), dropSchemas, cleanResult);
        Collections.reverse(schemaList);
        cleanSchemas(schemaList.toArray(Schema[]::new), dropSchemas, null);

        dropDatabaseObjectsPostSchemas(schemas);

        for (Schema schema : schemas) {
            if (dropSchemas.contains(schema.getName())) {
                dropSchema(schema, cleanResult);
            }
        }
    }

    private void dropDatabaseObjectsPreSchemas() {
        LOG.debug("Dropping pre-schema database level objects...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(), database, true).execute(() -> {
                database.cleanPreSchemas();
                return null;
            });
            stopWatch.stop();
            LOG.info(String.format("Successfully dropped pre-schema database level objects (execution time %s)",
                                   TimeFormat.format(stopWatch.getTotalTimeMillis())));
        } catch (FlywaySqlException e) {
            LOG.debug(e.getMessage());
            LOG.warn("Unable to drop pre-schema database level objects");
        }
    }

    private void dropDatabaseObjectsPostSchemas(Schema[] schemas) {
        LOG.debug("Dropping post-schema database level objects...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(), database, true).execute(() -> {
                database.cleanPostSchemas(schemas);
                return null;
            });
            stopWatch.stop();
            LOG.info(String.format("Successfully dropped post-schema database level objects (execution time %s)",
                                   TimeFormat.format(stopWatch.getTotalTimeMillis())));
        } catch (FlywaySqlException e) {
            LOG.debug(e.getMessage());
            LOG.warn("Unable to drop post-schema database level objects");
        }
    }

    private void dropSchema(final Schema schema, CleanResult cleanResult) {
        LOG.debug("Dropping schema " + schema + "...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(), database, true).execute(() -> {
                schema.drop();
                return null;
            });

            cleanResult.schemasDropped.add(schema.getName());

            stopWatch.stop();
            LOG.info(String.format("Successfully dropped schema %s (execution time %s)",
                                   schema, TimeFormat.format(stopWatch.getTotalTimeMillis())));
        } catch (FlywaySqlException e) {
            LOG.debug(e.getMessage());
            LOG.warn("Unable to drop schema " + schema + ". It was cleaned instead.");
            cleanResult.schemasCleaned.add(schema.getName());
        }
    }

    private void cleanSchemas(Schema[] schemas, List<String> dropSchemas, CleanResult cleanResult) {
        for (Schema schema : schemas) {
            if (dropSchemas.contains(schema.getName())) {
                try {
                    cleanSchema(schema);
                } catch (FlywayException ignored) {
                }
            } else {
                cleanSchema(schema);
                if (cleanResult != null) {
                    cleanResult.schemasCleaned.add(schema.getName());
                }
            }
        }
    }

    private void cleanSchema(Schema schema) {
        LOG.debug("Cleaning schema " + schema + "...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        doCleanSchema(schema);
        stopWatch.stop();
        LOG.info(String.format("Successfully cleaned schema %s (execution time %s)", schema, TimeFormat.format(stopWatch.getTotalTimeMillis())));
    }

    protected void doCleanSchema(Schema schema) {
        ExecutionTemplateFactory.createExecutionTemplate(connection.getJdbcConnection(), database, true).execute(() -> {
            schema.clean();
            return null;
        });
    }
}
