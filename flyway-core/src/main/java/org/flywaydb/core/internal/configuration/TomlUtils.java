/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.CustomLog;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.configuration.models.ConfigurationModel;
import org.flywaydb.core.internal.util.ObjectMapperFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CustomLog
public class TomlUtils {

    public static ConfigurationModel loadConfigurationFromEnvironment() {
        Map<String, String> environmentVariables = System.getenv()
                                                         .entrySet()
                                                         .stream()
                                                         .filter(e -> e.getKey().startsWith("flyway_") || e.getKey().startsWith("environments_"))
                                                         .collect(Collectors.toMap(k -> k.getKey().replace("_", "."), Map.Entry::getValue));
        return toConfiguration(unflattenMap(environmentVariables));
    }

    public static ConfigurationModel loadConfigurationFromCommandlineArgs(Map<String, String> commandLineArguments) {
        return toConfiguration(unflattenMap(commandLineArguments));
    }

    private static ConfigurationModel toConfiguration(Map<String, Object> properties) {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();

        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, String.class);

        //noinspection unchecked
        simpleModule.addDeserializer((Class<List<String>>) type.getRawClass(), new ListDeserializer());
        objectMapper.registerModule(simpleModule);
        return objectMapper.convertValue(properties, ConfigurationModel.class);
    }

    private static Map<String, Object> unflattenMap(Map<String, String> map) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String[] parts = entry.getKey().split("\\.");
            Map<String, Object> currentMap = result;
            for (int i = 0; i < parts.length; i++) {
                if (i != parts.length - 1) {
                    //noinspection unchecked
                    currentMap = (Map<String, Object>) currentMap.computeIfAbsent(parts[i], (x) -> new HashMap<String, Object>());
                } else {
                    currentMap.put(parts[i], entry.getValue());
                }
            }
        }
        return result;
    }

    public static ConfigurationModel loadConfigurationFiles(List<File> files, String workingDirectory) {
        ConfigurationModel defaultConfig = ConfigurationModel.defaults();
        return files.stream()
                    .map(f -> TomlUtils.loadConfigurationFile(f, workingDirectory))
                    .reduce(defaultConfig, ConfigurationModel::merge);
    }

    static ConfigurationModel loadConfigurationFile(File configFile, String workingDirectory) {
        if (!configFile.isAbsolute() && workingDirectory != null) {
            File temporaryFile = new File(workingDirectory, configFile.getPath());
            if (temporaryFile.exists()) {
                configFile = temporaryFile;
            }
        }

        try {
            return ObjectMapperFactory.getObjectMapper(configFile.toString())
                                      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                      .readerFor(ConfigurationModel.class)
                                      .readValue(configFile);
        } catch (IOException e) {
            throw new FlywayException("Unable to load config file: " + configFile.getAbsolutePath(), e);
        }
    }
}