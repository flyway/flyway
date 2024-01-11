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