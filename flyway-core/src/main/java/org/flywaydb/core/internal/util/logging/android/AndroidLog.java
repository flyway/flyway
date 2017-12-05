/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util.logging.android;

import org.flywaydb.core.api.logging.Log;

/**
 * Wrapper for an Android logger.
 */
public class AndroidLog implements Log {
    /**
     * The tag in the Android logs.
     */
    private static final String TAG = "Flyway";

    @Override
    public void debug(String message) {
        android.util.Log.d(TAG, message);
    }

    @Override
    public void info(String message) {
        android.util.Log.i(TAG, message);
    }

    @Override
    public void warn(String message) {
        android.util.Log.w(TAG, message);
    }

    @Override
    public void error(String message) {
        android.util.Log.e(TAG, message);
    }

    @Override
    public void error(String message, Exception e) {
        android.util.Log.e(TAG, message, e);
    }
}
