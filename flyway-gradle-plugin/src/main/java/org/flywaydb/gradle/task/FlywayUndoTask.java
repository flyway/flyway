/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
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
