/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.ant;

import org.flywaydb.core.Flyway;

/**
 * Ant task that triggers the migration of the configured database to the latest version.
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public class MigrateTask extends AbstractFlywayTask {
    @Override
    protected void doExecute(Flyway flyway) throws Exception {
        if (flyway.info().all().length == 0) {
            log.warn("Possible solution: run the Ant javac and copy tasks first so Flyway can find the migrations");
            return;
        }

        flyway.migrate();
    }
}
