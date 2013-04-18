import scala.xml._

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.3.0")

addSbtPlugin("com.googlecode.flyway" % "sbt-flyway" % (XML.load(Source.fromFile(new File("../pom.xml"))) \ "version").text)

resolvers += (
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)
