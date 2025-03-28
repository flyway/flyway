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
package org.flywaydb.core.internal.resource;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;

/**
 * Represents a resource name, parsed into its components.
 *
 * Versioned and Undo migrations are named in the form prefixVERSIONseparatorDESCRIPTIONsuffix;
 * Repeatable migrations and callbacks are named in the form prefixSeparatorDESCRIPTIONsuffix
 */
@RequiredArgsConstructor
public class ResourceName {
    private final String prefix;
    private final String version;
    private final String separator;
    private final String description;
    private final String rawDescription;
    private final String suffix;
    private final boolean isValid;
    private final String validityMessage;

    /**
     * Construct a result representing an invalid resource name
     *
     * @param message A message explaining the reason the resource name is invalid
     * @return The fully populated parsing result.
     */
    public static ResourceName invalid(String message) {
        return new ResourceName(null, null, null, null, null,
                                null, false, message);
    }

    /**
     * The prefix of the resource (eg. "V" for versioned migrations)
     */
    public String getPrefix() {
        if (!isValid) {
            throw new FlywayException("Cannot access prefix of invalid ResourceNameParseResult\r\n" + validityMessage);
        }
        return prefix;
    }

    private boolean isVersioned() {
        return (!"".equals(version));
    }

    /**
     * The version of the resource (eg. "1.2.3" for versioned migrations), or null for non-versioned
     * resources
     */
    public MigrationVersion getVersion() {
        if (isVersioned()) {
            return MigrationVersion.fromVersion(version);
        } else {
            return null;
        }
    }

    /**
     * The description of the resource
     */
    public String getDescription() {
        if (!isValid) {
            throw new FlywayException("Cannot access description of invalid ResourceNameParseResult\r\n" + validityMessage);
        }
        return description;
    }

    /**
     * The file type suffix of the resource (eg. ".sql" for SQL migration scripts)
     */
    public String getSuffix() {
        if (!isValid) {
            throw new FlywayException("Cannot access suffix of invalid ResourceNameParseResult\r\n" + validityMessage);
        }
        return suffix;
    }

    /**
     * The full name of the resource
     */
    public String getFilenameWithoutSuffix() {
        if (!isValid) {
            throw new FlywayException("Cannot access name of invalid ResourceNameParseResult\r\n" + validityMessage);
        }

        if ("".equals(description)) {
            return prefix + version;
        } else {
            return prefix + version + separator + description;
        }
    }

    /**
     * The filename of the resource as it appears on disk
     */
    public String getFilename() {
        if (!isValid) {
            throw new FlywayException("Cannot access name of invalid ResourceNameParseResult\r\n" + validityMessage);
        }

        if ("".equals(description)) {
            return prefix + version + suffix;
        } else {
            return prefix + version + separator + rawDescription + suffix;
        }
    }

    /**
     * Whether the resource name was successfully parsed.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * If the resource name was not successfully parsed, an explanation of the problem.
     */
    public String getValidityMessage() {
        return validityMessage;
    }
}
