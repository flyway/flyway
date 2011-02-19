/**
 * Copyright (C) 2010-2011 the original author or authors.
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
package com.googlecode.flyway.core.migration.java;

import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.migration.MigrationResolver;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Migration resolver for java class based migrations. The classes must have a name like V1 or V1_1_3 or V1__Description
 * or V1_1_3__Description.
 */
public class JavaMigrationResolver implements MigrationResolver {
    /**
     * The base package on the classpath where to migrations are located.
     */
    private final String basePackage;

    /**
     * Creates a new instance.
     *
     * @param basePackage The base package on the classpath where to migrations are located.
     */
    public JavaMigrationResolver(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public Collection<Migration> resolveMigrations() {
        Collection<Migration> migrations = new ArrayList<Migration>();

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(JavaMigration.class));
        Set<BeanDefinition> components = provider.findCandidateComponents(basePackage);
        for (BeanDefinition beanDefinition : components) {
            Class<?> clazz = ClassUtils.resolveClassName(beanDefinition.getBeanClassName(), null);
            JavaMigration javaMigration = (JavaMigration) BeanUtils.instantiateClass(clazz);
            migrations.add(new JavaMigrationExecutor(javaMigration));
        }

        return migrations;
    }
}
