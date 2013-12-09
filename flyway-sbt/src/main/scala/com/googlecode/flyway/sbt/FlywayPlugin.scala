/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.sbt

import sbt._
import sbt.classpath._
import Keys._

import com.googlecode.flyway.core.util.jdbc.DriverDataSource
import com.googlecode.flyway.core.Flyway
import javax.sql.DataSource
import com.googlecode.flyway.core.info.MigrationInfoDumper
import com.googlecode.flyway.core.util.logging.{LogFactory, LogCreator}
import scala.Some
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object FlywayPlugin extends Plugin {

  //*********************
  // common migration settings for all tasks
  //*********************

  val flywayDriver = settingKey[Option[String]]("The fully qualified classname of the jdbc driver to use to connect to the database. By default, the driver is autodetected based on the url.")
  val flywayUrl = settingKey[String]("The jdbc url to use to connect to the database.")
  val flywayUser = settingKey[Option[String]]("The user to use to connect to the database.")
  val flywayPassword = settingKey[Option[String]]("The password to use to connect to the database.")

  val flywaySchemas = settingKey[Seq[String]]("List of the schemas managed by Flyway. The first schema in the list will be automatically set as the default one during the migration. It will also be the one containing the metadata table. These schema names are case-sensitive. (default: The default schema for the datasource connection)")
  val flywayTable = settingKey[String]("The name of the metadata table that will be used by Flyway. (default: schema_version) By default (single-schema mode) the metadata table is placed in the default schema for the connection provided by the datasource. When the flyway.schemas property is set (multi-schema mode), the metadata table is placed in the first schema of the list.")
  val flywayInitVersion = settingKey[String]("The version to tag an existing schema with when executing init. (default: 1)")
  val flywayInitDescription = settingKey[String]("The description to tag an existing schema with when executing init. (default: << Flyway Init >>)")

  //*********************
  // common settings for migration loading tasks (used by migrate, validate, info)
  //*********************

  val flywayLocations = settingKey[Seq[String]]("Locations on the classpath to scan recursively for migrations. Locations may contain both sql and code-based migrations. (default: db/migration)")
  val flywayEncoding = settingKey[String]("The encoding of Sql migrations. (default: UTF-8)")
  val flywaySqlMigrationPrefix = settingKey[String]("The file name prefix for Sql migrations (default: V) ")
  val flywaySqlMigrationSuffix = settingKey[String]("The file name suffix for Sql migrations (default: .sql)")
  val flywayCleanOnValidationError = settingKey[Boolean]("Whether to automatically call clean or not when a validation error occurs. (default: {@code false})<br/> This is exclusively intended as a convenience for development. Even tough we strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that the next migration will bring you back to the state checked into SCM. Warning ! Do not enable in production !")
  val flywayTarget = settingKey[String]("The target version up to which Flyway should run migrations. Migrations with a higher version number will not be  applied. (default: the latest version)")
  val flywayOutOfOrder = settingKey[Boolean]("Allows migrations to be run \"out of order\" (default: {@code false}). If you already have versions 1 and 3 applied, and now a version 2 is found, it will be applied too instead of being ignored.")

  //*********************
  // settings for migrate
  //*********************

  val flywayIgnoreFailedFutureMigration = settingKey[Boolean]("Ignores failed future migrations when reading the metadata table. These are migrations that we performed by a newer deployment of the application that are not yet available in this version. For example: we have migrations available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0 (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not an option. An older version of the application can then be redeployed, even though a newer one failed due to a bad migration. (default: false)")
  val flywayPlaceholders = settingKey[Map[String, String]]("A map of <placeholder, replacementValue> to apply to sql migration scripts.")
  val flywayPlaceholderPrefix = settingKey[String]("The prefix of every placeholder. (default: ${ )")
  val flywayPlaceholderSuffix = settingKey[String]("The suffix of every placeholder. (default: } )")
  val flywayInitOnMigrate = settingKey[Boolean]("Whether to automatically call init when migrate is executed against a non-empty schema with no metadata table. This schema will then be initialized with the {@code initialVersion} before executing the migrations. Only migrations above {@code initialVersion} will then be applied. This is useful for initial Flyway production deployments on projects with an existing DB. Be careful when enabling this as it removes the safety net that ensures Flyway does not migrate the wrong database in case of a configuration mistake! (default: {@code false})")
  val flywayValidateOnMigrate = settingKey[Boolean]("Whether to automatically call validate or not when running migrate. (default: {@code false})")

  //*********************
  // convenience settings
  //*********************

  private case class FlywayConfigBase(schemas: Seq[String], table: String, initVersion: String, initDescription: String)
  private case class FlywayConfigMigrationLoading(locations: Seq[String], encoding: String, sqlMigrationPrefix: String, sqlMigrationSuffix: String,
                                           cleanOnValidationError: Boolean, target: String, outOfOrder: Boolean)

  private lazy val flywayDataSource = taskKey[DataSource]("The flyway datasource.")
  private lazy val flywayConfigBase = taskKey[FlywayConfigBase]("The flyway base configuration.")
  private lazy val flywayConfigMigrationLoading = taskKey[FlywayConfigMigrationLoading]("The flyway migration loading configuration.")
  private lazy val flyway = taskKey[Flyway]("The flyway object")

  //*********************
  // flyway tasks
  //*********************

  val flywayMigrate = taskKey[Unit]("Migrates of the configured database to the latest version.")
  val flywayValidate = taskKey[Unit]("Validates the applied migrations in the database against the available classpath migrations in order to detect accidental migration changes.")
  val flywayInfo = taskKey[Unit]("Retrieves the complete information about the migrations including applied, pending and current migrations with details and status.")
  val flywayClean = taskKey[Unit]("Drops all database objects.")
  val flywayInit = taskKey[Unit]("Initializes the metadata table in an existing schema.")
  val flywayRepair = taskKey[Unit]("Repairs the metadata table after a failed migration on a database without DDL transactions.")
  val flywayLog = taskKey[Unit]("Repairs the metadata table after a failed migration on a database without DDL transactions.")

  //*********************
  // flyway defaults
  //*********************



  lazy val flywaySettings :Seq[Setting[_]] = {
    val defaults = new Flyway()
    Seq[Setting[_]](
      flywayDriver := None,
      flywayUser := None,
      flywayPassword := None,
      flywayLocations := defaults.getLocations.toSeq,
      flywaySchemas := defaults.getSchemas.toSeq,
      flywayTable := defaults.getTable,
      flywayInitVersion := defaults.getInitVersion.getVersion,
      flywayInitDescription := defaults.getInitDescription,
      flywayEncoding := defaults.getEncoding,
      flywaySqlMigrationPrefix := defaults.getSqlMigrationPrefix,
      flywaySqlMigrationSuffix := defaults.getSqlMigrationSuffix,
      flywayCleanOnValidationError := defaults.isCleanOnValidationError,
      flywayTarget := defaults.getTarget.getVersion,
      flywayOutOfOrder := defaults.isOutOfOrder,
      flywayIgnoreFailedFutureMigration := defaults.isIgnoreFailedFutureMigration,
      flywayPlaceholders := defaults.getPlaceholders.asScala.toMap,
      flywayPlaceholderPrefix := defaults.getPlaceholderPrefix,
      flywayPlaceholderSuffix := defaults.getPlaceholderSuffix,
      flywayInitOnMigrate := defaults.isInitOnMigrate,
      flywayValidateOnMigrate := defaults.isValidateOnMigrate,
      flywayDataSource <<= (fullClasspath in Runtime, flywayDriver, flywayUrl, flywayUser, flywayPassword) map {
        (cp, driver, url, user, password) =>
          withContextClassLoader(cp) {
            new DriverDataSource(driver.getOrElse(null), url, user.getOrElse(null), password.getOrElse(null))
          }
      },
      flywayConfigBase <<= (flywaySchemas, flywayTable, flywayInitVersion, flywayInitDescription) map {
        (schemas, table, initVersion, initDescription) =>
          FlywayConfigBase(schemas, table, initVersion, initDescription)
      },
      flywayConfigMigrationLoading <<= (flywayLocations, flywayEncoding, flywaySqlMigrationPrefix, flywaySqlMigrationSuffix, flywayCleanOnValidationError, flywayTarget, flywayOutOfOrder) map {
        (locations, encoding, sqlMigrationPrefix, sqlMigrationSuffix, cleanOnValidationError, target, outOfOrder) =>
          FlywayConfigMigrationLoading(locations, encoding, sqlMigrationPrefix, sqlMigrationSuffix, cleanOnValidationError, target, outOfOrder)
      },
      flyway <<= (fullClasspath in Runtime, streams, flywayDataSource, flywayConfigBase, flywayConfigMigrationLoading, copyResources in Runtime) map {
        (cp, s, dataSource, configBase, configMigrationLoading, r) =>
          withContextClassLoader(cp) {
            LogFactory.setLogCreator(SbtLogCreator)
            redirectLogger(s)
            val flyway = new Flyway()
            flyway.setDataSource(dataSource)
            flyway.setSchemas(configBase.schemas: _*)
            flyway.setTable(configBase.table)
            flyway.setInitVersion(configBase.initVersion)
            flyway.setInitDescription(configBase.initDescription)
            flyway.setLocations(configMigrationLoading.locations: _*)
            flyway.setEncoding(configMigrationLoading.encoding)
            flyway.setSqlMigrationPrefix(configMigrationLoading.sqlMigrationPrefix)
            flyway.setSqlMigrationSuffix(configMigrationLoading.sqlMigrationSuffix)
            flyway.setCleanOnValidationError(configMigrationLoading.cleanOnValidationError)
            flyway.setTarget(configMigrationLoading.target)
            flyway.setOutOfOrder(configMigrationLoading.outOfOrder)
            flyway
          }
      },
      flywayMigrate <<= (fullClasspath in Runtime, flyway, streams, flywayIgnoreFailedFutureMigration, flywayPlaceholders, flywayPlaceholderPrefix, flywayPlaceholderSuffix, flywayInitOnMigrate, flywayValidateOnMigrate) map {
        (cp, flyway, s, ignoreFailedFutureMigration, placeholders, placeholderPrefix, placeholderSuffix, initOnMigrate, validateOnMigrate) =>
          withContextClassLoader(cp) {
            redirectLogger(s)
            flyway.setIgnoreFailedFutureMigration(ignoreFailedFutureMigration)
            flyway.setPlaceholders(placeholders)
            flyway.setPlaceholderPrefix(placeholderPrefix)
            flyway.setPlaceholderSuffix(placeholderSuffix)
            flyway.setInitOnMigrate(initOnMigrate)
            flyway.setValidateOnMigrate(validateOnMigrate)
            flyway.migrate()
          }
      },
      flywayValidate <<= (fullClasspath in Runtime, flyway, streams) map { (cp, flyway, s) => withContextClassLoader(cp) { redirectLogger(s); flyway.validate() } },
      flywayInfo <<= (fullClasspath in Runtime, flyway, streams) map { (cp, flyway, s) => withContextClassLoader(cp) { redirectLogger(s); s.log.info(MigrationInfoDumper.dumpToAsciiTable(flyway.info().all())) } },
      flywayRepair <<= (fullClasspath in Runtime, flyway, streams) map { (cp, flyway, s) => withContextClassLoader(cp) { redirectLogger(s); flyway.repair() } },
      flywayClean <<= (fullClasspath in Runtime, flyway, streams) map { (cp, flyway, s) => withContextClassLoader(cp) { redirectLogger(s); flyway.clean() } },
      flywayInit <<= (fullClasspath in Runtime, flyway, streams) map { (cp, flyway, s) => withContextClassLoader(cp) { redirectLogger(s); flyway.init() } }
    )
  }

  private def redirectLogger(streams: TaskStreams) {
    FlywaySbtLog.streams = Some(streams)
  }

  private def withContextClassLoader[T](cp: Types.Id[Keys.Classpath])(f: => T): T = {
    val classloader = ClasspathUtilities.toLoader(cp.map(_.data), getClass.getClassLoader)
    Threads.withContextClassLoader(classloader)(f)
  }
}

private[this] object Threads {
  def withContextClassLoader[T](classloader: ClassLoader)(b: => T): T = {
    val thread = Thread.currentThread
    val oldLoader = thread.getContextClassLoader
    try {
      thread.setContextClassLoader(classloader)
      b
    } finally {
      thread.setContextClassLoader(oldLoader)
    }
  }
}

private[this] object SbtLogCreator extends LogCreator {
  def createLogger(clazz: Class[_]) = FlywaySbtLog
}

private[this] object FlywaySbtLog extends com.googlecode.flyway.core.util.logging.Log {
  var streams: Option[TaskStreams] = None
  def debug(message: String) { streams map (_.log.debug(message)) }
  def info(message: String) { streams map (_.log.info(message)) }
  def warn(message: String) { streams map (_.log.warn(message)) }
  def error(message: String) { streams map (_.log.error(message)) }
  def error(message: String, e: Exception) { streams map (_.log.error(message)); streams map (_.log.trace(e)) }
}