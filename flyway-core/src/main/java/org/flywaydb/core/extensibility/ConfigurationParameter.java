package org.flywaydb.core.extensibility;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConfigurationParameter {
    public final String name;
    public final String description;
    public final boolean required;
}