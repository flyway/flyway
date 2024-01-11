package org.flywaydb.core.api.pattern;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.internal.license.FlywayRedgateEditionRequiredException;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidatePattern {
    private final String migrationType;
    private final String migrationState;
    private static final List<String> validMigrationTypes = Arrays.asList("*", "repeatable", "versioned");
    private static final List<String> validMigrationStates = Arrays.asList(
            "*",
            MigrationState.MISSING_SUCCESS.getPattern(),
            MigrationState.PENDING.getPattern(),
            MigrationState.IGNORED.getPattern(),
            MigrationState.FUTURE_SUCCESS.getPattern());
    private final String pattern;

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


        if (migrationType.equals("repeatable") || migrationType.equals("versioned")) {
            throw new FlywayRedgateEditionRequiredException("ignoreMigrationPattern with type '" + migrationType + "'");
        }


        if (!validMigrationTypes.contains(migrationType)) {
            throw new FlywayException("Invalid migration type '" + patternParts[0] + "'. Valid types are: " + validMigrationTypes);
        }

        if (!validMigrationStates.contains(migrationState)) {
            throw new FlywayException("Invalid migration state '" + patternParts[1] + "'. Valid states are: " + validMigrationStates);
        }

        return new ValidatePattern(migrationType, migrationState, pattern);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValidatePattern that = (ValidatePattern) o;
        return Objects.equals(migrationType, that.migrationType) && Objects.equals(migrationState, that.migrationState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(migrationType, migrationState);
    }

    @Override
    public String toString() {
        return pattern;
    }
}