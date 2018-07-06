/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.sqlscript;

/**
 * Represents a sql statement delimiter.
 */
public class Delimiter {
    public static final Delimiter SEMICOLON = new Delimiter(";", false);
    public static final Delimiter GO = new Delimiter("GO", true);

    /**
     * The actual delimiter string.
     */
    private final String delimiter;

    /**
     * Whether the delimiter sits alone on a new line or not.
     */
    private final boolean aloneOnLine;

    /**
     * Creates a new delimiter.
     *
     * @param delimiter The actual delimiter string.
     * @param aloneOnLine Whether the delimiter sits alone on a new line or not.
     */
    public Delimiter(String delimiter, boolean aloneOnLine) {
        this.delimiter = delimiter;
        this.aloneOnLine = aloneOnLine;
    }

    /**
     * @return The actual delimiter string.
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * @return Whether the delimiter sits alone on a new line or not.
     */
    public boolean isAloneOnLine() {
        return aloneOnLine;
    }

    @Override
    public String toString() {
        return (aloneOnLine ? "\n" : "") + delimiter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Delimiter delimiter1 = (Delimiter) o;

        return aloneOnLine == delimiter1.aloneOnLine && delimiter.equals(delimiter1.delimiter);
    }

    @Override
    public int hashCode() {
        int result = delimiter.hashCode();
        result = 31 * result + (aloneOnLine ? 1 : 0);
        return result;
    }
}