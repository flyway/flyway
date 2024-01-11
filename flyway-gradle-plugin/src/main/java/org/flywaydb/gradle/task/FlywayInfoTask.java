package org.flywaydb.gradle.task;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.info.MigrationInfoDumper;

public class FlywayInfoTask extends AbstractFlywayTask {
    public FlywayInfoTask() {
        super();
        setDescription("Prints the details and status information about all the migrations.");
    }

    @Override
    protected Object run(Flyway flyway) {
        MigrationInfoService info = flyway.info();
        MigrationInfo current = info.current();
        MigrationVersion currentSchemaVersion = current == null ? MigrationVersion.EMPTY : current.getVersion();
        System.out.println("Schema version: " + currentSchemaVersion);
        System.out.println(MigrationInfoDumper.dumpToAsciiTable(info.all()));
        return info;
    }
}