/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.core.experimental.schemahistory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.extensibility.MigrationType;

@RequiredArgsConstructor
public class SchemaHistoryModel {
    
    private final List<SchemaHistoryItem> schemaHistoryItems;
    
    public SchemaHistoryModel(){
        this(List.of());
    }
    
    public List<SchemaHistoryItem> getSchemaHistoryItems(){
        return Collections.unmodifiableList(schemaHistoryItems);
    }

    public Optional<SchemaHistoryItem> getSchemaHistoryItem(final int installedRank){
        return getSchemaHistoryItems().stream().filter(x -> x.getInstalledRank() == installedRank).findFirst();
    }

    public int calculateInstalledRank(MigrationType type) {
        if (schemaHistoryItems.isEmpty()) {
            return type == CoreMigrationType.SCHEMA ? 0 : 1;
        }
        return schemaHistoryItems
            .stream()
            .map(SchemaHistoryItem::getInstalledRank)
            .max(Integer::compareTo)
            .orElse(0) + 1;
    }

    public MigrationVersion getInitialVersion() {
        return schemaHistoryItems.stream().map(
            SchemaHistoryItem::getVersion).filter(Objects::nonNull).map(MigrationVersion::fromVersion).max(
            MigrationVersion::compareTo).orElse(MigrationVersion.EMPTY);
    }
}
