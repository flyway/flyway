import com.googlecode.flyway.sbt.FlywayPlugin._
import sbt._
import scala._

object FlywaySampleSbt extends Build {

  lazy val project = Project (
    "project",
    file ("."),
    settings = Defaults.defaultSettings ++ flywaySettings ++ Seq (
      flywayUrl := "jdbc:hsqldb:file:target/flyway_sample;shutdown=true",
      flywayUser := "SA",
      flywayPassword := "",
      flywayLocations := Some(Seq("com/googlecode/flyway/sample/migration/sbt"))
    )
  )
}
