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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

/**
 * Utility class for dealing with classpath resources.
 */
public class ResourceUtils {
    /**
     * Prevents instantiation.
     */
    private ResourceUtils() {
        //Do nothing
    }

    /**
     * Loads the resource at this location within the classpath in a string using UTF-8 encoding.
     *
     * @param location The location of the resource on the classpath.
     *
     * @return The resource contents as a string.
     */
    public static String loadResourceAsString(String location) {
        return loadResourceAsString(new ClassPathResource(location, ResourceUtils.class.getClassLoader()), "UTF-8");
    }

    /**
     * Loads this resource in a string using this encoding.
     *
     * @param resource The resource to load.
     * @param encoding The encoding of the resource.
     *
     * @return The resource contents as a string.
     */
    public static String loadResourceAsString(Resource resource, String encoding) {
        try {
            Reader reader = new InputStreamReader(resource.getInputStream(), Charset.forName(encoding));
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load resource: " + resource.getDescription() + " (encoding: " + encoding + ")", e);
        }
    }

    /**
     * retrieves the location of a resource
     * @param resource The resource to evaluate
     * @return location of the resource
     */
    public static String getResourceLocation(Resource resource) {
        try {
            return resource.getURL().toExternalForm();
        } catch (IOException e) {
            try {
                return resource.getFile().getAbsolutePath();
            } catch (IOException e1) {
                return resource.getFilename();
            }
        }
    }
}
