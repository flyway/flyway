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

import play.api.test._
import play.api.test.Helpers._
import java.io.File
import org.scalatest.FunSpec
import org.scalatest.matchers._

class ConfigReaderSpec extends FunSpec with ShouldMatchers {

  describe("ConfigReader") {

    it("should get database configurations") {
      running(FakeApplication(path = new File("playapp"))) {
        val reader = new ConfigReader(play.api.Play.current)
        val configMap = reader.getDatabaseConfigurations
        configMap.get("default") should be(Some(DatabaseConfiguration("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1", "sa", null)))
        configMap.get("secondary") should be(Some(DatabaseConfiguration("jdbc:h2:mem:example2;db_CLOSE_DELAY=-1", "sa", "secret")))
        configMap.get("third") should be(Some(DatabaseConfiguration("jdbc:h2:mem:example3;DB_CLOSE_DELAY=-1", "sa", null)))
      }
    }

  }
}
