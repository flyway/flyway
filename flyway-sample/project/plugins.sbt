import scala.xml._

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.3.0")

addSbtPlugin("com.googlecode.flyway" % "sbt-flyway" % (XML.load(Source.fromFile(new File("../pom.xml"))) \ "version").text)

libraryDependencies ++= Seq(
  "org.springframework" % "spring-jdbc" % "3.0.5.RELEASE"
)

resolvers += (
    "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
)
