/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.core.api.resource;

import java.util.Objects;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;

public record LoadableResourceMetadata(
    MigrationVersion version,
    String description,
    String prefix,
    LoadableResource loadableResource,
    SqlScriptMetadata sqlScriptMetadata,
    int checksum,
    MigrationType migrationType) {

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LoadableResourceMetadata that = (LoadableResourceMetadata) o;
        if (Objects.equals(prefix, that.prefix) && Objects.equals(version, that.version)) {
            if (version == null) {
                return Objects.equals(description, that.description);
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, prefix);
    }
    
    public boolean isRepeatable() {
        return version == null;
    }
    
    public boolean isVersioned() {
        return !isRepeatable();
    }
}
