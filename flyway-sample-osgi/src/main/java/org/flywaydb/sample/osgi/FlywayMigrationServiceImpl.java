/**
 * Copyright 2010-2016 Boxfuse GmbH
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
package org.flywaydb.sample.osgi;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import java.util.Properties;

public class FlywayMigrationServiceImpl implements FlywayMigrationService {
    @Override
    public void migrate(ClassLoader bundleClassLoader, String... locations) {
        Properties properties = new Properties();
        properties.setProperty("flyway.driver", "org.h2.Driver");
        properties.setProperty("flyway.url", "jdbc:h2:mem:flyway_separate_bundle_db;DB_CLOSE_DELAY=-1");
        properties.setProperty("flyway.user", "sa");
        properties.setProperty("flyway.password", "");

        Flyway flyway = new Flyway();
        flyway.configure(properties);
        flyway.setLocations(locations);
        flyway.setClassLoader(bundleClassLoader);
        flyway.migrate();
        System.out.println("Separate bundle new schema version: " + flyway.info().current().getVersion());
    }
}
