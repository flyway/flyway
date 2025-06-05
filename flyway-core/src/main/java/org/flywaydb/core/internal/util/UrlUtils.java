/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2025 Red Gate Software Ltd
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.flywaydb.core.internal.util;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import org.flywaydb.core.extensibility.Tier;
import org.flywaydb.core.internal.license.FlywayEditionUpgradeRequiredException;

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

    public static boolean isSecretManagerUrl(final String url, final String databaseType) {
        if (url.startsWith("jdbc-secretsmanager:" + databaseType + ":")) {




            throw new FlywayEditionUpgradeRequiredException((Tier) null, "jdbc-secretsmanager");

        }
        return false;
    }

    public static boolean isAwsWrapperUrl(final String url, final String databaseType) {
        return url.startsWith("jdbc:aws-wrapper:" + databaseType + ":");
    }

    public static Map<String, String> extractQueryParams(String uri) {
        uri = uri.replace('\\', '/');
        try {
            int queryIndex = uri.indexOf("?");

            // No query parameters detected in the connection string
            if (queryIndex == -1) {
                return Collections.emptyMap();
            }

            String baseUri = uri.substring(0, queryIndex);
            String queryPart = uri.substring(queryIndex + 1);

            String encodedQuery = URLEncoder.encode(queryPart, StandardCharsets.UTF_8);
            URI parsedUri = new URI(baseUri + "?" + encodedQuery);
            String query = parsedUri.getQuery();
            Map<String, String> queryParams = new HashMap<>();

            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=", 2);
                    if (keyValue.length == 2) {
                        queryParams.put(keyValue[0], keyValue[1]);
                    }
                }
            }
            return queryParams;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
