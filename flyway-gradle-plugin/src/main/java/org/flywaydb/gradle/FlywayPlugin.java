/*-
 * ========================LICENSE_START=================================
 * flyway-gradle-plugin
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
package org.flywaydb.gradle;

import org.flywaydb.gradle.task.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Registers the plugin's tasks.
 */
public class FlywayPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getExtensions().create("flyway", FlywayExtension.class);
        project.getTasks().register("flywayClean", FlywayCleanTask.class);
        project.getTasks().register("flywayBaseline", FlywayBaselineTask.class);
        project.getTasks().register("flywayMigrate", FlywayMigrateTask.class);
        project.getTasks().register("flywayUndo", FlywayUndoTask.class);
        project.getTasks().register("flywayValidate", FlywayValidateTask.class);
        project.getTasks().register("flywayInfo", FlywayInfoTask.class);
        project.getTasks().register("flywayRepair", FlywayRepairTask.class);
    }
}
