package org.flywaydb.gradle.task;

import org.flywaydb.core.Flyway;

public class FlywayCleanTask extends AbstractFlywayTask {
    public FlywayCleanTask() {
        super();
        setDescription("Drops all objects in the configured schemas.");
    }

    @Override
    protected Object run(Flyway flyway) {
        return flyway.clean();
    }
}