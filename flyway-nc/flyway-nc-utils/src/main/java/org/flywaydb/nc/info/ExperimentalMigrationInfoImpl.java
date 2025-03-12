/*-
 * ========================LICENSE_START=================================
 * flyway-nc-utils
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
package org.flywaydb.nc.info;

import java.time.ZoneId;
import java.util.Date;
import org.flywaydb.core.api.LoadableMigrationInfo;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.api.resource.LoadableResourceMetadata;
import org.flywaydb.core.experimental.schemahistory.ResolvedSchemaHistoryItem;
import org.flywaydb.core.extensibility.MigrationType;
import org.flywaydb.core.internal.sqlscript.SqlScriptMetadata;
import org.flywaydb.core.internal.util.Pair;

public class ExperimentalMigrationInfoImpl implements LoadableMigrationInfo {

    private final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration;
    private final MigrationState migrationState;

    public ExperimentalMigrationInfoImpl(final Pair<ResolvedSchemaHistoryItem, LoadableResourceMetadata> migration,
        MigrationState migrationState) {
        this.migration = migration;
        this.migrationState = migrationState;
    }

    @Override
    public MigrationType getType() {
        if (migration.getLeft() != null) {
            return migration.getLeft().getType();
        }
        return migration.getRight().migrationType();
    }

    @Override
    public Integer getChecksum() {
        if (migration.getLeft() != null) {
            return migration.getLeft().getChecksum();
        }
        return migration.getRight().checksum();
    }

    @Override
    public MigrationVersion getVersion() {
        if (migration.getLeft() != null) {
            return migration.getLeft().getVersion();
        }
        return migration.getRight().version();
    }

    @Override
    public String getDescription() {
        if (migration.getLeft() != null) {
            return migration.getLeft().getDescription();
        }
        return migration.getRight().description();
    }

    @Override
    public String getScript() {
        if (migration.getLeft() != null) {
            return migration.getLeft().getScript();
        }
        return migration.getRight().loadableResource().getFilename();
    }

    @Override
    public MigrationState getState() {
        return migrationState;
    }

    @Override
    public Date getInstalledOn() {
        if (migration.getLeft() != null) {
            return Date.from(migration.getLeft().getInstalledOn().atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    @Override
    public String getInstalledBy() {
        if (migration.getLeft() != null) {
            return migration.getLeft().getInstalledBy();
        }
        return null;
    }

    @Override
    public Integer getInstalledRank() {
        if (migration.getLeft() != null) {
            return migration.getLeft().getInstalledRank();
        }
        return null;
    }

    @Override
    public Integer getExecutionTime() {
        if (migration.getLeft() != null) {
            return migration.getLeft().getExecutionTime();
        }
        return null;
    }

    @Override
    public String getPhysicalLocation() {
        if (migration.getRight() != null) {
            return migration.getRight().loadableResource().getAbsolutePath();
        }
        return null;
    }

    @Override
    public int compareVersion(final MigrationInfo o) {
        return 0;
    }

    @Override
    public int compareTo(final MigrationInfo o) {
        return 0;
    }

    @Override
    public boolean isShouldExecute() {
        if (migration.getRight() != null && migration.getRight().sqlScriptMetadata() != null) {
            return migration.getRight().sqlScriptMetadata().shouldExecute();
        }
        return true;
    }

    @Override
    public Boolean isPlaceholderReplacement() {
        if (migration.getRight() != null && migration.getRight().sqlScriptMetadata() != null) {
            return migration.getRight().sqlScriptMetadata().placeholderReplacement();
        }
        return null;
    }

    @Override
    public Integer getResolvedChecksum() {
        return migration.getRight() == null ? null : migration.getRight().checksum();
    }

    @Override
    public Integer getAppliedChecksum() {
        return migration.getLeft() == null ? null : migration.getLeft().getChecksum();
    }

    @Override
    public String getResolvedDescription() {
        return migration.getRight() == null ? null : migration.getRight().description();
    }

    @Override
    public String getAppliedDescription() {
        return migration.getLeft() == null ? null : migration.getLeft().getDescription();
    }

    @Override
    public MigrationType getResolvedType() {
        return migration.getRight() == null ? null : migration.getRight().migrationType();
    }

    @Override
    public MigrationType getAppliedType() {
        return migration.getLeft() == null ? null : migration.getLeft().getType();
    }

    @Override
    public LoadableResource getLoadableResource() {
        if (migration.getRight() != null) {
            return migration.getRight().loadableResource();
        }
        return null;
    }

    @Override
    public SqlScriptMetadata getSqlScriptMetadata() {
        if (migration.getRight() != null) {
            return migration.getRight().sqlScriptMetadata();
        }
        return null;
    }
}
