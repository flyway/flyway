/*-
 * ========================LICENSE_START=================================
 * flyway-nc-core
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
package org.flywaydb.nc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.DockerUtils;
import org.flywaydb.core.internal.util.FileUtils;
import org.flywaydb.core.internal.util.FlywayDbWebsiteLinks;
import org.flywaydb.core.internal.util.StringUtils;

@CustomLog
public class NativeConnectorsProcessRunner {
    private final ProcessBuilder processBuilder;
    private final String tool;
    private final Collection<String> printStrings = new ArrayList<>();
    private boolean redirectOutput;

    public NativeConnectorsProcessRunner(final List<String> commands, final String tool) {
        processBuilder = new ProcessBuilder(commands);
        processBuilder.environment();
        this.tool = tool;
    }

    public void addTextToPrint(final String textToAdd) {
        printStrings.add(textToAdd);
    }

    public void addEnvironmentVariable(final String key, final String value) {
        processBuilder.environment().put(key, value);
    }

    public void removeEnvironmentVariable(final String key) {
        processBuilder.environment().remove(key);
    }

    public void setWorkingDirectory(final String workingDirectory) {
        processBuilder.directory(new File(workingDirectory));
    }

    public void redirectOutput() {
        final File output = createTempLogFile("output");
        processBuilder.redirectOutput(output);
        final File errorOutput = createTempLogFile("errorOutput");
        processBuilder.redirectError(errorOutput);
        redirectOutput = true;
    }

    private File createTempLogFile(final String filename) {
        try {
            final File tempFile = File.createTempFile(filename, ".log");
            tempFile.deleteOnExit();

            return tempFile;
        } catch (final Exception e) {
            throw new FlywayException("Failed to execute SQL file due to: " + e.getMessage());
        }
    }

    private String getOutputFromStream(final InputStream inputStream) throws IOException {
        return FileUtils.copyToString(new InputStreamReader(inputStream,
            StandardCharsets.UTF_8)).strip();
    }

    private String getOutputFromFile(final File file) throws IOException {
        return Files.readString(Path.of(file.getAbsolutePath())).strip();
    }

    public void executeMigrations(final boolean outputQueryResults, final boolean combinedStreams) {
        try {
            LOG.debug("Executing " + tool);
            final Process process = processBuilder.start();
            final boolean exited = process.waitFor(5, TimeUnit.MINUTES);
            if (!exited) {
                throw new FlywayException(tool + " execution timeout. Consider using smaller migrations");
            }
            final String stdOut = redirectOutput ? getOutputFromFile(processBuilder.redirectOutput().file()) : getOutputFromStream(process.getInputStream());
            final String stdErr = redirectOutput ? getOutputFromFile(processBuilder.redirectError().file()) : getOutputFromStream(process.getErrorStream());

            final int exitCode = process.exitValue();

            if (outputQueryResults) {
                if (StringUtils.hasText(stdOut)) {
                    LOG.info(stdOut);
                }
                if (StringUtils.hasText(stdErr)) {
                    LOG.warn(stdErr);
                }
            }

            if (exitCode != 0) {
                if (combinedStreams) {
                    final String exceptionMessage = Stream.of(stdOut, stdErr)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.joining("\n"));
                    throw new FlywayException(exceptionMessage);
                }
                throw new FlywayException(stdErr + " (ExitCode: " + exitCode + ")");
            }

        } catch (final FlywayException e) {
            throw e;
        } catch (final Exception e) {
            throw new FlywayException(e);
        }
    }

    public boolean checkToolInstalled(final boolean silent, final String errorMessage) {
        LOG.debug("Executing " + String.join(" ", processBuilder.command()));
        try {
            processBuilder.start();
        } catch (final Exception e) {
            if (silent) {
                return false;
            }
            if (DockerUtils.isContainer()) {
                throw new FlywayException(
                    tool + " is not installed on this docker image. Please use the " + tool + " docker image on our repository: "
                        + FlywayDbWebsiteLinks.OSS_DOCKER_REPOSITORY);
            }
            throw new FlywayException(errorMessage);
        }
        return true;
    }

    public void checkToolConnectivity() {
        try {
            final Process process = processBuilder.start();

            if (!printStrings.isEmpty()) {
                try (final PrintWriter writer = new PrintWriter(process.getOutputStream(), true)) {
                    printStrings.forEach(writer::println);
                }
            }

            final boolean exited = process.waitFor(1, TimeUnit.MINUTES);
            if (!exited) {
                throw new FlywayException(tool + " connection timeout");
            }

            final int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new FlywayException(tool + " failed to connect to the provided connection URL");
            }
        } catch (final Exception e) {
            throw new FlywayException(e.getMessage());
        }
    }
}
