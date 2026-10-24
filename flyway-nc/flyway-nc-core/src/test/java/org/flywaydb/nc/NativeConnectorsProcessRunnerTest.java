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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class NativeConnectorsProcessRunnerTest {
    @Test
    void defaultsToFiveMinutes() throws Exception {
        final Process process = successfulProcess();
        when(process.waitFor(300, TimeUnit.SECONDS)).thenReturn(true);

        try (final MockedConstruction<ProcessBuilder> builders = mockProcessBuilder(process)) {
            new NativeConnectorsProcessRunner(List.of("mongosh"), "Mongosh").executeMigrations(false, false);

            verify(process).waitFor(300, TimeUnit.SECONDS);
        }
    }

    @Test
    void allowsTimeoutLongerThanFiveMinutes() throws Exception {
        final Process process = successfulProcess();
        when(process.waitFor(600, TimeUnit.SECONDS)).thenReturn(true);

        try (final MockedConstruction<ProcessBuilder> builders = mockProcessBuilder(process)) {
            new NativeConnectorsProcessRunner(List.of("mongosh"), "Mongosh", 600).executeMigrations(false, false);

            verify(process).waitFor(600, TimeUnit.SECONDS);
        }
    }

    @Test
    void reportsConfiguredTimeout() throws Exception {
        final Process process = mock(Process.class);
        when(process.waitFor(1, TimeUnit.SECONDS)).thenReturn(false);

        try (final MockedConstruction<ProcessBuilder> builders = mockProcessBuilder(process)) {
            final NativeConnectorsProcessRunner runner = new NativeConnectorsProcessRunner(List.of("mongosh"), "Mongosh", 1);

            final FlywayException exception = assertThrows(FlywayException.class, () -> runner.executeMigrations(false, false));

            assertTrue(exception.getMessage().contains("flyway.nativeConnectors.processTimeout"));
            verify(process).waitFor(1, TimeUnit.SECONDS);
        }
    }

    @Test
    void preservesProcessErrors() throws Exception {
        final Process process = successfulProcess();
        when(process.waitFor(600, TimeUnit.SECONDS)).thenReturn(true);
        when(process.exitValue()).thenReturn(2);
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream("migration failed".getBytes(StandardCharsets.UTF_8)));

        try (final MockedConstruction<ProcessBuilder> builders = mockProcessBuilder(process)) {
            final NativeConnectorsProcessRunner runner = new NativeConnectorsProcessRunner(List.of("mongosh"), "Mongosh", 600);

            final FlywayException exception = assertThrows(FlywayException.class, () -> runner.executeMigrations(false, false));

            assertEquals("migration failed (ExitCode: 2)", exception.getMessage());
        }
    }

    @Test
    void retainsOneMinuteConnectivityTimeout() throws Exception {
        final Process process = mock(Process.class);
        when(process.waitFor(1, TimeUnit.MINUTES)).thenReturn(true);

        try (final MockedConstruction<ProcessBuilder> builders = mockProcessBuilder(process)) {
            new NativeConnectorsProcessRunner(List.of("mongosh"), "Mongosh", 600).checkToolConnectivity();

            verify(process).waitFor(1, TimeUnit.MINUTES);
        }
    }

    @Test
    void rejectsNonPositiveTimeout() {
        assertThrows(FlywayException.class, () -> new NativeConnectorsProcessRunner(List.of("mongosh"), "Mongosh", 0));
        assertThrows(FlywayException.class, () -> new NativeConnectorsProcessRunner(List.of("mongosh"), "Mongosh", -1));
    }

    private Process successfulProcess() {
        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        return process;
    }

    private MockedConstruction<ProcessBuilder> mockProcessBuilder(final Process process) {
        return mockConstruction(ProcessBuilder.class, (builder, context) -> when(builder.start()).thenReturn(process));
    }
}
