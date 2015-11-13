import scala.xml._
import org.flywaydb.sbt.FlywayPlugin._

organization := "org.flywaydb"

name := "flyway-sample"

libraryDependencies ++= Seq(
  "org.springframework" % "spring-jdbc" % "3.0.5.RELEASE",
  "org.hsqldb" % "hsqldb" % "2.2.8",
  "org.flywaydb" % "flyway-core" % (XML.load(Source.fromFile(new File("../pom.xml"))) \ "version").text
)

resolvers += (
    "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
)

flywayUrl := "jdbc:hsqldb:file:target/flyway_sample;shutdown=true"

flywayUser := "SA"

flywayLocations += "org.flywaydb.sample.migration"






