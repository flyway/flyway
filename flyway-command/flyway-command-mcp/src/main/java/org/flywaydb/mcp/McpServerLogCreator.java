/*-
 * ========================LICENSE_START=================================
 * flyway-command-mcp
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
package org.flywaydb.mcp;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Clock;
import java.util.List;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;
import org.flywaydb.core.internal.logging.multi.MultiLogger;

/**
 * A log creator that creates McpServerLog instances for use with stdio MCP servers. This is an experimental API and may
 * be removed or changed in future versions.
 */
@SuppressWarnings({ "unused", "WeakerAccess", "FieldNamingConvention" })
public class McpServerLogCreator implements LogCreator {
    private static OutputStream fileStream;

    @Override
    public Log createLogger(final Class<?> clazz) {
        final Log stdErrLog = new McpServerStdErrLog(System.err);
        final Log fileLog = new McpServerFileLog(getFileStream(stdErrLog));
        return new MultiLogger(List.of(fileLog, stdErrLog));
    }

    private static synchronized OutputStream getFileStream(final Log log) {
        if (fileStream == null) {
            fileStream = new McpServerLogFileActions(log).startNewLog(Clock.systemDefaultZone());
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    fileStream.close();
                } catch (final IOException ignored) {
                }
            }));
        }
        return fileStream;
    }
}
