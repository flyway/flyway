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

import com.googlecode.flyway.core.metadatatable.MetaDataTableRow;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Dumps migrations in an ascii-art table in the logs and the console.
 */
public class MetaDataTableRowDumper {
    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(MetaDataTableRowDumper.class);

    /**
     * Prevent instantiation.
     */
    private MetaDataTableRowDumper() {
        // Do nothing
    }

    /**
     * Dumps this metaDataTableRow in the log file.
     *
     * @param metaDataTableRow The metaDataTableRow to dump.
     */
    public static void dumpMigration(MetaDataTableRow metaDataTableRow) {
        List<MetaDataTableRow> metaDataTableRowList = new ArrayList<MetaDataTableRow>();
        if (metaDataTableRow != null) {
            metaDataTableRowList.add(metaDataTableRow);
        }
        dumpMigrations(metaDataTableRowList);
    }

    /**
     * Dumps this list of metaDataTableRows in the log file.
     *
     * @param metaDataTableRows The list of metaDataTableRows to dump.
     */
    public static void dumpMigrations(List<MetaDataTableRow> metaDataTableRows) {
        LOG.info("+-------------+------------------------+---------------------+---------+");
        LOG.info("| Version     | Description            | Installed on        | State   |");
        LOG.info("+-------------+------------------------+---------------------+---------+");

        if (metaDataTableRows.isEmpty()) {
            LOG.info("| No migrations applied yet                                            |");
        } else {
            for (MetaDataTableRow metaDataTableRow : metaDataTableRows) {
                LOG.info("| " + StringUtils.trimOrPad(metaDataTableRow.getVersion().toString(), 11)
                        + " | " + StringUtils.trimOrPad(metaDataTableRow.getDescription(), 22)
                        + " | " + StringUtils.trimOrPad(formatInstalledOnDate(metaDataTableRow.getInstalledOn()), 19)
                        + " | " + StringUtils.trimOrPad(metaDataTableRow.getState().name(), 7) + " |");
            }
        }

        LOG.info("+-------------+------------------------+---------------------+---------+");
    }

    /**
     * Formats the installedOn date for displaying it in the table.
     *
     * @param installedOn The date to format.
     * @return The date in a displayable format.
     */
    private static String formatInstalledOnDate(Date installedOn) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(installedOn);
    }
}
