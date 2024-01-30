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