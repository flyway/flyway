package com.googlecode.flyway.sample.sbt

import com.googlecode.flyway.core.Flyway
import com.googlecode.flyway.core.util.jdbc.DriverDataSource

/**
 * Simplest possible sample to demonstrate the usage of Flyway.
 */
object Main extends App {
  val dataSource = new DriverDataSource(null, "jdbc:hsqldb:file:db/flyway_sample;shutdown=true", "SA", "")
  val flyway = new Flyway()
  flyway.setDataSource(dataSource)
  flyway.setLocations("com.googlecode.flyway.sample.migration.sbt")
  flyway.migrate()

  //val jdbcTemplate = new SimpleJdbcTemplate(dataSource)
  //val results = jdbcTemplate.queryForMap("select name from test_user").asScala
  //println("Name: %s" format(results("NAME")))

}
