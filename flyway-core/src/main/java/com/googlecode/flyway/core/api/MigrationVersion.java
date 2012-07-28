/**
 * Copyright (C) 2010-2012 the original author or authors.
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

import com.googlecode.flyway.core.util.StringUtils;

/**
 * A version of a migration.
 *
 * @author Axel Fontaine
 */
public final class MigrationVersion implements Comparable<MigrationVersion> {
    /**
     * Version for an empty schema.
     */
    public static final MigrationVersion EMPTY = new MigrationVersion(null);

    /**
     * Latest version.
     */
    public static final MigrationVersion LATEST = new MigrationVersion(Long.toString(Long.MAX_VALUE));

    /**
     * The printable version.
     */
    private final String version;

    /**
     * Creates a Version using this version string.
     *
     * @param version The version in one of the following formats: 6, 6.0, 005, 1.2.3.4, 201004200021. <br/>{@code null}
     *                means that this version refers to an empty schema.
     */
    public MigrationVersion(String version) {
        this.version = version;
    }

    /**
     * @return The individual elements this version string is composed of. Ex. 1.2.3.4.0 -> [1, 2, 3, 4, 0]
     */
    private String[] getElements() {
        return StringUtils.tokenizeToStringArray(version, ".-");
    }

    /**
     * @return The version string
     */
    @Override
    public String toString() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MigrationVersion that = (MigrationVersion) o;
        return compareTo(that) == 0;
    }

    @Override
    public int hashCode() {
        return version.hashCode();
    }

    public int compareTo(MigrationVersion o) {
        if (o == null) {
            return 1;
        }

        if (this == EMPTY) {
            return Integer.MIN_VALUE;
        }

        if (this == LATEST) {
            return Integer.MAX_VALUE;
        }

        if (o == EMPTY) {
            return Integer.MAX_VALUE;
        }

        if (o == LATEST) {
            return Integer.MIN_VALUE;
        }
        final String[] elements1 = getElements();
        final String[] elements2 = o.getElements();
        int smallestNumberOfElements = Math.min(elements1.length, elements2.length);
        for (int i = 0; i < smallestNumberOfElements; i++) {
            String element1 = elements1[i];
            String element2 = elements2[i];
            final int compared;
            if (StringUtils.isNumeric(element1) && StringUtils.isNumeric(element2)) {
                compared = Long.valueOf(element1).compareTo(Long.valueOf(element2));
            } else {
                compared = element1.compareTo(element2);
            }
            if (compared != 0) {
                return compared;
            }
        }

        final int lengthDifference = elements1.length - elements2.length;
        if (lengthDifference > 0 && onlyTrailingZeroes(elements1, smallestNumberOfElements)) {
            return 0;
        }
        if (lengthDifference < 0 && onlyTrailingZeroes(elements2, smallestNumberOfElements)) {
            return 0;
        }
        return lengthDifference;
    }

    /**
     * Checks whether the elements at this position and beyond are only zeroes or not.
     *
     * @param elements The elements to check.
     * @param position The position where to start checking.
     *
     * @return {@code true} if they are all zeroes, {@code false} if not.
     */
    private boolean onlyTrailingZeroes(String[] elements, int position) {
        for (int i = position; i < elements.length; i++) {
            String element = elements[i];
            if (!StringUtils.isNumeric(element) || !Long.valueOf(element).equals(0L)) {
                return false;
            }
        }
        return true;
    }
}
