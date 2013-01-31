sbtPlugin := true

organization := "com.googlecode.flyway"

name := "flyway-sbt-plugin"

version := "2.0.4-SNAPSHOT"

crossScalaVersions := Seq("2.9.1", "2.9.2", "2.10.0")

libraryDependencies += "com.googlecode.flyway" % "flyway-core" % "2.0.4-SNAPSHOT"

resolvers += (
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)
