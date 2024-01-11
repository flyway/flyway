package org.flywaydb.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.info.MigrationInfoDumper;

/**
 * Maven goal to retrieve the complete information about the migrations including applied, pending and current migrations with
 * details and status.
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
@Mojo(name = "info",
        requiresDependencyResolution = ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST,
        threadSafe = true)
public class InfoMojo extends AbstractFlywayMojo {
    @Override
    protected void doExecute(Flyway flyway) {
        MigrationInfoService info = flyway.info();
        MigrationInfo current = info.current();
        MigrationVersion currentSchemaVersion = current == null ? MigrationVersion.EMPTY : current.getVersion();
        log.info("Schema version: " + currentSchemaVersion);
        log.info("");
        log.info(MigrationInfoDumper.dumpToAsciiTable(info.all()));
    }
}