package com.discordchatboxbroadcaster;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("discordchatboxbroadcaster")
public interface DiscordChatboxBroadcasterConfig extends Config {

	@ConfigItem(
			keyName = "webhookUrl",
			name = "Discord Webhook URL",
			description = "",
			position = 1,
			secret = true
	)
	default String webhookUrl() { return ""; }

	@ConfigItem(
			keyName = "notifyPet",
			name = "Notify Pet (Funny Feeling)",
			description = "Requires 'Untradeable loot notifications' enabled in OSRS settings to broadcast pet names.",
			position = 2
	)
	default boolean notifyPet() { return false; }

	@ConfigItem(keyName = "notifyCollectionLog", name = "Notify Collection Log", description = "", position = 3)
	default boolean notifyCollectionLog() { return false; }

	@ConfigItem(
			keyName = "notifyValuableDrop",
			name = "Notify Valuable Drop",
			description = "Requires 'Loot drop notifications' minimum value to be configured in OSRS settings to broadcast valuable drops.",
			position = 4
	)
	default boolean notifyValuableDrop() { return false; }

	@ConfigItem(keyName = "notifyQuest", name = "Notify Quest Completion", description = "", position = 5)
	default boolean notifyQuest() { return false; }

	@ConfigItem(keyName = "notifyNewRecord", name = "Notify New Record (PB)", description = "", position = 6)
	default boolean notifyNewRecord() { return false; }

	@ConfigItem(keyName = "notifyLevelUp", name = "Notify Level Up", description = "", position = 7)
	default boolean notifyLevelUp() { return false; }

	@ConfigItem(keyName = "notifyKill", name = "Notify Player Kill", description = "", position = 8)
	default boolean notifyKill() { return false; }

	@ConfigItem(keyName = "notifyDeath", name = "Notify Death", description = "", position = 9)
	default boolean notifyDeath() { return false; }

	@ConfigItem(keyName = "notifyCombatAchievement", name = "Notify Combat Achievement", description = "", position = 10)
	default boolean notifyCombatAchievement() { return false; }

	@ConfigItem(keyName = "notifyAchievementDiary", name = "Notify Achievement Diary", description = "", position = 11)
	default boolean notifyAchievementDiary() { return false; }

	@ConfigItem(
			keyName = "disableInTemporaryGameModes",
			name = "Disable in Temp Game Modes",
			description = "Disables all plugin functionality in temporary game modes (Leagues, Deadman, etc.)",
			position = 12
	)
	default boolean disableInTemporaryGameModes() { return false; }
}