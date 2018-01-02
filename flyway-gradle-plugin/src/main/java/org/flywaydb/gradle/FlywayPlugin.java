/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.gradle;

import org.flywaydb.gradle.task.FlywayBaselineTask;
import org.flywaydb.gradle.task.FlywayCleanTask;
import org.flywaydb.gradle.task.FlywayInfoTask;
import org.flywaydb.gradle.task.FlywayMigrateTask;
import org.flywaydb.gradle.task.FlywayRepairTask;
import org.flywaydb.gradle.task.FlywayUndoTask;
import org.flywaydb.gradle.task.FlywayValidateTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Registers the plugin's tasks.
 */
public class FlywayPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getExtensions().create("flyway", FlywayExtension.class);
        project.getTasks().create("flywayClean", FlywayCleanTask.class);
        project.getTasks().create("flywayBaseline", FlywayBaselineTask.class);
        project.getTasks().create("flywayMigrate", FlywayMigrateTask.class);
        project.getTasks().create("flywayUndo", FlywayUndoTask.class);
        project.getTasks().create("flywayValidate", FlywayValidateTask.class);
        project.getTasks().create("flywayInfo", FlywayInfoTask.class);
        project.getTasks().create("flywayRepair", FlywayRepairTask.class);
    }
}
