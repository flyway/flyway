/*
 * Copyright 2010-2018 Boxfuse GmbH
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
package org.flywaydb.core.internal.placeholder;

import java.util.Map;

/**
 * Tool for replacing placeholders.
 */
public interface PlaceholderReplacer {
    /**
     * @return The placeholder name-value mapping used for replacement.
     */
    Map<String, String> getPlaceholderReplacements();

    /**
     * Replaces the placeholders in this input string with their corresponding values.
     *
     * @param input The input to process.
     * @return The input string with all placeholders replaced.
     */
    String replacePlaceholders(String input);
}