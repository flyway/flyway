/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.commandline.configuration;

import lombok.CustomLog;
import lombok.NoArgsConstructor;
import org.flywaydb.commandline.Main;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.ClassUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CustomLog
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class CommandLineConfigurationUtils {

    private static final List<String> DYNAMIC_DRIVER_SUBDIRS = List.of("gcp");

    public static List<File> getJdbcDriverJarFiles() {
        final File driversDir = new File(ClassUtils.getInstallDir(Main.class), "drivers");
        if (!driversDir.isDirectory()) {
            LOG.debug("Directory for Jdbc Drivers not found: " + driversDir.getAbsolutePath());
            return Collections.emptyList();
        }

        final List<File> jarFiles = new ArrayList<>();
        collectJarFiles(driversDir, jarFiles);

        for (final String subdir : DYNAMIC_DRIVER_SUBDIRS) {
            collectJarFiles(new File(driversDir, subdir), jarFiles);
        }

        return jarFiles;
    }

    private static void collectJarFiles(final File directory, final List<File> jarFiles) {
        final File[] files = directory.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files != null) {
            jarFiles.addAll(Arrays.asList(files));
        }
    }

    public static List<File> getJavaMigrationJarFiles(final String[] dirs) {
        if (dirs.length == 0) {
            return Collections.emptyList();
        }

        final List<File> jarFiles = new ArrayList<>();
        for (final String dirName : dirs) {
            final File dir = new File(dirName);
            final File[] files = dir.listFiles((dir1, name) -> name.endsWith(".jar"));

            // see javadoc of listFiles(): null if given path is not a real directory
            if (files == null) {
                throw new FlywayException("Directory for Java Migrations not found: " + dirName);
            }

            jarFiles.addAll(Arrays.asList(files));
        }

        return jarFiles;
    }
}
