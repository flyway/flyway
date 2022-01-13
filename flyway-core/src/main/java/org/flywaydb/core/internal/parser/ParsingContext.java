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
package org.flywaydb.core.internal.parser;

import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.resource.ResourceName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@CustomLog
public class ParsingContext {
    private static final String DEFAULT_SCHEMA_PLACEHOLDER = "flyway:defaultSchema";
    private static final String USER_PLACEHOLDER = "flyway:user";
    private static final String DATABASE_PLACEHOLDER = "flyway:database";
    private static final String TIMESTAMP_PLACEHOLDER = "flyway:timestamp";
    private static final String FILENAME_PLACEHOLDER = "flyway:filename";
    private static final String WORKING_DIRECTORY_PLACEHOLDER = "flyway:workingDirectory";
    private static final String TABLE_PLACEHOLDER = "flyway:table";

    @Getter
    private final Map<String, String> placeholders = new HashMap<>();
    @Getter
    @Setter
    private Database database;

    public void populate(Database database, Configuration configuration) {
        setDatabase(database);

        String defaultSchemaName = configuration.getDefaultSchema();
        String[] schemaNames = configuration.getSchemas();

        Schema currentSchema = getCurrentSchema(database);
        String catalog = database.getCatalog();
        String currentUser = getCurrentUser(database);

        // cf. Flyway.prepareSchemas()
        if (defaultSchemaName == null) {
            if (schemaNames.length > 0) {
                defaultSchemaName = schemaNames[0];
            } else {
                defaultSchemaName = currentSchema.getName();
            }
        }

        if (defaultSchemaName != null) {
            placeholders.put(DEFAULT_SCHEMA_PLACEHOLDER, defaultSchemaName);
        }

        if (catalog != null) {
            placeholders.put(DATABASE_PLACEHOLDER, catalog);
        }

        placeholders.put(USER_PLACEHOLDER, currentUser);
        placeholders.put(TIMESTAMP_PLACEHOLDER, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        placeholders.put(WORKING_DIRECTORY_PLACEHOLDER, System.getProperty("user.dir"));
        placeholders.put(TABLE_PLACEHOLDER, configuration.getTable());
    }

    public void updateFilenamePlaceholder(ResourceName resourceName) {
        if (resourceName.isValid()) {
            placeholders.put(FILENAME_PLACEHOLDER, resourceName.getFilename());
        } else {
            placeholders.remove(FILENAME_PLACEHOLDER);
        }
    }

    private Schema getCurrentSchema(Database database) {
        try {
            return database.getMainConnection().getCurrentSchema();
        } catch (FlywayException e) {
            LOG.debug("Could not get schema for " + DEFAULT_SCHEMA_PLACEHOLDER + " placeholder.");
            return null;
        }
    }

    private String getCurrentUser(Database database) {
        try {
            return database.getCurrentUser();
        } catch (FlywayException e) {
            LOG.debug("Could not get user for " + USER_PLACEHOLDER + " placeholder.");
            return null;
        }
    }
}