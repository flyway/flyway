/**
 * Copyright (C) 2010-2012 the original author or authors.
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
package com.googlecode.flyway.core.util;

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
     * @return The root cause or {@code null} if the throwable is null or doesn't have a cause.
     */
    public static Throwable getRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        Throwable cause = throwable.getCause();
        Throwable rootCause = null;
        while (cause != null) {
            rootCause = cause;
            cause = cause.getCause();
        }

        return rootCause;
    }
}
