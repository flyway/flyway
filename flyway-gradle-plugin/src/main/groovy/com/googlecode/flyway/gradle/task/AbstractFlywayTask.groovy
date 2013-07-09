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
package com.googlecode.flyway.gradle.task;

import com.googlecode.flyway.core.Flyway
import com.googlecode.flyway.core.api.FlywayException
import com.googlecode.flyway.core.api.MigrationVersion
import com.googlecode.flyway.core.util.jdbc.DriverDataSource
import com.googlecode.flyway.gradle.FlywayExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * A base class for all flyway tasks.
 *
 * @author Ben Manes (ben.manes@gmail.com)
 * @author Allan Morstein (alkamo@gmail.com)
 */
abstract class AbstractFlywayTask extends DefaultTask {
  private static final DEFAULT_LOCATION = "db/migration"

  AbstractFlywayTask() {
    group = 'Flyway'
    project.afterEvaluate {
      def dependsOnTasks = project.flyway.dependsOnTasks
      if (isJavaProject()) {
        dependsOnTasks += project.tasks.processResources
      }
      this.dependsOn(dependsOnTasks)
    }
  }

  @TaskAction
  def runTask() {
    if (isJavaProject()) {
      addClassesDirToClassLoader()
    }

    project.flyway.databases.each { flywayExt ->
      logger.info "Executing ${this.getName()} for ${flywayExt.name}"
      try {
        run(flywayExt.name, create(flywayExt))
      } catch (Exception e) {
        throw new FlywayException(
          "Error occurred while executing ${this.getName()} for ${flywayExt.name}", e);
      }
    }
  }

  /** Executes the task's custom behavior. */
  def abstract run(String name, Flyway flyway)

  /** Creates a new, configured flyway instance */
  protected def create(FlywayExtension flywayExt) {
    logger.info 'Flyway configuration:'
    def flyway = new Flyway()
    addDataSourceTo(flyway, flywayExt)
    addMetadataTableTo(flyway, flywayExt)
    addSchemasTo(flyway, flywayExt)
    addInitVersionTo(flyway, flywayExt)
    addLocationsTo(flyway, flywayExt)
    addSqlMigrationSettingsTo(flyway, flywayExt)
    addTargetVersionTo(flyway, flywayExt)
    addValidationSettingsTo(flyway, flywayExt)
    flyway
  }

  private def addDataSourceTo(Flyway flyway, FlywayExtension flywayExt) {
    def dataSource = new DriverDataSource(
      flywayExt.driver ?: project.flyway.defaults.driver,
      flywayExt.url ?: project.flyway.defaults.url,
      flywayExt.user ?: project.flyway.defaults.user,
      flywayExt.password ?: project.flyway.defaults.password)
    flyway.setDataSource(dataSource)

    logger.info " - driver: ${flyway.dataSource.driver.class.name}"
    logger.info " - url: ${flyway.dataSource.url}"
    logger.info " - user: ${flyway.dataSource.user}"
    logger.info " - password: ${flyway.dataSource.password?.replaceAll('.', '*')}"
  }

  private def addMetadataTableTo(Flyway flyway, FlywayExtension flywayExt) {
    if (flywayExt.table != null || project.flyway.defaults.table != null) {
      flyway.setTable(flywayExt.table ?: project.flyway.defaults.table)
    }
    logger.info " - table: ${flyway.table}"
  }

  private def addInitVersionTo(Flyway flyway, FlywayExtension flywayExt) {
    if (flywayExt.initVersion != null || project.flyway.defaults.initVersion != null) {
      flyway.setInitVersion(flywayExt.initVersion ?: project.flyway.defaults.initVersion)
    }
    if (flywayExt.initDescription != null || project.flyway.defaults.initDescription != null) {
      flyway.setInitDescription(flywayExt.initDescription ?: project.flyway.defaults.initDescription)
    }
    logger.info " - initVersion: ${flyway.initVersion}"
    logger.info " - initDescription: ${flyway.initDescription}"
  }

  private def addSchemasTo(Flyway flyway, FlywayExtension flywayExt) {
    List schemas
    if (project.flyway.schemaDefaultFirst) {
      schemas = project.flyway.defaults.schemas + flywayExt.schemas
    } else {
      schemas = flywayExt.schemas + project.flyway.defaults.schemas
    }
    if (!schemas.isEmpty()) {
      flyway.setSchemas(schemas as String[])
    }
    logger.info " - schemas: ${flyway.schemas}"
  }

  private def addLocationsTo(Flyway flyway, FlywayExtension flywayExt) {
    def locations = flywayExt.locations + project.flyway.defaults.locations
    if (locations.isEmpty()) {
      locations += defaultLocations()
    }
    flyway.setLocations(locations as String[])
    logger.info ' - locations: ' + (locations.isEmpty() ? DEFAULT_LOCATION : locations)
  }

