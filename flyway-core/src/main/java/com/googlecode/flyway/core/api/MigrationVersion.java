/**
 * Copyright 2010-2013 Axel Fontaine and the many contributors.
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
package com.googlecode.flyway.core.api;

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
    public static final MigrationVersion LATEST = new MigrationVersion(Long.MAX_VALUE, "<< Latest Version >>");

    /**
     * Compiled pattern for matching proper version format
     */
    private static Pattern splitPattern = Pattern.compile("\\.(?=\\d)");

    /**
     * The version.
     */
    private final List<Long> version;

    /**
     * The printable text to represent the version.
     */
    private final String displayText;

    /**
     * Creates a Version using this version string.
     *
     * @param version The version in one of the following formats: 6, 6.0, 005, 1.2.3.4, 201004200021. <br/>{@code null}
     *                means that this version refers to an empty schema.
     */
    public MigrationVersion(String version) {
        String normalizedVersion = version.replace('_', '.');
        this.version = tokenizeToLongs(normalizedVersion);
        this.displayText = normalizedVersion;
    }

    /**
     * Creates a Version using this version string.
     *
     * @param version     The version in one of the following formats: 6, 6.0, 005, 1.2.3.4, 201004200021. <br/>{@code null}
     *                    means that this version refers to an empty schema.
     * @param displayText The alternative text to display instead of the version number.
     */
    private MigrationVersion(Long version, String displayText) {
        this.version = new ArrayList<Long>();
        this.version.add(version);
        this.displayText = displayText;
    }

    /**
     * @return The individual elements this version string is composed of. Ex. 1.2.3.4.0 -> [1, 2, 3, 4, 0]
     */
    private List<Long> getElements() {
        return version;
    }

    /**
     * @return The textual representation of the version.
     */
    @Override
    public String toString() {
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
        return version == null ? 0 : version.hashCode();
    }

    public int compareTo(MigrationVersion o) {
        if (o == null) {
            return 1;
        }

        if (this == EMPTY) {
            return o == EMPTY ? 0 : Integer.MIN_VALUE;
        }

        if (this == LATEST) {
            return o == LATEST ? 0 : Integer.MAX_VALUE;
        }

        if (o == EMPTY) {
            return Integer.MAX_VALUE;
        }

        if (o == LATEST) {
            return Integer.MIN_VALUE;
        }
        final List<Long> elements1 = getElements();
        final List<Long> elements2 = o.getElements();
        int largestNumberOfElements = Math.max(elements1.size(), elements2.size());
        for (int i = 0; i < largestNumberOfElements; i++) {
            final int compared = getOrZero(elements1, i).compareTo(getOrZero(elements2, i));
            if (compared != 0) {
                return compared;
            }
        }
        return 0;
    }

    private Long getOrZero(List<Long> elements, int i) {
        return i < elements.size() ? elements.get(i) : 0;
    }

    /**
     * Splits this string into list of Long
     *
     * @param str        The string to split.
     * @return The resulting array.
     */
    private List<Long> tokenizeToLongs(String str) {
        if (str == null) {
            return null;
        }
        List<Long> numbers = new ArrayList<Long>();
        for (String number : splitPattern.split(str)) {
            try {
                numbers.add(Long.valueOf(number));
            } catch (NumberFormatException e) {
                throw new FlywayException(
                    "Invalid version containing non-numeric characters. Only 0..9 and . are allowed. Invalid version: "
                    + str);
            }
        }
        for (int i = numbers.size() - 1; i > 0; i--) {
            if (numbers.get(i) != 0) break;
            numbers.remove(i);
        }
        return numbers;
    }
}
