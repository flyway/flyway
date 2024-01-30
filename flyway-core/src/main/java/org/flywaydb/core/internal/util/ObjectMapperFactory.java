package org.flywaydb.core.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import lombok.experimental.ExtensionMethod;
import org.flywaydb.core.api.FlywayException;

@ExtensionMethod(StringUtils.class)
public class ObjectMapperFactory {
    public static ObjectMapper getObjectMapper(String file) {
        String extension = getFileExtension(file);
        switch (extension.toLowerCase()) {
            case ".json":
                return new JsonMapper();
            case ".toml":
                return new TomlMapper();
            default:
                throw new FlywayException("No mapper found for '" + extension + "' extension");
        }
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