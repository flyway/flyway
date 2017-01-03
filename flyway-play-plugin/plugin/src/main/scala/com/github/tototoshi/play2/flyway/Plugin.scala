/*
 * Copyright 2013 Toshiyuki Takahashi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.flyawy.play

import java.io.File
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import com.googlecode.flyway.core.Flyway
import com.googlecode.flyway.core.api.MigrationInfo
import play.core._

class Plugin(implicit app: Application) extends play.api.Plugin
    with HandleWebCommandSupport
    with PluginConfiguration
    with FileUtils {

  private val configReader = new ConfigReader(app)

  private val databaseConfigurations = configReader.getDatabaseConfigurations

  private val allDatabaseNames = configReader.getDatabaseConfigurations.keys

  private val flywayPrefixToMigrationScript = "db/migration"

  private val playConfigDir = "conf"

  private val flyways: Map[String, Flyway] = {
    for {
      (dbName, configuration) <- configReader.getDatabaseConfigurations
      migrationFilesLocation = s"db/migration/${dbName}"
      if app.getFile(playConfigDir + "/" + migrationFilesLocation).exists
    } yield {
      val flyway = new Flyway
      flyway.setDataSource(configuration.url, configuration.user, configuration.password)
      flyway.setLocations(migrationFilesLocation)
      dbName -> flyway
    }
  }

  override lazy val enabled: Boolean = true

  private def migrationDescriptionToShow(dbName: String, migration: MigrationInfo): String = {
    val scriptPath = getFile(app.path, playConfigDir, flywayPrefixToMigrationScript, dbName, migration.getScript)
    s"""|--- ${migration.getScript} ---
        |${readFileToString(scriptPath)}""".stripMargin
  }

  private def checkState(dbName: String): Unit = {
    flyways.get(dbName).foreach { flyway =>
      val pendingMigrations = flyway.info().pending
      if (!pendingMigrations.isEmpty) {
        throw InvalidDatabaseRevision(
          dbName,
          pendingMigrations.map(migration => migrationDescriptionToShow(dbName, migration)).mkString("\n"))
      }
    }
  }

  override def onStart(): Unit = {
    for (dbName <- allDatabaseNames) {
      if (Play.isTest || app.configuration.getBoolean(s"db.${dbName}.migration.auto").getOrElse(false)) {
        migrateAutomatically(dbName)
      } else {
        checkState(dbName)
      }
    }
  }

  override def onStop(): Unit = {
  }

  private def migrateAutomatically(dbName: String): Unit = {
    flyways.get(dbName).foreach { flyway =>
      flyway.migrate()
    }
  }

  private def getRedirectUrlFromRequest(request: RequestHeader): String = {
    (for {
      urls <- request.queryString.get("redirect")
      url <- urls.headOption
    } yield url).getOrElse("/")
  }

  override def handleWebCommand(request: RequestHeader, sbtLink: SBTLink, path: java.io.File): Option[Result] = {
    request.path match {
      case applyPath(dbName) => {
        for {
          flyway <- flyways.get(dbName)
        } yield {
          flyway.migrate()
          sbtLink.forceReload()
          Redirect(getRedirectUrlFromRequest(request))
        }
      }
      case _ => {
        None
      }
    }
  }

}
