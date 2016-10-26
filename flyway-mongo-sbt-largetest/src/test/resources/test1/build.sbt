import org.flywaydb.sbt.MongoFlywayPlugin._

organization := "org.flywaydb"

name := "flyway-mongo-sbt-largetest"

libraryDependencies ++= Seq(
  "org.springframework" % "spring-jdbc" % "3.0.5.RELEASE",
  "org.flywaydb" % "flyway-core" % "0-SNAPSHOT"
)

resolvers += (
    "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
)

flywayMongoUri := "mongodb://localhost:27016/flyway_sample"

flywayLocations += "db/sbt"

flywayMongoUri in Test := "mongodb://localhost:27016/flyway_sample"
