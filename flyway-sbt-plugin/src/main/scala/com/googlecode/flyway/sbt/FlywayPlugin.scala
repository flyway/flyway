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
import scala.collection.JavaConversions._
import com.googlecode.flyway.core.util.logging.{LogFactory, LogCreator}
import scala.Some

object FlywayPlugin extends Plugin {

  //*********************
  // common migration settings for all tasks
  //*********************

  val flywayDriver = SettingKey[Option[String]]("The fully qualified classname of the jdbc driver to use to connect to the database. By default, the driver is autodetected based on the url.")
  val flywayUrl = SettingKey[String]("The jdbc url to use to connect to the database.")
  val flywayUser = SettingKey[String]("The user to use to connect to the database. (default: blank)")
  val flywayPassword = SettingKey[String]("The password to use to connect to the database. (default: blank)")

  val flywaySchemas = SettingKey[Option[Seq[String]]]("List of the schemas managed by Flyway. The first schema in the list will be automatically set as the default one during the migration. It will also be the one containing the metadata table. These schema names are case-sensitive. (default: The default schema for the datasource connection)")
  val flywayTable = SettingKey[Option[String]]("The name of the metadata table that will be used by Flyway. (default: schema_version) By default (single-schema mode) the metadata table is placed in the default schema for the connection provided by the datasource. When the flyway.schemas property is set (multi-schema mode), the metadata table is placed in the first schema of the list.")
  val flywayInitVersion = SettingKey[Option[String]]("The version to tag an existing schema with when executing init. (default: 1)")
  val flywayInitDescription = SettingKey[Option[String]]("The description to tag an existing schema with when executing init. (default: << Flyway Init >>)")

  //*********************
  // common settings for migration loading tasks (used by migrate, validate, info)
  //*********************

  val flywayLocations = SettingKey[Seq[String]]("Locations on the classpath to scan recursively for migrations. Locations may contain both sql and code-based migrations. (default: db/migration)")
  val flywayEncoding = SettingKey[Option[String]]("The encoding of Sql migrations. (default: UTF-8)")
  val flywaySqlMigrationPrefix = SettingKey[Option[String]]("The file name prefix for Sql migrations (default: V) ")
  val flywaySqlMigrationSuffix = SettingKey[Option[String]]("The file name suffix for Sql migrations (default: .sql)")
  val flywayCleanOnValidationError = SettingKey[Option[Boolean]]("Whether to automatically call clean or not when a validation error occurs. (default: {@code false})<br/> This is exclusively intended as a convenience for development. Even tough we strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that the next migration will bring you back to the state checked into SCM. Warning ! Do not enable in production !")
  val flywayTarget = SettingKey[Option[String]]("The target version up to which Flyway should run migrations. Migrations with a higher version number will not be  applied. (default: the latest version)")
  val flywayOutOfOrder = SettingKey[Option[Boolean]]("Allows migrations to be run \"out of order\" (default: {@code false}). If you already have versions 1 and 3 applied, and now a version 2 is found, it will be applied too instead of being ignored.")

  //*********************
  // settings for migrate
  //*********************

  val flywayIgnoreFailedFutureMigration = SettingKey[Option[Boolean]]("Ignores failed future migrations when reading the metadata table. These are migrations that we performed by a newer deployment of the application that are not yet available in this version. For example: we have migrations available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0 (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not an option. An older version of the application can then be redeployed, even though a newer one failed due to a bad migration. (default: false)")
  val flywayPlaceholders = SettingKey[Map[String, String]]("A map of <placeholder, replacementValue> to apply to sql migration scripts.")
  val flywayPlaceholderPrefix = SettingKey[Option[String]]("The prefix of every placeholder. (default: ${ )")
  val flywayPlaceholderSuffix = SettingKey[Option[String]]("The suffix of every placeholder. (default: } )")
  val flywayInitOnMigrate = SettingKey[Option[Boolean]]("Whether to automatically call init when migrate is executed against a non-empty schema with no metadata table. This schema will then be initialized with the {@code initialVersion} before executing the migrations. Only migrations above {@code initialVersion} will then be applied. This is useful for initial Flyway production deployments on projects with an existing DB. Be careful when enabling this as it removes the safety net that ensures Flyway does not migrate the wrong database in case of a configuration mistake! (default: {@code false})")
  val flywayValidateOnMigrate = SettingKey[Option[Boolean]]("Whether to automatically call validate or not when running migrate. (default: {@code false})")

  //*********************
  // convenience settings
  //*********************

  private case class FlywayConfigBase(schemas: Option[Seq[String]], table: Option[String], initVersion: Option[String], initDescription: Option[String])
  private case class FlywayConfigMigrationLoading(locations: Seq[String], encoding: Option[String], sqlMigrationPrefix: Option[String], sqlMigrationSuffix: Option[String],
                                           cleanOnValidationError: Option[Boolean], target: Option[String], outOfOrder: Option[Boolean])