  private def defaultLocations() {
    def defaults = []
    if (isJavaProject()) {
      def resources = project.sourceSets.main.output.resourcesDir.path
      defaults += "filesystem:${resources}"
    }
    if (hasClasses(DEFAULT_LOCATION)) {
      defaults += "classpath:${DEFAULT_LOCATION}"
    }
    defaults
  }

  private def addSqlMigrationSettingsTo(Flyway flyway, FlywayExtension flywayExt) {
    def defaults = project.flyway.defaults
    if (flywayExt.sqlMigrationPrefix != null || defaults.sqlMigrationPrefix != null) {
      flyway.setSqlMigrationPrefix(flywayExt.sqlMigrationPrefix ?: defaults.sqlMigrationPrefix)
    }
    if (flywayExt.sqlMigrationSuffix != null || defaults.sqlMigrationSuffix != null) {
      flyway.setSqlMigrationSuffix(flywayExt.sqlMigrationSuffix ?: defaults.sqlMigrationSuffix)
    }
    if (flywayExt.encoding != null || defaults.encoding != null) {
      flyway.setEncoding(flywayExt.encoding ?: defaults.encoding)
    }
    if (!(flywayExt.placeholders.isEmpty() && defaults.placeholders.isEmpty())) {
      flyway.setPlaceholders(flywayExt.placeholders + defaults.placeholders)
    }
    if (flywayExt.placeholderPrefix != null || defaults.placeholderPrefix != null) {
      flyway.setPlaceholderPrefix(flywayExt.placeholderPrefix ?: defaults.placeholderPrefix)
    }
    if (flywayExt.placeholderSuffix != null || defaults.placeholderSuffix != null) {
      flyway.setPlaceholderSuffix(flywayExt.placeholderSuffix ?: defaults.placeholderSuffix)
    }
    logger.info " - sql migration prefix: ${flyway.sqlMigrationPrefix}"
    logger.info " - sql migration prefix: ${flyway.sqlMigrationSuffix}"
    logger.info " - encoding: ${flyway.encoding}"
    logger.info " - placeholders: ${flyway.placeholders}"
    logger.info " - placeholder prefix: ${flyway.placeholderPrefix}"
    logger.info " - placeholder suffix: ${flyway.placeholderSuffix}"
  }

  private def addTargetVersionTo(Flyway flyway, FlywayExtension flywayExt) {
    if (flywayExt.target != null || project.flyway.defaults.target != null) {
      flyway.setTarget(new MigrationVersion(flywayExt.target ?: project.flyway.defaults.target))
    }
    logger.info " - target: ${flyway.target}"
  }

  private def addValidationSettingsTo(Flyway flyway, FlywayExtension flywayExt) {
    def defaults = project.flyway.defaults
    if (flywayExt.outOfOrder != null ||  defaults.outOfOrder != null) {
      flyway.setOutOfOrder(flywayExt.outOfOrder ?: defaults.outOfOrder)
    }
    if (flywayExt.validateOnMigrate != null ||  defaults.validateOnMigrate != null) {
      flyway.setValidateOnMigrate(flywayExt.validateOnMigrate ?: defaults.validateOnMigrate)
    }
    if (flywayExt.cleanOnValidationError != null ||  defaults.cleanOnValidationError != null) {
      flyway.setCleanOnValidationError(
        flywayExt.cleanOnValidationError ?: defaults.cleanOnValidationError)
    }
    if (flywayExt.initOnMigrate != null ||  defaults.initOnMigrate != null) {
      flyway.setInitOnMigrate(flywayExt.initOnMigrate ?: defaults.initOnMigrate)
    }
    logger.info " - out of order: ${flyway.outOfOrder}"
    logger.info " - validate on migrate: ${flyway.validateOnMigrate}"
    logger.info " - clean on validation error: ${flyway.cleanOnValidationError}"
    logger.info " - init on migrate: ${flyway.initOnMigrate}"
  }

  protected boolean isJavaProject() {
    project.plugins.hasPlugin('java')
  }

  private def addClassesDirToClassLoader() {
    def classesUrl = project.sourceSets.main.output.classesDir.toURI().toURL()
    def classLoader = Thread.currentThread().getContextClassLoader()
    if (hasClasses() && !classLoader.getURLs().contains(classesUrl)) {
      classLoader.addURL(classesUrl)
      logger.info "Added ${classesUrl} to classloader"
    }
  }

  private def hasClasses(subdirectory = "") {
    def classesDir = new File(project.sourceSets.main.output.classesDir, subdirectory)
    def classesUrl = classesDir.toURI().toURL()
    (classesDir.list()?.length ?: 0) > 0
  }
}
