/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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