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
package org.flywaydb.core.internal.util;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.toml.TomlMapper;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.api.FlywayException;

@ExtensionMethod(StringUtils.class)
public class ObjectMapperFactory {
    public static ObjectMapper getObjectMapper(String file) {
        String extension = getFileExtension(file);
        return switch (extension.toLowerCase()) {
            case ".json" -> new JsonMapper();
            case ".toml" -> new TomlMapper();
            default -> throw new FlywayException("No mapper found for '" + extension + "' extension");
        };
    }

    private static String getFileExtension(String filename) {
        if(filename.hasText()) {
            int dotLocation = filename.lastIndexOf('.');
            if (dotLocation > 0) {
                return filename.substring(dotLocation);
            }
        }
        return "";
    }
}
