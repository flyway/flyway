/*-
 * ========================LICENSE_START=================================
 * flyway-maven-plugin
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.exception.FlywayValidateException;

/**
 * Maven goal that triggers the migration of the configured database to the latest version.
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
@Mojo(name = "migrate",
        requiresDependencyResolution = ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST,
        threadSafe = true)
public class MigrateMojo extends AbstractFlywayMojo {
    @Override
    protected void doExecute(Flyway flyway) {
        try {
            flyway.migrate();
        } catch (final FlywayValidateException e) {
            if (cleanOnValidationErrorEnabled) {
                getLog().info("Validation failed. Cleaning database because cleanOnValidationError is enabled.");
                flyway.clean();
                flyway.migrate();
            } else {
                throw e;
            }
        }

        final MigrationInfo current = flyway.info().current();
        if (current != null && current.getVersion() != null) {
            mavenProject.getProperties().setProperty("flyway.current", current.getVersion().toString());
        }
    }
}
