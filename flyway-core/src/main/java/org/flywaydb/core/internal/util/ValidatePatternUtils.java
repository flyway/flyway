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