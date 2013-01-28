package com.googlecode.flyway.sbt

import sbt._
import classpath._
import Process._
import Keys._

import java.io.{File, PrintStream}
import java.text.SimpleDateFormat
import com.googlecode.flyway.core.util.jdbc.DriverDataSource
import com.googlecode.flyway.core.Flyway
import javax.sql.DataSource

object FlywayPlugin extends Plugin {

  val flywayDriver = SettingKey[Option[String]]("flyway-driver", "The fully qualified classname of the jdbc driver to use to connect to the database.\nBy default, the driver is autodetected based on the url.")
  val flywayUrl = SettingKey[String]("flyway-url", "The jdbc url to use to connect to the database.")
  val flywayUser = SettingKey[String]("flyway-user", "The user to use to connect to the database. (default: blank)")
  val flywayPassword = SettingKey[String]("flyway-password", "The password to use to connect to the database. (default: blank)")
  val flywayLocations = SettingKey[Seq[String]]("flyway-locations", "Locations on the classpath to scan recursively for migrations. Locations may contain both sql and code-based migrations. (default: db/migration)")

  lazy val flywayUpdateCl = TaskKey[Unit]("flyway-update-cl", "Update context classloader to runtime classpath")
  lazy val flywayDataSource = TaskKey[DataSource]("flyway-datasource", "the flyway datasource")
  lazy val flyway = TaskKey[Flyway]("flyway", "flyway object")

  val flywayMigrate = TaskKey[Unit]("flyway-migrate", "Migrates the database")

  lazy val flywaySettings :Seq[Setting[_]] = Seq[Setting[_]](
    flywayDriver := None,
    flywayLocations := Seq("db/migration"),
    flywayUpdateCl <<= (fullClasspath in Runtime, copyResources in Runtime) map { (cp, r) =>
      Thread.currentThread().setContextClassLoader(ClasspathUtilities.toLoader(cp.map(_.data), getClass.getClassLoader))
    },
    flywayDataSource <<= (flywayUpdateCl, flywayDriver, flywayUrl, flywayUser, flywayPassword) map {
      (cl, driver, url, user, password) =>
        new DriverDataSource(if (driver.isEmpty) null else driver.get, url, user, password)
      },
    flyway <<= (flywayDataSource, flywayLocations) map {
      (dataSource, locations) =>
        val flyway = new Flyway()
        flyway.setDataSource(dataSource)
        flyway.setLocations(locations:_*)
        flyway
    },
    flywayMigrate <<= (flywayUpdateCl, flyway, streams) map {
      (cl, flyway, s) =>
        flyway.migrate()
    }
  )

}