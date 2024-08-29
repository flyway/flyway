/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.extensibility;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

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
            result.append("Description:\n");
            Arrays.stream(description.split("\n")).map(String::trim).forEach(line -> result.append(indent)
                .append(line)
                .append("\n"));
            result.append("\n");
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
                final String parameterName = p.name.startsWith("flyway.")
                    ? p.name.substring("flyway.".length())
                    : p.name;
                result.append(indent).append(StringUtils.rightPad(parameterName, padSize, ' ')).append(p.description);
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
