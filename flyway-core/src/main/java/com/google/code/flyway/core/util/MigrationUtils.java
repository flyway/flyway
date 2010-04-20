/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.google.code.flyway.core.util;

import com.google.code.flyway.core.SchemaVersion;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Collection of utility methods used in migrations.
 *
 * @author Axel Fontaine
 */
public class MigrationUtils {
    /**
     * Prevents instantiation.
     */
    private MigrationUtils() {
        //Nothing
    }

    /**
     * Executes this sql script using this jdbcTemplate.
     *
     * @param jdbcTemplate   To execute the script.
     * @param scriptResource Classpath resource containing the script.
     */
    public static void executeSqlScript(SimpleJdbcTemplate jdbcTemplate, Resource scriptResource) {
        Reader reader = null;
        try {
            reader = new InputStreamReader(scriptResource.getInputStream(), "UTF-8");
            String[] sqlStatements = SqlScriptParser.parse(reader);
            for (String sqlStatement : sqlStatements) {
                jdbcTemplate.update(sqlStatement);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to execute sql script: " + scriptResource.getFilename(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    /**
     * Extracts the schema version from a migration name formatted as V1_2__Description.
     *
     * @param migrationName The string to parse.
     * @return The extracted schema version.
     */
    public static SchemaVersion extractSchemaVersion(String migrationName) {
        // Drop the leading V
        String version = migrationName.substring(1);

        // Handle the description
        String description = null;
        int descriptionPos = version.indexOf("__");
        if (descriptionPos != -1) {
            description = version.substring(descriptionPos + 2).replaceAll("_", " ");
            version = version.substring(0, descriptionPos);
        }

        return new SchemaVersion(version.replace("_", "."), description);
    }
}
