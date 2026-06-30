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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.flywaydb.core.internal.configuration.HelpText;
import org.flywaydb.core.internal.util.Pair;

public interface PluginMetadata extends Plugin {
    /**
     * @return The help text for this plugin
     */
    default String getHelpText() {
        return getHelpText(Collections.emptyList());
    }

    default String getHelpText(List<String> subCommands) {
        final String description = getDescription();
        List<ConfigurationParameter> configurationParameters = getConfigurationParameters();
        List<ConfigurationParameter> flags = getFlags();

        final List<String> filteredSubCommands = subCommands.stream()
            .filter(getAllowedSubCommands()::contains)
            .toList();

        if (configurationParameters != null && !filteredSubCommands.isEmpty()) {
            configurationParameters = configurationParameters.stream()
                .filter(x -> x.parentSubCommands.stream().anyMatch(filteredSubCommands::contains))
                .toList();
        }

        if (flags != null && !filteredSubCommands.isEmpty()) {
            flags = flags.stream()
                .filter(f -> filteredSubCommands.contains(f.name))
                .toList();
        }

        final List<String> examples = getExamples(filteredSubCommands);


        final HelpText helpText = new HelpText();
        helpText.setParameters(configurationParameters);
        helpText.setFlags(flags);
        helpText.setDescription(description);
        helpText.setExamples(examples);
        helpText.setDocumentationLink(getDocumentationLink());

        if (inPreview()) {
            helpText.setAdditionalInfo("(In preview)");
        }

        return helpText.getText();
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
