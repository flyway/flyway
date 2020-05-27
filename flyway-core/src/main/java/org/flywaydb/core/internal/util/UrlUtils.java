/*
 * Copyright 2010-2020 Redgate Software Ltd
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
package org.flywaydb.core.internal.util;

import org.flywaydb.core.api.FlywayException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Collection of utility methods for working with URLs.
 */
public class UrlUtils {
    /**
     * Prevent instantiation.
     */
    private UrlUtils() {
        // Do nothing
    }

    /**
     * Retrieves the file path of this URL, with any trailing slashes removed.
     *
     * @param url The URL to get the file path for.
     * @return The file path.
     */
    public static String toFilePath(URL url) {
        String filePath = new File(decodeURL(url.getPath().replace("+", "%2b"))).getAbsolutePath();
        if (filePath.endsWith("/")) {
            return filePath.substring(0, filePath.length() - 1);
        }
        return filePath;
    }

    /**
     * Decodes this UTF-8 encoded URL.
     *
     * @param url The url to decode.
     * @return The decoded URL.
     */
    public static String decodeURL(String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Can never happen", e);
        }
    }
}