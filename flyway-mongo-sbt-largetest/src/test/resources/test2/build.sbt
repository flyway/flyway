import org.flywaydb.sbt.MongoFlywayPlugin._

organization := "org.flywaydb"

name := "flyway-sbt-largetest"

libraryDependencies ++= Seq(
  "org.springframework" % "spring-jdbc" % "3.0.5.RELEASE",
  "org.flywaydb" % "flyway-core" % "0-SNAPSHOT"
)

resolvers += (
    "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
)

// Wrong port specified here. Test should override this port with the correct one
flywayMongoUri := "mongodb://localhost:27018/flyway_sample"
