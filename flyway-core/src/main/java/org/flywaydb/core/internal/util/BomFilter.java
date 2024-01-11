package org.flywaydb.core.internal.util;

public class BomFilter {
    private static final char BOM = '\ufeff';

    /**
     * Determine if this char is a UTF-8 Byte Order Mark
     *
     * @param c The char to check
     * @return Whether this char is a UTF-8 Byte Order Mark
     */
    public static boolean isBom(char c) {
        return c == BOM;
    }

    /**
     * Removes the UTF-8 Byte Order Mark from the start of a string if present.
     *
     * @param s The string
     * @return The string without a Byte Order Mark at the start
     */
    public static String FilterBomFromString(String s) {
        if (s.isEmpty()) {
            return s;
        }

        if (isBom(s.charAt(0))) {
            return s.substring(1);
        }

        return s;
    }
}