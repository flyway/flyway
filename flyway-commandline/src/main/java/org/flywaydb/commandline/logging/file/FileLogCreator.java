/*-
 * ========================LICENSE_START=================================
 * flyway-commandline
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.commandline.logging.file;

import org.flywaydb.commandline.configuration.CommandLineArguments;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.flywaydb.commandline.logging.console.ConsoleLog.Level;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileLogCreator implements LogCreator {
    private final Level level;
    private final Path path;

    public FileLogCreator(CommandLineArguments commandLineArguments) {
        String outputFilepath = "";

        if (commandLineArguments.isOutputFileSet()) {
            outputFilepath = commandLineArguments.getOutputFile();
        }

        this.level = commandLineArguments.getLogLevel();
        this.path = Paths.get(outputFilepath);

        prepareOutputFile(path);
    }

    public Log createLogger(Class<?> clazz) {
        return new FileLog(path, level);
    }

    private static void prepareOutputFile(Path path) {
        try {
            Files.write(path, "".getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException exception) {
            throw new FlywayException("Could not initialize log file at " + path + ".", exception);
        }
    }
}
