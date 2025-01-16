/*-
 * ========================LICENSE_START=================================
 * flyway-verb-utils
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
package org.flywaydb.verb;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.ParsingContext;
import org.flywaydb.core.internal.parser.PlaceholderReplacingReader;

public class FileReadingWithPlaceholderReplacement {
    public static String readFile(Configuration configuration, ParsingContext parsingContext, String physicalLocation){
        try {
            final PlaceholderReplacingReader reader = PlaceholderReplacingReader.create(configuration,
                parsingContext,
                Files.newBufferedReader(Path.of(physicalLocation)));
            try (final BufferedReader bufferedReader = new BufferedReader(reader)) {
                return String.join("\n", bufferedReader.lines().toList());
            }
        } catch (IOException e) {
            throw new FlywayException(e);
        }
        
    }
}
