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
package org.flywaydb.core.internal.resolver.sql;

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PlaceholderReplacingReader;
import org.flywaydb.core.internal.resolver.ChecksumCalculator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.resource.ResourceName;
import org.flywaydb.core.internal.resource.ResourceNameParser;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.internal.sqlscript.SqlScript;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

import java.io.Reader;
import java.util.*;

/**
 * Migration resolver for SQL files on the classpath. The SQL files must have names like
 * V1__Description.sql, V1_1__Description.sql or R__description.sql.
 */
public class SqlMigrationResolver implements MigrationResolver {
    /**
     * The SQL script executor factory.
     */
    private final SqlScriptExecutorFactory sqlScriptExecutorFactory;

    /**
     * The resource provider to use.
     */
    private final ResourceProvider resourceProvider;

    private final SqlScriptFactory sqlScriptFactory;

    /**
     * The Flyway configuration.
     */
    private final Configuration configuration;

    private final ParsingContext parsingContext;

    /**
     * Creates a new instance.
     *
     * @param resourceProvider         The Scanner for loading migrations on the classpath.
     * @param sqlScriptExecutorFactory The SQL script executor factory.
     * @param sqlScriptFactory         The SQL script factory.
     * @param configuration            The Flyway configuration.
     * @param parsingContext           The parsing context.
     */
    public SqlMigrationResolver(ResourceProvider resourceProvider,
                                SqlScriptExecutorFactory sqlScriptExecutorFactory, SqlScriptFactory sqlScriptFactory,
                                Configuration configuration, ParsingContext parsingContext) {
        this.sqlScriptExecutorFactory = sqlScriptExecutorFactory;
        this.resourceProvider = resourceProvider;
        this.sqlScriptFactory = sqlScriptFactory;
        this.configuration = configuration;
        this.parsingContext = parsingContext;
    }

    public List<ResolvedMigration> resolveMigrations(Context context) {
        List<ResolvedMigration> migrations = new ArrayList<>();

        String separator = configuration.getSqlMigrationSeparator();
        String[] suffixes = configuration.getSqlMigrationSuffixes();
        addMigrations(migrations, configuration.getSqlMigrationPrefix(), separator, suffixes,
                false



        );




        addMigrations(migrations, configuration.getRepeatableSqlMigrationPrefix(), separator, suffixes,
                true



        );

        Collections.sort(migrations, new ResolvedMigrationComparator());
        return migrations;
    }

    private LoadableResource[] createPlaceholderReplacingLoadableResources(List<LoadableResource> loadableResources) {
        List<LoadableResource> list = new ArrayList<>();

        for (final LoadableResource loadableResource : loadableResources) {
            LoadableResource placeholderReplacingLoadableResource = new LoadableResource() {
                @Override
                public Reader read() {
                    return PlaceholderReplacingReader.create(
                            configuration,
                            parsingContext,
                            loadableResource.read());
                }

                @Override
                public String getAbsolutePath() { return loadableResource.getAbsolutePath(); }
                @Override
                public String getAbsolutePathOnDisk() { return loadableResource.getAbsolutePathOnDisk(); }
                @Override
                public String getFilename() { return loadableResource.getFilename(); }
                @Override
                public String getRelativePath() { return loadableResource.getRelativePath(); }
            };

            list.add(placeholderReplacingLoadableResource);
        }

        return list.toArray(new LoadableResource[0]);
    }

    private Integer getChecksumForLoadableResource(boolean repeatable, List<LoadableResource> loadableResources) {
        if (repeatable && configuration.isPlaceholderReplacement()) {
            return ChecksumCalculator.calculate(createPlaceholderReplacingLoadableResources(loadableResources));
        }

        return ChecksumCalculator.calculate(loadableResources.toArray(new LoadableResource[0]));
    }

    private Integer getEquivalentChecksumForLoadableResource(boolean repeatable, List<LoadableResource> loadableResources) {
        if (repeatable) {
            return ChecksumCalculator.calculate(loadableResources.toArray(new LoadableResource[0]));
        }

        return null;
    }

    private void addMigrations(List<ResolvedMigration> migrations, String prefix,
                               String separator, String[] suffixes, boolean repeatable



    ){
        ResourceNameParser resourceNameParser = new ResourceNameParser(configuration);

        for (LoadableResource resource : resourceProvider.getResources(prefix, suffixes)) {
            String filename = resource.getFilename();
            ResourceName result = resourceNameParser.parse(filename);
            if (!result.isValid() || isSqlCallback(result) || !prefix.equals(result.getPrefix())) {
                continue;
            }

            SqlScript sqlScript = sqlScriptFactory.createSqlScript(resource, configuration.isMixed(), resourceProvider);

            List<LoadableResource> resources = new ArrayList<>();
            resources.add(resource);








            Integer checksum = getChecksumForLoadableResource(repeatable, resources);
            Integer equivalentChecksum = getEquivalentChecksumForLoadableResource(repeatable, resources);

            migrations.add(new ResolvedMigrationImpl(
                    result.getVersion(),
                    result.getDescription(),
                    resource.getRelativePath(),
                    checksum,
                    equivalentChecksum,



                            MigrationType.SQL,
                    resource.getAbsolutePathOnDisk(),
                    new SqlMigrationExecutor(sqlScriptExecutorFactory, sqlScript



                    )) {
                @Override
                public void validate() {
                    // Do nothing by default.
                }
            });
        }
    }



    /**
     * Checks whether this filename is actually a sql-based callback instead of a regular migration.
     *
     * @param result  The parsing result to check.
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    /* private -> testing */
    static boolean isSqlCallback(ResourceName result) {
        if (Event.fromId(result.getPrefix()) != null) {
            return true;
        }
        return false;
    }
}