/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.commandline;

import org.flywaydb.commandline.PrintStreamLog.Level;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * Log Creator for logging to a file
 */
class FileLogCreator implements LogCreator {
    private final Level level;
    private final String filePath;

    /**
     * Creates a new file Log Creator.
     *
     * @param level The minimum level to log at.
     * @param filePath File to write logs into
     */
    public FileLogCreator(Level level, String filePath) {
        this.level = level;
        this.filePath = filePath;
    }

    public Log createLogger(Class<?> clazz) {
        PrintStream filePrintStream = getFilePrintStream();
        return new PrintStreamLog(level, filePrintStream, filePrintStream);
    }

    private PrintStream getFilePrintStream() {
        try {
            return new PrintStream(this.filePath);
        } catch (FileNotFoundException e) {
            throw new FlywayException("Could not open file " + filePath + " for logging.");
        }
    }
}