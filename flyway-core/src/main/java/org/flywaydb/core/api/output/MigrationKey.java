/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.api.output;

import org.flywaydb.core.api.MigrationInfo;

public class MigrationKey implements Comparable<MigrationKey> {
    private final String value;

    public MigrationKey(final MigrationInfo migrationInfo) {
        value = createMigrationInfoKey(migrationInfo);
    }
    
    @Override
    public int compareTo(final MigrationKey o) {
        return value.compareTo(o.value);
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof MigrationKey && value.equals(((MigrationKey) o).value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    private String createMigrationInfoKey(final MigrationInfo migrationInfo) {
        if (migrationInfo.isVersioned()) {
            return migrationInfo.getVersion().toString();
        }

        if (migrationInfo.getChecksum() != null) {
            return migrationInfo.getDescription() + migrationInfo.getChecksum();
        }

        return migrationInfo.getDescription();
    }
}
