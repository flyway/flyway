/**
 * Copyright 2010-2015 Axel Fontaine
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
package org.flywaydb.sbt

import java.util.Properties

import org.flywaydb.core.Flyway
import org.flywaydb.core.internal.info.MigrationInfoDumper
import org.flywaydb.core.internal.util.logging.{LogCreator, LogFactory}
import sbt.Keys._
import sbt._
import sbt.classpath._

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object FlywayPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {

    //*********************
    // common migration settings for all tasks
    //*********************

    val flywayDriver = settingKey[String]("The fully qualified classname of the jdbc driver to use to connect to the database. By default, the driver is autodetected based on the url.")
    val flywayUrl = settingKey[String]("The jdbc url to use to connect to the database.")
    val flywayUser = settingKey[String]("The user to use to connect to the database.")
    val flywayPassword = settingKey[String]("The password to use to connect to the database.")

    val flywaySchemas = settingKey[Seq[String]]("List of the schemas managed by Flyway. The first schema in the list will be automatically set as the default one during the migration. It will also be the one containing the metadata table. These schema names are case-sensitive. (default: The default schema for the datasource connection)")
    val flywayTable = settingKey[String]("The name of the metadata table that will be used by Flyway. (default: schema_version) By default (single-schema mode) the metadata table is placed in the default schema for the connection provided by the datasource. When the flyway.schemas property is set (multi-schema mode), the metadata table is placed in the first schema of the list.")
    val flywayInitVersion = settingKey[String]("Deprecated and will be removed in Flyway 4.0. Use flywayBaselineVersion instead.")
    val flywayInitDescription = settingKey[String]("Deprecated and will be removed in Flyway 4.0. Use flywayBaselineDescription instead.")
    val flywayBaselineVersion = settingKey[String]("The version to tag an existing schema with when executing baseline. (default: 1)")
    val flywayBaselineDescription = settingKey[String]("The description to tag an existing schema with when executing baseline. (default: << Flyway Baseline >>)")

    //*********************
    // common settings for migration loading tasks (used by migrate, validate, info)
    //*********************

    val flywayLocations = settingKey[Seq[String]]("Locations on the classpath to scan recursively for migrations. Locations may contain both sql and code-based migrations. (default: db/migration)")
    val flywayResolvers = settingKey[Seq[String]](" The fully qualified class names of the custom MigrationResolvers to be used in addition to the built-in ones for resolving Migrations to apply.")
    val flywayEncoding = settingKey[String]("The encoding of Sql migrations. (default: UTF-8)")
    val flywaySqlMigrationPrefix = settingKey[String]("The file name prefix for Sql migrations (default: V) ")
    val flywaySqlMigrationSeparator = settingKey[String]("The file name separator for Sql migrations (default: __)")
    val flywaySqlMigrationSuffix = settingKey[String]("The file name suffix for Sql migrations (default: .sql)")
    val flywayCleanOnValidationError = settingKey[Boolean]("Whether to automatically call clean or not when a validation error occurs. (default: {@code false})<br/> This is exclusively intended as a convenience for development. Even tough we strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that the next migration will bring you back to the state checked into SCM. Warning ! Do not enable in production !")
    val flywayTarget = settingKey[String]("The target version up to which Flyway should run migrations. Migrations with a higher version number will not be  applied. (default: the latest version)")
    val flywayOutOfOrder = settingKey[Boolean]("Allows migrations to be run \"out of order\" (default: {@code false}). If you already have versions 1 and 3 applied, and now a version 2 is found, it will be applied too instead of being ignored.")
    val flywayCallbacks = settingKey[Seq[String]]("A list of fully qualified FlywayCallback implementation classnames that will be used for Flyway lifecycle notifications. (default: Empty)")

    //*********************
    // settings for migrate
    //*********************

    val flywayIgnoreFailedFutureMigration = settingKey[Boolean]("Ignores failed future migrations when reading the metadata table. These are migrations that we performed by a newer deployment of the application that are not yet available in this version. For example: we have migrations available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0 (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not an option. An older version of the application can then be redeployed, even though a newer one failed due to a bad migration. (default: false)")
    val flywayPlaceholders = settingKey[Map[String, String]]("A map of <placeholder, replacementValue> to apply to sql migration scripts.")
    val flywayPlaceholderPrefix = settingKey[String]("The prefix of every placeholder. (default: ${ )")
    val flywayPlaceholderSuffix = settingKey[String]("The suffix of every placeholder. (default: } )")
    val flywayInitOnMigrate = settingKey[Boolean]("Deprecated and will be removed in Flyway 4.0. Use flywayBaselineOnMigrate instead.")
    val flywayBaselineOnMigrate = settingKey[Boolean]("Whether to automatically call baseline when migrate is executed against a non-empty schema with no metadata table. This schema will then be baselined with the {@code baselineVersion} before executing the migrations. Only migrations above {@code baselineVersion} will then be applied. This is useful for initial Flyway production deployments on projects with an existing DB. Be careful when enabling this as it removes the safety net that ensures Flyway does not migrate the wrong database in case of a configuration mistake! (default: {@code false})")
    val flywayValidateOnMigrate = settingKey[Boolean]("Whether to automatically call validate or not when running migrate. (default: {@code true})")

    //*********************
    // flyway tasks
    //*********************

    val flywayMigrate = taskKey[Unit]("Migrates of the configured database to the latest version.")
    val flywayValidate = taskKey[Unit]("Validates the applied migrations in the database against the available classpath migrations in order to detect accidental migration changes.")
    val flywayInfo = taskKey[Unit]("Retrieves the complete information about the migrations including applied, pending and current migrations with details and status.")
    val flywayClean = taskKey[Unit]("Drops all database objects.")
    val flywayInit = taskKey[Unit]("Deprecated and will be removed in Flyway 4.0. Use flywayBaseline instead.")
    val flywayBaseline = taskKey[Unit]("Baselines an existing database, excluding all migrations up to and including baselineVersion.")
    val flywayRepair = taskKey[Unit]("Repairs the metadata table.")
  }

  //*********************
  // convenience settings
  //*********************

  private case class ConfigDataSource(driver: String, url: String, user: String, password: String) {
    def asProps: Map[String, String] =  (driver.isEmpty match {
      case true => Map()
      case false => Map("flyway.driver" -> driver)
    }) ++ Map("flyway.url" -> url, "flyway.user" -> user, "flyway.password" -> password)
  }
  private case class ConfigBase(schemas: Seq[String], table: String, initVersion: String, initDescription: String, baselineVersion: String, baselineDescription: String)
  private case class ConfigMigrationLoading(locations: Seq[String], resolvers: Seq[String], encoding: String,
                                            sqlMigrationPrefix: String, sqlMigrationSeparator: String, sqlMigrationSuffix: String,
                                            cleanOnValidationError: Boolean, target: String, outOfOrder: Boolean, callbacks: Seq[String])
  private case class ConfigMigrate(ignoreFailedFutureMigration: Boolean, placeholders: Map[String, String],
                                   placeholderPrefix: String, placeholderSuffix: String, initOnMigrate: Boolean, baselineOnMigrate: Boolean, validateOnMigrate: Boolean)
  private case class Config(dataSource: ConfigDataSource, base: ConfigBase, migrationLoading: ConfigMigrationLoading, migrate: ConfigMigrate)


  private lazy val flywayConfigDataSource = taskKey[ConfigDataSource]("The flyway data source configuration.")
  private lazy val flywayConfigBase = taskKey[ConfigBase]("The flyway base configuration.")
  private lazy val flywayConfigMigrationLoading = taskKey[ConfigMigrationLoading]("The flyway migration loading configuration.")
  private lazy val flywayConfigMigrate = taskKey[ConfigMigrate]("The flyway migrate configuration.")
  private lazy val flywayConfig = taskKey[Config]("The flyway configuration.")

  //*********************
  // flyway defaults
  //*********************

  override def projectSettings :Seq[Setting[_]] = flywayBaseSettings(Runtime) ++ inConfig(Test)(flywayBaseSettings(Test))

  def flywayBaseSettings(conf: Configuration) :Seq[Setting[_]] = {
    import org.flywaydb.sbt.FlywayPlugin.autoImport._
    val defaults = new Flyway()
    Seq[Setting[_]](
      flywayDriver := "",
      flywayUrl := "",
      flywayUser := "",
      flywayPassword := "",
      flywayLocations := defaults.getLocations.toSeq,
      flywayResolvers := Array.empty[String],
      flywaySchemas := defaults.getSchemas.toSeq,
      flywayTable := defaults.getTable,
      flywayInitVersion := defaults.getBaselineVersion.getVersion,
      flywayBaselineVersion := defaults.getBaselineVersion.getVersion,
      flywayInitDescription := defaults.getBaselineDescription,
      flywayBaselineDescription := defaults.getBaselineDescription,
      flywayEncoding := defaults.getEncoding,
      flywaySqlMigrationPrefix := defaults.getSqlMigrationPrefix,
      flywaySqlMigrationSeparator := defaults.getSqlMigrationSeparator,
      flywaySqlMigrationSuffix := defaults.getSqlMigrationSuffix,
      flywayTarget := defaults.getTarget.getVersion,
      flywayOutOfOrder := defaults.isOutOfOrder,
      flywayCallbacks := new Array[String](0),
      flywayIgnoreFailedFutureMigration := defaults.isIgnoreFailedFutureMigration,
      flywayPlaceholders := defaults.getPlaceholders.asScala.toMap,
      flywayPlaceholderPrefix := defaults.getPlaceholderPrefix,
      flywayPlaceholderSuffix := defaults.getPlaceholderSuffix,
      flywayInitOnMigrate := defaults.isBaselineOnMigrate,
      flywayBaselineOnMigrate := defaults.isBaselineOnMigrate,
      flywayValidateOnMigrate := defaults.isValidateOnMigrate,
      flywayCleanOnValidationError := defaults.isCleanOnValidationError,
      flywayConfigDataSource <<= (flywayDriver, flywayUrl, flywayUser, flywayPassword) map {
        (driver, url, user, password) => ConfigDataSource(driver, url, user, password)
      },
      flywayConfigBase <<= (flywaySchemas, flywayTable, flywayInitVersion, flywayInitDescription, flywayBaselineVersion, flywayBaselineDescription) map {
        (schemas, table, initVersion, initDescription, baselineVersion, baselineDescription) =>
          ConfigBase(schemas, table, initVersion, initDescription, baselineVersion, baselineDescription)
      },
      flywayConfigMigrationLoading <<= (flywayLocations, flywayResolvers, flywayEncoding, flywaySqlMigrationPrefix, flywaySqlMigrationSeparator, flywaySqlMigrationSuffix, flywayCleanOnValidationError, flywayTarget, flywayOutOfOrder, flywayCallbacks) map {
        (locations, resolvers, encoding, sqlMigrationPrefix, sqlMigrationSeparator, sqlMigrationSuffix, cleanOnValidationError, target, outOfOrder, callbacks) =>
          ConfigMigrationLoading(locations, resolvers, encoding, sqlMigrationPrefix, sqlMigrationSeparator, sqlMigrationSuffix, cleanOnValidationError, target, outOfOrder, callbacks)
      },
      flywayConfigMigrate <<= (flywayIgnoreFailedFutureMigration, flywayPlaceholders, flywayPlaceholderPrefix, flywayPlaceholderSuffix, flywayInitOnMigrate, flywayBaselineOnMigrate, flywayValidateOnMigrate) map {
        (ignoreFailedFutureMigration, placeholders, placeholderPrefix, placeholderSuffix, initOnMigrate, baselineOnMigrate, validateOnMigrate) =>
          ConfigMigrate(ignoreFailedFutureMigration, placeholders, placeholderPrefix, placeholderSuffix, initOnMigrate, baselineOnMigrate, validateOnMigrate)
      },
      flywayConfig <<= (flywayConfigDataSource, flywayConfigBase, flywayConfigMigrationLoading, flywayConfigMigrate) map {
        (dataSource, base, migrationLoading, migrate) => Config(dataSource, base, migrationLoading, migrate)
      },
      flywayMigrate <<= (fullClasspath in conf, flywayConfig, streams) map {
        (cp, config, s) => withPrepared(cp, s) { Flyway(config).migrate() }
      },
      flywayValidate <<= (fullClasspath in conf, flywayConfig, streams) map {
        (cp, config, s) => withPrepared(cp, s) { Flyway(config).validate() }
      },
      flywayInfo <<= (fullClasspath in conf, flywayConfig, streams) map {
        (cp, config, s) => withPrepared(cp, s) {
          val info = Flyway(config).info()
          s.log.info(MigrationInfoDumper.dumpToAsciiTable(info.all()))
          info
        }
      },
      flywayRepair <<= (fullClasspath in conf, flywayConfig, streams) map {
        (cp, config, s) => withPrepared(cp, s) { Flyway(config).repair() }
      },
      flywayClean <<= (fullClasspath in conf, flywayConfig, streams) map {
        (cp, config, s) => withPrepared(cp, s) { Flyway(config).clean() }
      },
      flywayInit <<= (fullClasspath in conf, flywayConfig, streams) map {
        (cp, config, s) => withPrepared(cp, s) { Flyway(config).init() }
      },
      flywayBaseline <<= (fullClasspath in conf, flywayConfig, streams) map {
        (cp, config, s) => withPrepared(cp, s) { Flyway(config).baseline() }
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
      flyway
      .configure(config.base)
      .configure(config.migrationLoading)
      .configure(config.migrate)
      .configureSysProps(config.dataSource)
    }
    def configure(config: ConfigBase): Flyway = {
      flyway.setSchemas(config.schemas: _*)
      flyway.setTable(config.table)
      flyway.setBaselineVersionAsString(config.initVersion)
      flyway.setBaselineVersionAsString(config.baselineVersion)
      flyway.setBaselineDescription(config.initDescription)
      flyway.setBaselineDescription(config.baselineDescription)
      flyway
    }
    def configure(config: ConfigMigrationLoading): Flyway = {
      flyway.setLocations(config.locations: _*)
      flyway.setEncoding(config.encoding)
      flyway.setSqlMigrationPrefix(config.sqlMigrationPrefix)
      flyway.setSqlMigrationSeparator(config.sqlMigrationSeparator)
      flyway.setSqlMigrationSuffix(config.sqlMigrationSuffix)
      flyway.setCleanOnValidationError(config.cleanOnValidationError)
      flyway.setTargetAsString(config.target)
      flyway.setOutOfOrder(config.outOfOrder)
      flyway.setCallbacksAsClassNames(config.callbacks: _*)
      flyway.setResolversAsClassNames(config.resolvers: _*)
      flyway
    }
    def configure(config: ConfigMigrate): Flyway = {
      flyway.setIgnoreFailedFutureMigration(config.ignoreFailedFutureMigration)
      flyway.setPlaceholders(config.placeholders)
      flyway.setPlaceholderPrefix(config.placeholderPrefix)
      flyway.setPlaceholderSuffix(config.placeholderSuffix)
      flyway.setBaselineOnMigrate(config.initOnMigrate)
      flyway.setBaselineOnMigrate(config.baselineOnMigrate)
      flyway.setValidateOnMigrate(config.validateOnMigrate)
      flyway
    }
    def configureSysProps(config: ConfigDataSource): Flyway = {
      val props = new Properties()
      System.getProperties.filter(e => e._1.startsWith("flyway")).foreach(e => props.put(e._1, e._2))
      config.asProps.filter(e => !sys.props.contains(e._1)).foreach(e => props.put(e._1, e._2))
      flyway.configure(props)
      flyway
    }
  }

  private object SbtLogCreator extends LogCreator {
    def createLogger(clazz: Class[_]) = FlywaySbtLog
  }

  private object FlywaySbtLog extends org.flywaydb.core.internal.util.logging.Log {
    var streams: Option[TaskStreams] = None
    def debug(message: String) { streams map (_.log.debug(message)) }
    def info(message: String) { streams map (_.log.info(message)) }
    def warn(message: String) { streams map (_.log.warn(message)) }
    def error(message: String) { streams map (_.log.error(message)) }
    def error(message: String, e: Exception) { streams map (_.log.error(message)); streams map (_.log.trace(e)) }
  }


}


