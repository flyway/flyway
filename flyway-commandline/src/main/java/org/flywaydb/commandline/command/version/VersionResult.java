package org.flywaydb.commandline.command.version;

import lombok.AllArgsConstructor;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.Tier;

import java.util.List;

@AllArgsConstructor
public class VersionResult implements OperationResult {
    public String version;
    public String command;
    public Tier edition;
    public List<PluginVersionResult> pluginVersions;
}