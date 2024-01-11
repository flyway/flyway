package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.pattern.ValidatePattern;

import java.util.Arrays;

public class ValidatePatternUtils {
    public static boolean isPendingIgnored(ValidatePattern[] ignorePatterns) {
        return Arrays.stream(ignorePatterns).anyMatch(p -> p.equals(ValidatePattern.fromPattern("*:pending")))
                || (Arrays.stream(ignorePatterns).anyMatch(p -> p.matchesMigration(true, MigrationState.PENDING))
                && Arrays.stream(ignorePatterns).anyMatch(p -> p.matchesMigration(false, MigrationState.PENDING)));
    }

    public static boolean isIgnoredIgnored(ValidatePattern[] ignorePatterns) {
        return Arrays.stream(ignorePatterns).anyMatch(p -> p.equals(ValidatePattern.fromPattern("*:ignored")))
                || (Arrays.stream(ignorePatterns).anyMatch(p -> p.matchesMigration(true, MigrationState.IGNORED))
                && Arrays.stream(ignorePatterns).anyMatch(p -> p.matchesMigration(false, MigrationState.IGNORED)));
    }

    public static boolean isMissingIgnored(ValidatePattern[] ignorePatterns) {
        return Arrays.stream(ignorePatterns).anyMatch(p -> p.equals(ValidatePattern.fromPattern("*:missing")))
                || (Arrays.stream(ignorePatterns).anyMatch(p -> p.matchesMigration(true, MigrationState.MISSING_SUCCESS))
                && Arrays.stream(ignorePatterns).anyMatch(p -> p.matchesMigration(false, MigrationState.MISSING_SUCCESS)));
    }

    public static boolean isFutureIgnored(ValidatePattern[] ignorePatterns) {
        return Arrays.stream(ignorePatterns).anyMatch(p -> p.equals(ValidatePattern.fromPattern("*:future")))
                || (Arrays.stream(ignorePatterns).anyMatch(p -> p.matchesMigration(true, MigrationState.FUTURE_SUCCESS))
                && Arrays.stream(ignorePatterns).anyMatch(p -> p.matchesMigration(false, MigrationState.FUTURE_SUCCESS)));
    }

    public static ValidatePattern[] getIgnoreAllPattern() {
        return new ValidatePattern[]{ValidatePattern.fromPattern("*:*")};
    }
}