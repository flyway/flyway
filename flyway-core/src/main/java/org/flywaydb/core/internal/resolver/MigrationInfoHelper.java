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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

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
     * @param prefix        The migration prefix.
     * @param separator     The migration separator.
     * @param suffixes      The migration suffixes.
     * @param repeatable    Whether this is a repeatable migration.
     * @return The extracted schema version.
     * @throws FlywayException if the migration name does not follow the standard conventions.
     */
    public static Pair<MigrationVersion, String> extractVersionAndDescription(String migrationName,
                                                                              String prefix, String separator,
                                                                              String[] suffixes, boolean repeatable) {
        // Only handles Java migrations now
        String cleanMigrationName = cleanMigrationName(migrationName, prefix, suffixes);

        int separatorPos = cleanMigrationName.indexOf(separator);

        String version;
        String description;
        if (separatorPos < 0) {
            version = cleanMigrationName;
            description = "";
        } else {
            version = cleanMigrationName.substring(0, separatorPos);
            description = cleanMigrationName.substring(separatorPos + separator.length()).replace("_", " ");
        }

        if (StringUtils.hasText(version)) {
            if (repeatable) {
                throw new FlywayException("Wrong repeatable migration name format: " + migrationName
                        + " (It cannot contain a version and should look like this: "
                        + prefix + separator + description + suffixes[0] + ")");
            }
            try {
                return Pair.of(MigrationVersion.fromVersion(version), description);
            } catch (Exception e) {
                throw new FlywayException("Wrong versioned migration name format: " + migrationName
                        + " (could not recognise version number " + version + ")", e);
            }
        }

        if (!repeatable) {
            throw new FlywayException("Wrong versioned migration name format: " + migrationName
                    + " (It must contain a version and should look like this: "
                    + prefix + "1.2" + separator + description + suffixes[0] + ")");
        }
        return Pair.of(null, description);
    }

    private static String cleanMigrationName(String migrationName, String prefix, String[] suffixes) {
        for (String suffix : suffixes) {
            if (migrationName.endsWith(suffix)) {
                return migrationName.substring(
                        StringUtils.hasLength(prefix) ? prefix.length() : 0,
                        migrationName.length() - suffix.length());
            }
        }
        return migrationName;
    }
}