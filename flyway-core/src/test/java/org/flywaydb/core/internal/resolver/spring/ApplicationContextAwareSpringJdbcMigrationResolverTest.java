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

import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.spring.dummyspring.DummyBean;
import org.flywaydb.core.internal.resolver.spring.dummyspring.V1__SpringManagedBeanMigration;
import org.flywaydb.core.internal.util.Location;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link ApplicationContextAwareSpringJdbcMigrationResolver}.
 */
public class ApplicationContextAwareSpringJdbcMigrationResolverTest {

    @Test
    public void migrationInApplicationContext_mustBeResolved() {
        final String testPropertyValue = "Test String";
        GenericApplicationContext applicationContext = setupApplicationContext(testPropertyValue);
        ApplicationContextAwareSpringJdbcMigrationResolver applicationContextAwareSpringJdbcMigrationResolver =
                new ApplicationContextAwareSpringJdbcMigrationResolver(Thread.currentThread().getContextClassLoader(),
                        new Location("org/flywaydb/core/internal/resolver/spring/dummyspring"), applicationContext);

        Collection<ResolvedMigration> resolvedMigrations = applicationContextAwareSpringJdbcMigrationResolver.resolveMigrations();

        assertEquals(1, resolvedMigrations.size());
        ResolvedMigration resolvedMigration = resolvedMigrations.iterator().next();

        assertEquals("1.0", resolvedMigration.getVersion().toString());

        // as the getDescription of the migration returns the value of the dummy bean we need to check if this is equal
        assertEquals(testPropertyValue, resolvedMigration.getDescription());
    }

    protected GenericApplicationContext setupApplicationContext(String dummyPropertyValue) {
        GenericApplicationContext applicationContext = new GenericApplicationContext();

        // create dummy bean definition and register it in the application context
        applicationContext.registerBeanDefinition("dummyBean", BeanDefinitionBuilder.genericBeanDefinition(DummyBean.class).
                addConstructorArgValue(dummyPropertyValue).getBeanDefinition());

        // create the spring jdbc migration and register it in the application context
        applicationContext.registerBeanDefinition("springJdbcMigration", BeanDefinitionBuilder.
                genericBeanDefinition(V1__SpringManagedBeanMigration.class).getBeanDefinition());

        // set the annotation processors and refresh the application context
        AnnotationConfigUtils.registerAnnotationConfigProcessors(applicationContext);
        applicationContext.refresh();

        return applicationContext;
    }

}
