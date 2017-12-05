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
package org.flywaydb.gradle.task;

import org.flywaydb.core.Flyway;

public class FlywayMigrateTask extends AbstractFlywayTask {
    public FlywayMigrateTask() {
        super();
        setDescription("Migrates the schema to the latest version.");
    }

    @Override
    protected Object run(Flyway flyway) {
        return flyway.migrate();
    }
}
