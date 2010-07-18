/**
 * Copyright (C) 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.flyway.core.migration;

import java.util.Arrays;

/**
 * A version of a database schema.
 *
 * @author Axel Fontaine
 */
public final class SchemaVersion implements Comparable<SchemaVersion> {
    /**
     * Schema version for an empty schema.
     */
    public static final SchemaVersion EMPTY = new SchemaVersion("<< empty schema >>");

    /**
     * Latest schema version.
     */
    public static final SchemaVersion LATEST = new SchemaVersion("<< latest >>");

    /**
     * The version components. These are the numbers of the version. ([major, minor, patch, ...]) At least one component must be present.
     */
    private final long[] components;

    /**
     * The printable version.
     */
    private final String version;

    /**
     * The description of this version.
     */
    private final String description;

    /**
     * Creates a special version. For internal use only.
     *
     * @param version The version to display.
     */
    private SchemaVersion(String version) {
        this.version = version;
        components = new long[0];
        description = null;
    }

    /**
     * Creates a SchemaVersion using this version string.
     *
     * @param rawVersion The version in one of the following formats: 6, 6.0, 005, 1.2.3.4, 201004200021.
     * @param description The description of this version.
     */
    public SchemaVersion(String rawVersion, String description) {
        this.description = description;

        String[] numbers = rawVersion.split("\\.");
        if (numbers == null) {
            numbers = new String[]{rawVersion};
        }

        String versionStr = "";
        components = new long[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            components[i] = Long.parseLong(numbers[i]);

            if (i > 0) {
                versionStr += ".";
            }
            versionStr += components[i];
        }

        version = versionStr;
    }

    /**
     * @return The version in printable format. Ex.: 6.2
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return The description of this version.
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        if (description == null) {
            return version;
        }

        return version + " (" + description + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchemaVersion that = (SchemaVersion) o;

        if (!Arrays.equals(components, that.components)) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        return version.equals(that.version);
    }

    @Override
    public int hashCode() {
        int result = components != null ? Arrays.hashCode(components) : 0;
        result = 31 * result + version.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(SchemaVersion o) {
        if (o == null) {
            return 1;
        }

        if (equals(o)) {
            return 0;
        }

        if (equals(EMPTY)) {
            return Integer.MIN_VALUE;
        }

        if (equals(LATEST)) {
            return Integer.MAX_VALUE;
        }

        if (o.equals(EMPTY)) {
            return Integer.MAX_VALUE;
        }

        if (o.equals(LATEST)) {
            return Integer.MIN_VALUE;
        }

        int maxComponents = Math.max(components.length, o.components.length);
        for (int i = 0; i < maxComponents; i++) {
            long myComponent = 0;
            if (i < components.length) {
                myComponent = components[i];
            }

            long otherComponent = 0;
            if (i < o.components.length) {
                otherComponent = o.components[i];
            }

            if (myComponent > otherComponent) {
                return 1;
            }
            if (myComponent < otherComponent) {
                return -1;
            }
        }

        return 0;
    }
}
