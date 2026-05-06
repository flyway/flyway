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
package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.internal.resolver.ChecksumCalculator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.resource.UndoResourceTypeProvider;
import org.flywaydb.core.internal.sqlscript.SqlScript;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UndoSqlMigrationResolver implements MigrationResolver {

    @Override
    public Collection<ResolvedMigration> resolveMigrations(Context context) {
        Configuration config = context.configuration;
        ResourceNameParser parser = new ResourceNameParser(config);
        List<ResolvedMigration> migrations = new ArrayList<>();

        for (LoadableResource resource : context.resourceProvider.getResources(
                UndoResourceTypeProvider.UNDO_PREFIX, config.getSqlMigrationSuffixes())) {
            ResourceName name = parser.parse(resource.getFilename());
            if (!name.isValid() || !UndoResourceTypeProvider.UNDO_PREFIX.equals(name.getPrefix())) {
                continue;
            }

            SqlScript sqlScript = context.sqlScriptFactory.createSqlScript(
                    resource, config.isMixed(), context.resourceProvider);
            Integer checksum = ChecksumCalculator.calculate(resource);

            migrations.add(new ResolvedMigrationImpl(
                    name.getVersion(),
                    name.getDescription(),
                    resource.getRelativePath(),
                    checksum,
                    null,
                    CoreMigrationType.UNDO_SCRIPT,
                    resource.getAbsolutePathOnDisk(),
                    new SqlMigrationExecutor(context.sqlScriptExecutorFactory, sqlScript,
                            false, config.isBatch())));
        }
        return migrations;
    }
}
