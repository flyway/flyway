/*
 * Copyright (C) Red Gate Software Ltd 2010-2021
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
package org.flywaydb.core.api.pattern;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

import java.util.Arrays;
import java.util.List;

public class ValidatePattern {
    private final String migrationType;
    private final String migrationState;
    private static final List<String> validMigrationTypes = Arrays.asList("*", "repeatable", "versioned");
    private static final List<String> validMigrationStates = Arrays.asList(
            "*",
            MigrationState.MISSING_SUCCESS.getDisplayName().toLowerCase(),
            MigrationState.PENDING.getDisplayName().toLowerCase(),
            MigrationState.IGNORED.getDisplayName().toLowerCase(),
            MigrationState.FUTURE_SUCCESS.getDisplayName().toLowerCase());

    private ValidatePattern(String migrationType, String migrationState) {
        this.migrationType = migrationType;
        this.migrationState = migrationState;
    }

    public static ValidatePattern fromPattern(String pattern) {
        if (pattern == null) {
            throw new FlywayException("Null pattern not allowed.");
        }

        String[] patternParts = pattern.split(":");

        if (patternParts.length != 2) {
            throw new FlywayException("Invalid pattern '" + pattern + "'. Pattern must be of the form <migration_type>:<migration_state> " +
                    "See " +
                    FlywayDbWebsiteLinks.IGNORE_MIGRATION_PATTERNS + " for full details");
        }

        String migrationType = patternParts[0].trim().toLowerCase();
        String migrationState = patternParts[1].trim().toLowerCase();

        if (!validMigrationTypes.contains(migrationType)) {
            throw new FlywayException("Invalid migration type '" + patternParts[0] + "'. Valid types are: " + validMigrationTypes);
        }

        if (!validMigrationStates.contains(migrationState)) {
            throw new FlywayException("Invalid migration state '" + patternParts[1] + "'. Valid states are: " + validMigrationStates);
        }

        return new ValidatePattern(migrationType, migrationState);
    }

    public boolean matchesMigration(boolean isVersioned, MigrationState state) {
        if (!state.getDisplayName().equalsIgnoreCase(migrationState) && !migrationState.equals("*")) {
            return false;
        }

        if (migrationType.equals("*")) {
            return true;
        }
        if (isVersioned && migrationType.equalsIgnoreCase("versioned")) {
            return true;
        }
        if (!isVersioned && migrationType.equalsIgnoreCase("repeatable")) {
            return true;
        }

        return false;
    }
}