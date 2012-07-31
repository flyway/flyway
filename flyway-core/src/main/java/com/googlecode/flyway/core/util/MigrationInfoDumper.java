/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.util;

import com.googlecode.flyway.core.api.MigrationInfo;
import com.googlecode.flyway.core.util.logging.Log;
import com.googlecode.flyway.core.util.logging.LogFactory;

/**
 * Dumps migrations in an ascii-art table in the logs and the console.
 */
public class MigrationInfoDumper {
    private static final Log LOG = LogFactory.getLog(MigrationInfoDumper.class);

    /**
     * Prevent instantiation.
     */
    private MigrationInfoDumper() {
        // Do nothing
    }

    /**
     * Dumps this list of migrationInfos in the log file.
     *
     * @param migrationInfos The list of migrationInfos to dump.
     */
    public static void dumpMigrations(MigrationInfo[] migrationInfos) {
        LOG.info("+-------------+------------------------+---------------------+---------+");
        LOG.info("| Version     | Description            | Installed on        | State   |");
        LOG.info("+-------------+------------------------+---------------------+---------+");

        if (migrationInfos.length == 0) {
            LOG.info("| No migrations applied and no migrations pending                      |");
        } else {
            for (MigrationInfo migrationInfo : migrationInfos) {
                LOG.info("| " + StringUtils.trimOrPad(migrationInfo.getVersion().toString(), 11)
                        + " | " + StringUtils.trimOrPad(migrationInfo.getDescription(), 22)
                        + " | " + StringUtils.trimOrPad(DateUtils.formatDateAsIsoString(migrationInfo.getInstalledOn()), 19)
                        + " | " + StringUtils.trimOrPad(migrationInfo.getState().getDisplayName(), 7) + " |");
            }
        }

        LOG.info("+-------------+------------------------+---------------------+---------+");
    }
}
