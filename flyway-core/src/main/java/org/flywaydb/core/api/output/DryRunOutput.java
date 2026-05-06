package org.flywaydb.core.api.output;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class DryRunOutput {
    public String version;      // null for repeatable migrations
    public String description;
    public String type;         // "SQL", "JDBC", "SCRIPT", etc.
    public String filepath;
    public String sqlContent;   // file content for SQL/SCRIPT types; null for Java migrations
}
