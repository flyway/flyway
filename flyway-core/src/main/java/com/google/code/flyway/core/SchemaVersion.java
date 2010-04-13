package com.google.code.flyway.core;

/**
 * A version of a database schema.
 *
 * @author Axel Fontaine
 */
public final class SchemaVersion implements Comparable<SchemaVersion> {
    /**
     * The latest version string.
     */
    private static final String LATEST_STR = "latest";

    /**
     * Latest schema version.
     */
    public static final SchemaVersion LATEST = new SchemaVersion(LATEST_STR);

    /**
     * Is it the latest version?
     */
    private final boolean latest;

    /**
     * The major version.
     */
    private final int major;

    /**
     * The minor version.
     */
    private final int minor;

    /**
     * Creates a SchemaVersion composed of a major and a minor version number.
     *
     * @param major The major version.
     * @param minor The minor version.
     */
    public SchemaVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
        latest = false;
    }

    /**
     * Creates a SchemaVersion using this version string.
     *
     * @param targetVersion The version in one of the following formats:<br/> <ul> <li>major Ex.: 6 (meaning 6.0)</li>
     *                      <li>major.minor Ex.: 6.2</li> <li>'latest' for the latest version available.</li> </ul>
     */
    public SchemaVersion(String targetVersion) {
        if (LATEST_STR.equals(targetVersion)) {
            major = Integer.MAX_VALUE;
            minor = Integer.MAX_VALUE;
            latest = true;
        } else {
            String[] versions = targetVersion.split("\\.");
            major = Integer.parseInt(versions[0]);
            if (versions.length == 1) {
                minor = 0;
            } else {
                minor = Integer.parseInt(versions[1]);
            }
            latest = false;
        }
    }

    /**
     * @return The version in the format major.minor. Ex.: 6.2
     */
    @Override
    public String toString() {
        if (latest) {
            return LATEST_STR;
        }

        return major + "." + minor;
    }

    /**
     * @return The major version.
     */
    public int getMajor() {
        return major;
    }

    /**
     * @return The minor version.
     */
    public int getMinor() {
        return minor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (latest ? 1231 : 1237);
        result = prime * result + major;
        result = prime * result + minor;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SchemaVersion other = (SchemaVersion) obj;
        if (latest != other.latest)
            return false;
        if (major != other.major)
            return false;
        return minor == other.minor;
    }

    @Override
    public int compareTo(SchemaVersion o) {
        if (equals(o)) {
            return 0;
        }

        if (latest) {
            return Integer.MAX_VALUE;
        }

        if (o.latest) {
            return Integer.MIN_VALUE;
        }

        int majorDiff = major - o.major;
        if (majorDiff != 0) {
            return majorDiff;
        }

        return minor - o.minor;
    }
}
