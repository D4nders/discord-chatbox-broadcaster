package com.discordchatboxbroadcaster;

import com.discordchatboxbroadcaster.config.BackgroundStyle;
import com.discordchatboxbroadcaster.config.CombatAchievementTier;
import com.discordchatboxbroadcaster.config.ImageScale;
import com.discordchatboxbroadcaster.config.XpMilestoneInterval;
import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("discordchatboxbroadcaster")
public interface DiscordChatboxBroadcasterConfig extends Config {

	@ConfigSection(
			name = "General",
			description = "General webhook and plugin settings",
			position = 0,
			closedByDefault = false
	)
	String generalSection = "generalSection";

	@ConfigSection(
			name = "Appearance",
			description = "Appearance and styling of generated chatbox images",
			position = 1,
			closedByDefault = true
	)
	String appearanceSection = "appearanceSection";

	@ConfigSection(
			name = "Pets",
			description = "Pet drop notification settings",
			position = 2,
			closedByDefault = true
	)
	String petsSection = "petsSection";

	@ConfigSection(
			name = "Collection Log",
			description = "Collection Log notification settings",
			position = 3,
			closedByDefault = true
	)
	String collectionLogSection = "collectionLogSection";

	@ConfigSection(
			name = "Valuable Drops",
			description = "Valuable drop notification settings",
			position = 4,
			closedByDefault = true
	)
	String valuableDropsSection = "valuableDropsSection";

	@ConfigSection(
			name = "Quests",
			description = "Quest completion notification settings",
			position = 5,
			closedByDefault = true
	)
	String questsSection = "questsSection";

	@ConfigSection(
			name = "Personal Bests",
			description = "Personal best and record notification settings",
			position = 6,
			closedByDefault = true
	)
	String personalBestsSection = "personalBestsSection";

	@ConfigSection(
			name = "Level Ups",
			description = "Skill level up notification settings",
			position = 7,
			closedByDefault = true
	)
	String levelUpsSection = "levelUpsSection";

	@ConfigSection(
			name = "XP Milestones",
			description = "XP milestone notification settings",
			position = 8,
			closedByDefault = true
	)
	String xpMilestonesSection = "xpMilestonesSection";

	@ConfigSection(
			name = "Combat Achievements",
			description = "Combat Achievement task notification settings",
			position = 9,
			closedByDefault = true
	)
	String combatAchievementsSection = "combatAchievementsSection";

	@ConfigSection(
			name = "Achievement Diaries",
			description = "Achievement Diary notification settings",
			position = 10,
			closedByDefault = true
	)
	String achievementDiariesSection = "achievementDiariesSection";

	@ConfigSection(
			name = "PvP",
			description = "Player versus Player notification settings",
			position = 11,
			closedByDefault = true
	)
	String pvpSection = "pvpSection";

	// --- General Section ---

	@ConfigItem(
			keyName = "webhookUrl",
			name = "Discord Webhook URL",
			description = "The Discord incoming webhook URL to broadcast messages to.",
			position = 1,
			section = generalSection,
			secret = true
	)
	default String webhookUrl() { return ""; }

	@ConfigItem(
			keyName = "webhookUsername",
			name = "Webhook Username",
			description = "Override the webhook username displayed in Discord. Leave empty to use the webhook's default name.",
			position = 2,
			section = generalSection
	)
	default String webhookUsername() { return ""; }

	@ConfigItem(
			keyName = "webhookAvatarUrl",
			name = "Webhook Avatar URL",
			description = "Override the webhook avatar image URL in Discord. Leave empty to use the webhook's default avatar.",
			position = 3,
			section = generalSection
	)
	default String webhookAvatarUrl() { return ""; }

	@ConfigItem(
			keyName = "attachScreenshot",
			name = "Attach Screenshot",
			description = "Attach a full game screenshot alongside the chatbox image in the Discord notification.",
			position = 4,
			section = generalSection
	)
	default boolean attachScreenshot() { return false; }

	@ConfigItem(
			keyName = "disableInTemporaryGameModes",
			name = "Disable in Temp Game Modes",
			description = "Disables all plugin functionality in temporary game modes (Leagues, Deadman, etc.).",
			position = 5,
			section = generalSection
	)
	default boolean disableInTemporaryGameModes() { return false; }

	// --- Appearance Section ---

	@ConfigItem(
			keyName = "showClanName",
			name = "Show Clan Name",
			description = "Show clan name prefix in chat messages if the player is in a clan.",
			position = 1,
			section = appearanceSection
	)
	default boolean showClanName() { return true; }

	@ConfigItem(
			keyName = "showAccountIcon",
			name = "Show Account Icon",
			description = "Show account type icon (Ironman, Hardcore, Ultimate, etc.) before the player name.",
			position = 2,
			section = appearanceSection
	)
	default boolean showAccountIcon() { return true; }

	@ConfigItem(
			keyName = "backgroundStyle",
			name = "Background Style",
			description = "The visual style and background of the generated chatbox image.",
			position = 3,
			section = appearanceSection
	)
	default BackgroundStyle backgroundStyle() { return BackgroundStyle.CLASSIC; }

	@ConfigItem(
			keyName = "highlightColor",
			name = "Highlight Color",
			description = "The font color used for highlighted terms like item names, levels, and records.",
			position = 4,
			section = appearanceSection
	)
	default Color highlightColor() { return new Color(239, 16, 32); }

	@ConfigItem(
			keyName = "clanNameColor",
			name = "Clan Name Color",
			description = "The font color used for the clan name prefix.",
			position = 5,
			section = appearanceSection
	)
	default Color clanNameColor() { return Color.BLUE; }

