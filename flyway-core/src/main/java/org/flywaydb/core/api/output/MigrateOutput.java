package org.flywaydb.core.api.output;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MigrateOutput {
    public String category;
    public String version;
    public String description;
    public String type;
    public String filepath;
    public int executionTime;
}