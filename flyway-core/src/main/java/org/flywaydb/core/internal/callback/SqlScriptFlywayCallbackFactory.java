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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.database.SqlScript;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.scanner.LoadableResource;
import org.flywaydb.core.internal.util.scanner.Scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flyway Callback factory, looking for SQL scripts (named like on the callback methods) inside the configured locations.
 */
public class SqlScriptFlywayCallbackFactory {
    private static final Log LOG = LogFactory.getLog(SqlScriptFlywayCallbackFactory.class);

    private final List<Callback> callbacks = new ArrayList<>();

    /**
     * Creates a new instance.
     *
     * @param database            The database-specific support.
     * @param scanner             The Scanner for loading migrations on the classpath.
     * @param locations           The locations where migrations are located.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @param configuration       The Flyway configuration.
     */
    public SqlScriptFlywayCallbackFactory(Database database, Scanner scanner, List<Location> locations,
                                          PlaceholderReplacer placeholderReplacer, Configuration configuration) {
        Map<Event, SqlScript> callbacksFound = new HashMap<>();

        LOG.debug("Scanning for SQL callbacks ...");
        for (Location location : locations) {
            LoadableResource[] resources;
            try {
                resources = scanner.scanForResources(location, "", configuration.getSqlMigrationSuffixes());
            } catch (FlywayException e) {
                // Ignore missing locations
                continue;
            }
            for (LoadableResource resource : resources) {
                String key = stripSuffix(resource.getFilename(), configuration.getSqlMigrationSuffixes());
                Event event = Event.fromId(key);
                if (event != null) {
                    SqlScript existing = callbacksFound.get(event);
                    if (existing != null) {
                        throw new FlywayException("Found more than 1 SQL callback script for " + key + "!\n" +
                                "Offenders:\n" +
                                "-> " + existing.getResource().getLocationOnDisk() + "\n" +
                                "-> " + resource.getLocationOnDisk());
                    }
                    SqlScript sqlScript = database.createSqlScript(resource,
                            placeholderReplacer.replacePlaceholders(resource.loadAsString(configuration.getEncoding())),
                            configuration.isMixed()



                    );
                    callbacksFound.put(event, sqlScript);
                    callbacks.add(new SqlScriptCallback(event, sqlScript));
                }
            }
        }
    }

    public List<Callback> getCallbacks() {
        return callbacks;
    }

    private String stripSuffix(String fileName, String[] suffixes) {
        for (String suffix : suffixes) {
            if (fileName.endsWith(suffix)) {
                return fileName.substring(0, fileName.length() - suffix.length());
            }
        }
        return fileName;
    }

    private static class SqlScriptCallback implements Callback {
        private final Event event;
        private final SqlScript sqlScript;

        private SqlScriptCallback(Event event, SqlScript sqlScript) {
            this.event = event;
            this.sqlScript = sqlScript;
        }

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
            LOG.info("Executing SQL callback: " + event.getId()
                    + (sqlScript.executeInTransaction() ? "" : " [non-transactional]"));
            sqlScript.execute(new JdbcTemplate(context.getConnection(), 0));
        }
    }
}