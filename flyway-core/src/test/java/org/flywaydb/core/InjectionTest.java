/**
 * Copyright 2010-2015 Boxfuse GmbH
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
package org.flywaydb.core;

import org.flywaydb.core.api.resolver.MigrationResolver;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class InjectionTest {

    @Test
    public void customCallbacksMustContainFlywayConfiguration() {
        Properties properties = createProperties("callback");

        FlywayCallbackImpl callback = new FlywayCallbackImpl();

        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setCallbacks(callback);

        callback.assertFlywayConfigurationSet();
    }

    @Test
    public void customCallbacksViaPropertiesMustContainFlywayConfiguration() {
        Properties properties = createProperties("callback");
        properties.setProperty("flyway.callbacks", FlywayCallbackImpl.class.getName());

        final Flyway flyway = new Flyway();
        flyway.configure(properties);

        FlywayCallbackImpl callback = (FlywayCallbackImpl) flyway.getCallbacks()[0];
        callback.assertFlywayConfigurationSet();
    }

    @Test
    public void customResolversMustContainFlywayConfiguration() {
        Properties properties = createProperties("resolver");

        FlywayResolverImpl resolver = new FlywayResolverImpl();

        final Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setResolvers(resolver);

        resolver.assertFlywayConfigurationSet();
    }

    @Test
    public void customResolversViaPropertiesMustContainFlywayConfiguration() {
        Properties properties = createProperties("resolver");
        properties.setProperty("flyway.resolvers", FlywayResolverImpl.class.getName());

        final Flyway flyway = new Flyway();
        flyway.configure(properties);

        for (MigrationResolver resolver : flyway.getResolvers()) {
            if (resolver instanceof FlywayResolverImpl) {
                ((FlywayResolverImpl) resolver).assertFlywayConfigurationSet();
                return;
            }
        }

        Assert.fail("Flyway instance does not contain expected instance of FlywayResolverImpl.");
    }

    private Properties createProperties(String name) {
        Properties properties = new Properties();
        properties.setProperty("flyway.user", "sa");
        properties.setProperty("flyway.password", "");
        properties.setProperty("flyway.url", "jdbc:h2:mem:flyway_test_injection_" + name + ";DB_CLOSE_DELAY=-1");
        properties.setProperty("flyway.driver", "org.h2.Driver");
        properties.setProperty("flyway.locations", "migration/dbsupport/h2/sql/domain");
        properties.setProperty("flyway.validateOnMigrate", "false");
        return properties;
    }

}
