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

import org.scalatest.FunSpec
import org.scalatest.matchers._

class PluginConfigurationSpec extends FunSpec with ShouldMatchers {

  val config = new PluginConfiguration {}

  describe("PluginConfiguration") {

    describe("applyPath") {
      it("construct path to apply migration") {
        config.applyPath("foo") should be("/@flyway/apply/foo")
      }
      it("extract db to apply migration") {
        val dbName = "/@flyway/apply/foo/" match {
          case config.applyPath(db) => Some(db)
          case _ => None
        }
        dbName should be(Some("foo"))
      }
    }
  }
}

