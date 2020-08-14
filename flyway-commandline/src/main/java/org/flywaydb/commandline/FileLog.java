/*
 * Copyright 2010-2020 Redgate Software Ltd
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

import org.flywaydb.commandline.ConsoleLog.Level;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

class FileLog implements Log {

    private final Path path;
    private final Level level;

    public FileLog(Path path, Level level) {
        this.path = path;
        this.level = level;
    }

    @Override
    public boolean isDebugEnabled() {
        return level == Level.DEBUG;
    }

    @Override
    public void debug(String message) {
        if (isDebugEnabled()) {
            writeLogMessage("DEBUG", message);
        }
    }

    @Override
    public void info(String message) {
        if (level.compareTo(Level.INFO) <= 0) {
            writeLogMessage(message);
        }
    }

    @Override
    public void warn(String message) {
        writeLogMessage("WARNING", message);
    }

    @Override
    public void error(String message) {
        writeLogMessage("ERROR", message);
    }

    @Override
    public void error(String message, Exception e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        String stackTrace = stringWriter.toString();

        writeLogMessage("ERROR", message);
        writeLogMessage(stackTrace);
    }

    private void writeLogMessage(String prefix, String message) {
        String logMessage = prefix + ": " + message;
        writeLogMessage(logMessage);
    }

    private void writeLogMessage(String logMessage) {
        try {
            Files.write(path, (logMessage + "\n").getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        } catch(IOException exception) {
            throw new FlywayException("Could not write to file at " + path + ".", exception);
        }
    }
}