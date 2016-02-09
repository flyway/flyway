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
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;

import java.util.Hashtable;
import java.util.Properties;

/**
 * Entry point for the OSGi bundle
 */
public class OsgiActivator implements BundleActivator {
    public void start(BundleContext context) throws Exception {
        System.out.println("Starting Flyway Sample OSGi");

        ServiceFactory factory = new FlywayMigrationServiceFactory();
        context.registerService(FlywayMigrationService.class.getName(), factory, new Hashtable<String, Object>());

        try {
            Properties properties = new Properties();
            properties.setProperty("flyway.driver", "org.h2.Driver");
            properties.setProperty("flyway.url", "jdbc:h2:mem:flyway_db;DB_CLOSE_DELAY=-1");
            properties.setProperty("flyway.user", "sa");
            properties.setProperty("flyway.password", "");

            Flyway flyway = new Flyway();
            flyway.configure(properties);
            flyway.setLocations("db.migration", "org.flywaydb.sample.osgi.fragment");
            flyway.migrate();

            System.out.println("New schema version: " + flyway.info().current().getVersion());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping Flyway Sample OSGi");
    }
}
