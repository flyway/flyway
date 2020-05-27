/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.api;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A version of a migration.
 *
 * @author Axel Fontaine
 */
public final class MigrationVersion implements Comparable<MigrationVersion> {
    /**
     * Version for an empty schema.
     */
    public static final MigrationVersion EMPTY = new MigrationVersion(null, "<< Empty Schema >>");

    /**
     * Latest version.
     */
    public static final MigrationVersion LATEST = new MigrationVersion(BigInteger.valueOf(-1), "<< Latest Version >>");

    /**
     * Current version. Only a marker. For the real version use Flyway.info().current() instead.
     */
    public static final MigrationVersion CURRENT = new MigrationVersion(BigInteger.valueOf(-2), "<< Current Version >>");

    /**
     * Regex for matching proper version format
     */
    private static final Pattern SPLIT_REGEX = Pattern.compile("\\.(?=\\d)");

    /**
     * The individual parts this version string is composed of. Ex. 1.2.3.4.0 -> [1, 2, 3, 4, 0]
     */
    private final List<BigInteger> versionParts;

    /**
     * The printable text to represent the version.
     */
    private final String displayText;

    /**
     * Create a MigrationVersion from a version String.
     *
     * @param version The version String. The value {@code current} will be interpreted as MigrationVersion.CURRENT,
     *                a marker for the latest version that has been applied to the database.
     * @return The MigrationVersion
     */
    @SuppressWarnings("ConstantConditions")
    public static MigrationVersion fromVersion(String version) {
        if ("current".equalsIgnoreCase(version)) return CURRENT;
        if ("latest".equalsIgnoreCase(version) || LATEST.getVersion().equals(version)) return LATEST;
        if (version == null) return EMPTY;
        return new MigrationVersion(version);
    }

    /**
     * Creates a Version using this version string.
     *
     * @param version The version in one of the following formats: 6, 6.0, 005, 1.2.3.4, 201004200021. <br/>{@code null}
     *                means that this version refers to an empty schema.
     */
    private MigrationVersion(String version) {
        String normalizedVersion = version.replace('_', '.');
        this.versionParts = tokenize(normalizedVersion);
        this.displayText = normalizedVersion;
    }

    /**
     * Creates a Version using this version string.
     *
     * @param version     The version in one of the following formats: 6, 6.0, 005, 1.2.3.4, 201004200021. <br/>{@code null}
     *                    means that this version refers to an empty schema.
     * @param displayText The alternative text to display instead of the version number.
     */
    private MigrationVersion(BigInteger version, String displayText) {
        this.versionParts = new ArrayList<>();
        this.versionParts.add(version);
        this.displayText = displayText;
    }

    /**
     * @return The textual representation of the version.
     */
    @Override
    public String toString() {
        return displayText;
    }

    /**
     * @return Numeric version as String
     */
    public String getVersion() {
        if (this.equals(EMPTY)) return null;
        if (this.equals(LATEST)) return Long.toString(Long.MAX_VALUE);
        return displayText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MigrationVersion version1 = (MigrationVersion) o;

        return compareTo(version1) == 0;
    }

    @Override
    public int hashCode() {
        return versionParts == null ? 0 : versionParts.hashCode();
    }

    /**
     * Convenience method for quickly checking whether this version is at least as new as this other version.
     *
     * @param otherVersion The other version.
     * @return {@code true} if this version is equal or newer, {@code false} if it is older.
     */
    public boolean isAtLeast(String otherVersion) {
        return compareTo(MigrationVersion.fromVersion(otherVersion)) >= 0;
    }

    /**
     * Convenience method for quickly checking whether this version is newer than this other version.
     *
     * @param otherVersion The other version.
     * @return {@code true} if this version is newer, {@code false} if it is not.
     */
    public boolean isNewerThan(String otherVersion) {
        return compareTo(MigrationVersion.fromVersion(otherVersion)) > 0;
    }

    /**
     * Convenience method for quickly checking whether this major version is newer than this other major version.
     *
     * @param otherVersion The other version.
     * @return {@code true} if this major version is newer, {@code false} if it is not.
     */
    public boolean isMajorNewerThan(String otherVersion) {
        return getMajor().compareTo(MigrationVersion.fromVersion(otherVersion).getMajor()) > 0;
    }

    /**
     * @return The major version.
     */
    public BigInteger getMajor() {
        return versionParts.get(0);
    }

    /**
     * @return The major version as a string.
     */
    public String getMajorAsString() {
        return versionParts.get(0).toString();
    }

    /**
     * @return The minor version as a string.
     */
    public String getMinorAsString() {
        if (versionParts.size() == 1) {
            return "0";
        }
        return versionParts.get(1).toString();
    }

    @Override
    public int compareTo(MigrationVersion o) {
        if (o == null) {
            return 1;
        }

        if (this == EMPTY) {
            if (o == EMPTY) return 0;
            else return -1;
        }

        if (this == CURRENT) {
            return o == CURRENT ? 0 : -1;
        }

        if (this == LATEST) {
            if (o == LATEST) return 0;
            else return 1;
        }

        if (o == EMPTY) {
            return 1;
        }

        if (o == CURRENT) {
            return 1;
        }

        if (o == LATEST) {
            return -1;
        }
        final List<BigInteger> parts1 = versionParts;
        final List<BigInteger> parts2 = o.versionParts;
        int largestNumberOfParts = Math.max(parts1.size(), parts2.size());
        for (int i = 0; i < largestNumberOfParts; i++) {
            final int compared = getOrZero(parts1, i).compareTo(getOrZero(parts2, i));
            if (compared != 0) {
                return compared;
            }
        }
        return 0;
    }

    private BigInteger getOrZero(List<BigInteger> elements, int i) {
        return i < elements.size() ? elements.get(i) : BigInteger.ZERO;
    }

    /**
     * Splits this string into list of Long
     *
     * @param versionStr The string to split.
     * @return The resulting array.
     */
    private List<BigInteger> tokenize(String versionStr) {
        List<BigInteger> parts = new ArrayList<>();
        for (String part : SPLIT_REGEX.split(versionStr)) {
            parts.add(toBigInteger(versionStr, part));
        }

        for (int i = parts.size() - 1; i > 0; i--) {
            if (!parts.get(i).equals(BigInteger.ZERO)) {
                break;
            }
            parts.remove(i);
        }

        return parts;
    }

    private BigInteger toBigInteger(String versionStr, String part) {
        try {
            return new BigInteger(part);
        } catch (NumberFormatException e) {
            throw new FlywayException("Version may only contain 0..9 and . (dot). Invalid version: " + versionStr);
        }
    }
}