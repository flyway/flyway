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
package org.flywaydb.sample.osgi.separate;

import org.flywaydb.sample.osgi.FlywayMigrationService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Entry point for the OSGi bundle
 */
public class OsgiActivator implements BundleActivator {
    public void start(BundleContext context) throws Exception {
        System.out.println("Starting Flyway Sample OSGi Separate Bundle");
        for (int i = 0; i < 100; i++) {
            ServiceReference sr = context.getServiceReference(FlywayMigrationService.class.getName());
            if (sr != null) {
                FlywayMigrationService ms = (FlywayMigrationService) context.getService(sr);
                ms.migrate(getClass().getClassLoader(), "db.migration", "org.flywaydb.sample.osgi.separate.migration");
                context.ungetService(sr);
                break;
            }
            Thread.sleep(100);
        }
        System.exit(0);
    }

    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping Flyway Sample OSGi Separate Bundle");
    }
}
