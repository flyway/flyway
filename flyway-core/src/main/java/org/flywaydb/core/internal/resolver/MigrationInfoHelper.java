/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
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
        String cleanMigrationName = cleanMigrationName(migrationName, prefix, suffixes);

        // Handle the description
        int descriptionPos = cleanMigrationName.indexOf(separator);
        if (descriptionPos < 0) {
            throw new FlywayException("Wrong migration name format: " + migrationName
                    + "(It should look like this: "
                    + prefix + (repeatable ? "" : "1.2") + separator + "Description" + suffixes[0] + ")");
        }

        String version = cleanMigrationName.substring(0, descriptionPos);
        String description = cleanMigrationName.substring(descriptionPos + separator.length()).replaceAll("_", " ");
        if (StringUtils.hasText(version)) {
            if (repeatable) {
                throw new FlywayException("Wrong repeatable migration name format: " + migrationName
                        + "(It cannot contain a version and should look like this: "
                        + prefix + separator + description + suffixes[0] + ")");
            }
            return Pair.of(MigrationVersion.fromVersion(version), description);
        }
        if (!repeatable) {
            throw new FlywayException("Wrong versioned migration name format: " + migrationName
                    + "(It must contain a version and should look like this: "
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
