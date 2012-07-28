/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.migration;

import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.exception.FlywayException;

/**
 * Parsing support for migrations that use the standard Flyway version + description embedding in their name. These
 * migrations have names like 1_2__Description .
 */
public class MigrationInfoHelper {
    /**
     * Prevents instantiation.
     */
    private MigrationInfoHelper() {
        //Do nothing.
    }

    /**
     * Extracts the schema version from a migration name formatted as 1_2__Description.
     *
     * @param migrationName The string to parse.
     *
     * @return The extracted schema version.
     */
    public static MigrationVersion extractVersion(String migrationName) {
        String rawVersion;

        // Handle the description
        int descriptionPos = migrationName.indexOf("__");
        if (descriptionPos < 0) {
            rawVersion = migrationName;
        } else {
            rawVersion = migrationName.substring(0, descriptionPos);
        }

        String version = rawVersion.replace("_", ".");

        if (version.startsWith(".")) {
            throw new FlywayException(
                    "Invalid version starting with a dot (.) instead of a digit or a letter: " + version);
        }

        return new MigrationVersion(version);
    }

    /**
     * Extracts the description from a migration name formatted as 1_2__Description.
     *
     * @param migrationName The string to parse.
     *
     * @return The extracted description.
     */
    public static String extractDescription(String migrationName) {
        // Handle the description
        int descriptionPos = migrationName.indexOf("__");
        if (descriptionPos >= 0) {
            return migrationName.substring(descriptionPos + 2).replaceAll("_", " ");
        }

        return null;
    }
}
