sbtPlugin := true

organization := "com.googlecode.flyway"

name := "flyway-sbt-plugin"

version := "2.0.4-SNAPSHOT"

crossScalaVersions := Seq("2.9.1", "2.9.2")

resolvers += (
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)

publishMavenStyle := true

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

libraryDependencies += "com.googlecode.flyway" % "flyway-core" % "2.0.4-SNAPSHOT"

