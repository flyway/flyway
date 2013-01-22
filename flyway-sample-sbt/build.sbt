// import com.googlecode.flyway.sbt.FlywayPlugin

organization := "com.googlecode.flyway"

name := "flyway-sample-sbt"

version := "2.0.4-SNAPSHOT"

resolvers += (
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)

libraryDependencies ++= Seq(
  "com.googlecode.flyway" % "flyway-core" % "2.0.4-SNAPSHOT",
  "org.hsqldb" % "hsqldb" % "2.2.8"
)

// addSbtPlugin("com.googlecode.flyway" % "flyway-sbt-plugin" % "2.0.4-SNAPSHOT")

// FlywayPlugin.newSettings

// newSetting := "light"