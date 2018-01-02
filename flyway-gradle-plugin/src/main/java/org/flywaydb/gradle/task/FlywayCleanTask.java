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
package org.flywaydb.gradle.task;

import org.flywaydb.core.Flyway;

public class FlywayCleanTask extends AbstractFlywayTask {
    public FlywayCleanTask() {
        super();
        setDescription("Drops all objects in the configured schemas.");
    }

    @Override
    protected Object run(Flyway flyway) {
        flyway.clean();
        return null;
    }
}
