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

trait PluginConfiguration {
  private val applyPathRoot = "/@flyway/apply"
  private val applyPathRegex = s"""${applyPathRoot}/([a-zA-Z0-9_]+)/""".r

  object applyPath {

    def apply(dbName: String): String = {
      applyPathRoot + "/" + dbName
    }

    def unapply(path: String): Option[String] = {
      applyPathRegex.findFirstMatchIn(path).map(_.group(1))
    }

  }

}
