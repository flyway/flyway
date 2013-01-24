package com.googlecode.flyway.sbt

import sbt._
import classpath._
import Process._
import Keys._

import java.io.{File, PrintStream}
import java.text.SimpleDateFormat
import com.googlecode.flyway.core.util.jdbc.DriverDataSource
import com.googlecode.flyway.core.Flyway

object FlywayPlugin extends Plugin {

  object FlywayKeys {
    val flywayDriver= SettingKey[Option[String]]("flyway-driver", "The fully qualified classname of the jdbc driver to use to connect to the database.\nBy default, the driver is autodetected based on the url.")
    val flywayUrl = SettingKey[String]("flyway-url", "The jdbc url to use to connect to the database.")
    val flywayUser = SettingKey[String]("flyway-user", "The user to use to connect to the database. (default: blank)")
    val flywayPassword = SettingKey[String]("flyway-password", "The password to use to connect to the database. (default: blank)")
    val migrate = TaskKey[Unit]("migrate", "Migrates the database")
  }

  import FlywayKeys._

  lazy val flywaySettings :Seq[Setting[_]] = Seq[Setting[_]](
    //flywayDriver := None,
    //flywayUrl := "jdbc:hsqldb:mem:sample;shutdown=true",
    //flywayUser := "SA",
    //flywayPassword := "",
    migrate <<= (fullClasspath in Runtime, copyResources in Runtime, flywayDriver, flywayUrl, flywayUser, flywayPassword) map {
      (cp, resources, driver, url, user, password) =>
      Thread.currentThread().setContextClassLoader(ClasspathUtilities.toLoader(cp.map(_.data), getClass.getClassLoader))
      val dataSource = new DriverDataSource(if (driver.isEmpty) null else driver.get, url, user, password)
      val flyway = new Flyway()
      flyway.setDataSource(dataSource)
      flyway.setLocations("com/googlecode/flyway/sample/migration/sbt")
      flyway.migrate()
    }
  )

}