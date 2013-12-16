import scala.xml._
import com.googlecode.flyway.sbt.FlywayPlugin._

organization := "com.googlecode.flyway"

name := "flyway-sbt-largetest"

libraryDependencies ++= Seq(
  "org.springframework" % "spring-jdbc" % "3.0.5.RELEASE",
  "org.hsqldb" % "hsqldb" % "2.2.8",
  "com.googlecode.flyway" % "flyway-core" % "0-SNAPSHOT"
)

resolvers += (
    "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
)

seq(flywaySettings: _*)

flywayUrl := "jdbc:hsqldb:file:target/flyway_sample;shutdown=true"

flywayUser := "SA"

flywayLocations += "db/sbt"






