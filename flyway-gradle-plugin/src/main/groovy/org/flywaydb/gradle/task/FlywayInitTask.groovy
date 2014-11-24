/**
 * Copyright 2010-2014 Axel Fontaine
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
package org.flywaydb.gradle.task

import org.flywaydb.core.Flyway
import org.flywaydb.core.internal.util.logging.Log
import org.flywaydb.core.internal.util.logging.LogFactory

@Deprecated
class FlywayInitTask extends AbstractFlywayTask {
  private static final Log LOG = LogFactory.getLog(FlywayInitTask)

  FlywayInitTask() {
    description = 'Baselines an existing database, excluding all migrations up to and including baselineVersion.'
  }

  def run(Flyway flyway) {
    LOG.warn("flywayInit is deprecated and will be removed in Flyway 4.0. Use flywayBaseline instead.")
    flyway.baseline()
  }
}
