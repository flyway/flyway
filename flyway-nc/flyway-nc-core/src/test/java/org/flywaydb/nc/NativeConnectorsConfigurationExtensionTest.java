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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.internal.configuration.ConfigUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NativeConnectorsConfigurationExtensionTest {
    @Test
    void acceptsConfiguredProcessTimeout() {
        final ClassicConfiguration configuration = new ClassicConfiguration();

        assertDoesNotThrow(() -> configuration.configure(Map.of("flyway.nativeConnectors.processTimeout", "600")));
        assertEquals(600, configuration.getConfigurationExtension(NativeConnectorsConfigurationExtension.class)
            .getProcessTimeout());
    }

    @Test
    void defaultsToFiveMinutes() {
        final ClassicConfiguration configuration = new ClassicConfiguration();

        assertEquals(300, configuration.getConfigurationExtension(NativeConnectorsConfigurationExtension.class)
            .getProcessTimeout());
    }

    @Test
    void acceptsEnvironmentVariable() {
        final ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.configure(ConfigUtils.environmentVariablesToPropertyMap(
            Map.of("FLYWAY_NATIVE_CONNECTORS_PROCESS_TIMEOUT", "900")));

        assertEquals(900, configuration.getConfigurationExtension(NativeConnectorsConfigurationExtension.class)
            .getProcessTimeout());
        assertNull(new NativeConnectorsConfigurationExtension()
            .getConfigurationParameterFromEnvironmentVariable("UNRELATED"));
    }

    @Test
    void preservesTimeoutWhenCopyingConfiguration() {
        final ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.getConfigurationExtension(NativeConnectorsConfigurationExtension.class).setProcessTimeout(600);

        final ClassicConfiguration copy = new ClassicConfiguration(configuration);

        assertEquals(600, copy.getConfigurationExtension(NativeConnectorsConfigurationExtension.class).getProcessTimeout());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1 })
    void rejectsNonPositiveTimeout(final int timeout) {
        final NativeConnectorsConfigurationExtension configuration = new NativeConnectorsConfigurationExtension();

        assertThrows(FlywayException.class, () -> configuration.setProcessTimeout(timeout));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "-1" })
    void rejectsNonPositiveConfiguredTimeout(final String timeout) {
        final ClassicConfiguration configuration = new ClassicConfiguration();

        assertThrows(FlywayException.class,
            () -> configuration.configure(Map.of("flyway.nativeConnectors.processTimeout", timeout)));
    }
}
