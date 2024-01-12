/*
 * Copyright (C) Red Gate Software Ltd 2010-2024
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
package org.flywaydb.core.internal.util;

import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;

import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class ExternalProcessRunner {
    @SneakyThrows
    public int run(final String[] command, final Consumer<? super String> onStdOut,
        final Consumer<? super String> onStdErr) {
        final Process process = new ProcessBuilder(command).start();

        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        try (final InputStreamReader outStream = new InputStreamReader(process.getInputStream(),
            StandardCharsets.UTF_8);
            final InputStreamReader errStream = new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8);
            final Scanner stdOut = new Scanner(outStream);
            final Scanner stdErr = new Scanner(errStream)
        ) {
            final Future<?> handleStdOut = executorService.submit(() -> {
                while (stdOut.hasNextLine()) {
                    onStdOut.accept(stdOut.nextLine());
                }
            });
            final Future<?> handleStdErr = executorService.submit(() -> {
                while (stdErr.hasNextLine()) {
                    onStdErr.accept(stdErr.nextLine());
                }
            });
            handleStdOut.get();
            handleStdErr.get();
        } finally {
            executorService.shutdown();
        }

        return process.waitFor();
    }
}