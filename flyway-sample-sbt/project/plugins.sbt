addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0")

resolvers += (
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)

libraryDependencies ++= Seq(
  "com.googlecode.flyway" % "flyway-core" % "2.0.4-SNAPSHOT",
  "org.springframework" % "spring-jdbc" % "3.0.5.RELEASE",
  "org.hsqldb" % "hsqldb" % "2.2.8"
)