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
package org.flywaydb.core.api;

public class MigrationPattern {
    private final String migrationName;

    public MigrationPattern(String migrationName) {
        this.migrationName = migrationName;

    }

    public boolean matches(MigrationVersion version, String description) {
        if (version != null) {
            String pattern = migrationName.replace("_", ".");
            return pattern.equals(version.toString());
        } else {
            String pattern = migrationName.replace("_", " ");
            return pattern.equals(description);
        }
    }

    @Override
    public String toString() {
        return migrationName;
    }

    @Override
    public int hashCode() {
        return migrationName.hashCode();
    }
}