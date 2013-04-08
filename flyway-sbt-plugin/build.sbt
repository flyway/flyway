sbtPlugin := true

organization := "com.googlecode.flyway"

name := "sbt-flyway"

crossScalaVersions := Seq("2.9.1", "2.9.2", "2.10.0", "2.10.1")

resolvers += (
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)
