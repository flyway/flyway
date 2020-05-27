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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.clazz.ClassProvider;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resolver.java.FixedJavaMigrationResolver;
import org.flywaydb.core.internal.resolver.java.ScanningJavaMigrationResolver;
import org.flywaydb.core.internal.resolver.sql.SqlMigrationResolver;
import org.flywaydb.core.internal.resource.ResourceProvider;
import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Facility for retrieving and sorting the available migrations from the classpath through the various migration
 * resolvers.
 */
public class CompositeMigrationResolver implements MigrationResolver {
    /**
     * The migration resolvers to use internally.
     */
    private Collection<MigrationResolver> migrationResolvers = new ArrayList<>();

    /**
     * The available migrations, sorted by version, newest first. An empty list is returned when no migrations can be
     * found.
     */
    private List<ResolvedMigration> availableMigrations;

    /**
     * Creates a new CompositeMigrationResolver.
     *
     * @param resourceProvider         The resource provider.
     * @param classProvider            The class provider.
     * @param configuration            The Flyway configuration.
     * @param sqlScriptFactory         The SQL statement builder factory.
     * @param customMigrationResolvers Custom Migration Resolvers.
     * @param parsingContext           The parsing context
     */
    public CompositeMigrationResolver(ResourceProvider resourceProvider,
                                      ClassProvider<JavaMigration> classProvider,
                                      Configuration configuration,
                                      SqlScriptExecutorFactory sqlScriptExecutorFactory,
                                      SqlScriptFactory sqlScriptFactory,
                                      ParsingContext parsingContext,
                                      MigrationResolver... customMigrationResolvers
    ) {
        if (!configuration.isSkipDefaultResolvers()) {
            migrationResolvers.add(new SqlMigrationResolver(resourceProvider, sqlScriptExecutorFactory, sqlScriptFactory,
                    configuration, parsingContext));
            migrationResolvers.add(new ScanningJavaMigrationResolver(classProvider, configuration));
        }
        migrationResolvers.add(new FixedJavaMigrationResolver(configuration.getJavaMigrations()));

        migrationResolvers.addAll(Arrays.asList(customMigrationResolvers));
    }

    /**
     * Finds all available migrations using all migration resolvers (sql, java, ...).
     *
     * @return The available migrations, sorted by version, oldest first. An empty list is returned when no migrations
     * can be found.
     * @throws FlywayException when the available migrations have overlapping versions.
     */
    public List<ResolvedMigration> resolveMigrations(Context context) {
        if (availableMigrations == null) {
            availableMigrations = doFindAvailableMigrations(context);
        }

        return availableMigrations;
    }

    /**
     * Finds all available migrations using all migration resolvers (sql, java, ...).
     *
     * @return The available migrations, sorted by version, oldest first. An empty list is returned when no migrations
     * can be found.
     * @throws FlywayException when the available migrations have overlapping versions.
     */
    private List<ResolvedMigration> doFindAvailableMigrations(Context context) throws FlywayException {
        List<ResolvedMigration> migrations = new ArrayList<>(collectMigrations(migrationResolvers, context));
        Collections.sort(migrations, new ResolvedMigrationComparator());

        checkForIncompatibilities(migrations);

        return migrations;
    }

    /**
     * Collects all the migrations for all migration resolvers.
     *
     * @param migrationResolvers The migration resolvers to check.
     * @return All migrations.
     */
    /* private -> for testing */
    static Collection<ResolvedMigration> collectMigrations(Collection<MigrationResolver> migrationResolvers, Context context) {
        Set<ResolvedMigration> migrations = new HashSet<>();
        for (MigrationResolver migrationResolver : migrationResolvers) {
            migrations.addAll(migrationResolver.resolveMigrations(context));
        }
        return migrations;
    }

    /**
     * Checks for incompatible migrations.
     *
     * @param migrations The migrations to check.
     * @throws FlywayException when two different migration with the same version number are found.
     */
    /* private -> for testing */
    static void checkForIncompatibilities(List<ResolvedMigration> migrations) {
        ResolvedMigrationComparator resolvedMigrationComparator = new ResolvedMigrationComparator();
        TreeSet<ResolvedMigration> repeatableMigrations = new TreeSet<>(resolvedMigrationComparator);
        TreeSet<ResolvedMigration> versionedMigrations = new TreeSet<>(resolvedMigrationComparator);
        TreeSet<ResolvedMigration> intermediateBaselineMigrations = new TreeSet<>(resolvedMigrationComparator);

        for (int i = 0; i < migrations.size(); i++) {
            ResolvedMigration next = migrations.get(i);

            if (next.getVersion() == null) {
                if (!repeatableMigrations.add(next)) {
                    ResolvedMigration current = repeatableMigrations.pollLast();
                    throw new FlywayException(String.format("Found more than one repeatable migration with description %s%nOffenders:%n-> %s (%s)%n-> %s (%s)",
                            current.getDescription(),
                            current.getPhysicalLocation(),
                            current.getType(),
                            next.getPhysicalLocation(),
                            next.getType()),
                            ErrorCode.DUPLICATE_REPEATABLE_MIGRATION);
                }
            } else {
                if (next.getType().isIntermediateBaseline()) {
                    if (!intermediateBaselineMigrations.add(next)) {
                        ResolvedMigration current = intermediateBaselineMigrations.pollLast();
                        throw new FlywayException(String.format("Found more than one intermediate baseline migration with version %s%nOffenders:%n-> %s (%s)%n-> %s (%s)",
                                current.getVersion(),
                                current.getPhysicalLocation(),
                                current.getType(),
                                next.getPhysicalLocation(),
                                next.getType()),
                                ErrorCode.DUPLICATE_INTERMEDIATE_BASELINE_MIGRATION);
                    }
                } else {
                    if (!versionedMigrations.add(next)) {
                        ResolvedMigration current = versionedMigrations.pollLast();
                        throw new FlywayException(String.format("Found more than one migration with version %s%nOffenders:%n-> %s (%s)%n-> %s (%s)",
                                current.getVersion(),
                                current.getPhysicalLocation(),
                                current.getType(),
                                next.getPhysicalLocation(),
                                next.getType()),
                                ErrorCode.DUPLICATE_VERSIONED_MIGRATION);
                    }
                }
            }
        }
    }
}