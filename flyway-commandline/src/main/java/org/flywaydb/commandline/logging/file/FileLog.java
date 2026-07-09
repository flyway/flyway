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
package org.flywaydb.commandline.logging.file;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.flywaydb.core.api.logging.LogFactory;

@RequiredArgsConstructor
public class FileLog implements Log {

    private final Path path;

    @Override
    public void debug(final String message) {
        if (LogFactory.isDebugEnabled()) {
            writeLogMessage("DEBUG", message);
        }
    }

    @Override
    public void info(final String message) {
        if (!LogFactory.isQuietMode()) {
            writeLogMessage(message);
        }
    }

    @Override
    public void warn(final String message) {
        writeLogMessage("WARNING", message);
    }

    @Override
    public void error(final String message) {
        writeLogMessage("ERROR", message);
    }

    @Override
    public void error(final String message, final Exception e) {
        final StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        final String stackTrace = stringWriter.toString();

        writeLogMessage("ERROR", message);
        writeLogMessage(stackTrace);
    }

    public void notice(final String message) {}

    private void writeLogMessage(final String prefix, final String message) {
        final String logMessage = prefix + ": " + message;
        writeLogMessage(logMessage);
    }

    private void writeLogMessage(final String logMessage) {
        try {
            Files.write(path, (logMessage + "\n").getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new FlywayException("Could not write to file at " + path + ".", exception);
        }
    }
}
