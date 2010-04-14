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
    private final String versionStr;

    /**
     * Creates *the* latest version.
     */
    private SchemaVersion() {
        latest = true;
        components = new long[0];
        versionStr = "<< latest >>";
    }

    /**
     * Creates a SchemaVersion using this version string.
     *
     * @param targetVersion The version in one of the following formats:<br/> <ul> <li>major Ex.: 6 (meaning 6.0)</li>
     *                      <li>major.minor Ex.: 6.2</li> <li>'latest' for the latest version available.</li> </ul>
     */
    public SchemaVersion(String targetVersion) {
        latest = false;

        String[] numbers = targetVersion.split("\\.");
        if (numbers == null) {
            numbers = new String[]{targetVersion};
        }

        components = new long[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            components[i] = Long.parseLong(numbers[i]);
        }

        versionStr = targetVersion;
    }

    /**
     * @return The version in printable format. Ex.: 6.2
     */
    @Override
    public String toString() {
        return versionStr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchemaVersion that = (SchemaVersion) o;

        if (latest != that.latest) return false;
        if (!Arrays.equals(components, that.components)) return false;
        return versionStr.equals(that.versionStr);
    }

    @Override
    public int hashCode() {
        int result = (latest ? 1 : 0);
        result = 31 * result + Arrays.hashCode(components);
        result = 31 * result + versionStr.hashCode();
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
