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
package com.googlecode.flyway.core.util.scanner.osgi;

import org.eclipse.core.runtime.FileLocator;

import java.io.IOException;
import java.net.URL;

/**
 * Resolves OSGi-specific resource URLs into Standard Java resource URLs using Equinox Common.
 */
public class EquinoxCommonResourceUrlResolver {
    /**
     * Prevent instantiation.
     */
    private EquinoxCommonResourceUrlResolver() {
        // Do nothing
    }

    /**
     * Resolves an OSGi-specific resource URL into Standard Java resource URL.
     *
     * @param url The OSGi-specific resource URL.
     * @return The Standard Java resource URL.
     *
     * @throws IOException when the resolution failed.
     */
    public static URL osgiToJavaURL(URL url) throws IOException {
        return FileLocator.toFileURL(url);
    }
}
