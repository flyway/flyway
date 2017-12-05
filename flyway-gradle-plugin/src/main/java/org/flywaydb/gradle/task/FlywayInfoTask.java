/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.gradle.task;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.internal.info.MigrationInfoDumper;

public class FlywayInfoTask extends AbstractFlywayTask {
    public FlywayInfoTask() {
        super();
        setDescription("Prints the details and status information about all the migrations.");
    }

    @Override
    protected Object run(Flyway flyway) {
        System.out.println(MigrationInfoDumper.dumpToAsciiTable(flyway.info().all()));
        return null;
    }
}
