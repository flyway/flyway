/**
 * Copyright (C) 2009-2010 the original author or authors.
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

import com.googlecode.flyway.core.migration.Migration;
import com.googlecode.flyway.core.util.StringUtils;
import com.googlecode.flyway.core.util.TimeFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Dumps migrations in an ascii-art table in the logs and the Maven console.
 */
public class MigrationDumper {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(MigrationDumper.class);

    /**
     * Prevent instantiation.
     */
    private MigrationDumper() {
        // Do nothing
    }

    /**
     * Dumps this list of migrations in the log file.
     *
     * @param migrations The list of migrations to dump.
     */
    public static void dumpMigrations(List<Migration> migrations) {
        LOG.info("+-------------+---------------------------+----------------+-----------+");
        LOG.info("| Version     | Description               | Execution time | State     |");
        LOG.info("+-------------+---------------------------+----------------+-----------+");

        if (migrations.isEmpty()) {
            LOG.info("| No migrations applied yet                                            |");
        } else {
            for (Migration migration : migrations) {
                LOG.info("| " + StringUtils.trimOrPad(migration.getVersion().getVersion(), 11)
                        + " | " + StringUtils.trimOrPad(migration.getVersion().getDescription(), 25)
                        + " | " + StringUtils.trimOrPad(TimeFormat.format(migration.getExecutionTime()), 14)
                        + " | " + StringUtils.trimOrPad(migration.getState().name(), 9) + " |");
            }
        }

        LOG.info("+-------------+---------------------------+----------------+-----------+");
    }
}
