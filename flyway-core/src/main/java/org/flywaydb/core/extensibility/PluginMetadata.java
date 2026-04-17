/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

public interface PluginMetadata extends Plugin {
    /**
     * @return The help text for this plugin
     */
    default String getHelpText() {
        return getHelpText(Collections.emptyList());
    }

    default String getHelpText(List<String> subCommands) {
        StringBuilder result = new StringBuilder();

        String indent = "    ";
        String description = getDescription();
        List<ConfigurationParameter> configurationParameters = getConfigurationParameters();
        List<ConfigurationParameter> flags = getFlags();

        List<String> filteredSubCommands = subCommands.stream()
            .filter(getAllowedSubCommands()::contains)
            .collect(Collectors.toList());

        if (configurationParameters != null && !filteredSubCommands.isEmpty()) {
            configurationParameters = configurationParameters.stream()
                .filter(x -> x.parentSubCommands.stream().anyMatch(filteredSubCommands::contains))
                .collect(Collectors.toList());
        }

        if (flags != null && !filteredSubCommands.isEmpty()) {
            flags = flags.stream()
                .filter(f -> filteredSubCommands.contains(f.name))
                .collect(Collectors.toList());
        }

        List<String> examples = getExamples(filteredSubCommands);
        String documentationLink = getDocumentationLink();

        if (inPreview()) {
            result.append("(In preview)\n\n");
        }

        if (description != null) {
            result.append("Description:\n");
            Arrays.stream(description.split("\n")).map(String::trim).forEach(line -> result.append(indent)
                .append(line)
                .append("\n"));
            result.append("\n");
        }

        int padSize = 0;
        if (configurationParameters != null) {
            padSize = configurationParameters.stream().map(p -> p.name + (p.required ? " [REQUIRED]" : "")).mapToInt(
                String::length).max().orElse(0) + 2;
        }
        if (flags != null) {
            padSize = Math.max(padSize, flags.stream().map(p -> p.name + (p.required ? " [REQUIRED]" : "")).mapToInt(
                String::length).max().orElse(0) + 2);
        }

        if (configurationParameters != null) {
            result.append("Configuration parameters: (Format: -key=value)\n");
            for (ConfigurationParameter p : configurationParameters) {
                final String parameterName = p.name.startsWith("flyway.")
                    ? p.name.substring("flyway.".length())
                    : p.name;
                final String fullParameter = parameterName + (p.required ? " [REQUIRED]" : "");
                result.append(indent).append(StringUtils.rightPad(fullParameter, padSize, ' '));

                final String descriptionPadding = " ".repeat(indent.length() + padSize);
                final List<String> descriptionLines = Arrays.stream(p.description.split("\n")).toList();

                result.append(descriptionLines.get(0)).append("\n");
                for (int i = 1; i < descriptionLines.size(); i++) {
                    result.append(descriptionPadding).append(descriptionLines.get(i)).append("\n");
                }
            }
            result.append("\n");
        }

        if (flags != null) {
            result.append("Flags:\n");
            for (ConfigurationParameter p : flags) {
                final String flagName = p.name + (p.required ? " [REQUIRED]" : "");
                result.append(indent).append(StringUtils.rightPad(flagName, padSize, ' ')).append(p.description).append(
                    "\n");
            }
            result.append("\n");
        }

        if (!examples.isEmpty()) {
            result.append("Example:\n")
                .append(examples.stream().map(e -> indent + e).collect(Collectors.joining("\n")))
                .append("\n\n");
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
     * @return A list of examples of how this plugin is to be used
     */
    default List<String> getExamples(List<String> subCommands) {
        List<String> examples = (subCommands.isEmpty() ? getAllowedSubCommands() : subCommands)
            .stream()
            .map(this::getExamplePerSubCmd)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toCollection(ArrayList::new));

        if (examples.isEmpty() && getExample() != null) {
            examples.add(getExample());
        }

        return examples;
    }

    /**
     * @return An example of how a sub-command of this plugin is to be used
     */
    default String getExamplePerSubCmd(String subCmd) {
        return getExample();
    }

    /**
     * @return A list of sub-commands relevant to this plugin
     */
    default List<String> getAllowedSubCommands() {
        return getFlags() == null ?
            Collections.emptyList() : 
            getFlags().stream().map(ConfigurationParameter::getName).toList();
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

    default boolean inPreview() {
        return false;
    }
}
