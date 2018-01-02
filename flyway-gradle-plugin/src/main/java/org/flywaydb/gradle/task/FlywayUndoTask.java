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

public class FlywayUndoTask extends AbstractFlywayTask {
    public FlywayUndoTask() {
        super();
        setDescription("Undoes the most recently applied versioned migration. Flyway Pro and Flyway Enterprise only.");
    }

    @Override
    protected Object run(Flyway flyway) {
        return flyway.undo();
    }
}
