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

import static org.flywaydb.core.internal.util.FileUtils.createDirIfNotExists;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
        } catch (final JsonProcessingException e) {
            throw new FlywayException("Unable to serialize object to JSON", e);
        }
    }

    public static JsonMapper getJsonMapper() {
        final JsonMapper mapper = new JsonMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(Include.ALWAYS);

        // NOTE: This is a workaround while we use both GSON and ObjectMapper
        // Once we fully migrate to ObjectMapper, we can remove this line
        // and use @JsonIgnore rather than transient
        mapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

    public static <T> List<T> toList(final String json) {
        try {
            return getJsonMapper().readValue(json, new TypeReference<>() {});
        } catch (final JsonProcessingException e) {
            throw new FlywayException("Unable to parse JSON: " + json, e);
        }
    }

    public static String getFromJson(final String json, final String key) {
        if (json == null || json.isBlank()) {
            return null;
        }

        final var factory = new JsonFactory();
        try (final var parser = factory.createParser(json)) {
            parser.nextToken();
            while (parser.nextToken() != JsonToken.END_OBJECT && parser.currentToken() != null) {
                if (parser.currentToken().isStructStart()) {
                    // Only look at lop level fields
                    parser.skipChildren();
                } else if (parser.currentToken() == JsonToken.FIELD_NAME && key.equals(parser.currentName())) {
                    parser.nextToken();
                    return parser.getText();
                }
            }
        } catch (final IOException e) {
            throw new FlywayException("Unable to parse JSON: " + e.getMessage(), e);
        }

        return null;
    }

    public static ArrayNode parseJsonArray(final String json) {
        try {
            return (ArrayNode) getJsonMapper().readTree(json);
        } catch (final Exception e) {
            throw new FlywayException("Unable to parse JSON: " + json, e);
        }
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

    public static List<String> getJsonSectionItems(final String json, final String sectionName) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(json);
            final JsonNode firstElement = root.get(0);

            if (firstElement == null) {
                return Collections.emptyList();
            }

            final JsonNode section = firstElement.get(sectionName);
            final List<String> fieldNames = new ArrayList<>();

            if (section != null && section.isObject()) {
                final Iterator<Entry<String, JsonNode>> fields = section.fields();
                while (fields.hasNext()) {
                    final Map.Entry<String, JsonNode> entry = fields.next();
                    fieldNames.add(entry.getKey());
                }
            }

            return fieldNames;
        } catch (final Exception e) {
            throw new FlywayException("Unable to parse JSON: " + e.getMessage());
        }
    }
}
