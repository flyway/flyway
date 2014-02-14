addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.2")

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "0-SNAPSHOT")

libraryDependencies ++= Seq(
  "org.springframework" % "spring-jdbc" % "3.0.5.RELEASE"
)

resolvers += (
    "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
)
