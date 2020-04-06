/*
 * Copyright 2010-2020 Redgate Software Ltd
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

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Schema;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class ParsingContext {
    private static final Log LOG = LogFactory.getLog(ParsingContext.class);

    private static final String DEFAULT_SCHEMA_PLACEHOLDER = "flyway:defaultSchema";
    private static final String USER_PLACEHOLDER = "flyway:user";
    private static final String DATABASE_PLACEHOLDER = "flyway:database";
    private static final String TIMESTAMP_PLACEHOLDER = "flyway:timestamp";

    private Map<String, String> placeholders = new HashMap<>();

    public Map<String, String> getPlaceholders() {
        return placeholders;
    }

    public void populate(Database database, Configuration configuration) {
        String defaultSchemaName = configuration.getDefaultSchema();
        String[] schemaNames = configuration.getSchemas();

        Schema currentSchema = getCurrentSchema(database);
        String catalog = getCatalog(database);
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
    }

    private String getCatalog(Database database) {
        try {
            return database.getMainConnection().getJdbcConnection().getCatalog();
        } catch (SQLException e) {
            LOG.debug("Could not get database name for " + DATABASE_PLACEHOLDER + " placeholder.");
            return null;
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