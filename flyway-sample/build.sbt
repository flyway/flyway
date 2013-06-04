import scala.xml._
import com.googlecode.flyway.sbt.FlywayPlugin._

organization := "com.googlecode.flyway"

name := "flyway-sample"

crossScalaVersions := Seq("2.9.1", "2.9.2", "2.9.3", "2.10.1")

libraryDependencies ++= Seq(
  "org.springframework" % "spring-jdbc" % "3.0.5.RELEASE",
  "org.hsqldb" % "hsqldb" % "2.2.8",
  "com.googlecode.flyway" % "flyway-core" % (XML.load(Source.fromFile(new File("../pom.xml"))) \ "version").text
)

resolvers += (
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)

seq(flywaySettings: _*)

flywayUrl := "jdbc:hsqldb:file:target/flyway_sample;shutdown=true"

flywayUser := "SA"

flywayPassword := ""

flywayLocations += "com.googlecode.flyway.sample.migration"





