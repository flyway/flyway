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
import play.api.test._
import play.api.test.Helpers._
import java.io.File
import scalikejdbc._
import scalikejdbc.SQLInterpolation._

class PluginSpec extends FunSpec
    with ShouldMatchers {

  def fixture = new {

  }

  def test() = {
    DB autoCommit { implicit session =>

      val people =
        sql"SELECT * FROM person"
          .map(rs => rs.int("id") -> rs.string("name"))
          .list
          .apply()

      people.size should be(4)

      sql"DROP TABLE person".execute.apply()

      // Table created by flyway
      sql"""DROP TABLE "schema_version"""".execute.apply()
    }

    NamedDB('secondary) autoCommit { implicit session =>
      val person =
        sql"SELECT * FROM job"
          .map(rs => rs.int("id") -> rs.string("name"))
          .list
          .apply()

      person.size should be(3)

      sql"DROP TABLE job".execute.apply()

      // Table created by flyway
      sql"""DROP TABLE "schema_version"""".execute.apply()
    }

  }

  describe("Plugin") {

    it("should migrate automatically when testing") {
      running(FakeApplication(path = new File("playapp"),
        additionalPlugins = Seq("scalikejdbc.PlayPlugin"))) {
        test()
      }
    }

    it("should work fine with in-memory databases.") {

      running(FakeApplication(
        path = new File("playapp"),
        additionalConfiguration =
          inMemoryDatabase(name = "default", Map("DB_CLOSE_DELAY" -> "-1")) ++
            inMemoryDatabase(name = "secondary", Map("DB_CLOSE_DELAY" -> "-1")),
        additionalPlugins = Seq("scalikejdbc.PlayPlugin"))) {
        test()
      }
    }

  }
}

