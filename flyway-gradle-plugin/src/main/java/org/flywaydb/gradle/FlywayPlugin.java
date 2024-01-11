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
        project.getTasks().create("flywayClean", FlywayCleanTask.class);
        project.getTasks().create("flywayBaseline", FlywayBaselineTask.class);
        project.getTasks().create("flywayMigrate", FlywayMigrateTask.class);
        project.getTasks().create("flywayUndo", FlywayUndoTask.class);
        project.getTasks().create("flywayValidate", FlywayValidateTask.class);
        project.getTasks().create("flywayInfo", FlywayInfoTask.class);
        project.getTasks().create("flywayRepair", FlywayRepairTask.class);
    }
}