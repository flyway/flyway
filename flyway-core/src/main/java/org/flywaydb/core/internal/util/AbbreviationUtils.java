package org.flywaydb.core.internal.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Various abbreviation-related utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AbbreviationUtils {

    /**
     * Abbreviates this description to a length that will fit in the database.
     *
     * @param description The description to process.
     * @return The abbreviated version.
     */
    public static String abbreviateDescription(String description) {
        if (description == null) {
            return null;
        }

        if (description.length() <= 200) {
            return description;
        }

        return description.substring(0, 197) + "...";
    }

    /**
     * Abbreviates this script to a length that will fit in the database.
     *
     * @param script The script to process.
     * @return The abbreviated version.
     */
    public static String abbreviateScript(String script) {
        if (script == null) {
            return null;
        }

        if (script.length() <= 1000) {
            return script;
        }

        return "..." + script.substring(3, 1000);
    }
}