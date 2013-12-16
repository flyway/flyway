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
import com.googlecode.flyway.core.info.MigrationInfoDumper
import com.googlecode.flyway.core.util.logging.{LogFactory, LogCreator}
import scala.Some
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object FlywayPlugin extends Plugin {

  //*********************
  // common migration settings for all tasks
  //*********************

  val flywayDriver = settingKey[String]("The fully qualified classname of the jdbc driver to use to connect to the database. By default, the driver is autodetected based on the url.")
  val flywayUrl = settingKey[String]("The jdbc url to use to connect to the database.")
  val flywayUser = settingKey[String]("The user to use to connect to the database.")
  val flywayPassword = settingKey[String]("The password to use to connect to the database.")

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

  private case class ConfigDataSource(driver: String, url: String, user: String, password: String)
  private case class ConfigBase(schemas: Seq[String], table: String, initVersion: String, initDescription: String)
  private case class ConfigMigrationLoading(locations: Seq[String], encoding: String, sqlMigrationPrefix: String, sqlMigrationSuffix: String,
                                           cleanOnValidationError: Boolean, target: String, outOfOrder: Boolean)
  private case class ConfigMigrate(ignoreFailedFutureMigration: Boolean, placeholders: Map[String, String],
                                         placeholderPrefix: String, placeholderSuffix: String, initOnMigrate: Boolean, validateOnMigrate: Boolean)
  private case class Config(dataSource: ConfigDataSource, base: ConfigBase, migrationLoading: ConfigMigrationLoading, migrate: ConfigMigrate)

  private lazy val flywayConfigDataSource = taskKey[ConfigDataSource]("The flyway data source configuration.")
  private lazy val flywayConfigBase = taskKey[ConfigBase]("The flyway base configuration.")
  private lazy val flywayConfigMigrationLoading = taskKey[ConfigMigrationLoading]("The flyway migration loading configuration.")
  private lazy val flywayConfigMigrate = taskKey[ConfigMigrate]("The flyway migrate configuration.")
  private lazy val flywayConfig = taskKey[Config]("The flyway configuration.")

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
      flywayDriver := "",
      flywayUser := "",
      flywayPassword := "",
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
      flywayConfigDataSource <<= (flywayDriver, flywayUrl, flywayUser, flywayPassword) map {
        (driver, url, user, password) => ConfigDataSource(driver, url, user, password)
      },
      flywayConfigBase <<= (flywaySchemas, flywayTable, flywayInitVersion, flywayInitDescription) map {
        (schemas, table, initVersion, initDescription) =>
          ConfigBase(schemas, table, initVersion, initDescription)
      },
      flywayConfigMigrationLoading <<= (flywayLocations, flywayEncoding, flywaySqlMigrationPrefix, flywaySqlMigrationSuffix, flywayCleanOnValidationError, flywayTarget, flywayOutOfOrder) map {
        (locations, encoding, sqlMigrationPrefix, sqlMigrationSuffix, cleanOnValidationError, target, outOfOrder) =>
          ConfigMigrationLoading(locations, encoding, sqlMigrationPrefix, sqlMigrationSuffix, cleanOnValidationError, target, outOfOrder)
      },
      flywayConfigMigrate <<= (flywayIgnoreFailedFutureMigration, flywayPlaceholders, flywayPlaceholderPrefix, flywayPlaceholderSuffix, flywayInitOnMigrate, flywayValidateOnMigrate) map {
        (ignoreFailedFutureMigration, placeholders, placeholderPrefix, placeholderSuffix, initOnMigrate, validateOnMigrate) =>
          ConfigMigrate(ignoreFailedFutureMigration, placeholders, placeholderPrefix, placeholderSuffix, initOnMigrate, validateOnMigrate)
      },
      flywayConfig <<= (flywayConfigDataSource, flywayConfigBase, flywayConfigMigrationLoading, flywayConfigMigrate) map {
        (dataSource, base, migrationLoading, migrate) => Config(dataSource, base, migrationLoading, migrate)
      },
      flywayMigrate <<= (fullClasspath in Runtime, flywayConfig, streams) map {
        (cp, config, s) => withPrepared(cp, s) { Flyway(config).configureSysProps().migrate() }
      },
      flywayValidate <<= (fullClasspath in Runtime, flywayConfig, streams) map {
        (cp, config, s) => withPrepared(cp, s) { Flyway(config).configureSysProps().validate() }
      },
      flywayInfo <<= (fullClasspath in Runtime, flywayConfig, streams) map {
        (cp, config, s) => withPrepared(cp, s) { s.log.info(MigrationInfoDumper.dumpToAsciiTable(Flyway(config).configureSysProps().info().all())) }
      },
      flywayRepair <<= (fullClasspath in Runtime, flywayConfig, streams) map {
        (cp, config, s) => withPrepared(cp, s) { Flyway(config).configureSysProps().repair() }
      },
      flywayClean <<= (fullClasspath in Runtime, flywayConfig, streams) map {
        (cp, config, s) => withPrepared(cp, s) { Flyway(config).configureSysProps().clean() }
      },
      flywayInit <<= (fullClasspath in Runtime, flywayConfig, streams) map {
        (cp, config, s) => withPrepared(cp, s) { Flyway(config).configureSysProps().init() }
      }
    )
  }

  private def withPrepared[T](cp: Types.Id[Keys.Classpath], streams: TaskStreams)(f: => T): T = {
    registerAsFlywayLogger(streams)
    withContextClassLoader(cp)(f)
  }

  /**
   * registers sbt log as a static logger for Flyway
   */
  private def registerAsFlywayLogger(streams: TaskStreams) {
    LogFactory.setLogCreator(SbtLogCreator)
    FlywaySbtLog.streams = Some(streams)
  }

  private def withContextClassLoader[T](cp: Types.Id[Keys.Classpath])(f: => T): T = {
    val classloader = ClasspathUtilities.toLoader(cp.map(_.data), getClass.getClassLoader)
    val thread = Thread.currentThread
    val oldLoader = thread.getContextClassLoader
    try {
      thread.setContextClassLoader(classloader)
      f
    } finally {
      thread.setContextClassLoader(oldLoader)
    }
  }

  private object Flyway {
    def apply(config: Config): Flyway = {
      val flyway = new Flyway()
      flyway.configure(config)
      flyway
    }
  }

  private implicit class StringOps(val s: String) extends AnyVal {
    def emptyToNull(): String = s match {
      case ss if ss.isEmpty => null
      case _ => s
    }
  }

  private implicit class FlywayOps(val flyway: Flyway) extends AnyVal {
    def configure(config: Config): Flyway = {
      flyway.configure(config.dataSource)
      .configure(config.base)
      .configure(config.migrationLoading)
      .configure(config.migrate)
    }

    def configure(config: ConfigDataSource): Flyway = {
      flyway.setDataSource(new DriverDataSource(config.driver.emptyToNull(), config.url, config.user, config.password))
      flyway
    }
    def configure(config: ConfigBase): Flyway = {
      flyway.setSchemas(config.schemas: _*)
      flyway.setTable(config.table)
      flyway.setInitVersion(config.initVersion)
      flyway.setInitDescription(config.initDescription)
      flyway
    }
    def configure(config: ConfigMigrationLoading): Flyway = {
      flyway.setLocations(config.locations: _*)
      flyway.setEncoding(config.encoding)
      flyway.setSqlMigrationPrefix(config.sqlMigrationPrefix)
      flyway.setSqlMigrationSuffix(config.sqlMigrationSuffix)
      flyway.setCleanOnValidationError(config.cleanOnValidationError)
      flyway.setTarget(config.target)
      flyway.setOutOfOrder(config.outOfOrder)
      flyway
    }
    def configure(config: ConfigMigrate): Flyway = {
      flyway.setIgnoreFailedFutureMigration(config.ignoreFailedFutureMigration)
      flyway.setPlaceholders(config.placeholders)
      flyway.setPlaceholderPrefix(config.placeholderPrefix)
      flyway.setPlaceholderSuffix(config.placeholderSuffix)
      flyway.setInitOnMigrate(config.initOnMigrate)
      flyway.setValidateOnMigrate(config.validateOnMigrate)
      flyway
    }
    def configureSysProps(): Flyway = {
      flyway.configure(System.getProperties)
      flyway
    }
  }

  private object SbtLogCreator extends LogCreator {
    def createLogger(clazz: Class[_]) = FlywaySbtLog
  }

  private object FlywaySbtLog extends com.googlecode.flyway.core.util.logging.Log {
    var streams: Option[TaskStreams] = None
    def debug(message: String) { streams map (_.log.debug(message)) }
    def info(message: String) { streams map (_.log.info(message)) }
    def warn(message: String) { streams map (_.log.warn(message)) }
    def error(message: String) { streams map (_.log.error(message)) }
    def error(message: String, e: Exception) { streams map (_.log.error(message)); streams map (_.log.trace(e)) }
  }
}


