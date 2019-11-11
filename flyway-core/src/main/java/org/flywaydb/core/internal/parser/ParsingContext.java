/*
 * Copyright 2010-2019 Boxfuse GmbH
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
package org.flywaydb.core.internal.parser;

import java.util.HashMap;
import java.util.Map;

public class ParsingContext {
    private static final String CURRENT_SCHEMA_PLACEHOLDER = "flyway.currentSchema";
    private Map<String, String> placeholders = new HashMap<>();

    public Map<String, String> getPlaceholders() {
        return placeholders;
    }

    public void setCurrentSchema(String value) {
        placeholders.put(CURRENT_SCHEMA_PLACEHOLDER, value);
    }
}