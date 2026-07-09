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
package org.flywaydb.core.api.configuration;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A configuration parameter that is no longer supported. Either replaced by a newer parameter ({@link Kind#REPLACED})
 * or removed entirely ({@link Kind#REMOVED}). The {@code reason} provides a human-readable explanation suitable for
 * display to end users.
 */
public record ObsoleteParameter(String rawKey, Kind kind, String replacement, String reason) {
    @Getter
    @RequiredArgsConstructor
    public enum Kind {
        REPLACED("Replaced"),
        REMOVED("Removed");

        @JsonValue
        private final String value;
    }
}
