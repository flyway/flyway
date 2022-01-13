/*
 * Copyright (C) Red Gate Software Ltd 2010-2022
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

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DataUnits {

    BYTE(1, "B"),
    KILOBYTE(1024, "kB"),
    MEGABYTE(1024 * 1024, "MB"),
    GIGABYTE(1024 * 1024 * 1024, "GB");

    private final long factor;
    private final String suffix;

    /**
     * Returns the number of bytes for your number of data units
     *
     * @param units Number of data units to convert to bytes
     * @return Number of bytes in data units
     */
    public long toBytes(long units) {
        return units * factor;
    }

    /**
     * Returns the number of data units for your number of bytes
     *
     * @param bytes Number of bytes to convert to data units
     * @return Number of bytes in data units
     */
    public long fromBytes(long bytes) {
        return bytes / factor;
    }

    /**
     * @return The specified bytes as a human-readable string normalized to this unit
     */
    public String toHumanReadableString(long bytes) {
        return fromBytes(bytes) + " " + suffix;
    }
}