/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.gradle.task;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.info.MigrationInfoDumper;

public class FlywayInfoTask extends AbstractFlywayTask {
    public FlywayInfoTask() {
        super();
        setDescription("Prints the details and status information about all the migrations.");
    }

    @Override
    protected Object run(Flyway flyway) {
        MigrationInfoService info = flyway.info();
        MigrationInfo current = info.current();
        MigrationVersion currentSchemaVersion = current == null ? MigrationVersion.EMPTY : current.getVersion();
        System.out.println("Schema version: " + currentSchemaVersion);
        System.out.println(MigrationInfoDumper.dumpToAsciiTable(info.all()));
        return null;
    }
}