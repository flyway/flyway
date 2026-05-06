package org.flywaydb.core.api.output;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class UndoOutput {
    public String version;
    public String description;
    public String type;
    public String filepath;
    public int executionTime;
}
