/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.flywaydb.core.Flyway;

/**
 * Maven goal that drops all database objects (tables, views, procedures, triggers, ...) in the configured schemas.
 * The schemas are cleaned in the order specified by the {@code schemas} property..
 */
@SuppressWarnings({"JavaDoc", "UnusedDeclaration"})
@Mojo(name = "clean",
        requiresDependencyResolution = ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.CLEAN,
        threadSafe = true)
public class CleanMojo extends AbstractFlywayMojo {
    @Override
    protected void doExecute(Flyway flyway) throws Exception {
        flyway.clean();
    }
}