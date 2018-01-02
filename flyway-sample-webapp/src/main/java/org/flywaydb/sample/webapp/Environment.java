/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.sample.webapp;

import org.flywaydb.core.Flyway;

/**
 * Environment of this application.
 */
public class Environment {
    /**
     * Creates a new Flyway instance.
     *
     * @return The fully configured Flyway instance.
     */
    public static Flyway createFlyway() {
        Flyway flyway = new Flyway();
        flyway.setDataSource("jdbc:h2:mem:flyway_db;DB_CLOSE_DELAY=-1", "sa", "");

        flyway.setLocations("db.migration",
                "db/more/migrations",
                "org.flywaydb.sample.migration",
                "org/flywaydb/sample/webapp/migration");

        return flyway;
    }
}
