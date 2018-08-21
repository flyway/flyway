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
package org.flywaydb.core.internal.logging.android;

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
    public boolean isDebugEnabled() {
        return android.util.Log.isLoggable(TAG, android.util.Log.DEBUG);
    }

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