/**
 * Copyright 2010-2015 Boxfuse GmbH
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
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.SqlScript;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.Scanner;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Flyway Callback, looking for SQL scripts (named like on the callback methods) inside the configured locations.
 */
public class SqlScriptFlywayCallback implements FlywayCallback {
    private static final Log LOG = LogFactory.getLog(SqlScriptFlywayCallback.class);

    private final Map<String, SqlScript> scripts = new HashMap<String, SqlScript>();

    /**
     * Creates a new instance.
     *
     * @param dbSupport           The database-specific support.
     * @param scanner             The Scanner for loading migrations on the classpath.
     * @param locations           The locations where migrations are located.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @param encoding            The encoding of Sql migrations.
     * @param sqlMigrationSuffix  The suffix for sql migrations
     */
    public SqlScriptFlywayCallback(DbSupport dbSupport, Scanner scanner, Locations locations,
                                   PlaceholderReplacer placeholderReplacer, String encoding, String sqlMigrationSuffix) {
        scripts.put("beforeClean", null);
        scripts.put("afterClean", null);
        scripts.put("beforeMigrate", null);
        scripts.put("afterMigrate", null);
        scripts.put("beforeEachMigrate", null);
        scripts.put("afterEachMigrate", null);
        scripts.put("beforeValidate", null);
        scripts.put("afterValidate", null);
        scripts.put("beforeBaseline", null);
        scripts.put("afterBaseline", null);
        scripts.put("beforeRepair", null);
        scripts.put("afterRepair", null);
        scripts.put("beforeInfo", null);
        scripts.put("afterInfo", null);

        for (Location location : locations.getLocations()) {
            Resource[] resources;
            try {
                resources = scanner.scanForResources(location, "", sqlMigrationSuffix);
            } catch (FlywayException e) {
                // Ignore missing locations
                continue;
            }
            for (Resource resource : resources) {
                String key = resource.getFilename().replace(sqlMigrationSuffix, "");
                if (scripts.keySet().contains(key)) {
                    SqlScript existing = scripts.get(key);
                    if (existing != null) {
                        throw new FlywayException("Found more than 1 SQL callback script for " + key + "!\n" +
                                "Offenders:\n" +
                                "-> " + existing.getResource().getLocationOnDisk() + "\n" +
                                "-> " + resource.getLocationOnDisk());
                    }
                    scripts.put(key, new SqlScript(dbSupport, resource, placeholderReplacer, encoding));
                }
            }
        }
    }

    @Override
    public void beforeClean(Connection connection) {
        execute("beforeClean", connection);
    }

    @Override
    public void afterClean(Connection connection) {
        execute("afterClean", connection);
    }

    @Override
    public void beforeMigrate(Connection connection) {
        execute("beforeMigrate", connection);
    }

    @Override
    public void afterMigrate(Connection connection) {
        execute("afterMigrate", connection);
    }

    @Override
    public void beforeEachMigrate(Connection connection, MigrationInfo info) {
        execute("beforeEachMigrate", connection);
    }

    @Override
    public void afterEachMigrate(Connection connection, MigrationInfo info) {
        execute("afterEachMigrate", connection);
    }

    @Override
    public void beforeValidate(Connection connection) {
        execute("beforeValidate", connection);
    }

    @Override
    public void afterValidate(Connection connection) {
        execute("afterValidate", connection);
    }

    @Override
    public void beforeBaseline(Connection connection) {
        execute("beforeBaseline", connection);
    }

    @Override
    public void afterBaseline(Connection connection) {
        execute("afterBaseline", connection);
    }

    @Override
    public void beforeRepair(Connection connection) {
        execute("beforeRepair", connection);
    }

    @Override
    public void afterRepair(Connection connection) {
        execute("afterRepair", connection);
    }

    @Override
    public void beforeInfo(Connection connection) {
        execute("beforeInfo", connection);
    }

    @Override
    public void afterInfo(Connection connection) {
        execute("afterInfo", connection);
    }

    private void execute(String key, Connection connection) {
        SqlScript sqlScript = scripts.get(key);
        if (sqlScript != null) {
            LOG.info("Executing SQL callback: " + key);
            sqlScript.execute(new JdbcTemplate(connection, 0));
        }
    }
}