  private lazy val flywayDataSource = TaskKey[DataSource]("The flyway datasource.")
  private lazy val flywayConfigBase = TaskKey[FlywayConfigBase]("The flyway base configuration.")
  private lazy val flywayConfigMigrationLoading = TaskKey[FlywayConfigMigrationLoading]("The flyway migration loading configuration.")
  private lazy val flyway = TaskKey[Flyway]("The flyway object")

  //*********************
  // flyway tasks
  //*********************

  val flywayMigrate = TaskKey[Unit]("Migrates of the configured database to the latest version.")
  val flywayValidate = TaskKey[Unit]("Validates the applied migrations in the database against the available classpath migrations in order to detect accidental migration changes.")
  val flywayInfo = TaskKey[Unit]("Retrieves the complete information about the migrations including applied, pending and current migrations with details and status.")
  val flywayClean = TaskKey[Unit]("Drops all database objects.")
  val flywayInit = TaskKey[Unit]("Initializes the metadata table in an existing schema.")
  val flywayRepair = TaskKey[Unit]("Repairs the metadata table after a failed migration on a database without DDL transactions.")
  val flywayLog = TaskKey[Unit]("Repairs the metadata table after a failed migration on a database without DDL transactions.")

  //*********************
  // flyway defaults
  //*********************

  lazy val flywaySettings :Seq[Setting[_]] = Seq[Setting[_]](
    flywayDriver := None,
    flywayLocations := Seq("db/migration"),
    flywaySchemas := None,
    flywayTable := None,
    flywayInitVersion := None,
    flywayInitDescription := None,
    flywayEncoding := None,
    flywaySqlMigrationPrefix := None,
    flywaySqlMigrationSuffix := None,
    flywayCleanOnValidationError := None,
    flywayTarget := None,
    flywayOutOfOrder := None,
    flywayIgnoreFailedFutureMigration := None,
    flywayPlaceholders := Map(),
    flywayPlaceholderPrefix := None,
    flywayPlaceholderSuffix := None,
    flywayInitOnMigrate := None,
    flywayValidateOnMigrate := None,
    flywayDataSource <<= (fullClasspath in Runtime, flywayDriver, flywayUrl, flywayUser, flywayPassword) map {
      (cp, driver, url, user, password) =>
        withContextClassLoader(cp) {
          new DriverDataSource(if (driver.isEmpty) null else driver.get, url, user, password)
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
          configBase.schemas map (flyway.setSchemas(_: _*))
          configBase.table map flyway.setTable
          configBase.initVersion map flyway.setInitVersion
          configBase.initDescription map flyway.setInitDescription
          flyway.setLocations(configMigrationLoading.locations: _*)
          configMigrationLoading.encoding map flyway.setEncoding
          configMigrationLoading.sqlMigrationPrefix map flyway.setSqlMigrationPrefix
          configMigrationLoading.sqlMigrationSuffix map flyway.setSqlMigrationSuffix
          configMigrationLoading.cleanOnValidationError map flyway.setCleanOnValidationError
          configMigrationLoading.target map flyway.setTarget
          configMigrationLoading.outOfOrder map flyway.setOutOfOrder
          flyway
        }
    },
    flywayMigrate <<= (fullClasspath in Runtime, flyway, streams, flywayIgnoreFailedFutureMigration, flywayPlaceholders, flywayPlaceholderPrefix, flywayPlaceholderSuffix, flywayInitOnMigrate, flywayValidateOnMigrate) map {
      (cp, flyway, s, ignoreFailedFutureMigration, placeholders, placeholderPrefix, placeholderSuffix, initOnMigrate, validateOnMigrate) =>
        withContextClassLoader(cp) {
          redirectLogger(s)
          ignoreFailedFutureMigration map flyway.setIgnoreFailedFutureMigration
          flyway.setPlaceholders(placeholders)
          placeholderPrefix map flyway.setPlaceholderPrefix
          placeholderSuffix map flyway.setPlaceholderSuffix
          initOnMigrate map flyway.setInitOnMigrate
          validateOnMigrate map flyway.setValidateOnMigrate
          flyway.migrate()
        }
    },
    flywayValidate <<= (fullClasspath in Runtime, flyway, streams) map { (cp, flyway, s) => withContextClassLoader(cp) { redirectLogger(s); flyway.validate() } },
    flywayInfo <<= (fullClasspath in Runtime, flyway, streams) map { (cp, flyway, s) => withContextClassLoader(cp) { redirectLogger(s); s.log.info(MigrationInfoDumper.dumpToAsciiTable(flyway.info().all())) } },
    flywayRepair <<= (fullClasspath in Runtime, flyway, streams) map { (cp, flyway, s) => withContextClassLoader(cp) { redirectLogger(s); flyway.repair() } },
    flywayClean <<= (fullClasspath in Runtime, flyway, streams) map { (cp, flyway, s) => withContextClassLoader(cp) { redirectLogger(s); flyway.clean() } },
    flywayInit <<= (fullClasspath in Runtime, flyway, streams) map { (cp, flyway, s) => withContextClassLoader(cp) { redirectLogger(s); flyway.init() } }
  )

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