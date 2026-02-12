/*-
 * ========================LICENSE_START=================================
 * flyway-command-test-connection
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
package org.flywaydb.testconnection;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.configuration.models.EnvironmentModel;

@RequiredArgsConstructor
class StandardInEnvironmentModelProvider {
    private final InputStream stdIn;

    EnvironmentModel getModel() {
        try (final var scanner = new Scanner(stdIn, StandardCharsets.UTF_8)) {
            final var request = scanner.nextLine();
            return JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build()
                .readerFor(EnvironmentModel.class)
                .readValue(request);
        } catch (final Exception e) {
            throw new FlywayException("Error reading environment from standard in", e);
        }
    }
}
