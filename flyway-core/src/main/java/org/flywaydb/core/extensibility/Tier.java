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
package org.flywaydb.core.extensibility;

import lombok.Getter;

import java.util.List;

public enum Tier {
    COMMUNITY("Community"),
    TEAMS("Teams"),
    ENTERPRISE("Enterprise");

    public static final List<Tier> PREMIUM = List.of(Tier.TEAMS, Tier.ENTERPRISE);

    public static boolean isAtLeast(Tier tier, Tier minimumTier) {
        if (minimumTier == null) {
            return true;
        }

        if (tier == null) {
            return false;
        }

        return tier.ordinal() >= minimumTier.ordinal();
    }

    @Getter
    private final String displayName;
    @Getter
    private final String description;

    Tier(String displayName) {
        this.displayName = displayName;
        this.description = "Flyway " + displayName + " Edition";
    }

    public static String asString(Tier tier) {
        if (tier == null) {
            return "OSS";
        }
        return tier.toString();
    }
}
