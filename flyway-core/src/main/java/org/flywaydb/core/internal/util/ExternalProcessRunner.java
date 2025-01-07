/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core.internal.util;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.SneakyThrows;

public class ExternalProcessRunner {

    @SneakyThrows
    public int run(final String[] command,
                   final Consumer<? super String> onStdOut,
                   final Consumer<? super String> onStdErr) {
        return run(command, null, null, onStdOut, onStdErr);
    }

    @SneakyThrows
    public int run(final String[] command,
                   final Path workingDirectory,
                   final Consumer<? super String> onStdOut,
                   final Consumer<? super String> onStdErr) {
        return run(command, workingDirectory, null, onStdOut, onStdErr);
    }

    @SneakyThrows
    public int run(final String[] command,
                   final Path workingDirectory,
                   final String stdIn,
                   final Consumer<? super String> onStdOut,
                   final Consumer<? super String> onStdErr) {
        return run(command, workingDirectory, null, stdIn, onStdOut, onStdErr);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @SneakyThrows
    public int run(final String[] command,
                   final Path workingDirectory,
                   final Map<String, String> env,
                   final String stdIn,
                   final Consumer<? super String> onStdOut,
                   final Consumer<? super String> onStdErr) {
        final var processBuilder = new ProcessBuilder(command);
        if (workingDirectory != null) {
            processBuilder.directory(workingDirectory.toFile());
        }
        if (env != null) {
            processBuilder.environment().putAll(env);
        }
        final var process = processBuilder.start();

        final var executorService = Executors.newFixedThreadPool(2);
        try (final var outStream = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
             final var errStream = new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8);
             final var inputSteam = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);
             final var stdOut = new Scanner(outStream);
             final var stdErr = new Scanner(errStream)) {
            final var handleStdOut = executorService.submit(() -> {
                while (stdOut.hasNextLine()) {
                    onStdOut.accept(stdOut.nextLine());
                }
            });
            final var handleStdErr = executorService.submit(() -> {
                while (stdErr.hasNextLine()) {
                    onStdErr.accept(stdErr.nextLine());
                }
            });

            if (stdIn != null) {
                inputSteam.write(stdIn);
            }
            inputSteam.close();

            handleStdOut.get();
            handleStdErr.get();
        } finally {
            executorService.shutdown();
        }

        return process.waitFor();
    }

    @SneakyThrows
    public int run(final ProcessBuilder processBuilder,
                   final Path workingDirectory,
                   final Map<String, String> env,
                   final String stdIn) {

        if (workingDirectory != null) {
            processBuilder.directory(workingDirectory.toFile());
        }
        if (env != null) {
            processBuilder.environment().putAll(env);
        }
        final var process = processBuilder.start();

        if (stdIn != null) {
            try (final var inputSteam = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
                inputSteam.write(stdIn);
            }
        }

        return process.waitFor();
    }
}
