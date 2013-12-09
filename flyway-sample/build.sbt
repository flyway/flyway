import scala.xml._
import com.googlecode.flyway.sbt.FlywayPlugin._

organization := "com.googlecode.flyway"

name := "flyway-sample"

libraryDependencies ++= Seq(
  "org.springframework" % "spring-jdbc" % "3.0.5.RELEASE",
  "org.hsqldb" % "hsqldb" % "2.2.8",
  "com.googlecode.flyway" % "flyway-core" % (XML.load(Source.fromFile(new File("../pom.xml"))) \ "version").text
)

resolvers += (
    "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
)

seq(flywaySettings: _*)

flywayUrl := "jdbc:hsqldb:file:target/flyway_sample;shutdown=true"

flywayUser := Some("SA")

flywayPassword := Some("")

flywayLocations += "com.googlecode.flyway.sample.migration"





