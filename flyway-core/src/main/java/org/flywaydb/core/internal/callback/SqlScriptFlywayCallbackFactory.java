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
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.util.placeholder.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.LoadableResource;
import org.flywaydb.core.internal.util.scanner.Scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flyway Callback factory, looking for SQL scripts (named like on the callback methods) inside the configured locations.
 */
public class SqlScriptFlywayCallbackFactory {
    private static final Log LOG = LogFactory.getLog(SqlScriptFlywayCallbackFactory.class);

    private final List<SqlScriptCallback> callbacks = new ArrayList<>();

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
        Map<String, SqlScript> callbacksFound = new HashMap<>();

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
                String name = stripSuffix(resource.getFilename(), configuration.getSqlMigrationSuffixes());
                String id;
                String description;
                int separatorIndex = name.indexOf(configuration.getSqlMigrationSeparator());
                if (separatorIndex >= 0) {
                    id = name.substring(0, separatorIndex);
                    description = name.substring(separatorIndex + configuration.getSqlMigrationSeparator().length());
                } else {
                    id = name;
                    description = null;
                }
                Event event = Event.fromId(id);
                if (event != null) {
                    SqlScript existing = callbacksFound.get(name);
                    if (existing != null) {
                        throw new FlywayException("Found more than 1 SQL callback script called " + name + "!\n" +
                                "Offenders:\n" +
                                "-> " + existing.getResource().getLocationOnDisk() + "\n" +
                                "-> " + resource.getLocationOnDisk());
                    }
                    SqlScript sqlScript = database.createSqlScript(resource,
                            placeholderReplacer,
                            configuration.isMixed()



                    );
                    callbacksFound.put(name, sqlScript);
                    callbacks.add(new SqlScriptCallback(event, description, sqlScript));
                }
            }
        }
        Collections.sort(callbacks);
    }

    public List<Callback> getCallbacks() {
        return new ArrayList<>(callbacks);
    }

    private String stripSuffix(String fileName, String[] suffixes) {
        for (String suffix : suffixes) {
            if (fileName.endsWith(suffix)) {
                return fileName.substring(0, fileName.length() - suffix.length());
            }
        }
        return fileName;
    }

    private static class SqlScriptCallback implements Callback, Comparable<SqlScriptCallback> {
        private final Event event;
        private final String description;
        private final SqlScript sqlScript;

        private SqlScriptCallback(Event event, String description, SqlScript sqlScript) {
            this.event = event;
            this.description = description;
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
                    + (description == null ? "" : " - " + description)
                    + (sqlScript.executeInTransaction() ? "" : " [non-transactional]"));
            sqlScript.execute(new JdbcTemplate(context.getConnection(), 0));
        }

        @Override
        public int compareTo(SqlScriptCallback o) {
            int result = event.compareTo(o.event);
            if (result == 0) {
                if (description == null) {
                    return Integer.MIN_VALUE;
                }
                if (o.description == null) {
                    return Integer.MAX_VALUE;
                }
                result = description.compareTo(o.description);
            }
            return result;
        }
    }
}