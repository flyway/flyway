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
import java.math.BigInteger;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.Log;

/**
 * Provides actions for managing log files for use with the mcp server log implementation. This is an experimental API
 * and may be removed or changed in future versions.
 */
@RequiredArgsConstructor
class McpServerLogFileActions {
    private static final int MAX_RETRIES = 5;
    private final Log log;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss", Locale.ROOT)
        .withZone(ZoneOffset.UTC);
    private final Pattern pattern = Pattern.compile(
        "flyway-mcp (\\d{4}_\\d{2}_\\d{2}_\\d{2}_\\d{2}_\\d{2})_(\\d+)\\.log");

    Optional<LogFileIdentifier> parseIdentifier(final Path path) {
        final Matcher matcher = pattern.matcher(path.getFileName().toString());
        if (matcher.matches()) {
            try {
                final Instant instant = Instant.from(timeFormatter.parse(matcher.group(1)));
                return Optional.of(new LogFileIdentifier(path, instant, new BigInteger(matcher.group(2))));
            } catch (final DateTimeParseException ignored) {
            }
        }
        return Optional.empty();
    }

    List<LogFileIdentifier> getAllLogs() {
        final Path dir = getLogFileDirectory();
        if (!Files.isDirectory(dir)) {
            return List.of();
        }

        try (final Stream<Path> stream = Files.find(dir, 1, (p, a) -> a.isRegularFile())) {
            return stream.flatMap(x -> parseIdentifier(x).stream()).toList();
        } catch (final IOException e) {
            throw new FlywayException("Failed to list MCP log files", e);
        }
    }

    OutputStream startNewLog(final Clock clock) {
        try {
            final Path dir = getLogFileDirectory();
            Files.createDirectories(dir);

            for (int retry = 0; retry < MAX_RETRIES; retry++) {
                final Instant now = Instant.now(clock).truncatedTo(ChronoUnit.SECONDS);
                final BigInteger number = getAllLogs().stream()
                    .filter(x -> x.instant().equals(now))
                    .reduce(BigInteger.ZERO, (a, b) -> a.max(b.number()), BigInteger::max);
                final Path path = dir.resolve("flyway-mcp "
                    + timeFormatter.format(now)
                    + "_"
                    + number.add(BigInteger.ONE)
                    + ".log");

                try {
                    return Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                } catch (final FileAlreadyExistsException ignored) {
                    log.debug("Failed to create MCP log file on retry " + (1 + retry) + " / " + MAX_RETRIES);
                }
            }

            throw new FlywayException("Failed to create MCP log file after " + MAX_RETRIES + " retries");
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
            getAllLogs().stream().sorted(Comparator.reverseOrder()).skip(maxLogs).forEach(x -> {
                try {
                    Files.deleteIfExists(x.path());
                } catch (final IOException e) {
                    log.warn("Failed to delete old MCP log file " + x.path() + ": " + e.getMessage());
                }
            });
        } catch (final Exception e) {
            log.warn("Failed to prune old MCP log files: " + e.getMessage());
        }
    }

    private Path getLogFileDirectory() {
        final boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
        return isWindows
            ? Path.of(System.getenv("LocalAppData"), "Red Gate", "Logs", "Flyway")
            : Path.of(System.getProperty("user.home"), ".local", "share", "Red Gate", "Logs", "Flyway");
    }

    record LogFileIdentifier(Path path, Instant instant, BigInteger number) implements Comparable<LogFileIdentifier> {
        @Override
        public int compareTo(final @NonNull McpServerLogFileActions.LogFileIdentifier o) {
            final int instantComparison = instant.compareTo(o.instant);
            if (instantComparison != 0) {
                return instantComparison;
            }

            final int numberComparison = number.compareTo(o.number);
            if (numberComparison != 0) {
                return numberComparison;
            }

            return path.compareTo(o.path);
        }
    }
}
