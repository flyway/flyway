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

import java.io.PrintStream;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.logging.Log;

/**
 * A log implementation for stdio MCP servers - writes logs to stderr with a log level prefix. This is an experimental
 * API and may be removed or changed in future versions.
 */
@RequiredArgsConstructor
public class McpServerStdErrLog implements Log {
    private final PrintStream stream;

    @Override
    public void warn(final String message) {
        stream.println("[WARN] " + message);
    }

    @Override
    public void error(final String message) {
        stream.println("[ERROR] " + message);
    }

    @Override
    public void error(final String message, final Exception e) {
        stream.println("[ERROR] " + message);
        stream.println("[ERROR] " + e.getClass().getName() + " stacktrace:");
        Arrays.stream(e.getStackTrace()).forEach(x -> stream.println("[ERROR]  " + x));
    }

    @Override
    public void debug(final String message) {
    }

    @Override
    public void info(final String message) {
    }

    @Override
    public void notice(final String message) {
    }
}
