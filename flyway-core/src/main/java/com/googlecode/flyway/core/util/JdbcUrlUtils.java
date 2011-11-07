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

import com.googlecode.flyway.core.exception.FlywayException;

/**
 * Utility class for dealing with jdbc urls.
 */
public class JdbcUrlUtils {
    /**
     * Prevents instantiation.
     */
    private JdbcUrlUtils() {
        //Do nothing
    }

    /**
     * Checks the validity of this jdbc url.
     *
     * @param jdbcUrl The url to check.
     *
     * @throws FlywayException when the url is invalid.
     */
    public static void validate(String jdbcUrl) throws FlywayException {
        if (!jdbcUrl.toLowerCase().startsWith("jdbc:")) {
            throw new FlywayException("Invalid jdbc url (should start with jdbc:) : " + jdbcUrl);
        }
    }
}
