sbtPlugin := true

organization := "com.googlecode.flyway"

name := "flyway-sbt-plugin"

version := "2.0.4-SNAPSHOT"

libraryDependencies += "com.googlecode.flyway" % "flyway-core" % "2.0.4-SNAPSHOT"

resolvers += (
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)
