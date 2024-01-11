package org.flywaydb.core.internal.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Collection of utility methods for working with URLs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlUtils {
    /**
     * Retrieves the file path of this URL, with any trailing slashes removed.
     *
     * @param url The URL to get the file path for.
     * @return The file path.
     */
    public static String toFilePath(URL url) {
        String filePath = new File(decodeURLSafe(url.getPath())).getAbsolutePath();
        if (filePath.endsWith("/")) {
            return filePath.substring(0, filePath.length() - 1);
        }
        return filePath;
    }

    /**
     * Decodes this UTF-8 encoded URL.
     *
     * Shall be made private, new code shall always call decodeURLSafe() instead.
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

    public static String decodeURLSafe(String url) {
       return decodeURL(url.replace("+", "%2b"));
    }
}