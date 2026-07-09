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

import lombok.CustomLog;
import org.flywaydb.core.api.CoreMigrationType;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.api.resource.LoadableResource;
import org.flywaydb.core.api.resource.Resource;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resolver.ChecksumCalculator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Migration resolver for SQL files on the classpath. The SQL files must have names like V1__Description.sql,
 * V1_1__Description.sql, or R__description.sql.
 */
@CustomLog
public class SqlMigrationResolver implements MigrationResolver {
    private final SqlScriptExecutorFactory sqlScriptExecutorFactory;
    private final ResourceProvider resourceProvider;
    private final SqlScriptFactory sqlScriptFactory;
    private final Configuration configuration;
    private final ParsingContext parsingContext;

    public SqlMigrationResolver(final ResourceProvider resourceProvider,
        final SqlScriptExecutorFactory sqlScriptExecutorFactory,
        final SqlScriptFactory sqlScriptFactory,
        final Configuration configuration,
        final ParsingContext parsingContext) {
        this.sqlScriptExecutorFactory = sqlScriptExecutorFactory;
        this.resourceProvider = resourceProvider;
        this.sqlScriptFactory = sqlScriptFactory;
        this.configuration = configuration;
        this.parsingContext = parsingContext;
    }

    @Override
    public List<ResolvedMigration> resolveMigrations(final Context context) {
        final List<ResolvedMigration> migrations = new ArrayList<>();
        final String[] suffixes = configuration.getSqlMigrationSuffixes();

        addMigrations(migrations, configuration.getSqlMigrationPrefix(), suffixes, false);
        addMigrations(migrations, configuration.getRepeatableSqlMigrationPrefix(), suffixes, true);

        migrations.sort(new ResolvedMigrationComparator());
        return migrations;
    }

    private LoadableResource[] createPlaceholderReplacingLoadableResources(final List<LoadableResource> loadableResources) {
        return loadableResources.stream()
            .map(loadableResource -> LoadableResource.createPlaceholderReplacingLoadableResource(loadableResource,
                configuration,
                parsingContext))
            .toArray(LoadableResource[]::new);
    }

    private Integer getChecksumForLoadableResource(final boolean repeatable,
        final List<LoadableResource> loadableResources,
        final ResourceName resourceName,
        final boolean placeholderReplacement) {
        if (repeatable && placeholderReplacement) {
            parsingContext.updateFilenamePlaceholder(resourceName, configuration);
            return ChecksumCalculator.calculate(createPlaceholderReplacingLoadableResources(loadableResources));
        }

        return ChecksumCalculator.calculate(loadableResources.toArray(LoadableResource[]::new));
    }

    private Integer getEquivalentChecksumForLoadableResource(final boolean repeatable,
        final List<LoadableResource> loadableResources) {
        if (repeatable) {
            return ChecksumCalculator.calculate(loadableResources.toArray(LoadableResource[]::new));
        }

        return null;
    }

    private void addMigrations(final List<ResolvedMigration> migrations,
        final String prefix,
        final String[] suffixes,
        final boolean repeatable) {
        final ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);

        for (final LoadableResource resource : resourceProvider.getResources(prefix, suffixes)) {
            final String filename = resource.getFilename();
            final ResourceName resourceName = resourceNameParser.parse(filename);
            if (!resourceName.isValid() || isSqlCallback(resourceName) || !prefix.equals(resourceName.getPrefix())) {
                continue;
            }

            final SqlScript sqlScript = sqlScriptFactory.createSqlScript(resource,
                configuration.isMixed(),
                resourceProvider);

            final List<LoadableResource> resources = new ArrayList<>();
            resources.add(resource);

            if (sqlScript.includeReferencedScriptsInChecksum()) {
                final SortedSet<LoadableResource> referencedResources = new TreeSet<>();
                for (final SqlScript referencedSqlScript : sqlScript.getReferencedSqlScripts()) {
                    referencedResources.add(referencedSqlScript.getResource());
                }
                if (!referencedResources.isEmpty()) {
                    LOG.debug("Calculating checksum for '"
                        + filename
                        + "' using the following referenced scripts: "
                        + referencedResources.stream().map(Resource::getFilename).collect(Collectors.joining(",")));
                }
                resources.addAll(referencedResources);
            }

            final Integer checksum = getChecksumForLoadableResource(repeatable,
                resources,
                resourceName,
                sqlScript.placeholderReplacement());
            final Integer equivalentChecksum = getEquivalentChecksumForLoadableResource(repeatable, resources);

            migrations.add(new ResolvedMigrationImpl(resourceName.getVersion(),
                resourceName.getDescription(),
                resource.getRelativePath(),
                checksum,
                equivalentChecksum,
                CoreMigrationType.SQL,
                resource.getAbsolutePathOnDisk(),
                new SqlMigrationExecutor(sqlScriptExecutorFactory, sqlScript, false, configuration.isBatch())));
        }
    }

    /**
     * Checks whether this filename is actually a sql-based callback instead of a regular migration.
     *
     * @param result The parsing result to check.
     */
    protected static boolean isSqlCallback(final ResourceName result) {
        return Event.fromId(result.getPrefix()) != null;
    }
}
