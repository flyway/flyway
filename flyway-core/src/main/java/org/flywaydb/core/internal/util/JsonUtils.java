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
import com.google.gson.reflect.TypeToken;
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
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {
    public static String jsonToFile(String filename, String json) {
        return jsonToFile(filename, JsonParser.parseString(json).getAsJsonObject());
    }

    public static String jsonToFile(String filename, Object json) {
        try (FileWriter fileWriter = new FileWriter(filename)) {
            getGson().toJson(json, fileWriter);
            return filename;
        } catch (Exception e) {
            throw new FlywayException("Unable to write JSON to file: " + e.getMessage());
        }
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .create();
    }

    public static <T> List<T> toList(String json) {
        Type listType = new TypeToken<ArrayList<T>>() { }.getType();
        return getGson().fromJson(json, listType);
    }

    public static String getFromJson(String json, String key) {
        return getGson().fromJson(json, JsonObject.class).get(key).getAsString();
    }

    public static <T extends OperationResult> CompositeResult<T> appendIfExists(String filename, CompositeResult<T> json, JsonDeserializer<CompositeResult<T>> deserializer) {
        if (!Files.exists(Paths.get(filename))) {
            return json;
        }

        CompositeResult<T> existingObject;
        Type existingObjectType = new TypeToken<CompositeResult<T>>() { }.getType();

        try (FileReader reader = new FileReader(filename)) {

            existingObject = new GsonBuilder()
                    .registerTypeAdapter(existingObjectType, deserializer)
                    .create()
                    .fromJson(reader, existingObjectType);
        } catch (Exception e) {
            throw new FlywayException("Unable to read filename: " + filename, e);
        }

        existingObject.individualResults.addAll(json.individualResults);
        return existingObject;
    }

    public static Object parseJsonArray(String json) {
        return JsonParser.parseString(json).getAsJsonArray();
    }
}