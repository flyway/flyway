package org.flywaydb.commandline.command.version;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PluginVersionResult {
    public String name;
    public String version;
    public boolean isLicensed;
}