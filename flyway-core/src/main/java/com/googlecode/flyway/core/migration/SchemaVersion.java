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

import com.googlecode.flyway.core.util.StringUtils;

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
     * The printable version.
     */
    private final String rawVersion;

    /**
     * The description of this version.
     */
    private final String description;

    /**
     * Creates a special version. For internal use only.
     *
     * @param rawVersion The version to display.
     */
    private SchemaVersion(String rawVersion) {
        this.rawVersion = rawVersion;
        description = null;
    }

    /**
     * Creates a SchemaVersion using this version string.
     *
     * @param rawVersion  The version in one of the following formats: 6, 6.0, 005, 1.2.3.4, 201004200021.
     * @param description The description of this version.
     */
    public SchemaVersion(String rawVersion, String description) {
        this.description = description;
        this.rawVersion = rawVersion;
    }

    /**
     * evaluates a normalized version without leading or trailing '0'
     * @return version without leading 0
     */
    private String getNormalizedVersion() {
        String[] numbers = getElements();
        if (numbers == null) {
            numbers = new String[]{rawVersion};
        }

        String versionStr = "";
        for (int i = 0; i < numbers.length; i++) {
            if (i > 0) {
                //if (justTrailingNull(numbers, i)) {
                //    break;
                //}
                versionStr += ".";
            }
            final String s = numbers[i];
            if (StringUtils.isNumeric(s)) {
                versionStr += Long.parseLong(s);
            } else {
                versionStr += s;
            }
        }
        return versionStr;
    }

    /**
     * @return The version in printable format. Ex.: 6.2
     */
    public String getVersion() {
        return getNormalizedVersion();
    }

    /**
     * @return The description of this version.
     */
    public String getDescription() {
        return description;
    }

    private String[] getElements() {
        return StringUtils.split(rawVersion, "_.-");
    }

    @Override
    public String toString() {
        if (description == null) {
            return rawVersion;
        }

        return rawVersion + " (" + description + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchemaVersion that = (SchemaVersion) o;

        //noinspection SimplifiableIfStatement
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        return rawVersion.equals(that.rawVersion);
    }

    @Override
    public int hashCode() {
        int result = rawVersion.hashCode();
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
        final String[] elements1 = getElements();
        final String[] elements2 = o.getElements();
        int max = Math.min(elements1.length, elements2.length);
        for (int i = 0; i < max; i++) {
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
        final int result = new Integer(elements1.length).compareTo(elements2.length);
        if (result > 0 && justTrailingNull(elements1, max)) {
            return 0;
        }
        if (result < 0 && justTrailingNull(elements2, max)) {
            return 0;
        }
        return result;
    }

    private boolean justTrailingNull(String[] elements1, int max) {
        for (int i = max; i < elements1.length; i++) {
            String element = elements1[i];
            if (!StringUtils.isNumeric(element) || !Long.valueOf(element).equals(0L)) {
                return false;
            }
        }
        return true;
    }


}
