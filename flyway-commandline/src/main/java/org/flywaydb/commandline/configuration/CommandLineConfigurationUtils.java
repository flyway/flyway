/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.commandline.configuration;

import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.flywaydb.commandline.Main;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CustomLog
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class CommandLineConfigurationUtils {

    public static List<File> getTomlConfigFilePaths() {
        String[] fileLocations = StringUtils.tokenizeToStringArray(System.getenv("FLYWAY_CONFIG_FILES"), ",");

        return fileLocations == null ? new ArrayList<>() : Arrays.stream(fileLocations)
                                                                 .filter(f -> f.endsWith(".toml"))
                                                                 .map(File::new)
                                                                 .collect(Collectors.toList());
    }

    public static List<File> getJdbcDriverJarFiles() {
        File driversDir = new File(ClassUtils.getInstallDir(Main.class), "drivers");
        File[] files = driversDir.listFiles((dir, name) -> name.endsWith(".jar"));

        // see javadoc of listFiles(): null if given path is not a real directory
        if (files == null) {
            LOG.debug("Directory for Jdbc Drivers not found: " + driversDir.getAbsolutePath());
            return Collections.emptyList();
        }

        return Arrays.asList(files);
    }

    public static List<File> getJavaMigrationJarFiles(String[] dirs) {
        if (dirs.length == 0) {
            return Collections.emptyList();
        }

        List<File> jarFiles = new ArrayList<>();
        for (String dirName : dirs) {
            File dir = new File(dirName);
            File[] files = dir.listFiles((dir1, name) -> name.endsWith(".jar"));

            // see javadoc of listFiles(): null if given path is not a real directory
            if (files == null) {
                throw new FlywayException("Directory for Java Migrations not found: " + dirName);
            }

            jarFiles.addAll(Arrays.asList(files));
        }

        return jarFiles;
    }
}