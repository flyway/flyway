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
package org.flywaydb.core.api;

import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class MigrationPattern {
    private static final Pattern VERSION_NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)*");
    private final String migrationName;

    public boolean matches(MigrationVersion version, String description) {
        String migrationNameAsVersion = migrationName.replace("_", ".");
        if (version != null && isValidVersionNumber(migrationNameAsVersion)) {
            MigrationVersion patternVersion = MigrationVersion.fromVersion(migrationNameAsVersion);
            return patternVersion.equals(version);
        }
        String pattern = migrationName.replace("_", " ");
        return pattern.equals(description);
    }

    @Override
    public String toString() {
        return migrationName;
    }

    @Override
    public int hashCode() {
        return migrationName.hashCode();
    }

    private static boolean isValidVersionNumber(String versionNumber) {
        return VERSION_NUMBER_PATTERN.matcher(versionNumber).matches();
    }
}