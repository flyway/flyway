/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package com.googlecode.flyway.core;

/**
 * Types for checksum validations.<br>
 */
public enum ValidationType {

    /**
     * default not checksum validation for all migrations
     */
    NONE("none"),

    /**
     * validate the checksum for all sql migrations
     */
    ALL("all"),

    /**
     * validate the checksum for all sql migrations and clean schema if validation fails
     */
    ALL_CLEAN("all-clean");

    /**
     * external code for validation type
     * used by maven plugin
     */
    private final String code;

    private ValidationType(String code) {
        this.code = code;
    }

    /**
     * create ValidationType from external code
     * @param code The external code
     * @return The ValidationType or null if no matching code is found
     */
    public static ValidationType fromCode(String code) {
        final ValidationType[] types = ValidationType.values();
        for (ValidationType type : types) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    public boolean isAll() {
        return this.equals(ALL) || this.equals(ALL_CLEAN);
    }

    public boolean isCleanOnError() {
        return this.equals(ALL_CLEAN);
    }


}
