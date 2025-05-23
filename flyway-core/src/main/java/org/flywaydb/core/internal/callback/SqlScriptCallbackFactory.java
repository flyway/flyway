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

import lombok.CustomLog;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

import java.util.*;

/**
 * Callback factory, looking for SQL scripts (named like on the callback methods) inside the configured locations.
 */
@CustomLog
public class SqlScriptCallbackFactory {

    private final List<SqlScriptCallback> callbacks = new ArrayList<>();

    /**
     * Creates a new instance.
     *
     * @param resourceProvider The resource provider.
     * @param sqlScriptFactory The SQL statement factory.
     * @param configuration The Flyway configuration.
     */
    public SqlScriptCallbackFactory(ResourceProvider resourceProvider,
                                    SqlScriptExecutorFactory sqlScriptExecutorFactory,
                                    SqlScriptFactory sqlScriptFactory,
                                    Configuration configuration) {
        Map<String, SqlScript> callbacksFound = new HashMap<>();

        LOG.debug("Scanning for SQL callbacks ...");
        Collection<LoadableResource> resources = resourceProvider.getResources("", configuration.getSqlMigrationSuffixes());
        ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);

        for (LoadableResource resource : resources) {
            ResourceName parsedName = resourceNameParser.parse(resource.getFilename());
            if (!parsedName.isValid()) {
                continue;
            }

            String name = parsedName.getFilenameWithoutSuffix();
            Event event = Event.fromId(parsedName.getPrefix());
            if (event != null) {
                SqlScript existing = callbacksFound.get(name);
                if (existing != null) {
                    throw new FlywayException("Found more than 1 SQL callback script called " + name + "!\n" +
                                                      "Offenders:\n" +
                                                      "-> " + existing.getResource().getAbsolutePathOnDisk() + "\n" +
                                                      "-> " + resource.getAbsolutePathOnDisk());
                }
                SqlScript sqlScript = sqlScriptFactory.createSqlScript(resource, configuration.isMixed(), resourceProvider);
                callbacksFound.put(name, sqlScript);

                boolean batch = configuration.isBatch();

                callbacks.add(new SqlScriptCallback(event, parsedName.getDescription(), sqlScriptExecutorFactory, sqlScript, batch));
            }
        }
        Collections.sort(callbacks);
    }

    public List<Callback> getCallbacks() {
        return new ArrayList<>(callbacks);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class SqlScriptCallback implements Callback, Comparable<SqlScriptCallback> {
        private final Event event;
        private final String description;
        private final SqlScriptExecutorFactory sqlScriptExecutorFactory;
        private final SqlScript sqlScript;
        private final boolean batch;

        @Override
        public boolean supports(Event event, Context context) {
            return this.event == event;
        }

        @Override
        public boolean canHandleInTransaction(Event event, Context context) {
            return sqlScript.executeInTransaction();
        }

        @Override
        public void handle(Event event, Context context) {
            if (!sqlScript.shouldExecute()) {
                LOG.debug("Not executing SQL callback: " + event.getId() + (description == null ? "" : " - " + description));
                return;
            }

            LOG.info("Executing SQL callback: " + event.getId()
                             + (description == null ? "" : " - " + description)
                             + (sqlScript.executeInTransaction() ? "" : " [non-transactional]"));

            boolean outputQueryResults = context.getConfiguration().isOutputQueryResults();

            sqlScriptExecutorFactory.createSqlScriptExecutor(context.getConnection(), false, batch, outputQueryResults).execute(sqlScript, context.getConfiguration());
        }

        @Override
        public String getCallbackName() {
            return description;
        }

        @Override
        public int compareTo(SqlScriptCallback o) {
            int result = event.compareTo(o.event);
            if (result == 0) {
                if (description == null) {
                    return -1;
                }
                if (o.description == null) {
                    return 1;
                }
                result = description.compareTo(o.description);
            }
            return result;
        }
    }
}
