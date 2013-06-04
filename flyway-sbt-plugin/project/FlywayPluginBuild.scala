/**
 * Copyright (C) 2010-2013 the original author or authors.
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

import sbt._
import scala._
import Keys._
import scala.xml.{XML, Source}

object FlywayPluginBuild extends Build {

  val pom = XML.load(Source.fromFile(new File("../pom.xml")))
  val flywayVersion = (pom \ "version").text

  lazy val project = Project (
    "project",
    file ("."),
    settings = Defaults.defaultSettings ++ Seq(
      sbtPlugin := true,
      name := "sbt-flyway",
      organization := "com.googlecode.flyway",
      version := flywayVersion,
      crossScalaVersions := Seq("2.9.1", "2.9.2", "2.9.3", "2.10.1"),
      resolvers += (
        "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
        ),
      libraryDependencies += "com.googlecode.flyway" % "flyway-core" % flywayVersion
    )
  )
}
