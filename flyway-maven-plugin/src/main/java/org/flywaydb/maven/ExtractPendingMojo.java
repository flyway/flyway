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
package org.flywaydb.maven;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.maven.project.MavenProject;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.internal.info.MigrationInfoDumper;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Maven goal to extract and copy pending migration to the folder specified in @{@link ExtractPendingMojo#extractPendingFolderPath }
 *
 * @goal extractpending
 */
@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
public class ExtractPendingMojo extends AbstractFlywayMojo {

    /**
     * Folder to copy pending migration.</p>
     *
     * @parameter property="flyway.extractPendingFolderPath"
     */
    protected String extractPendingFolderPath = "target/pending";


    @Override
    protected void doExecute(Flyway flyway) throws Exception {

        /*
         * Log pending migration
         */
        MigrationInfo[] pendingInfos = flyway.info().pending();
        log.info("\n" + "Pending Script => " + "\n" + MigrationInfoDumper.dumpToAsciiTable(pendingInfos));

        /*
         * Get locations without "filesystem:" prefix
         */
        //list to save migration path get from flyway.locations properties
        List<String> pathMigrationList = new ArrayList<String>();

        String tempLocationWithoutPrefix = "";
        //loop locations and save in pathMigrationList
        for (String location : flyway.getLocations()) {

            //save only filesystem locations
            if (org.apache.commons.lang3.StringUtils.contains(location, Location.FILESYSTEM_PREFIX)) {

                tempLocationWithoutPrefix = org.apache.commons.lang3.StringUtils.substringAfter(location, Location.FILESYSTEM_PREFIX);
                log.info("\n" + "locationWithoutPrefix => " + tempLocationWithoutPrefix);
                pathMigrationList.add(tempLocationWithoutPrefix);
            }
            /*
             * TODO - managing Location.CLASSPATH_PREFIX
             */
        }


        /*
         * Create destination folder path
         */
        File destinationPendingFolder = new File(extractPendingFolderPath);

        //clean destination folder path
        FileUtils.deleteDirectory(destinationPendingFolder);

        /*
         * Loop and Save Pending Migration
         */
        int numOfPendingFileCopied = 0;

        log.info("\n" + "Loop and Save Pending Migration =>");

        for (MigrationInfo pendingInfo : pendingInfos) {

            String scriptName = pendingInfo.getScript();

            for (String pathMigration : pathMigrationList) {

                File fileSrc = new File(pathMigration + File.separator + scriptName);

                if (fileSrc.exists()) {

                    //Copy pending migration in destinationPendingFolder
                    FileUtils.copyFileToDirectory(fileSrc, destinationPendingFolder);
                    log.info("\n" + "Copied pending file: " + fileSrc.getPath() + "\n in: " + destinationPendingFolder.getAbsolutePath());

                    //increment numOfPendingFileCopied
                    numOfPendingFileCopied = numOfPendingFileCopied + 1;

                    //Migration found so break loop
                    break;
                } else {
                    log.info("\n" + "fileSrc.exists()=" + fileSrc.exists() + "  - Pending file NOT exist in " + fileSrc.getPath() + " will try with next location");
                }
            }
        }

        /*
         * Check consistency
         */
        log.info("\n" + "Num of pending file copied => " +  numOfPendingFileCopied);

        if(ArrayUtils.getLength(pendingInfos) != numOfPendingFileCopied ){
            throw new FlywayException("Number of copied pending files are different from flyway pending file !!! - Note that the plugin not manage classpath migration. Check source code of extractpending goal");
        }
    }
}