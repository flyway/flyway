/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util;

/**
 * Utility class for dealing with exceptions.
 */
public class ExceptionUtils {
    /**
     * Prevents instantiation.
     */
    private ExceptionUtils() {
        //Do nothing
    }

    /**
     * Returns the root cause of this throwable.
     *
     * @param throwable The throwable to inspect.
     *
     * @return The root cause or the throwable itself if it doesn't have a cause.
     */
    public static Throwable getRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        Throwable cause = throwable;
        Throwable rootCause;
        while ((rootCause = cause.getCause()) != null) {
            cause = rootCause;
        }

        return cause;
    }
}
