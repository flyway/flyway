import com.googlecode.flyway.sbt.FlywayPlugin._
import com.googlecode.flyway.sbt.FlywayPlugin.FlywayKeys._
import sbt._

object FlywaySampleSbt extends Build {

  lazy val project = Project (
    "project",
    file ("."),
    settings = Defaults.defaultSettings ++ flywaySettings ++ Seq (
      flywayDriver := None,
      flywayUrl := "jdbc:hsqldb:mem:sample;shutdown=true",
      flywayUser := "SA",
      flywayPassword := ""
    )
  )
}
