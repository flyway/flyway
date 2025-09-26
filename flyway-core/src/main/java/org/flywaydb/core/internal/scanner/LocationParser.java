/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
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
package org.flywaydb.core.internal.scanner;

import static org.flywaydb.core.internal.scanner.ClasspathLocationHandler.CLASSPATH_PREFIX;

import java.util.function.Function;
import java.util.regex.Pattern;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.Location;

public class LocationParser {
    public static Location parseLocation(final String descriptor) {
        final String normalizedDescriptor = descriptor.trim();

        final String prefix;
        final String rawPath;
        if (normalizedDescriptor.contains(":")) {
            prefix = normalizedDescriptor.substring(0, normalizedDescriptor.indexOf(":") + 1);
            rawPath = normalizedDescriptor.substring(normalizedDescriptor.indexOf(":") + 1);
        } else {
            prefix = CLASSPATH_PREFIX;
            rawPath = normalizedDescriptor;
        }

        final ReadOnlyLocationHandler locationHandler = Flyway.configure()
            .getPluginRegister()
            .getInstancesOf(ReadOnlyLocationHandler.class)
            .stream()
            .filter(x -> x.canHandlePrefix(prefix))
            .findFirst()
            .orElseThrow(() -> new FlywayException(
                "Unknown prefix for location (should be one of filesystem:, classpath:, gcs:, or s3:): "
                    + normalizedDescriptor));

        return locationHandler.handlesWildcards() && containsWildcards(rawPath) ? parseWildcardLocation(rawPath,
            prefix,
            locationHandler.getPathSeparator(),
            locationHandler::normalizePath) : Location.fromPath(prefix, locationHandler.normalizePath(rawPath));
    }

    private static boolean containsWildcards(final String rawPath) {
        return rawPath.contains("*") || rawPath.contains("?");
    }

    /**
     * Process the rawPath into a rootPath and a regex. Supported wildcards: **: Match any 0 or more directories *:
     * Match any sequence of non-separator characters ?: Match any single character
     */
    private static Location parseWildcardLocation(final String rawPath,
        final String prefix,
        final String separator,
        final Function<? super String, String> normalizePath) {
        // we need to figure out the root, and create the regex
        final String escapedSeparator = separator.replace("\\", "\\\\").replace("/", "\\/");

        // split on either of the path separators
        final String[] pathSplit = rawPath.split("[\\\\/]");

        final StringBuilder rootPart = new StringBuilder();
        final StringBuilder patternPart = new StringBuilder();

        boolean endsInFile = false;
        boolean skipSeparator = false;
        boolean inPattern = false;
        for (final String pathPart : pathSplit) {
            endsInFile = false;

            if (pathPart.contains("*") || pathPart.contains("?")) {
                inPattern = true;
            }

            if (inPattern) {
                if (skipSeparator) {
                    skipSeparator = false;
                } else {
                    patternPart.append("/");
                }

                String regex;
                if ("**".equals(pathPart)) {
                    regex = "([^/]+/)*?";

                    // this pattern contains the ending separator, so make sure we skip appending it after
                    skipSeparator = true;
                } else {
                    endsInFile = pathPart.contains(".");

                    regex = pathPart;
                    regex = regex.replace(".", "\\.");
                    regex = regex.replace("?", "[^/]");
                    regex = regex.replace("*", "[^/]+?");
                }

                patternPart.append(regex);
            } else {
                rootPart.append(separator).append(pathPart);
            }
        }

        // We always append a separator before each part, so ensure we skip it when setting the final rootPath
        final String rootPath = normalizePath.apply(!rootPart.isEmpty() ? rootPart.substring(1) : "");

        // Again, skip first separator
        String pattern = patternPart.substring(1);

        // Replace the temporary / with the actual escaped separator
        pattern = pattern.replace("/", escapedSeparator);

        // Prepend the rootPath if it is non-empty
        if (!rootPart.isEmpty()) {
            pattern = rootPath.replace(separator, escapedSeparator) + escapedSeparator + pattern;
        }

        // if the path did not end in a file, then append the file match pattern
        if (!endsInFile) {
            pattern = pattern + escapedSeparator + "(?<relpath>.*)";
        }

        final Pattern pathRegex = Pattern.compile(pattern);
        return Location.fromWildcardPath(prefix, rootPath, rawPath, pathRegex);
    }
}
