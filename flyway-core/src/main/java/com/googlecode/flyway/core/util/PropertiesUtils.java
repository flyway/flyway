/**
 * Copyright (C) 2010-2013 the original author or authors.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * Utility for dealing with property files. Necessary due to Java 5 compatibility.
 * <p/>
 * Code inspired from Spring's DefaultPropertiesPersister.
 */
public final class PropertiesUtils {
    /**
     * Prevent instantiation.
     */
    private PropertiesUtils() {
        // Do nothing
    }

    /**
     * Loads this properties data from this string.
     *
     * @param propertiesData The string containing the properties data.
     * @return The matching properties object.
     * @throws IOException when the data could not be read.
     */
    public static Properties loadPropertiesFromString(String propertiesData) throws IOException {
        Properties properties = new Properties();

        BufferedReader in = new BufferedReader(new StringReader(propertiesData));
        while (true) {
            String line = in.readLine();
            if (line == null) {
                return properties;
            }
            line = StringUtils.trimLeadingWhitespace(line);
            if (line.length() > 0) {
                char firstChar = line.charAt(0);
                if (firstChar != '#' && firstChar != '!') {
                    while (endsWithContinuationMarker(line)) {
                        String nextLine = in.readLine();
                        line = line.substring(0, line.length() - 1);
                        if (nextLine != null) {
                            line += StringUtils.trimLeadingWhitespace(nextLine);
                        }
                    }
                    int separatorIndex = line.indexOf("=");
                    if (separatorIndex == -1) {
                        separatorIndex = line.indexOf(":");
                    }
                    String key = (separatorIndex != -1 ? line.substring(0, separatorIndex) : line);
                    String value = (separatorIndex != -1) ? line.substring(separatorIndex + 1) : "";
                    key = StringUtils.trimTrailingWhitespace(key);
                    value = StringUtils.trimLeadingWhitespace(value);
                    properties.put(unescape(key), unescape(value));
                }
            }
        }
    }

    private static boolean endsWithContinuationMarker(String line) {
        boolean evenSlashCount = true;
        int index = line.length() - 1;
        while (index >= 0 && line.charAt(index) == '\\') {
            evenSlashCount = !evenSlashCount;
            index--;
        }
        return !evenSlashCount;
    }

    private static String unescape(String str) {
        StringBuffer outBuffer = new StringBuffer(str.length());
        for (int index = 0; index < str.length(); ) {
            char c = str.charAt(index++);
            if (c == '\\') {
                c = str.charAt(index++);
                if (c == 't') {
                    c = '\t';
                } else if (c == 'r') {
                    c = '\r';
                } else if (c == 'n') {
                    c = '\n';
                } else if (c == 'f') {
                    c = '\f';
                }
            }
            outBuffer.append(c);
        }
        return outBuffer.toString();
    }
}
