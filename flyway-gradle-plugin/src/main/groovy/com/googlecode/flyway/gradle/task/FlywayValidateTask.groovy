/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.gradle.task

import com.googlecode.flyway.core.Flyway

/**
 * @author Ben Manes (ben.manes@gmail.com)
 */
class FlywayValidateTask extends AbstractFlywayTask {

  FlywayValidateTask() {
    description = 'Validates the applied migrations against the ones available on the classpath.'
  }

  def run(Flyway flyway) {
    flyway.validate()
  }
}
