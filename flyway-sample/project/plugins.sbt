import scala.xml._

addSbtPlugin("org.flywaydb" % "flyway-sbt" % (XML.load(Source.fromFile(new File("../pom.xml"))) \ "version").text)

libraryDependencies ++= Seq(
  "org.springframework" % "spring-jdbc" % "3.0.5.RELEASE"
)

resolvers += (
    "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
)
