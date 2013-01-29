package com.googlecode.flyway.sbt

import sbt._
import classpath._
import Keys._

import com.googlecode.flyway.core.util.jdbc.DriverDataSource
import com.googlecode.flyway.core.Flyway
import javax.sql.DataSource
import com.googlecode.flyway.core.info.MigrationInfoDumper
import scala.collection.JavaConversions._

object FlywayPlugin extends Plugin {

  // val flyway = SettingKey[Option[String]]("flyway-", "")

  //*********************
  // common migration settings for all tasks
  //*********************

  val flywayDriver = SettingKey[Option[String]]("flyway-driver", "The fully qualified classname of the jdbc driver to use to connect to the database.\nBy default, the driver is autodetected based on the url.")
  val flywayUrl = SettingKey[String]("flyway-url", "The jdbc url to use to connect to the database.")
  val flywayUser = SettingKey[String]("flyway-user", "The user to use to connect to the database. (default: blank)")
  val flywayPassword = SettingKey[String]("flyway-password", "The password to use to connect to the database. (default: blank)")

  val flywaySchemas = SettingKey[Option[Seq[String]]]("flyway-schemas", "List of the schemas managed by Flyway. The first schema in the list will be automatically set as the default one during\nthe migration. It will also be the one containing the metadata table. These schema names are case-sensitive.\n(default: The default schema for the datasource connection)")
  val flywayTable = SettingKey[Option[String]]("flyway-table", "The name of the metadata table that will be used by Flyway. (default: schema_version)\nBy default (single-schema mode) the\nmetadata table is placed in the default schema for the connection provided by the datasource. When the\nflyway.schemas property is set (multi-schema mode), the metadata table is placed in the first schema of\nthe list.")
  val flywayInitVersion = SettingKey[Option[String]]("flyway-init-version", "The version to tag an existing schema with when executing init. (default: 1)")
  val flywayInitDescription = SettingKey[Option[String]]("flyway-init-description", "The description to tag an existing schema with when executing init. (default: << Flyway Init >>)")

  //*********************
  // common settings for migration loading tasks (used by migrate, validate, info)
  //*********************

  val flywayLocations = SettingKey[Option[Seq[String]]]("flyway-locations", "Locations on the classpath to scan recursively for migrations. Locations may contain both sql and code-based migrations. (default: db/migration)")
  val flywayEncoding = SettingKey[Option[String]]("flyway-encoding", "The encoding of Sql migrations. (default: UTF-8)")
  val flywaySqlMigrationPrefix = SettingKey[Option[String]]("flyway-sqlMigrationPrefix", "The file name prefix for Sql migrations (default: V) ")
  val flywaySqlMigrationSuffix = SettingKey[Option[String]]("flyway-sqlMigrationSuffix", "The file name suffix for Sql migrations (default: .sql)")
  val flywayCleanOnValidationError = SettingKey[Option[Boolean]]("flyway-cleanOnValidationError", "Whether to automatically call clean or not when a validation error occurs. (default: {@code false})<br/>\nThis is exclusively intended as a convenience for development. Even tough we strongly recommend not to change migration scripts once they have been checked into SCM and run, this provides a way of dealing with this case in a smooth manner. The database will be wiped clean automatically, ensuring that the next migration will bring you back to the state checked into SCM.\nWarning ! Do not enable in production !")
  val flywayTarget = SettingKey[Option[String]]("flyway-target", "The target version up to which Flyway should run migrations. Migrations with a higher version number will not be  applied. (default: the latest version)")
  val flywayOutOfOrder = SettingKey[Option[String]]("flyway-outOfOrder", "Allows migrations to be run \"out of order\" (default: {@code false}).\nIf you already have versions 1 and 3 applied, and now a version 2 is found,\nit will be applied too instead of being ignored.")

  //*********************
  // settings for migrate
  //*********************

  val flywayIgnoreFailedFutureMigration = SettingKey[Option[Boolean]]("flyway-ignore-failed-future-migration", "Ignores failed future migrations when reading the metadata table. These are migrations that we performed by a\n   * newer deployment of the application that are not yet available in this version. For example: we have migrations\n   * available on the classpath up to version 3.0. The metadata table indicates that a migration to version 4.0\n   * (unknown to us) has already been attempted and failed. Instead of bombing out (fail fast) with an exception, a\n   * warning is logged and Flyway terminates normally. This is useful for situations where a database rollback is not\n   * an option. An older version of the application can then be redeployed, even though a newer one failed due to a\n   * bad migration. (default: false)")
  val flywayPlaceholders = SettingKey[Map[String, String]]("flyway-placeholders", "A map of <placeholder, replacementValue> to apply to sql migration scripts.")
  val flywayPlaceholderPrefix = SettingKey[Option[String]]("flyway-placeholder-prefix", "The prefix of every placeholder. (default: ${ )")
  val flywayPlaceholderSuffix = SettingKey[Option[String]]("flyway-placeholder-suffix", "The suffix of every placeholder. (default: } )")
  val flywayInitOnMigrate = SettingKey[Option[Boolean]]("flyway-init-on-migrate", "Whether to automatically call init when migrate is executed against a non-empty schema with no metadata table.\n   * This schema will then be initialized with the {@code initialVersion} before executing the migrations.\n   * Only migrations above {@code initialVersion} will then be applied.\n   * </p>\n   * <p>\n   * This is useful for initial Flyway production deployments on projects with an existing DB.\n   * </p>\n   * <p>\n   * Be careful when enabling this as it removes the safety net that ensures\n   * Flyway does not migrate the wrong database in case of a configuration mistake! (default: {@code false})")
  val flywayValidateOnMigrate = SettingKey[Option[Boolean]]("flyway-validate-on-migrate", "Whether to automatically call validate or not when running migrate. (default: {@code false})")

