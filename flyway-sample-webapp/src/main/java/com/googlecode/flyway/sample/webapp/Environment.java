/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.sample.webapp;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.util.jdbc.DriverDataSource;
import org.h2.Driver;

/**
 * Environment of this application.
 */
public class Environment {
    /**
     * The Jdbc Driver for AppEngine.
     */
    private static final String APPENGINE_JDBC_DRIVER = "com.google.appengine.api.rdbms.AppEngineDriver";

    /**
     * Checks whether we are currently running on AppEngine.
     *
     * @return {@code true} if we are, {@code false} if not.
     */
    public static boolean runningOnGoogleAppEngine() {
        return System.getProperty("com.google.appengine.runtime.environment") != null;
    }

    /**
     * Creates a new Flyway instance.
     *
     * @return The fully configured Flyway instance.
     */
    public static Flyway createFlyway() {
        Flyway flyway = new Flyway();

        if (runningOnGoogleAppEngine()) {
            flyway.setDataSource(new DriverDataSource(
                    APPENGINE_JDBC_DRIVER,
                    "jdbc:google:rdbms://flyway-test-project:flywaycloudsql/flyway_cloudsql_db",
                    null,
                    null));
        } else {
            flyway.setDataSource(
                    new DriverDataSource(new Driver(), "jdbc:h2:mem:flyway_db;DB_CLOSE_DELAY=-1", "sa", ""));
        }

        flyway.setLocations("db.migration",
                "db/more/migrations",
                "com.googlecode.flyway.sample.migration",
                "com/googlecode/flyway/sample/webapp/migration");

        return flyway;
    }
}
