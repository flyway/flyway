package com.google.code.flyway.core;

import java.util.Arrays;

/**
 * A version of a database schema.
 *
 * @author Axel Fontaine
 */
public final class SchemaVersion implements Comparable<SchemaVersion> {
    /**
     * Latest schema version.
     */
    public static final SchemaVersion LATEST = new SchemaVersion();

    /**
     * Is it the latest version?
     */
    private final boolean latest;

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
     * Creates *the* latest version.
     */
    private SchemaVersion() {
        latest = true;
        components = new long[0];
        version = "<< latest >>";
        description = null;
    }

    /**
     * Creates a SchemaVersion using this version string.
     *
     * @param rawVersion The version in one of the following formats: 6, 6.0, 005, 1.2.3.4, 201004200021.
     * @param description The description of this version.
     */
    public SchemaVersion(String rawVersion, String description) {
        latest = false;

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
        this.description = description;
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

        if (latest != that.latest) return false;
        if (!Arrays.equals(components, that.components)) return false;
        return version.equals(that.version);
    }

    @Override
    public int hashCode() {
        int result = (latest ? 1 : 0);
        result = 31 * result + Arrays.hashCode(components);
        result = 31 * result + version.hashCode();
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

        if (latest) {
            return Integer.MAX_VALUE;
        }

        if (o.latest) {
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
