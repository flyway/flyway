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
package com.googlecode.flyway.gradle

/**
 * The container that hold the defaults and configuration properties for Flyway.
 *
 * @author Allan Morstein (alkamo@gmail.com)
 */
public class FlywayContainer {

  /** The dependencies that all flyway tasks depend on. */
  List<Object> dependsOnTasks = []

  /**
   * Indicates the order to concatenate the schemas:
   * <ul>
   *   <li>true: schemas from the default values will be appended first
   *   <li>false: the database-specific schemas will be appended first
   * </ul>
   */
  Boolean schemaDefaultFirst = true

  /** @see http://www.gradle.org/docs/current/javadoc/org/gradle/api/Task.html#dependencies */
  def dependsOnTasks(Object... paths) {
    dependsOnTasks += paths
  }

  def getDependsOnTasks() {
    dependsOnTasks
  }
}
