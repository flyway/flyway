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
package org.flywaydb.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.flywaydb.core.Flyway;
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
    protected void doExecute(Flyway flyway) throws Exception {
        log.info("\n" + MigrationInfoDumper.dumpToAsciiTable(flyway.info().all()));
    }
}