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
package org.flywaydb.core.internal.resolver;

import org.flywaydb.core.api.ClassProvider;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.api.resolver.UnresolvedMigration;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resolver.java.FixedJavaMigrationResolver;
import org.flywaydb.core.internal.resolver.java.ScanningJavaMigrationResolver;
import org.flywaydb.core.internal.resolver.sql.SqlMigrationResolver;

import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;
import org.flywaydb.core.internal.util.Pair;

import java.util.*;

/**
 * Facility for retrieving and sorting the available migrations from the classpath through the various migration
 * resolvers.
 */
public class CompositeMigrationResolver implements MigrationResolver {
    /**
     * The migration resolvers to use internally.
     */
    private final Collection<MigrationResolver> migrationResolvers = new ArrayList<>();
    /**
     * The available migrations, sorted by version, newest first. An empty list is returned when no migrations can be
     * found.
     */
    private Pair<List<ResolvedMigration>, Collection<UnresolvedMigration>> availableMigrations;

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
    public Pair<List<ResolvedMigration>, Collection<UnresolvedMigration>> attemptResolveMigrations(Context context) {
        if (availableMigrations == null) {
            availableMigrations = doFindAvailableMigrations(context);
        }

        return availableMigrations;
    }

    private Pair<List<ResolvedMigration>, Collection<UnresolvedMigration>> doFindAvailableMigrations(Context context) throws FlywayException {
        Pair<Collection<ResolvedMigration>, Collection<UnresolvedMigration>> migrations = collectMigrations(migrationResolvers, context);
        ArrayList<ResolvedMigration> resolvedMigrations = new ArrayList<>(migrations.getLeft());
        resolvedMigrations.sort(new ResolvedMigrationComparator());

        checkForIncompatibilities(resolvedMigrations);

        return Pair.of(resolvedMigrations, migrations.getRight());
    }

    /**
     * Collects all the migrations for all migration resolvers.
     *
     * @param migrationResolvers The migration resolvers to check.
     * @return All migrations.
     */
    static Pair<Collection<ResolvedMigration>, Collection<UnresolvedMigration>> collectMigrations(Collection<MigrationResolver> migrationResolvers, Context context) {
        Set<ResolvedMigration> migrations = new HashSet<>();
        Set<UnresolvedMigration> unresolvedMigrations = new HashSet<>();
        for (MigrationResolver migrationResolver : migrationResolvers) {
            Pair<List<ResolvedMigration>, Collection<UnresolvedMigration>> collectionsPair = migrationResolver.attemptResolveMigrations(context);
            migrations.addAll(collectionsPair.getLeft());
            unresolvedMigrations.addAll(collectionsPair.getRight());
        }
        return Pair.of(migrations, unresolvedMigrations);
    }

    /**
     * Checks for incompatible migrations.
     *
     * @param migrations The migrations to check.
     * @throws FlywayException when two different migration with the same version number are found.
     */
    static void checkForIncompatibilities(List<ResolvedMigration> migrations) {
        ResolvedMigrationComparator resolvedMigrationComparator = new ResolvedMigrationComparator();
        // check for more than one migration with same version
        for (int i = 0; i < migrations.size() - 1; i++) {
            ResolvedMigration current = migrations.get(i);
            ResolvedMigration next = migrations.get(i + 1);
            if (resolvedMigrationComparator.compare(current, next) == 0) {





                if (current.getVersion() != null) {
                    throw new FlywayException(String.format("Found more than one migration with version %s\nOffenders:\n-> %s (%s)\n-> %s (%s)",
                                                            current.getVersion(),
                                                            current.getPhysicalLocation(),
                                                            current.getType(),
                                                            next.getPhysicalLocation(),
                                                            next.getType()),
                                              ErrorCode.DUPLICATE_VERSIONED_MIGRATION);
                }
                throw new FlywayException(String.format("Found more than one repeatable migration with description %s\nOffenders:\n-> %s (%s)\n-> %s (%s)",
                                                        current.getDescription(),
                                                        current.getPhysicalLocation(),
                                                        current.getType(),
                                                        next.getPhysicalLocation(),
                                                        next.getType()),
                                          ErrorCode.DUPLICATE_REPEATABLE_MIGRATION);
            }
        }
    }
}