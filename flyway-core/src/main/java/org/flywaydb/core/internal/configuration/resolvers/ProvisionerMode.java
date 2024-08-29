/*-
 * ========================LICENSE_START=================================
 * flyway-core
 * ========================================================================
 * Copyright (C) 2010 - 2024 Red Gate Software Ltd
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
package org.flywaydb.core.internal.configuration.resolvers;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.FlywayException;

@Getter
@RequiredArgsConstructor
public enum ProvisionerMode {
    Provision("provision"),
    Reprovision("reprovision"),
    Skip("skip");

    private final String value;

    public static ProvisionerMode fromString(String value) {
        return Arrays.stream(values())
            .filter(color -> color.value.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new FlywayException("Unknown provisioner mode: " + value));
    }

    public boolean isHigherPriorityThan(ProvisionerMode other) {
        return switch (this) {
            case Reprovision -> other != Reprovision;
            case Provision -> other == Skip;
            case Skip -> false;
        };
    }
}
