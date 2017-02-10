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

import com.mongodb.MongoClient;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.MongoFlywayCallback;
import org.flywaydb.core.api.configuration.MongoFlywayConfiguration;
import org.flywaydb.core.internal.dbsupport.MongoScript;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.Scanner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MongoFlyway Callback, looking for Mongo javascript (named like on the callback methods) inside
 * the configured locations.
 */
public class MongoScriptFlywayCallback extends MongoFlywayCallback {
    private static final Log LOG = LogFactory.getLog(MongoScriptFlywayCallback.class);
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
            BEFORE_VALIDATE, AFTER_VALIDATE, BEFORE_BASELINE, AFTER_BASELINE,
            BEFORE_REPAIR, AFTER_REPAIR, BEFORE_INFO, AFTER_INFO);

    private final Map<String, MongoScript> scripts = new HashMap<String, MongoScript>();

    /**
     * Creates a new instance.
     *
     * @param scanner               The Scanner for loading migrations on the classpath.
     * @param locations             The locations where migrations are located.
     * @param placeholderReplacer   The placeholder replacer to use.
     * @param configuration         The Mongo configuration object
     */
    public MongoScriptFlywayCallback(Scanner scanner, Locations locations,
                                     PlaceholderReplacer placeholderReplacer,
                                     MongoFlywayConfiguration configuration) {
        super(configuration);
        String encoding = configuration.getEncoding();
        String mongoMigrationSuffix = configuration.getMongoMigrationSuffix();
        String databaseName = configuration.getDatabaseName();
        for (String callback : ALL_CALLBACKS) {
            scripts.put(callback, null);
        }

        LOG.debug("Scanning for MongoScript callbacks ...");
        for (Location location : locations.getLocations()) {
            Resource[] resources;
            try {
                resources = scanner.scanForResources(location, "", mongoMigrationSuffix);
            } catch (FlywayException e) {
                // Ignore missing locations
                continue;
            }
            for (Resource resource : resources) {
                String key = resource.getFilename().replace(mongoMigrationSuffix, "");
                if (scripts.keySet().contains(key)) {
                    MongoScript existing = scripts.get(key);
                    if (existing != null) {
                        throw new FlywayException("Found more than 1 Mongo callback script for " +
                                key + "!\n" + "Offenders:\n" +
                                "-> " + existing.getResource().getLocationOnDisk() + "\n" +
                                "-> " + resource.getLocationOnDisk());
                    }
                    scripts.put(key, new MongoScript(resource, encoding, databaseName, placeholderReplacer));
                }
            }
        }
    }

    @Override
    public void beforeClean(MongoClient client) {
        execute(BEFORE_CLEAN, client);
    }

    @Override
    public void afterClean(MongoClient client) {
        execute(AFTER_CLEAN, client);
    }

    @Override
    public void beforeMigrate(MongoClient client) {
        execute(BEFORE_MIGRATE, client);
    }

    @Override
    public void afterMigrate(MongoClient client) {
        execute(AFTER_MIGRATE, client);
    }

    @Override
    public void beforeEachMigrate(MongoClient client, MigrationInfo info) {
        execute(BEFORE_EACH_MIGRATE, client);
    }

    @Override
    public void afterEachMigrate(MongoClient client, MigrationInfo info) {
        execute(AFTER_EACH_MIGRATE, client);
    }

    @Override
    public void beforeValidate(MongoClient client) {
        execute(BEFORE_VALIDATE, client);
    }

    @Override
    public void afterValidate(MongoClient client) {
        execute(AFTER_VALIDATE, client);
    }

    @Override
    public void beforeBaseline(MongoClient client) {
        execute(BEFORE_BASELINE, client);
    }

    @Override
    public void afterBaseline(MongoClient client) {
        execute(AFTER_BASELINE, client);
    }

    @Override
    public void beforeRepair(MongoClient client) {
        execute(BEFORE_REPAIR, client);
    }

    @Override
    public void afterRepair(MongoClient client) {
        execute(AFTER_REPAIR, client);
    }

    @Override
    public void beforeInfo(MongoClient client) {
        execute(BEFORE_INFO, client);
    }

    @Override
    public void afterInfo(MongoClient client) {
        execute(AFTER_INFO, client);
    }
    
    private void execute(String key, MongoClient client) {
        MongoScript mongoScript = scripts.get(key);
        if (mongoScript != null) {
            LOG.info("Executing Mongo javaScript callback: " + key);
            mongoScript.execute(client);
        }
    }
}
