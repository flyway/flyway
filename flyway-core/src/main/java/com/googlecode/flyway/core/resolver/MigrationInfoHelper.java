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
package com.googlecode.flyway.core.resolver;

import com.googlecode.flyway.core.api.FlywayException;
import com.googlecode.flyway.core.api.MigrationVersion;
import com.googlecode.flyway.core.util.Pair;

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
     * Extracts the schema version and the description from a migration name formatted as 1_2__Description.
     *
     * @param migrationName The migration name to parse. Should not contain any folders or packages.
     * @return The extracted schema version.
     * @throws FlywayException if the migration name does not follow the standard conventions.
     */
    public static Pair<MigrationVersion, String> extractVersionAndDescription(String migrationName, String prefix, String suffix) {
        String cleanMigrationName = migrationName.substring(prefix.length(), migrationName.length() - suffix.length());

        String version;
        String description;

        // Handle the description
        int descriptionPos = cleanMigrationName.indexOf("__");
        if (descriptionPos < 0) {
            throw new FlywayException("Wrong migration name format: " + migrationName + "(It should look like this: " + prefix + "1_2__Description" + suffix + ")");
        } else {
            version = cleanMigrationName.substring(0, descriptionPos).replace("_", ".");
            description = cleanMigrationName.substring(descriptionPos + 2).replaceAll("_", " ");
        }

        if (version.startsWith(".")) {
            throw new FlywayException(
                    "Invalid version starting with a dot (.) instead of a digit or a letter: " + version);
        }

        return Pair.of(new MigrationVersion(version), description);
    }
}
