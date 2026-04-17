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

import static org.flywaydb.core.internal.util.FileUtils.createDirIfNotExists;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import java.io.File;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {

    public static String jsonToFile(final String filename, final Object json) {
        final File file = new File(filename);

        createDirIfNotExists(file);

        try {
            final JsonMapper mapper = getJsonMapper();
            mapper.writeValue(file, json);
            return file.getCanonicalPath();
        } catch (final Exception e) {
            throw new FlywayException("Unable to write JSON to file: " + e.getMessage());
        }
    }

    public static String toJson(final Object object) {
        try {
            return getJsonMapper().writeValueAsString(object);
        } catch (final JacksonException e) {
            throw new FlywayException("Unable to serialize object to JSON", e);
        }
    }

    public static JsonMapper getJsonMapper() {
        return JsonMapper.builder()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(DateTimeFeature.WRITE_DATES_WITH_ZONE_ID, true)
            .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(Include.ALWAYS))
            .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(Include.ALWAYS))

            // NOTE: This is a workaround while we use both GSON and ObjectMapper
            // Once we fully migrate to ObjectMapper, we can remove this line
            // and use @JsonIgnore rather than transient
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();
    }

    public static <T> List<T> toList(final String json) {
        try {
            return getJsonMapper().readValue(json, new TypeReference<>() {});
        } catch (final JacksonException e) {
            throw new FlywayException("Unable to parse JSON: " + json, e);
        }
    }

    public static String getFromJson(final String json, final String key) {
        if (json == null || json.isBlank()) {
            return null;
        }


        final var factory = JsonFactory.builder().build();
        try (final var parser = factory.createParser(ObjectReadContext.empty(), json)) {
            parser.nextToken();
            while (parser.nextToken() != JsonToken.END_OBJECT && parser.currentToken() != null) {
                if (parser.currentToken().isStructStart()) {
                    // Only look at lop level fields
                    parser.skipChildren();
                } else if (parser.currentToken() == JsonToken.PROPERTY_NAME && key.equals(parser.currentName())) {
                    parser.nextToken();
                    return parser.getString();
                }
            }
        }

        return null;
    }

    public static <T> T parseJson(final String json, final Class<T> clazz) {
        try {
            return getJsonMapper().readValue(json, clazz);
        } catch (final Exception e) {
            throw new FlywayException("Unable to parse JSON: " + e.getMessage());
        }
    }

    public static <T> T parseJson(final String json, final TypeReference<T> typeReference) {
        try {
            return getJsonMapper().readValue(json, typeReference);
        } catch (final Exception e) {
            throw new FlywayException("Unable to parse JSON: " + e.getMessage());
        }
    }
}
