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
package org.flywaydb.core.internal.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.extensibility.ConfigurationParameter;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

@Getter
@Setter
public class HelpText {
    private static final String INDENT = "    ";

    List<String> usage;
    List<Pair<String, String>> commands;
    List<? extends ConfigurationParameter> parameters;
    List<? extends ConfigurationParameter> flags;
    List<String> examples;
    String description;
    String documentationLink;
    String additionalInfo;

    public String getText() {
        final StringBuilder result = new StringBuilder();

        addUsageText(result);

        addAdditionalInfoText(result);

        addDescriptionText(result);

        final int padSize = getPadSize();

        addCommandsText(result, padSize);

        addParametersText(result, padSize);

        addFlagsText(result, padSize);

        addExamplesText(result);

        addDocumentationText(result);

        return result.toString();
    }

    private void addUsageText(final StringBuilder result) {
        if (usage != null) {
            result.append("Usage:").append(System.lineSeparator());
            usage.forEach(line -> result.append(INDENT).append(line).append(System.lineSeparator()));
            result.append(System.lineSeparator());
        }
    }

    private void addAdditionalInfoText(final StringBuilder result) {
        if (additionalInfo != null) {
            result.append(additionalInfo).append(System.lineSeparator()).append(System.lineSeparator());
        }
    }

    private void addDocumentationText(final StringBuilder result) {
        if (documentationLink != null) {
            result.append("More info at ").append(documentationLink).append(System.lineSeparator());
        }
    }

    private void addExamplesText(final StringBuilder result) {
        if (examples != null && !examples.isEmpty()) {
            result.append("Example:")
                .append(System.lineSeparator())
                .append(examples.stream().map(e -> INDENT + e).collect(Collectors.joining(System.lineSeparator())))
                .append(System.lineSeparator())
                .append(System.lineSeparator());
        }
    }

    private void addDescriptionText(final StringBuilder result) {
        if (description != null) {
            result.append("Description:").append(System.lineSeparator());
            Arrays.stream(description.split(System.lineSeparator()))
                .map(String::trim)
                .forEach(line -> result.append(INDENT).append(line).append(System.lineSeparator()));
            result.append(System.lineSeparator());
        }
    }

    private void addFlagsText(final StringBuilder result, final int padSize) {
        if (flags != null) {
            result.append("Flags:").append(System.lineSeparator());
            for (final ConfigurationParameter p : flags) {
                final String flagName = p.name + (p.required ? " [REQUIRED]" : "");
                result.append(INDENT)
                    .append(StringUtils.rightPad(flagName, padSize, ' '))
                    .append(p.description)
                    .append(System.lineSeparator());
            }
            result.append(System.lineSeparator());
        }
    }

    private void addCommandsText(final StringBuilder result, final int padSize) {
        if (commands != null) {

            result.append("Commands:").append(System.lineSeparator());
            for (final Pair<String, String> command : commands) {
                final List<String> lines = Arrays.stream(command.getRight().split(System.lineSeparator()))
                    .map(String::trim)
                    .toList();
                result.append(INDENT)
                    .append(StringUtils.rightPad(command.getLeft(), padSize, ' '))
                    .append(lines.get(0))
                    .append(System.lineSeparator());
                for (int i = 1; i < lines.size(); i++) {
                    result.append(INDENT)
                        .append(" ".repeat(padSize))
                        .append(lines.get(i))
                        .append(System.lineSeparator());
                }
            }
            result.append(System.lineSeparator());
        }
    }

    private void addParametersText(final StringBuilder result, final int padSize) {
        if (parameters != null) {
            result.append("Configuration parameters: (Format: -key=value)").append(System.lineSeparator());
            for (final ConfigurationParameter p : parameters) {
                final String parameterName = p.name.startsWith("flyway.")
                    ? p.name.substring("flyway.".length())
                    : p.name;
                final String fullParameter = parameterName + (p.required ? " [REQUIRED]" : "");

                if (p.description == null) {
                    result.append(INDENT).append(fullParameter).append(System.lineSeparator());
                    continue;
                }

                result.append(INDENT).append(StringUtils.rightPad(fullParameter, padSize, ' '));

                final String descriptionPadding = " ".repeat(INDENT.length() + padSize);
                final List<String> descriptionLines = Arrays.stream(p.description.split(System.lineSeparator()))
                    .toList();

                result.append(descriptionLines.get(0)).append(System.lineSeparator());
                for (int i = 1; i < descriptionLines.size(); i++) {
                    result.append(descriptionPadding).append(descriptionLines.get(i)).append(System.lineSeparator());
                }
            }
            result.append(System.lineSeparator());
        }
    }

    private int getPadSize() {
        int padSize = 0;
        if (parameters != null) {
            padSize = parameters.stream()
                .filter(p -> p.description != null)
                .map(p -> p.name + (p.required ? " [REQUIRED]" : ""))
                .mapToInt(String::length)
                .max()
                .orElse(0) + 2;
        }
        if (flags != null) {
            padSize = Math.max(padSize,
                flags.stream()
                    .filter(p -> p.description != null)
                    .map(p -> p.name + (p.required ? " [REQUIRED]" : ""))
                    .mapToInt(String::length)
                    .max()
                    .orElse(0) + 2);
        }

        if (commands != null) {
            padSize = Math.max(padSize,
                commands.stream().map(Pair::getLeft).mapToInt(String::length).max().orElse(0) + 2);
        }
        return padSize;
    }
}
