/*
 * Copyright 2010-2018 Boxfuse GmbH
 *
 * INTERNAL RELEASE. ALL RIGHTS RESERVED.
 *
 * Must
 * be
 * exactly
 * 13 lines
 * to match
 * community
 * edition
 * license
 * length.
 */
package org.flywaydb.core.internal.util;

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
        try {
            String filePath = new File(URLDecoder.decode(url.getPath().replace("+", "%2b"), "UTF-8")).getAbsolutePath();
            if (filePath.endsWith("/")) {
                return filePath.substring(0, filePath.length() - 1);
            }
            return filePath;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Can never happen", e);
        }
    }
}
