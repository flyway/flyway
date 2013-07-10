/**
 * Copyright (C) 2010-2013 the original author or authors.
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
package com.googlecode.flyway.gradle

import com.googlecode.flyway.gradle.task.FlywayCleanTask
import com.googlecode.flyway.gradle.task.FlywayInfoTask
import com.googlecode.flyway.gradle.task.FlywayInitTask
import com.googlecode.flyway.gradle.task.FlywayMigrateTask
import com.googlecode.flyway.gradle.task.FlywayRepairTask
import com.googlecode.flyway.gradle.task.FlywayValidateTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registers the plugin's tasks.
 *
 * @author Ben Manes (ben.manes@gmail.com)
 */
public class FlywayPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.extensions.create('flyway', FlywayExtension)
    project.tasks.create('flywayClean', FlywayCleanTask)
    project.tasks.create('flywayInit', FlywayInitTask)
    project.tasks.create('flywayMigrate', FlywayMigrateTask)
    project.tasks.create('flywayValidate', FlywayValidateTask)
    project.tasks.create('flywayInfo', FlywayInfoTask)
    project.tasks.create('flywayRepair', FlywayRepairTask)
  }
}
