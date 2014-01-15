/**
 * Copyright 2010-2014 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.maven;

import com.googlecode.flyway.core.Flyway;
import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.info.MigrationInfoDumper;

/**
 * Maven goal that shows the status (current version) of the database.
 *
 * @goal status
 * @since 0.8
 * @deprecated Use flyway:info instead. Will be removed in Flyway 3.0.
 */
@Deprecated
public class StatusMojo extends AbstractFlywayMojo {
    @Override
    protected void doExecute(Flyway flyway) throws Exception {
        log.warn("flyway:status is deprecated. Use flyway:info instead.");
        MigrationInfo current = flyway.info().current();

        if (current == null) {
            log.info("\n" + MigrationInfoDumper.dumpToAsciiTable(new MigrationInfo[0]));
        } else {
            log.info("\n" + MigrationInfoDumper.dumpToAsciiTable(new MigrationInfo[]{current}));
        }
    }
}