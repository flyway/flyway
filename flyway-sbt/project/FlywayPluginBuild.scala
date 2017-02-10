/**
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import sbt.Keys._
import sbt._

import scala.xml.{Source, XML}

object FlywayPluginBuild extends Build {

  val pom = XML.load(Source.fromFile(new File("../pom.xml")))
  val flywayVersion = (pom \ "version").text

  lazy val project = Project (
    "project",
    file ("."),
    settings = Defaults.coreDefaultSettings ++ Seq(
      sbtPlugin := true,
      name := "flyway-sbt",
      organization := "org.flywaydb",
      version := flywayVersion,
      resolvers += (
        "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
        ),
      libraryDependencies += "org.flywaydb" % "flyway-core" % flywayVersion
    )
  )
}
