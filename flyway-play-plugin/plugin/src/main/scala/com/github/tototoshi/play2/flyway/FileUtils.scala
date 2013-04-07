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

trait FileUtils {

  def getFile(directory: File, names: String*): File = {
    names.foldLeft(directory) { (result, name) =>
      new File(result, name);
    }
  }

  def readFileToString(file: File): String = {
    val src = scala.io.Source.fromFile(file)
    try {
      src.mkString
    } finally {
      src.close()
    }
  }

}

