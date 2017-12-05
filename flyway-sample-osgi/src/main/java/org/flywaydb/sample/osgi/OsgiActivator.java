/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.sample.osgi;

import org.flywaydb.core.Flyway;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Properties;

/**
 * Entry point for the OSGi bundle
 */
public class OsgiActivator implements BundleActivator {
    public void start(BundleContext context) throws Exception {
        System.out.println("Starting Flyway Sample OSGi");

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class<?> h2 = classLoader.loadClass("org.h2.Driver");

            Properties properties = new Properties();
            properties.setProperty("flyway.driver", h2.getName());
            properties.setProperty("flyway.url", "jdbc:h2:mem:flyway_db;DB_CLOSE_DELAY=-1");
            properties.setProperty("flyway.user", "sa");
            properties.setProperty("flyway.password", "");

            Flyway flyway = new Flyway(classLoader);
            flyway.configure(properties);
            flyway.setLocations("db.migration", "org.flywaydb.sample.osgi.fragment");
            flyway.migrate();

            System.out.println("New schema version: " + flyway.info().current().getVersion());

            System.exit(0);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public void stop(BundleContext context) throws Exception {
        System.out.println("Stopping Flyway Sample OSGi");
    }
}
