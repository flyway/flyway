/*
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
package org.flywaydb.sample.webapp;

import org.flywaydb.core.Flyway;

/**
 * Environment of this application.
 */
public class Environment {
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
            flyway.setDataSource("jdbc:google:rdbms://flyway-test-project:flywaycloudsql/flyway_cloudsql_db", null, null);
        } else {
            flyway.setDataSource("jdbc:h2:mem:flyway_db;DB_CLOSE_DELAY=-1", "sa", "");
        }

        flyway.setLocations("db.migration",
                "db/more/migrations",
                "org.flywaydb.sample.migration",
                "org/flywaydb/sample/webapp/migration");

        return flyway;
    }
}
