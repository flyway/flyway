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

import play.api._

class ConfigReader(app: Application) {

  private def getAllDatabaseNames: Seq[String] = (for {
    config <- app.configuration.getConfig("db").toList
    dbName <- config.subKeys
  } yield {
    dbName
  }).distinct

  def getDatabaseConfigurations: Map[String, DatabaseConfiguration] = {
    (for {
      dbName <- getAllDatabaseNames
    } yield {
      val url = app.configuration.getString(s"db.${dbName}.url").getOrElse(
        throw new MigrationConfigurationException(s"db.${dbName}.url is not set."))
      val user = app.configuration.getString(s"db.${dbName}.user").orNull
      val password = app.configuration.getString(s"db.${dbName}.password").orNull
      dbName -> DatabaseConfiguration(url, user, password)
    }).toMap

  }

}

