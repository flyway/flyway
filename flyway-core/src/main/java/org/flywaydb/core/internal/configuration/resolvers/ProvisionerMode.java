package org.flywaydb.core.internal.configuration.resolvers;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;

@Getter
@RequiredArgsConstructor
public enum ProvisionerMode {
    Provision("provision"),
    Reprovision("reprovision"),
    Skip("skip");

    private final String value;

    public static ProvisionerMode fromString(String value) {
        return Arrays.stream(values())
            .filter(color -> color.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new FlywayException("Unknown provisioner mode: " + value));
    }
}