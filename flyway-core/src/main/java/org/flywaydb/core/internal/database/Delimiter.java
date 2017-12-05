/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.database;

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
        return delimiter;
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
