package org.flywaydb.core.api.output;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RepairOutput {
    public String version;
    public String description;
    public String filepath;
}