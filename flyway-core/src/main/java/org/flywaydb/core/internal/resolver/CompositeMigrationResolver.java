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
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.resolver.java.FixedJavaMigrationResolver;
import org.flywaydb.core.internal.resolver.java.ScanningJavaMigrationResolver;
import org.flywaydb.core.internal.resolver.sql.SqlMigrationResolver;

import org.flywaydb.core.internal.sqlscript.SqlScriptExecutorFactory;
import org.flywaydb.core.internal.sqlscript.SqlScriptFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CompositeMigrationResolver implements MigrationResolver {
    private final Collection<MigrationResolver> migrationResolvers = new ArrayList<>();
    private final ResourceProvider resourceProvider;
    private final SqlScriptFactory sqlScriptFactory;
    private final SqlScriptExecutorFactory sqlScriptExecutorFactory;
    private final StatementInterceptor statementInterceptor;
    private List<ResolvedMigration> availableMigrations;

    public CompositeMigrationResolver(ResourceProvider resourceProvider,
                                      ClassProvider<JavaMigration> classProvider,
                                      Configuration configuration,
                                      SqlScriptExecutorFactory sqlScriptExecutorFactory,
                                      SqlScriptFactory sqlScriptFactory,
                                      ParsingContext parsingContext,
                                      StatementInterceptor statementInterceptor,
                                      MigrationResolver... customMigrationResolvers) {
        this.resourceProvider = resourceProvider;
        this.sqlScriptFactory = sqlScriptFactory;
        this.sqlScriptExecutorFactory = sqlScriptExecutorFactory;
        this.statementInterceptor = statementInterceptor;

        if (!configuration.isSkipDefaultResolvers()) {
            migrationResolvers.add(new SqlMigrationResolver(resourceProvider, sqlScriptExecutorFactory, sqlScriptFactory, configuration, parsingContext));
            migrationResolvers.add(new ScanningJavaMigrationResolver(classProvider, configuration));

            migrationResolvers.addAll(configuration.getPluginRegister().getPlugins(MigrationResolver.class));




        }

        migrationResolvers.add(new FixedJavaMigrationResolver(configuration.getJavaMigrations()));
        migrationResolvers.addAll(Arrays.asList(customMigrationResolvers));
    }

    static void checkForIncompatibilities(List<ResolvedMigration> migrations) {
        ResolvedMigrationComparator resolvedMigrationComparator = new ResolvedMigrationComparator();

        // check for more than one migration with same version
        for (int i = 0; i < migrations.size() - 1; i++) {
            ResolvedMigration current = migrations.get(i);
            ResolvedMigration next = migrations.get(i + 1);

            if (current.canCompareWith(next) && next.canCompareWith(current) && resolvedMigrationComparator.compare(current, next) == 0) {
                if (current.getVersion() != null) {
                    throw new FlywayException(String.format("Found more than one migration with version %s\nOffenders:\n-> %s (%s)\n-> %s (%s)",
                                                            current.getVersion(),
                                                            current.getPhysicalLocation(),
                                                            current.getType(),
                                                            next.getPhysicalLocation(),
                                                            next.getType()), ErrorCode.DUPLICATE_VERSIONED_MIGRATION);
                }
                throw new FlywayException(String.format("Found more than one repeatable migration with description %s\nOffenders:\n-> %s (%s)\n-> %s (%s)",
                                                        current.getDescription(),
                                                        current.getPhysicalLocation(),
                                                        current.getType(),
                                                        next.getPhysicalLocation(),
                                                        next.getType()), ErrorCode.DUPLICATE_REPEATABLE_MIGRATION);
            }
        }
    }

    @Override
    public Collection<ResolvedMigration> resolveMigrations(Context context) {
        if (availableMigrations == null) {
            availableMigrations = doFindAvailableMigrations(context);
        }
        return availableMigrations;
    }

    public Collection<ResolvedMigration> resolveMigrations(Configuration configuration) {
        return resolveMigrations(new Context(configuration, resourceProvider, sqlScriptFactory, sqlScriptExecutorFactory, statementInterceptor));
    }

    private List<ResolvedMigration> doFindAvailableMigrations(Context context) throws FlywayException {
        List<ResolvedMigration> migrations = new ArrayList<>(collectMigrations(migrationResolvers, context));
        migrations.sort(new ResolvedMigrationComparator());

        checkForIncompatibilities(migrations);

        return migrations;
    }

    Collection<ResolvedMigration> collectMigrations(Collection<MigrationResolver> migrationResolvers, Context context) {
        return migrationResolvers.stream()
                .flatMap(mr -> mr.resolveMigrations(context).stream())
                .collect(Collectors.toSet());
    }
}