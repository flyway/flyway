/*
 * Copyright 2010-2017 Boxfuse GmbH
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
package org.flywaydb.core.internal.util.scanner;

/**
 * A loadable resource.
 */
public interface Resource {
    /**
     * @return The location of the resource on the classpath (path and filename).
     */
    String getLocation();

    /**
     * Retrieves the location of this resource on disk.
     *
     * @return The location of this resource on disk.
     */
    String getLocationOnDisk();

    /**
     * Loads this resource as a string.
     *
     * @param encoding The encoding to use.
     * @return The string contents of the resource.
     */
    String loadAsString(String encoding);

    /**
     * Loads this resource as a byte array.
     *
     * @return The contents of the resource.
     */
    byte[] loadAsBytes();

    /**
     * @return The filename of this resource, without the path.
     */
    String getFilename();
}
