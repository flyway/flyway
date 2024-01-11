package org.flywaydb.core.api.output;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InfoOutput {
    public String category;
    public String version;
    public String rawVersion;
    public String description;
    public String type;
    public String installedOnUTC;
    public String state;
    public String undoable;
    public String filepath;
    public String undoFilepath;
    public String installedBy;
    public int executionTime;
}