package com.google.code.flyway.core.java;

import com.google.code.flyway.core.Migration;
import com.google.code.flyway.core.MigrationResolver;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Migration resolver for java class based migrations.
 * The classes must have a name like V1 or V1_1_3 or V1__Description or V1_1_3__Description.
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
    public Collection<Migration> resolvesMigrations() {
        Collection<Migration> migrations = new ArrayList<Migration>();

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(Migration.class));
        Set<BeanDefinition> components = provider.findCandidateComponents(basePackage);
        for (BeanDefinition beanDefinition : components) {
            Class<?> clazz = ClassUtils.resolveClassName(beanDefinition.getBeanClassName(), null);
            Migration migration = (Migration) BeanUtils.instantiateClass(clazz);
            migrations.add(migration);
        }

        return migrations;
    }
}
