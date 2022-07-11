/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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

import com.google.gson.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.output.CompositeResult;
import org.flywaydb.core.api.output.OperationResult;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {
    public static String jsonToFile(String filename, String json) {
        return jsonToFile(filename, JsonParser.parseString(json).getAsJsonObject());
    }

    public static String jsonToFile(String filename, Object json) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .create();

        try (FileWriter fileWriter = new FileWriter(filename)) {
            gson.toJson(json, fileWriter);
            return filename;
        } catch (Exception e) {
            throw new FlywayException("Unable to write JSON to file: " + e.getMessage());
        }
    }

    public static CompositeResult appendIfExists(String filename, CompositeResult json, Class<? extends OperationResult> operationResultImplementation) {
        if (!Files.exists(Paths.get(filename))) {
            return json;
        }

        CompositeResult existingObject;
        try (FileReader reader = new FileReader(filename)) {
            existingObject = new GsonBuilder()
                    .registerTypeAdapter(OperationResult.class, new OperationResultDeserializer(operationResultImplementation))
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                    .create()
                    .fromJson(reader, CompositeResult.class);
        } catch (Exception e) {
            throw new FlywayException("Unable to read filename: " + filename, e);
        }

        existingObject.individualResults.addAll(json.individualResults);
        return existingObject;
    }

    public static Object parseJsonArray(String json) {
        return JsonParser.parseString(json).getAsJsonArray();
    }

    private static class OperationResultDeserializer implements JsonDeserializer<OperationResult> {
        private final Class<? extends OperationResult> clazz;

        public OperationResultDeserializer(Class<? extends OperationResult> clazz) {
            this.clazz = clazz;
        }

        @Override
        public OperationResult deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, clazz);
        }
    }
}