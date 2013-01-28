import com.googlecode.flyway.sbt.FlywayPlugin._
import com.googlecode.flyway.sbt.FlywayPlugin.FlywayKeys._
import sbt._
import scala._

object FlywaySampleSbt extends Build {

  lazy val project = Project (
    "project",
    file ("."),
    settings = Defaults.defaultSettings ++ flywaySettings ++ Seq (
      flywayUrl := "jdbc:hsqldb:mem:sample;shutdown=true",
      flywayUser := "SA",
      flywayPassword := "",
      flywayLocations := Seq("com/googlecode/flyway/sample/migration/sbt")
    )
  )
}
