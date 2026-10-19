package com.discordchatboxbroadcaster.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CombatAchievementTier {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    ELITE("Elite"),
    MASTER("Master"),
    GRANDMASTER("Grandmaster");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

    public static CombatAchievementTier fromName(String text) {
        if (text == null) {
            return null;
        }
        for (CombatAchievementTier tier : values()) {
            if (tier.name().equalsIgnoreCase(text) || tier.name.equalsIgnoreCase(text)) {
                return tier;
            }
        }
        return null;
    }
}
