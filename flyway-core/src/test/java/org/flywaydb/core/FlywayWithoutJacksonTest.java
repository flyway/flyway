/*-
 * ========================LICENSE_START=================================
 * flyway-core
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
package org.flywaydb.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.h2.Driver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FlywayWithoutJacksonTest {
    @TempDir
    Path migrations;

    @Test
    void migratesValidatesAndReadsHistoryWithoutJackson() throws Exception {
        Files.writeString(migrations.resolve("V1__create_table.sql"), "CREATE TABLE test_table (id INT PRIMARY KEY);");
        final URL[] classpath = {
            Flyway.class.getProtectionDomain().getCodeSource().getLocation(),
            Driver.class.getProtectionDomain().getCodeSource().getLocation()
        };

        // Keep every Jackson artifact out, including the optional dependencies on the test classpath.
        try (final URLClassLoader classLoader = new URLClassLoader(classpath, ClassLoader.getPlatformClassLoader())) {
            assertThrows(ClassNotFoundException.class,
                () -> classLoader.loadClass("com.fasterxml.jackson.annotation.JsonIgnore"));
            assertThrows(ClassNotFoundException.class,
                () -> classLoader.loadClass("tools.jackson.databind.ObjectMapper"));

            final Class<?> flywayClass = classLoader.loadClass("org.flywaydb.core.Flyway");
            final Object configuration = flywayClass.getMethod("configure", ClassLoader.class).invoke(null, classLoader);
            final Class<?> configurationClass = configuration.getClass();
            configurationClass.getMethod("dataSource", String.class, String.class, String.class)
                .invoke(configuration, "jdbc:h2:mem:without_jackson;DB_CLOSE_DELAY=-1", "sa", "");
            configurationClass.getMethod("locations", String[].class)
                .invoke(configuration, (Object) new String[] { "filesystem:" + migrations });
            final Object flyway = configurationClass.getMethod("load").invoke(configuration);

            final Object result = flywayClass.getMethod("migrate").invoke(flyway);
            assertEquals(1, result.getClass().getField("migrationsExecuted").get(result));
            flywayClass.getMethod("validate").invoke(flyway);

            final Object info = flywayClass.getMethod("info").invoke(flyway);
            final Class<?> infoServiceClass = classLoader.loadClass("org.flywaydb.core.api.MigrationInfoService");
            assertEquals(1, Array.getLength(infoServiceClass.getMethod("applied").invoke(info)));
        }
    }
}
