/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2026 Red Gate Software Ltd
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

import static org.flywaydb.core.extensibility.AwsSecretsManagerSupport.JDBC_SECRETS_MANAGER;
import static org.flywaydb.core.extensibility.AwsSecretsManagerSupport.JDBC_SECRETS_MANAGER_PREFIX;
import static org.flywaydb.core.internal.configuration.ConfigUtils.isOSS;

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
    public static String toFilePath(final URL url) {
        final String filePath = new File(decodeURLSafe(url.getPath())).getAbsolutePath();
        if (filePath.endsWith("/")) {
            return filePath.substring(0, filePath.length() - 1);
        }
        return filePath;
    }

    /**
     * Decodes this UTF-8 encoded URL.
     * <p>
     * Shall be made private, new code shall always call decodeURLSafe() instead.
     *
     * @param url The url to decode.
     * @return The decoded URL.
     */
    public static String decodeURL(final String url) {
        try {
            return URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Can never happen", e);
        }
    }

    public static String decodeURLSafe(final String url) {
        return decodeURL(url.replace("+", "%2b"));
    }

    public static void guardJdbcSecretsManagerURL(final String url) {
        if (url.startsWith(JDBC_SECRETS_MANAGER_PREFIX) && isOSS()) {
            throw new FlywayEditionUpgradeRequiredException(null, JDBC_SECRETS_MANAGER);
        }
    }

    public static boolean isSecretManagerUrl(final String url, final String databaseType) {
        if (url.startsWith(JDBC_SECRETS_MANAGER_PREFIX + databaseType + ":")) {
            if (isOSS()) {
                throw new FlywayEditionUpgradeRequiredException(null, JDBC_SECRETS_MANAGER);
            }

            return true;
        }
        return false;
    }

    public static boolean isAwsWrapperUrl(final String url, final String databaseType) {
        return url.startsWith("jdbc:aws-wrapper:" + databaseType + ":");
    }

    public static Map<String, String> extractQueryParams(String uri) {
        uri = uri.replace('\\', '/');
        try {
            final int queryIndex = uri.indexOf("?");

            // No query parameters detected in the connection string
            if (queryIndex == -1) {
                return Collections.emptyMap();
            }

            final String baseUri = uri.substring(0, queryIndex);
            final String queryPart = uri.substring(queryIndex + 1);

            final String encodedQuery = URLEncoder.encode(queryPart, StandardCharsets.UTF_8);
            final URI parsedUri = new URI(baseUri + "?" + encodedQuery);
            final String query = parsedUri.getQuery();
            final Map<String, String> queryParams = new HashMap<>();

            if (query != null) {
                final String[] pairs = query.split("&");
                for (final String pair : pairs) {
                    final String[] keyValue = pair.split("=", 2);
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
