/**
 * Copyright (C) 2010-2011 the original author or authors.
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

import java.sql.Driver;

/**
 * Utility methods for dealing with classes.
 */
public class ClassUtils {
    /**
     * Prevents instantiation.
     */
    private ClassUtils() {
        // Do nothing
    }

    /**
     * Creates a new instance of this class.
     *
     * @param className The fully qualified name of the class to instantiate.
     * @param <T> The type of the new instance.
     *
     * @return The new instance.
     * @throws Exception Thrown when the instantiation failed.
     */
    public static <T> T instantiate(String className) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return (T) Class.forName(className, true, classLoader).newInstance();
    }
}
