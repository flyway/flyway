/**
 * Copyright 2010-2017 Boxfuse GmbH
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

import org.flywaydb.core.MongoFlyway
import org.flywaydb.core.internal.info.MigrationInfoDumper
import org.flywaydb.core.internal.util.logging.LogCreator
import org.flywaydb.core.internal.util.logging.LogFactory
import sbt.Keys._
import sbt._
import sbt.classpath._

import scala.collection.JavaConverters.mapAsScalaMapConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.collection.JavaConverters.propertiesAsScalaMapConverter

object MongoFlywayPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {

    //*********************
    // common migration settings for all tasks
    //*********************

    val flywayMongoUri = settingKey[String]("The Mongo URI to use to connect to the database.")
    val flywayTable = settingKey[String]("The name of the metadata table that will be used by Flyway.(default: schema_version)")
    val flywayBaselineVersion = settingKey[String]("The version to tag an existing schema with when executing baseline. (default: 1)")
    val flywayBaselineDescription = settingKey[String]("The description to tag an existing schema with " +
      "when executing baseline. (default: << Flyway Baseline >>)")

    //*********************
    // common settings for migration loading tasks (used by migrate, validate, info)
    //*********************

    val flywayLocations = settingKey[Seq[String]]("Locations on the classpath to scan recursively for migrations. " +
      "Locations may contain both js and code-based migrations. (default: filesystem:src/main/resources/db/migration)")
    val flywayResolvers = settingKey[Seq[String]](" The fully qualified class names of the custom MigrationResolvers " +
      "to be used in addition to the built-in ones for resolving Migrations to apply.")
    val flywaySkipDefaultResolvers = settingKey[Boolean]("Whether default built-in resolvers should be skipped. (default: false)")
    val flywayEncoding = settingKey[String]("The encoding of JavaScript migrations. (default: UTF-8)")
    val flywayMongoMigrationPrefix = settingKey[String]("The file name prefix for mongo js migrations (default: V)")
    val flywayRepeatableMongoMigrationPrefix = settingKey[String]("The file name prefix for repeatable mongo js migrations (default: R)")
    val flywayMongoMigrationSeparator = settingKey[String]("The file name separator for mongo js migrations (default: __)")
    val flywayMongoMigrationSuffix = settingKey[String]("The file name suffix for mongo js migrations (default: .js)")
    val flywayCleanOnValidationError = settingKey[Boolean]("Whether to automatically call clean or not when a validation error occurs. (default: {@code false}) " +
      "This is exclusively intended as a convenience for development. Even tough we strongly recommend not to change migration scripts once they " +
      "have been checked into SCM and run, this provides a way of dealing with this case in a smooth manner. The database will be wiped clean " +
      "automatically, ensuring that the next migration will bring you back to the state checked into SCM. Warning ! Do not enable in production !")
    val flywayCleanDisabled = settingKey[Boolean]("Whether to disable clean. This is especially useful " +
      "for production environments where running clean can be quite a career limiting move. (default: false)")
    val flywayTarget = settingKey[String]("The target version up to which Flyway should run migrations. " +
      "Migrations with a higher version number will not be  applied. (default: the latest version)")
    val flywayOutOfOrder = settingKey[Boolean]("Allows migrations to be run \"out of order\" (default: {@code false}). " +
      "If you already have versions 1 and 3 applied, and now a version 2 is found, it will be applied too instead of being ignored.")
    val flywayCallbacks = settingKey[Seq[String]]("A list of fully qualified MongoFlywayCallback implementation " +
      "classnames that will be used for Flyway lifecycle notifications. (default: Empty)")
    val flywaySkipDefaultCallbacks = settingKey[Boolean]("Whether default built-in callbacks should be skipped. (default: false)")

    //*********************
    // settings for migrate
    //*********************

    val flywayIgnoreFutureMigrations = settingKey[Boolean]("Ignores future migrations when reading the metadata table. " +
      "These are migrations that were performed by a newer deployment of the application that are not yet available in this version. " +
      "For example: we have migrations available on the classpath up to version 3.0. The metadata table indicates that a migration " +
      "to version 4.0 (unknown to us) has already been applied. Instead of bombing out (fail fast) with an exception, a warning is " +
      "logged and Flyway continues normally. This is useful for situations where one must be able to redeploy an older version of the " +
      "application after the database has been migrated by a newer one. (default: true)")
    val flywayPlaceholderReplacement = settingKey[Boolean]("Whether placeholders should be replaced. (default: true)")
    val flywayPlaceholders = settingKey[Map[String, String]]("A map of <placeholder, replacementValue> to apply to mongo js migration scripts.")
    val flywayPlaceholderPrefix = settingKey[String]("The prefix of every placeholder. (default: ${ )")
    val flywayPlaceholderSuffix = settingKey[String]("The suffix of every placeholder. (default: } )")
    val flywayBaselineOnMigrate = settingKey[Boolean]("Whether to automatically call baseline when migrate is executed against a " +
      "non-empty database with no metadata table. This database will then be baselined with the {@code baselineVersion} before " +
      "executing the migrations. Only migrations above {@code baselineVersion} will then be applied. This is useful for initial " +
      "Flyway production deployments on projects with an existing DB. Be careful when enabling this as it removes the safety " +
      "net that ensures Flyway does not migrate the wrong database in case of a configuration mistake! (default: {@code false})")
    val flywayValidateOnMigrate = settingKey[Boolean]("Whether to automatically call validate or not when running migrate. (default: {@code true})")

    //*********************
    // flyway tasks
    //*********************

    val flywayMigrate = taskKey[Unit]("Migrates of the configured database to the latest version.")
    val flywayValidate = taskKey[Unit]("Validate applied migrations against resolved ones (on the filesystem or classpath) " +
      "to detect accidental changes that may prevent the database(s) from being recreated exactly. Validation fails if " +
      "differences in migration names, types or checksums are found, versions have been applied that aren't resolved " +
      "locally anymore or versions have been resolved that haven't been applied yet")
    val flywayInfo = taskKey[Unit]("Retrieves the complete information about the migrations including applied, " +
      "pending and current migrations with details and status.")
    val flywayClean = taskKey[Unit]("Drops all database objects.")
    val flywayBaseline = taskKey[Unit]("Baselines an existing database, excluding all migrations up to and including baselineVersion.")
    val flywayRepair = taskKey[Unit]("Repairs the metadata table.")
  }

  //*********************
  // convenience settings
  //*********************

  private case class ConfigBase(uri: String, table: String, baselineVersion: String, baselineDescription: String)
  private case class ConfigMigrationLoading(locations: Seq[String], resolvers: Seq[String],
                                            skipDefaultResolvers: Boolean, encoding: String,
                                            cleanOnValidationError: Boolean, cleanDisabled: Boolean,
                                            target: String,outOfOrder: Boolean, callbacks: Seq[String],
                                            skipDefaultCallbacks: Boolean)
  private case class ConfigMongoMigration(mongoMigrationPrefix: String, repeatableMongoMigrationPrefix: String,
                                          mongoMigrationSeparator: String, mongoMigrationSuffix: String)
  private case class ConfigMigrate(ignoreFutureMigrations: Boolean, placeholderReplacement: Boolean,
                                   placeholders: Map[String, String], placeholderPrefix: String,
                                   placeholderSuffix: String, baselineOnMigrate: Boolean,
                                   validateOnMigrate: Boolean)
  private case class Config(base: ConfigBase, migrationLoading: ConfigMigrationLoading,
                            mongoMigration: ConfigMongoMigration, migrate: ConfigMigrate)


  private lazy val flywayConfigBase = taskKey[ConfigBase]("The flyway base configuration.")
  private lazy val flywayConfigMigrationLoading = taskKey[ConfigMigrationLoading]("The flyway migration loading configuration.")
  private lazy val flywayConfigMongoMigration = taskKey[ConfigMongoMigration]("The flyway mongo migration configuration.")
  private lazy val flywayConfigMigrate = taskKey[ConfigMigrate]("The flyway migrate configuration.")
  private lazy val flywayConfig = taskKey[Config]("The flyway configuration.")

  //*********************
  // flyway defaults
  //*********************

  override def projectSettings :Seq[Setting[_]] = flywayBaseSettings(Runtime) ++ inConfig(Test)(flywayBaseSettings(Test))

  def flywayBaseSettings(conf: Configuration) :Seq[Setting[_]] = {
    import autoImport._
    val defaults = new MongoFlyway()
    Seq[Setting[_]](
      flywayMongoUri := "",
      flywayLocations := List("filesystem:src/main/resources/db/migration"),
      flywayResolvers := Array.empty[String],
      flywaySkipDefaultResolvers := defaults.isSkipDefaultResolvers,
      flywayTable := defaults.getTable,
      flywayBaselineVersion := defaults.getBaselineVersion.getVersion,
      flywayBaselineDescription := defaults.getBaselineDescription,
      flywayEncoding := defaults.getEncoding,
      flywayMongoMigrationPrefix := defaults.getMongoMigrationPrefix,
      flywayRepeatableMongoMigrationPrefix := defaults.getRepeatableMongoMigrationPrefix,
      flywayMongoMigrationSeparator := defaults.getMongoMigrationSeparator,
      flywayMongoMigrationSuffix := defaults.getMongoMigrationSuffix,
      flywayTarget := defaults.getTarget.getVersion,
      flywayOutOfOrder := defaults.isOutOfOrder,
      flywayCallbacks := new Array[String](0),
      flywaySkipDefaultCallbacks := defaults.isSkipDefaultCallbacks,
      flywayIgnoreFutureMigrations := defaults.isIgnoreFutureMigrations,
      flywayPlaceholderReplacement := defaults.isPlaceholderReplacement,
      flywayPlaceholders := defaults.getPlaceholders.asScala.toMap,
      flywayPlaceholderPrefix := defaults.getPlaceholderPrefix,
      flywayPlaceholderSuffix := defaults.getPlaceholderSuffix,
      flywayBaselineOnMigrate := defaults.isBaselineOnMigrate,
      flywayValidateOnMigrate := defaults.isValidateOnMigrate,
      flywayCleanOnValidationError := defaults.isCleanOnValidationError,
      flywayCleanDisabled := defaults.isCleanDisabled,

      flywayConfigBase := ConfigBase(flywayMongoUri.value, flywayTable.value, flywayBaselineVersion.value,
        flywayBaselineDescription.value),

      flywayConfigMigrationLoading := ConfigMigrationLoading(flywayLocations.value, flywayResolvers.value,
        flywaySkipDefaultResolvers.value, flywayEncoding.value, flywayCleanOnValidationError.value,
        flywayCleanDisabled.value, flywayTarget.value, flywayOutOfOrder.value, flywayCallbacks.value,
        flywaySkipDefaultCallbacks.value),

      flywayConfigMongoMigration := ConfigMongoMigration(flywayMongoMigrationPrefix.value,
        flywayRepeatableMongoMigrationPrefix.value, flywayMongoMigrationSeparator.value,
        flywayMongoMigrationSuffix.value),

      flywayConfigMigrate := ConfigMigrate(flywayIgnoreFutureMigrations.value,
        flywayPlaceholderReplacement.value, flywayPlaceholders.value,
        flywayPlaceholderPrefix.value, flywayPlaceholderSuffix.value,
        flywayBaselineOnMigrate.value, flywayValidateOnMigrate.value),

      flywayConfig := Config(flywayConfigBase.value, flywayConfigMigrationLoading.value,
        flywayConfigMongoMigration.value, flywayConfigMigrate.value),

      flywayMigrate := withPrepared((fullClasspath in conf).value, streams.value) {
        MongoFlyway(flywayConfig.value).migrate()
      },

      flywayValidate := withPrepared((fullClasspath in conf).value, streams.value) {
        MongoFlyway(flywayConfig.value).validate()
      },

      flywayInfo := withPrepared((fullClasspath in conf).value, streams.value) {
        val info = MongoFlyway(flywayConfig.value).info()
        streams.value.log.info(MigrationInfoDumper.dumpToAsciiTable(info.all()))
        info
      },

      flywayRepair := withPrepared((fullClasspath in conf).value, streams.value) {
        MongoFlyway(flywayConfig.value).repair()
      },

      flywayClean := withPrepared((fullClasspath in conf).value, streams.value) {
        MongoFlyway(flywayConfig.value).clean()
      },

      flywayBaseline := withPrepared((fullClasspath in conf).value, streams.value) {
        MongoFlyway(flywayConfig.value).baseline()
      }
    )
  }

  private def withPrepared[T](cp: Types.Id[Keys.Classpath], streams: TaskStreams)(f: => T): T = {
    registerAsFlywayLogger(streams)
    withContextClassLoader(cp)(f)
  }

  /**
   * Registers sbt log as a static logger for Flyway
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

  private object MongoFlyway {
    def apply(config: Config): MongoFlyway = {
      val flyway = new MongoFlyway()
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

  private implicit class FlywayOps(val flyway: MongoFlyway) extends AnyVal {
    def configure(config: Config): MongoFlyway = {
      flyway
        .configure(config.base)
        .configure(config.migrationLoading)
        .configure(config.mongoMigration)
        .configure(config.migrate)
        .configureSysProps(config.base.uri)
    }

    def configure(config: ConfigBase): MongoFlyway = {
      flyway.setTable(config.table)
      flyway.setBaselineVersionAsString(config.baselineVersion)
      flyway.setBaselineDescription(config.baselineDescription)
      flyway
    }

    def configure(config: ConfigMigrationLoading): MongoFlyway = {
      flyway.setLocations(config.locations: _*)
      flyway.setEncoding(config.encoding)
      flyway.setCleanOnValidationError(config.cleanOnValidationError)
      flyway.setCleanDisabled(config.cleanDisabled)
      flyway.setTargetAsString(config.target)
      flyway.setOutOfOrder(config.outOfOrder)
      flyway.setMongoCallbacksAsClassNames(config.callbacks: _*)
      flyway.setResolversAsClassNames(config.resolvers: _*)
      flyway.setSkipDefaultResolvers(config.skipDefaultResolvers)
      flyway.setSkipDefaultCallbacks(config.skipDefaultCallbacks)
      flyway
    }

    def configure(config: ConfigMongoMigration): MongoFlyway = {
      flyway.setMongoMigrationPrefix(config.mongoMigrationPrefix)
      flyway.setRepeatableMongoMigrationPrefix(config.repeatableMongoMigrationPrefix)
      flyway.setMongoMigrationSeparator(config.mongoMigrationSeparator)
      flyway.setMongoMigrationSuffix(config.mongoMigrationSuffix)
      flyway
    }

    def configure(config: ConfigMigrate): MongoFlyway = {
      flyway.setIgnoreFutureMigrations(config.ignoreFutureMigrations)
      flyway.setPlaceholders(config.placeholders.asJava)
      flyway.setPlaceholderPrefix(config.placeholderPrefix)
      flyway.setPlaceholderSuffix(config.placeholderSuffix)
      flyway.setBaselineOnMigrate(config.baselineOnMigrate)
      flyway.setValidateOnMigrate(config.validateOnMigrate)
      flyway
    }

    def configureSysProps(uri: String): MongoFlyway = {
      val props = new Properties()
      val uriKey = "flyway.mongoUri"
      System.getProperties.asScala.filter { e => e._1.startsWith("flyway") }
        .foreach { e => props.put(e._1, e._2) }
      if (!sys.props.contains(uriKey)) props.put(uriKey, uri)
      flyway.configure(props)
      flyway
    }
  }

  private object SbtLogCreator extends LogCreator {
    def createLogger(clazz: Class[_]) = FlywaySbtLog
  }

  private object FlywaySbtLog extends org.flywaydb.core.internal.util.logging.Log {
    var streams: Option[TaskStreams] = None
    def debug(message: String) { streams.foreach(_.log.debug(message)) }
    def info(message: String) { streams.foreach(_.log.info(message)) }
    def warn(message: String) { streams.foreach(_.log.warn(message)) }
    def error(message: String) { streams.foreach(_.log.error(message)) }
    def error(message: String, e: Exception) {
      streams.foreach(_.log.error(message)); streams.foreach(_.log.trace(e))
    }
  }

}
