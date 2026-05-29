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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;

@RequiredArgsConstructor
class McpServerLogFileActions {
    private final Log log;
    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_n", Locale.ROOT)
        .withZone(ZoneOffset.UTC);
    private final Pattern logPattern = Pattern.compile("flyway-mcp \\d{4}_\\d{2}_\\d{2}_\\d{2}_\\d{2}_\\d{2}_\\d+\\.log");

    OutputStream startNewLog() {
        try {
            final Path path = getLogPath();
            Files.createDirectories(path.getParent());
            return Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (final FileAlreadyExistsException ignored) {
            try {
                return Files.newOutputStream(getLogPath(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            } catch (final IOException e) {
                log.error("Failed to create MCP log file on retry", e);
                throw new FlywayException("Failed to create MCP log file on retry", e);
            }
        } catch (final IOException e) {
            log.error("Failed to create MCP log file", e);
            throw new FlywayException("Failed to create MCP log file", e);
        }
    }

    void pruneLogs(final int maxLogs) {
        if (maxLogs < 1) {
            log.warn("maxLogs configured as < 1 - removal of old logs skipped");
            return;
        }
        try {
            final Path dir = getLogFileDirectory();
            if (!Files.isDirectory(dir)) {
                return;
            }

            try (final Stream<Path> stream = Files.list(dir)) {
                stream.filter(Files::isRegularFile)
                    .filter(p -> logPattern.matcher(p.getFileName().toString()).matches())
                    .sorted(Comparator.comparing(Path::getFileName).reversed())
                    .skip(maxLogs)
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (final IOException e) {
                            log.warn("Failed to delete old MCP log file " + p + ": " + e.getMessage());
                        }
                    });
            }
        } catch (final Exception e) {
            log.warn("Failed to prune old MCP log files: " + e.getMessage());
        }
    }

    private Path getLogPath() {
        final String filename = "flyway-mcp " + format.format(Instant.now()) + ".log";
        return getLogFileDirectory().resolve(filename);
    }

    private Path getLogFileDirectory() {
        final boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
        return isWindows
            ? Path.of(System.getenv("LocalAppData"), "Red Gate", "Logs", "Flyway")
            : Path.of(System.getProperty("user.home"), ".local", "share", "Red Gate", "Logs", "Flyway");
    }
}
