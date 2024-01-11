package org.flywaydb.core.extensibility;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.flywaydb.core.api.FlywayException;

import java.util.Map;

public interface ConfigurationExtension extends Plugin {
    @JsonIgnore
    String getNamespace();
    @Deprecated
    default void extractParametersFromConfiguration(Map<String, String> configuration) {
        // Do nothing
    }
    String getConfigurationParameterFromEnvironmentVariable(String environmentVariable);

    @Override
    default Plugin copy() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(this), this.getClass());
        }
        catch (Exception e) {
            throw new FlywayException(e);
        }
    }

    @JsonIgnore
    default boolean isStub() {
        return false;
    }
}