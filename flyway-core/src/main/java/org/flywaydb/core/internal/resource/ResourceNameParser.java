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
package org.flywaydb.core.internal.resource;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.extensibility.ResourceType;
import org.flywaydb.core.extensibility.ResourceTypeProvider;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.*;

public class ResourceNameParser {
    private final Configuration configuration;
    private final List<Pair<String, ResourceType>> prefixes;

    public ResourceNameParser(Configuration configuration) {
        this.configuration = configuration;
        // Versioned and Undo migrations are named in the form prefixVERSIONseparatorDESCRIPTIONsuffix
        // Repeatable migrations and callbacks are named in the form prefixSeparatorDESCRIPTIONsuffix
        prefixes = populatePrefixes(configuration);
    }

    public ResourceName parse(String resourceName) {
        return parse(resourceName, configuration.getSqlMigrationSuffixes());
    }

    public ResourceName parse(String resourceName, String[] suffixes) {
        // Strip off suffixes
        Pair<String, String> suffixResult = stripSuffix(resourceName, suffixes);

        // Find the appropriate prefix
        Pair<String, ResourceType> prefix = findPrefix(suffixResult.getLeft(), prefixes);
        if (prefix != null) {

            // Strip off prefix
            Pair<String, String> prefixResult = stripPrefix(suffixResult.getLeft(), prefix.getLeft());
            String name = prefixResult.getRight();
            Pair<String, String> splitName = StringUtils.splitAtFirstSeparator(name, configuration.getSqlMigrationSeparator());
            boolean isValid = true;
            String validationMessage = "";
            String exampleDescription = ("".equals(splitName.getRight())) ? "description" : splitName.getRight();

            // Validate the name
            if (!prefix.getRight().isVersioned()) {
                // Must not have a version (that is, something before the separator)
                if (!"".equals(splitName.getLeft())) {
                    isValid = false;
                    validationMessage = "Invalid repeatable migration / callback name format: " + resourceName
                            + " (It cannot contain a version and should look like this: "
                            + prefixResult.getLeft() + configuration.getSqlMigrationSeparator() + exampleDescription + suffixResult.getRight() + ")";
                }
            } else {
                // Must have a version (that is, something before the separator)
                if ("".equals(splitName.getLeft())) {
                    isValid = false;
                    validationMessage = "Invalid versioned migration name format: " + resourceName
                            + " (It must contain a version and should look like this: "
                            + prefixResult.getLeft() + "1.2" + configuration.getSqlMigrationSeparator() + exampleDescription + suffixResult.getRight() + ")";
                } else {
                    // ... and that must be a legitimate version
                    try {
                        MigrationVersion.fromVersion(splitName.getLeft());
                    } catch (Exception e) {
                        isValid = false;
                        validationMessage = "Invalid versioned migration name format: " + resourceName
                                + " (could not recognise version number " + splitName.getLeft() + ")";
                    }
                }
            }

            String description = splitName.getRight().replace("_", " ");
            return new ResourceName(prefixResult.getLeft(), splitName.getLeft(),
                                    configuration.getSqlMigrationSeparator(), description, splitName.getRight(), suffixResult.getRight(),
                                    isValid, validationMessage);
        }

        // Didn't match any prefix
        return ResourceName.invalid("Unrecognised migration name format: " + resourceName);
    }

    private Pair<String, ResourceType> findPrefix(String nameWithoutSuffix, List<Pair<String, ResourceType>> prefixes) {
        for (Pair<String, ResourceType> prefix : prefixes) {
            if (nameWithoutSuffix.startsWith(prefix.getLeft())) {
                return prefix;
            }
        }
        return null;
    }

    private Pair<String, String> stripSuffix(String name, String[] suffixes) {
        for (String suffix : suffixes) {
            if (name.toUpperCase().endsWith(suffix.toUpperCase())) {
                return Pair.of(name.substring(0, name.length() - suffix.length()), name.substring(name.length() - suffix.length()));
            }
        }
        return Pair.of(name, "");
    }

    private Pair<String, String> stripPrefix(String fileName, String prefix) {
        if (fileName.startsWith(prefix)) {
            return Pair.of(prefix, fileName.substring(prefix.length()));
        }
        return null;
    }

    private List<Pair<String, ResourceType>> populatePrefixes(Configuration configuration) {
        List<Pair<String, ResourceType>> prefixes = new ArrayList<>();

        configuration.getPluginRegister().getPlugins(ResourceTypeProvider.class)
                .forEach(resourceTypeProvider -> prefixes.addAll(resourceTypeProvider.getPrefixTypePairs(configuration)));

        Comparator<Pair<String, ResourceType>> prefixComparator = (p1, p2) -> {
            // Sort most-hard-to-match first; that is, in descending order of prefix length
            return p2.getLeft().length() - p1.getLeft().length();
        };

        prefixes.sort(prefixComparator);
        return prefixes;
    }
}
