/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;

/**
 * Undoes the most recently applied versioned migration.
 * <p><i>Flyway Pro and Flyway Enterprise only</i></p>
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
@Mojo(name = "undo",
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true)
public class UndoMojo extends AbstractFlywayMojo {
    @Override
    protected void doExecute(Flyway flyway) {
        flyway.undo();

        MigrationInfo current = flyway.info().current();
        if (current != null && current.getVersion() != null) {
            mavenProject.getProperties().setProperty("flyway.current", current.getVersion().toString());
        }
    }
}
