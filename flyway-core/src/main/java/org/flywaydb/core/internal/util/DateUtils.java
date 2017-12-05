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
package org.flywaydb.core.internal.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility methods for dealing with dates.
 */
public class DateUtils {
    /**
     * Prevents instantiation.
     */
    private DateUtils() {
        // Do nothing
    }

    /**
     * Formats this date in the standard ISO format.
     *
     * @param date The date to format.
     * @return The date in ISO format. An empty string if the date is null.
     */
    public static String formatDateAsIsoString(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
