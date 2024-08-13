/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
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
        try {
            return getJsonMapper().readTree(json).get(key).asText();
        } catch (final JsonProcessingException e) {
            return "";
        }
    }

    public static JsonArray parseJsonArray(final String json) {
        return JsonParser.parseString(json).getAsJsonArray();
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
