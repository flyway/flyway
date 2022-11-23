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
package org.flywaydb.core.internal.configuration;

import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@CustomLog
public class TomlUtils {
    public static Map<String, String> loadConfigurationFile(File configFile, String encoding) {
        if (!configFile.isFile() || !configFile.canRead()) {
            return new HashMap<>();
        }

        LOG.warn("Loading TOML config file: " + configFile.getAbsolutePath());
        LOG.warn("This is an experimental feature, which is subject to change, and not recommended for use as yet");

        TomlMapper tomlMapper = new TomlMapper();
        MapLikeType mapType = tomlMapper.getTypeFactory().constructMapLikeType(Map.class, String.class, Object.class);

        try {
            Map<String, Map<String, Object>> valueMap = tomlMapper.readValue(new InputStreamReader(new FileInputStream(configFile), encoding), mapType);
            return flatten(valueMap.get("flyway"), "flyway");
        } catch (IOException e) {
            throw new FlywayException("Unable to load config file: " + configFile.getAbsolutePath(), e);
        }
    }

    private static Map<String, String> flatten(Map<String, Object> map, String parentKey) {
        Map<String, String> result = new HashMap<>();

        for (String key : map.keySet()) {
            Object value = map.get(key);

            if (value instanceof Map) {
                Map<String, String> subTree = flatten(((Map) value), key);

                for (String subKey : subTree.keySet()) {
                    result.put(parentKey + "." + subKey, subTree.get(subKey));
                }
            } else {
                result.put(parentKey + "." + key, value.toString());
            }
        }

        return result;
    }
}