  //*********************
  // convenience settings
  //*********************

  lazy val flywayUpdateCl = TaskKey[Unit]("flyway-update-cl", "Update context classloader to runtime classpath")
  lazy val flywayDataSource = TaskKey[DataSource]("flyway-datasource", "the flyway datasource")
  lazy val flyway = TaskKey[Flyway]("flyway", "flyway object")

  //*********************
  // flyway tasks
  //*********************

  val flywayMigrate = TaskKey[Unit]("flyway-migrate", "Migrates of the configured database to the latest version.")
  val flywayValidate = TaskKey[Unit]("flyway-validate", "Validates the applied migrations in the database against the available classpath migrations in order to\ndetect accidental migration changes.")
  val flywayInfo = TaskKey[Unit]("flyway-info", "retrieve the complete information about the migrations including applied, pending and current migrations with\ndetails and status.")
  val flywayClean = TaskKey[Unit]("flyway-clean", "Drops all database objects.")

  //*********************
  // flyway defaults
  //*********************

  lazy val flywaySettings :Seq[Setting[_]] = Seq[Setting[_]](
    flywayDriver := None,
    flywayLocations := None,
    flywaySchemas := None,
    flywayTable := None,
    flywayInitVersion := None,
    flywayInitDescription := None,
    flywayLocations := None,
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
    flywayUpdateCl <<= (fullClasspath in Runtime, copyResources in Runtime) map { (cp, r) =>
      Thread.currentThread().setContextClassLoader(ClasspathUtilities.toLoader(cp.map(_.data), getClass.getClassLoader))
    },
    flywayDataSource <<= (flywayUpdateCl, flywayDriver, flywayUrl, flywayUser, flywayPassword) map {
      (cl, driver, url, user, password) =>
        new DriverDataSource(if (driver.isEmpty) null else driver.get, url, user, password)
      },
    flyway <<= (flywayDataSource, flywaySchemas, flywayTable, flywayInitVersion, flywayInitDescription,
      flywayLocations, flywayEncoding, flywaySqlMigrationPrefix, flywaySqlMigrationSuffix, flywayCleanOnValidationError, flywayTarget, flywayOutOfOrder) map {
      (dataSource, schemas, table, initVersion, initDescription,
        locations, encoding, sqlMigrationPrefix, sqlMigrationSuffix, cleanOnValidationError, target, outOfOrder) =>
      val flyway = new Flyway()
        flyway.setDataSource(dataSource)
        schemas map (flyway.setSchemas(_:_*))
        table map  (flyway.setTable(_))
        initVersion map (flyway.setInitVersion(_))
        initDescription map (flyway.setInitDescription(_))

        locations map (flyway.setLocations(_:_*))
        encoding map (flyway.setEncoding(_))
        sqlMigrationPrefix map (flyway.setSqlMigrationPrefix(_))
        sqlMigrationSuffix map (flyway.setSqlMigrationSuffix(_))
        cleanOnValidationError map (flyway.setCleanOnValidationError(_))
        target map (flyway.setTarget(_))

        flyway
    },
    flywayMigrate <<= (flyway, flywayIgnoreFailedFutureMigration, flywayPlaceholders, flywayPlaceholderPrefix, flywayPlaceholderSuffix, flywayInitOnMigrate, flywayValidateOnMigrate) map {
      (flyway, ignoreFailedFutureMigration, placeholders, placeholderPrefix, placeholderSuffix, initOnMigrate, validateOnMigrate) =>
        ignoreFailedFutureMigration map (flyway.setIgnoreFailedFutureMigration(_))
        flyway.setPlaceholders(placeholders)
        placeholderPrefix map (flyway.setPlaceholderPrefix(_))
        placeholderSuffix map (flyway.setPlaceholderSuffix(_))
        initOnMigrate map (flyway.setInitOnMigrate(_))
        validateOnMigrate map (flyway.setValidateOnMigrate(_))
        flyway.migrate()
    },
    flywayValidate <<= flyway map { _.validate() },
    flywayInfo <<= (flyway, streams) map { (flyway, s) => s.log.info(MigrationInfoDumper.dumpToAsciiTable(flyway.info().all())) },
    flywayClean <<= flyway map { _.clean() }
  )

}