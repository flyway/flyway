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
import java.io.File

class FileUtilsSpec extends FunSpec with ShouldMatchers {

  val fileutils = new FileUtils {}

  describe("FileUtils") {

    it("should join paths and get File") {
      val f = new File("/a")
      val f1 = "b"
      val f2 = "c"
      fileutils.getFile(f, f1, f2).getAbsolutePath should be("/a/b/c")
    }

    it("should read File") {
      val f = new File("plugin/src/test/resources/sample.sql")
      val s = fileutils.readFileToString(f)
      s should be("""|create table person (
                     |    id int not null,
                     |    name varchar(100) not null
                     |);
                     |""".stripMargin)
    }

  }

}