	@ConfigItem(
			keyName = "imageScale",
			name = "Image Scale",
			description = "Scale factor for the generated chatbox image to maintain crispness.",
			position = 6,
			section = appearanceSection
	)
	default ImageScale imageScale() { return ImageScale.X1; }

	// --- Pets Section ---

	@ConfigItem(
			keyName = "notifyPet",
			name = "Notify Pet (Funny Feeling)",
			description = "Requires 'Untradeable loot notifications' enabled in OSRS settings to broadcast pet names.",
			position = 1,
			section = petsSection
	)
	default boolean notifyPet() { return false; }

	// --- Collection Log Section ---

	@ConfigItem(
			keyName = "notifyCollectionLog",
			name = "Notify Collection Log",
			description = "Broadcast when a new item is unlocked in the Collection Log.",
			position = 1,
			section = collectionLogSection
	)
	default boolean notifyCollectionLog() { return false; }

	// --- Valuable Drops Section ---

	@ConfigItem(
			keyName = "notifyValuableDrop",
			name = "Notify Valuable Drop",
			description = "Requires 'Loot drop notifications' minimum value to be configured in OSRS settings to broadcast valuable drops.",
			position = 1,
			section = valuableDropsSection
	)
	default boolean notifyValuableDrop() { return false; }

	@Range(min = 0)
	@ConfigItem(
			keyName = "valuableDropMinValue",
			name = "Minimum Value",
			description = "Minimum drop value (in coins) to trigger a broadcast. Set to 0 to broadcast all valuable drops.",
			position = 2,
			section = valuableDropsSection
	)
	default int valuableDropMinValue() { return 0; }

	// --- Quests Section ---

	@ConfigItem(
			keyName = "notifyQuest",
			name = "Notify Quest Completion",
			description = "Broadcast when a quest is completed.",
			position = 1,
			section = questsSection
	)
	default boolean notifyQuest() { return false; }

	// --- Personal Bests Section ---

	@ConfigItem(
			keyName = "notifyNewRecord",
			name = "Notify New Record (PB)",
			description = "Broadcast when a new personal best (record) time or kill count is achieved.",
			position = 1,
			section = personalBestsSection
	)
	default boolean notifyNewRecord() { return false; }

	// --- Level Ups Section ---

	@ConfigItem(
			keyName = "notifyLevelUp",
			name = "Notify Level Up",
			description = "Broadcast when leveling up a skill.",
			position = 1,
			section = levelUpsSection
	)
	default boolean notifyLevelUp() { return false; }

	@Range(min = 1, max = 99)
	@ConfigItem(
			keyName = "levelUpMinLevel",
			name = "Minimum Level",
			description = "Minimum skill level required to trigger a level up broadcast.",
			position = 2,
			section = levelUpsSection
	)
	default int levelUpMinLevel() { return 1; }

	@Range(min = 1, max = 98)
	@ConfigItem(
			keyName = "levelUpInterval",
			name = "Level Interval",
			description = "Only broadcast level ups at multiples of this interval (level 99 is always broadcast).",
			position = 3,
			section = levelUpsSection
	)
	default int levelUpInterval() { return 1; }

	// --- XP Milestones Section ---

	@ConfigItem(
			keyName = "notifyXpMilestone",
			name = "Notify XP Milestones",
			description = "Broadcast when reaching an XP milestone in a skill past level 99.",
			position = 1,
			section = xpMilestonesSection
	)
	default boolean notifyXpMilestone() { return false; }

	@ConfigItem(
			keyName = "xpMilestoneInterval",
			name = "Milestone Interval",
			description = "XP interval between milestone notifications past level 99.",
			position = 2,
			section = xpMilestonesSection
	)
	default XpMilestoneInterval xpMilestoneInterval() { return XpMilestoneInterval.ONE_MILLION; }

	// --- Combat Achievements Section ---

	@ConfigItem(
			keyName = "notifyCombatAchievement",
			name = "Notify Combat Achievement",
			description = "Broadcast when completing a Combat Achievement task.",
			position = 1,
			section = combatAchievementsSection
	)
	default boolean notifyCombatAchievement() { return false; }

	@ConfigItem(
			keyName = "combatAchievementMinTier",
			name = "Minimum Tier",
			description = "Minimum task tier required to trigger a Combat Achievement broadcast.",
			position = 2,
			section = combatAchievementsSection
	)
	default CombatAchievementTier combatAchievementMinTier() { return CombatAchievementTier.EASY; }

	@ConfigItem(
			keyName = "notifyCombatAchievementTierCompletion",
			name = "Notify Tier Completion",
			description = "Broadcast when completing an entire tier of Combat Achievements. Requires 'Notify Combat Achievement' to be enabled.",
			position = 3,
			section = combatAchievementsSection
	)
	default boolean notifyCombatAchievementTierCompletion() { return true; }

	// --- Achievement Diaries Section ---

	@ConfigItem(
			keyName = "notifyAchievementDiary",
			name = "Notify Achievement Diary",
			description = "Broadcast when completing an Achievement Diary tier.",
			position = 1,
			section = achievementDiariesSection
	)
	default boolean notifyAchievementDiary() { return false; }

	// --- PvP Section ---

	@ConfigItem(
			keyName = "notifyKill",
			name = "Notify Player Kill",
			description = "Broadcast when defeating another player in PvP.",
			position = 1,
			section = pvpSection
	)
	default boolean notifyKill() { return false; }

	@ConfigItem(
			keyName = "notifyDeath",
			name = "Notify Death",
			description = "Broadcast when dying.",
			position = 2,
			section = pvpSection
	)
	default boolean notifyDeath() { return false; }
}