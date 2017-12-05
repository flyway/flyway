/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 */
package org.flywaydb.core.internal.util.logging.android;

import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogCreator;

/**
 * Log Creator for Android.
 */
public class AndroidLogCreator implements LogCreator {
    public Log createLogger(Class<?> clazz) {
        return new AndroidLog();
    }
}
