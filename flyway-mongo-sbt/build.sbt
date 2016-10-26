sbtPlugin := true

name := "flyway-mongo-sbt"

organization := "org.flywaydb"

version := "0-SNAPSHOT"

resolvers ++= Seq(
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "org.flywaydb" % "flyway-core" % "0-SNAPSHOT",
  "org.mongodb" % "mongodb-driver" % "3.3.0"
)
