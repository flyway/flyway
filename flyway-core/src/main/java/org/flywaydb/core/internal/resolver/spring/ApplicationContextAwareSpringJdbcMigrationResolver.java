/**
 * Copyright 2010-2015 Axel Fontaine
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
package org.flywaydb.core.internal.resolver.spring;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.Scanner;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Migration resolver for {@link SpringJdbcMigration}s which are registered in the given {@link ApplicationContext}.
 * This resolver provides the ability to use other beans registered in the {@link ApplicationContext} and reference
 * them via Spring's dependency injection facility inside the {@link SpringJdbcMigration}s.
 */
public class ApplicationContextAwareSpringJdbcMigrationResolver extends SpringJdbcMigrationResolver {

    private final ApplicationContext applicationContext;

    /**
     * Creates a new instance.
     *
     * @param scanner            The scanner used for resources and classes
     * @param location           The base package on the classpath where to migrations are located.
     * @param applicationContext The Spring application context to load the {@link SpringJdbcMigration}s from.
     */
    public ApplicationContextAwareSpringJdbcMigrationResolver(Scanner scanner, Location location,
                                                              FlywayConfiguration configuration, ApplicationContext applicationContext) {
        super(scanner, location, configuration);
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<ResolvedMigration> resolveMigrations() {
        // get all beans of type SpringJdbcMigration from the application context
        Map<String, SpringJdbcMigration> springJdbcMigrationBeans = this.applicationContext.getBeansOfType(SpringJdbcMigration.class);

        ArrayList<ResolvedMigration> resolvedMigrations = new ArrayList<ResolvedMigration>();

        // resolve the migration and populate it with the migration info
        for (SpringJdbcMigration springJdbcMigrationBean : springJdbcMigrationBeans.values()) {
            ResolvedMigrationImpl resolvedMigration = extractMigrationInfo(springJdbcMigrationBean);
            resolvedMigration.setPhysicalLocation(ClassUtils.getLocationOnDisk(springJdbcMigrationBean.getClass()));
            resolvedMigration.setExecutor(new SpringJdbcMigrationExecutor(springJdbcMigrationBean));

            resolvedMigrations.add(resolvedMigration);
        }

        Collections.sort(resolvedMigrations, new ResolvedMigrationComparator());
        return resolvedMigrations;
    }

}
