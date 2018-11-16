/**
 * Copyright 2010-2017 Boxfuse GmbH
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

import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.spring.BaseApplicationContextAwareSpringMigration;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.clazz.ClassProvider;
import org.flywaydb.core.internal.resolver.AbstractJavaMigrationResolver;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.resolver.java.JavaMigrationExecutor;
import org.flywaydb.core.internal.util.ClassUtils;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Migration resolver for {@link BaseApplicationContextAwareSpringMigration}s which are registered in the given {@link ApplicationContext}.
 * This resolver provides the ability to use other beans registered in the {@link ApplicationContext} and reference
 * them via Spring's dependency injection facility inside the {@link org.flywaydb.core.api.migration.JavaMigration}s.
 */
public class ApplicationContextAwareSpringJavaMigrationResolver extends AbstractJavaMigrationResolver<BaseApplicationContextAwareSpringMigration, JavaMigrationExecutor> {

    private final ApplicationContext applicationContext;

    /**
     * Creates a new instance.
     *
     * @param applicationContext The Spring application context to load the {@link SpringJdbcMigration}s from.
     */
    public ApplicationContextAwareSpringJavaMigrationResolver(ClassProvider classProvider, Configuration configuration, ApplicationContext applicationContext) {
        super(classProvider, configuration);
        this.applicationContext = applicationContext;
    }

    @Override
    public List<ResolvedMigration> resolveMigrations(Context context) {
        // get all beans of type ApplicationContextAwareSpringMigration from the application context
        Map<String, BaseApplicationContextAwareSpringMigration> springJavaMigrationBeans = this.applicationContext.getBeansOfType(getImplementedInterface());

        ArrayList<ResolvedMigration> resolvedMigrations = new ArrayList<ResolvedMigration>();

        // resolve the migration and populate it with the migration info
        for (BaseApplicationContextAwareSpringMigration springJavaMigrationBean : springJavaMigrationBeans.values()) {
            ResolvedMigrationImpl resolvedMigration = extractMigrationInfo(springJavaMigrationBean);
            resolvedMigration.setPhysicalLocation(ClassUtils.getLocationOnDisk(springJavaMigrationBean.getClass()));
            resolvedMigration.setExecutor(createExecutor(springJavaMigrationBean));

            resolvedMigrations.add(resolvedMigration);
        }

        Collections.sort(resolvedMigrations, new ResolvedMigrationComparator());
        return resolvedMigrations;
    }

    @Override
    protected String getMigrationTypeStr() {
        return "Spring JDBC";
    }

    @Override
    protected Class<BaseApplicationContextAwareSpringMigration> getImplementedInterface() {
        return BaseApplicationContextAwareSpringMigration.class;
    }

    @Override
    protected JavaMigrationExecutor createExecutor(BaseApplicationContextAwareSpringMigration migration) {
        return new JavaMigrationExecutor(migration);
    }

    @Override
    protected MigrationType getMigrationType() {
        return MigrationType.SPRING_JDBC;
    }
}
