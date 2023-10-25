/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.core.internal.resolver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.flywaydb.core.api.resource.LoadableResource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChecksumCalculator {
    /**
     * Calculates the checksum of these resources. The checksum is encoding and line-ending independent.
     *
     * @return The crc-32 checksum of the bytes.
     */
    public static int calculate(LoadableResource... loadableResources) {
        int checksum;




            checksum = loadableResources[0].calculateChecksum();












        return checksum;
    }
}