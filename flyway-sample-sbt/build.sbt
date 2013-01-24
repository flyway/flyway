organization := "com.googlecode.flyway"

name := "flyway-sample-sbt"

version := "2.0.4-SNAPSHOT"

resolvers += (
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)

libraryDependencies ++= Seq(
  "org.hsqldb" % "hsqldb" % "2.2.8"
)


// FlywayPlugin.newSettings

// newSetting := "light"