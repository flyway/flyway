package org.flywaydb.gradle.task;

import org.flywaydb.core.Flyway;

public class FlywayBaselineTask extends AbstractFlywayTask {
    public FlywayBaselineTask() {
        super();
        setDescription("Baselines an existing database, excluding all migrations up to and including baselineVersion.");
    }

    @Override
    protected Object run(Flyway flyway) {
        return flyway.baseline();
    }
}