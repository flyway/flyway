package org.flywaydb.core.extensibility;

import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.Collections;
import java.util.List;

public interface PluginMetadata extends Plugin {
    /**
     * @return The help text for this plugin
     */
    default String getHelpText() {
        StringBuilder result = new StringBuilder();

        String indent = "    ";
        String description = getDescription();
        List<ConfigurationParameter> configurationParameters = getConfigurationParameters();
        List<ConfigurationParameter> flags = getFlags();
        String example = getExample();
        String documentationLink = getDocumentationLink();

        if (description != null) {
            result.append("Description:\n").append(indent).append(description).append("\n\n");
        }

        int padSize = 0;
        if (configurationParameters != null) {
            padSize = configurationParameters.stream().mapToInt(p -> p.name.length()).max().orElse(0) + 2;
        }
        if (flags != null) {
            padSize = Math.max(padSize, flags.stream().mapToInt(p -> p.name.length()).max().orElse(0) + 2);
        }

        if (configurationParameters != null) {
            result.append("Configuration parameters: (Format: -key=value)\n");
            for (ConfigurationParameter p : configurationParameters) {
                result.append(indent).append(StringUtils.rightPad(p.name.substring("flyway.".length()), padSize, ' ')).append(p.description);
                if (p.required) {
                    result.append(" [REQUIRED]");
                }
                result.append("\n");
            }
            result.append("\n");
        }

        if (flags != null) {
            result.append("Flags:\n");
            for (ConfigurationParameter p : flags) {
                result.append(indent).append(StringUtils.rightPad(p.name, padSize, ' ')).append(p.description);
                if (p.required) {
                    result.append(" [REQUIRED]");
                }
                result.append("\n");
            }
            result.append("\n");
        }

        if (example != null) {
            result.append("Example:\n").append(indent).append(example).append("\n\n");
        }

        if (documentationLink != null) {
            result.append("Online documentation: ").append(documentationLink).append("\n");
        }

        return result.toString();
    }

    /**
     * @return A description of what this plugin does
     */
    default String getDescription() {
        return null;
    }

    /**
     * @return A list of the configuration parameters this plugin makes use of
     */
    default List<ConfigurationParameter> getConfigurationParameters() {
        return null;
    }

    /**
     * @return A list of the CLI only flags this plugin makes use of
     */
    default List<ConfigurationParameter> getFlags() {
        return null;
    }

    /**
     * @return An example of how this plugin is to be used
     */
    default String getExample() {
        return null;
    }

    /**
     * @return A list of &lt;command, description&gt; pairs that describe how this plugin is to be used
     */
    default List<Pair<String, String>> getUsage() {
        return Collections.emptyList();
    }

    /**
     * @return A link to the documentation for this plugin
     */
    default String getDocumentationLink() {
        return null;
    }
}