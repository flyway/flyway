/*
 * Copyright 2010-2017 Boxfuse GmbH
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
import org.flywaydb.core.api.configuration.FlywayConfiguration;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flyway Callback, looking for SQL scripts (named like on the callback methods) inside the configured locations.
 */
public class SqlScriptFlywayCallback implements FlywayCallback {
    private static final Log LOG = LogFactory.getLog(SqlScriptFlywayCallback.class);
    private static final String BEFORE_CLEAN = "beforeClean";
    private static final String AFTER_CLEAN = "afterClean";
    private static final String BEFORE_MIGRATE = "beforeMigrate";
    private static final String AFTER_MIGRATE = "afterMigrate";
    private static final String BEFORE_EACH_MIGRATE = "beforeEachMigrate";
    private static final String AFTER_EACH_MIGRATE = "afterEachMigrate";
    private static final String BEFORE_VALIDATE = "beforeValidate";
    private static final String AFTER_VALIDATE = "afterValidate";
    private static final String BEFORE_BASELINE = "beforeBaseline";
    private static final String AFTER_BASELINE = "afterBaseline";
    private static final String BEFORE_REPAIR = "beforeRepair";
    private static final String AFTER_REPAIR = "afterRepair";
    private static final String BEFORE_INFO = "beforeInfo";
    private static final String AFTER_INFO = "afterInfo";

    public static final List<String> ALL_CALLBACKS = Arrays.asList(
            BEFORE_CLEAN, AFTER_CLEAN,
            BEFORE_MIGRATE, BEFORE_EACH_MIGRATE, AFTER_EACH_MIGRATE, AFTER_MIGRATE,
            BEFORE_VALIDATE, AFTER_VALIDATE,
            BEFORE_BASELINE, AFTER_BASELINE,
            BEFORE_REPAIR, AFTER_REPAIR,
            BEFORE_INFO, AFTER_INFO);

    private final Map<String, SqlScript> scripts = new HashMap<String, SqlScript>();

    /**
     * Creates a new instance.
     *
     * @param dbSupport           The database-specific support.
     * @param scanner             The Scanner for loading migrations on the classpath.
     * @param locations           The locations where migrations are located.
     * @param placeholderReplacer The placeholder replacer to apply to sql migration scripts.
     * @param configuration       The Flyway configuration.
     */
    public SqlScriptFlywayCallback(DbSupport dbSupport, Scanner scanner, Locations locations,
                                   PlaceholderReplacer placeholderReplacer, FlywayConfiguration configuration) {
        for (String callback : ALL_CALLBACKS) {
            scripts.put(callback, null);
        }

        LOG.debug("Scanning for SQL callbacks ...");
        for (Location location : locations.getLocations()) {
            Resource[] resources;
            try {
                resources = scanner.scanForResources(location, "", configuration.getSqlMigrationSuffix());
            } catch (FlywayException e) {
                // Ignore missing locations
                continue;
            }
            for (Resource resource : resources) {
                String key = resource.getFilename().replace(configuration.getSqlMigrationSuffix(), "");
                if (scripts.keySet().contains(key)) {
                    SqlScript existing = scripts.get(key);
                    if (existing != null) {
                        throw new FlywayException("Found more than 1 SQL callback script for " + key + "!\n" +
                                "Offenders:\n" +
                                "-> " + existing.getResource().getLocationOnDisk() + "\n" +
                                "-> " + resource.getLocationOnDisk());
                    }
                    scripts.put(key, new SqlScript(dbSupport, resource, placeholderReplacer, configuration.getEncoding(), configuration.isMixed()));
                }
            }
        }
    }

    @Override
    public void beforeClean(Connection connection) {
        execute(BEFORE_CLEAN, connection);
    }

    @Override
    public void afterClean(Connection connection) {
        execute(AFTER_CLEAN, connection);
    }

    @Override
    public void beforeMigrate(Connection connection) {
        execute(BEFORE_MIGRATE, connection);
    }

    @Override
    public void afterMigrate(Connection connection) {
        execute(AFTER_MIGRATE, connection);
    }

    @Override
    public void beforeEachMigrate(Connection connection, MigrationInfo info) {
        execute(BEFORE_EACH_MIGRATE, connection);
    }

    @Override
    public void afterEachMigrate(Connection connection, MigrationInfo info) {
        execute(AFTER_EACH_MIGRATE, connection);
    }

    @Override
    public void beforeValidate(Connection connection) {
        execute(BEFORE_VALIDATE, connection);
    }

    @Override
    public void afterValidate(Connection connection) {
        execute(AFTER_VALIDATE, connection);
    }

    @Override
    public void beforeBaseline(Connection connection) {
        execute(BEFORE_BASELINE, connection);
    }

    @Override
    public void afterBaseline(Connection connection) {
        execute(AFTER_BASELINE, connection);
    }

    @Override
    public void beforeRepair(Connection connection) {
        execute(BEFORE_REPAIR, connection);
    }

    @Override
    public void afterRepair(Connection connection) {
        execute(AFTER_REPAIR, connection);
    }

    @Override
    public void beforeInfo(Connection connection) {
        execute(BEFORE_INFO, connection);
    }

    @Override
    public void afterInfo(Connection connection) {
        execute(AFTER_INFO, connection);
    }

    private void execute(String key, Connection connection) {
        SqlScript sqlScript = scripts.get(key);
        if (sqlScript != null) {
            LOG.info("Executing SQL callback: " + key);
            sqlScript.execute(new JdbcTemplate(connection, 0));
        }
    }
}
