package org.flywaydb.core.internal.util;

public final class TimeSpecifierUtils {
    @SuppressWarnings("MagicNumber")
    public static Long parseToSeconds(final String value) {
        if (!StringUtils.hasText(value)) {
            throw new NumberFormatException("time specifier is empty");
        }

        final char specifier = value.charAt(value.length() - 1);
        if (Character.isDigit(specifier)) {
            return Long.parseLong(value); // No specifier assumes seconds
        }

        final long number = Long.parseLong(value.substring(0, value.length() - 1));
        switch (specifier) {
            case 's' -> {
                return number;
            }
            case 'm' -> {
                return number * 60;
            }
            case 'h' -> {
                return number * 60 * 60;
            }
            case 'd' -> {
                return number * 60 * 60 * 24;
            }
        }
        throw new NumberFormatException("unknown time specifier " + specifier);
    }

    public static Long tryParseToSeconds(final String value) {
        try {
            return value != null ? parseToSeconds(value) : null;
        } catch (final NumberFormatException e) {
            return null;
        }
    }
}