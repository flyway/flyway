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

import java.io.File;
import java.io.FileWriter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {
    public static String jsonToFile(String folder, String filename, String json) {
        return jsonToFile(folder, filename, JsonParser.parseString(json).getAsJsonObject());
    }

    public static String jsonToFile(String folder, String filename, Object json) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .create();
        String fullFilename = folder + File.separator + filename + ".json";
        try (FileWriter fileWriter = new FileWriter(fullFilename)) {
            gson.toJson(json, fileWriter);
            return fullFilename;
        } catch (Exception e) {
            throw new FlywayException("Unable to write JSON to file: " + e.getMessage());
        }
    }

    public static Object parseJsonArray(String json) {
        return JsonParser.parseString(json).getAsJsonArray();
    }
}