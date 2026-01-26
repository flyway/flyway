/*-
 * ========================LICENSE_START=================================
 * flyway-gradle-plugin
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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
package org.flywaydb.gradle.task;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.exception.FlywayValidateException;

public class FlywayMigrateTask extends AbstractFlywayTask {
    public FlywayMigrateTask() {
        super();
        setDescription("Migrates the schema to the latest version.");
    }

    @Override
    protected Object run(Flyway flyway) {
        try {
            return flyway.migrate();
        } catch (final FlywayValidateException e) {
            if (cleanOnValidationErrorEnabled) {
                getLogger().info("Validation failed. Cleaning database because cleanOnValidationError is enabled.");
                flyway.clean();
                return flyway.migrate();
            }
            throw e;
        }
    }
}
