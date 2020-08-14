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

public class BomFilter {
    private static final char BOM = '\ufeff';

    /**
     * Determine if this char is a UTF-8 Byte Order Mark
     * @param c The char to check
     * @return Whether this char is a UTF-8 Byte Order Mark
     */
    public static boolean isBom(char c) {
        return c == BOM;
    }

    /**
     * Removes the UTF-8 Byte Order Mark from the start of a string if present.
     * @param s The string
     * @return The string without a Byte Order Mark at the start
     */
    public static String FilterBomFromString(String s) {
        if (s.isEmpty()) {
            return s;
        }

       if (isBom(s.charAt(0))) {
           return s.substring(1);
       }

       return s;
    }
}